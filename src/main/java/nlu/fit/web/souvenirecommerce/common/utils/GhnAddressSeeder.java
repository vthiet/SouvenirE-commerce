package nlu.fit.web.souvenirecommerce.common.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nlu.fit.web.souvenirecommerce.features.shipping.service.GhnService;
import nlu.fit.web.souvenirecommerce.model.entity.Address;
import nlu.fit.web.souvenirecommerce.model.entity.Province;
import nlu.fit.web.souvenirecommerce.model.entity.Ward;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class GhnAddressSeeder {

    private final GhnService ghnService = new GhnService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        try {
            new GhnAddressSeeder().seed();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void seed() throws Exception {
        List<GhnProvince> ghnProvinces = loadGhnProvinces();
        if (ghnProvinces.isEmpty()) {
            throw new IllegalStateException("GHN trả về danh sách tỉnh/thành trống.");
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Province> provinces = session.createQuery(
                            "from Province p order by p.code",
                            Province.class)
                    .getResultList();

            Map<Integer, List<Ward>> wardsByProvince = loadLocalWards(session);
            List<Address> addresses = loadAddresses(session);

            int matchedProvinces = 0;
            int matchedWards = 0;
            int backfilledAddresses = 0;
            int skippedDistricts = 0;

            for (Province province : provinces) {
                GhnProvince matchedProvince = matchProvince(province, ghnProvinces);
                if (matchedProvince == null) {
                    System.out.printf(Locale.ROOT,
                            "[GHN] Skip province %s (%d): no safe match%n",
                            displayName(province.getFullName(), province.getName()),
                            province.getCode());
                    continue;
                }

                province.setGhnProvinceId(matchedProvince.id());
                province.setGhnProvinceName(matchedProvince.name());
                matchedProvinces++;

                List<GhnDistrict> districts = loadGhnDistricts(matchedProvince.id());
                List<GhnWard> ghnWards = new ArrayList<>();
                int provinceSkippedDistricts = 0;
                for (GhnDistrict district : districts) {
                    try {
                        ghnWards.addAll(loadGhnWards(matchedProvince.id(), district));
                    } catch (Exception e) {
                        provinceSkippedDistricts++;
                        skippedDistricts++;
                        System.out.printf(Locale.ROOT,
                                "[GHN] Skip district %-30s (%d) in province %-30s (%d): %s%n",
                                displayName(district.name(), null),
                                district.id(),
                                displayName(province.getFullName(), province.getName()),
                                matchedProvince.id(),
                                e.getMessage());
                    }
                }

                Map<String, List<GhnWard>> wardIndex = buildWardIndex(ghnWards);
                List<Ward> localWards = wardsByProvince.getOrDefault(province.getCode(), List.of());
                int provinceWardMatches = 0;
                for (Ward ward : localWards) {
                    GhnWard matchedWard = matchWard(ward, wardIndex);
                    if (matchedWard == null) {
                        continue;
                    }

                    ward.setGhnDistrictId(matchedWard.districtId());
                    ward.setGhnDistrictName(matchedWard.districtName());
                    ward.setGhnWardCode(matchedWard.code());
                    ward.setGhnWardName(matchedWard.name());
                    provinceWardMatches++;
                }
                matchedWards += provinceWardMatches;

                System.out.printf(Locale.ROOT,
                        "[GHN] Province %-30s -> GHN %d, districts: %d, wards mapped: %d/%d%s%n",
                        displayName(province.getFullName(), province.getName()),
                        matchedProvince.id(),
                        districts.size(),
                        provinceWardMatches,
                        localWards.size(),
                        provinceSkippedDistricts > 0 ? ", skipped districts: " + provinceSkippedDistricts : "");
            }

            for (Address address : addresses) {
                if (backfillAddress(address)) {
                    backfilledAddresses++;
                }
            }

            Transaction tx = session.beginTransaction();
            tx.commit();

            System.out.println("=================================");
            System.out.println("GHN mapping seeding completed.");
            System.out.println("Provinces mapped: " + matchedProvinces + "/" + provinces.size());
            System.out.println("Wards mapped: " + matchedWards);
            System.out.println("Addresses backfilled: " + backfilledAddresses);
            System.out.println("Districts skipped: " + skippedDistricts);
            System.out.println("=================================");
        }
    }

    private Map<Integer, List<Ward>> loadLocalWards(Session session) {
        List<Ward> wards = session.createQuery("""
                        select w
                        from Ward w
                        join fetch w.province
                        order by w.code asc
                        """, Ward.class)
                .getResultList();

        return wards.stream().collect(Collectors.groupingBy(
                ward -> ward.getProvince().getCode(),
                LinkedHashMap::new,
                Collectors.toCollection(ArrayList::new)
        ));
    }

    private List<Address> loadAddresses(Session session) {
        return session.createQuery("""
                        select distinct a
                        from Address a
                        left join fetch a.provinceEntity
                        left join fetch a.wardEntity
                        order by a.id asc
                        """, Address.class)
                .getResultList();
    }

    private List<GhnProvince> loadGhnProvinces() throws Exception {
        JsonNode data = readArray(ghnService.getProvinces());
        List<GhnProvince> provinces = new ArrayList<>();
        for (JsonNode node : data) {
            Integer id = intValue(node, "ProvinceID");
            String name = textValue(node, "ProvinceName");
            if (id == null || isBlank(name)) {
                continue;
            }
            provinces.add(new GhnProvince(id, name, buildAliases(name, textValues(node, "NameExtension"))));
        }
        return provinces;
    }

    private List<GhnDistrict> loadGhnDistricts(int provinceId) throws Exception {
        JsonNode data = readArray(ghnService.getDistricts(provinceId));
        List<GhnDistrict> districts = new ArrayList<>();
        for (JsonNode node : data) {
            Integer id = intValue(node, "DistrictID");
            String name = textValue(node, "DistrictName");
            if (id == null || isBlank(name)) {
                continue;
            }
            districts.add(new GhnDistrict(id, name, buildAliases(name, textValues(node, "NameExtension"))));
        }
        return districts;
    }

    private List<GhnWard> loadGhnWards(int provinceId, GhnDistrict district) throws Exception {
        List<JsonNode> data = readWardNodes(ghnService.getWards(district.id()));
        List<GhnWard> wards = new ArrayList<>();
        for (JsonNode node : data) {
            String code = textValue(node, "WardCode");
            String name = textValue(node, "WardName");
            if (isBlank(code) || isBlank(name)) {
                continue;
            }
            wards.add(new GhnWard(
                    provinceId,
                    district.id(),
                    district.name(),
                    code.trim(),
                    name,
                    buildAliases(name, textValues(node, "NameExtension"))));
        }
        return wards;
    }

    private GhnProvince matchProvince(Province province, List<GhnProvince> ghnProvinces) {
        Set<String> exactNames = exactProvinceNames(province);
        for (GhnProvince ghnProvince : ghnProvinces) {
            if (exactNames.contains(normalizeLocationName(ghnProvince.name()))) {
                return ghnProvince;
            }
        }

        Set<String> localAliases = aliasesForProvince(province);
        List<GhnProvince> matches = new ArrayList<>();
        for (GhnProvince ghnProvince : ghnProvinces) {
            if (intersects(localAliases, ghnProvince.aliases())) {
                matches.add(ghnProvince);
            }
        }
        return matches.size() == 1 ? matches.get(0) : null;
    }

    private Set<String> exactProvinceNames(Province province) {
        return buildAliases(
                province == null ? null : province.getName(),
                province == null ? null : province.getFullName()
        );
    }

    private Map<String, List<GhnWard>> buildWardIndex(List<GhnWard> wards) {
        Map<String, List<GhnWard>> index = new LinkedHashMap<>();
        for (GhnWard ward : wards) {
            for (String alias : ward.aliases()) {
                index.computeIfAbsent(alias, key -> new ArrayList<>()).add(ward);
            }
        }
        return index;
    }

    private GhnWard matchWard(Ward ward, Map<String, List<GhnWard>> wardIndex) {
        Set<String> localAliases = aliasesForWard(ward);
        LinkedHashSet<GhnWard> matches = new LinkedHashSet<>();
        for (String alias : localAliases) {
            List<GhnWard> candidates = wardIndex.get(alias);
            if (candidates != null) {
                matches.addAll(candidates);
            }
        }
        return matches.size() == 1 ? matches.iterator().next() : null;
    }

    private boolean backfillAddress(Address address) {
        boolean changed = false;

        Province provinceEntity = address.getProvinceEntity();
        if (provinceEntity != null) {
            Integer ghnProvinceId = provinceEntity.getGhnProvinceId();
            if (address.getGhnProvinceId() == null && ghnProvinceId != null) {
                address.setGhnProvinceId(ghnProvinceId);
                changed = true;
            }

            String provinceName = displayName(provinceEntity.getFullName(), provinceEntity.getName());
            if (isBlank(address.getProvince())) {
                address.setProvince(provinceName);
                changed = true;
            }
            if (isBlank(address.getCity())) {
                address.setCity(provinceName);
                changed = true;
            }
        }

        Ward wardEntity = address.getWardEntity();
        if (wardEntity != null) {
            if (address.getGhnDistrictId() == null && wardEntity.getGhnDistrictId() != null) {
                address.setGhnDistrictId(wardEntity.getGhnDistrictId());
                changed = true;
            }

            if (isBlank(address.getGhnWardCode()) && !isBlank(wardEntity.getGhnWardCode())) {
                address.setGhnWardCode(wardEntity.getGhnWardCode());
                changed = true;
            }

            if (isBlank(address.getDistrict()) && !isBlank(wardEntity.getGhnDistrictName())) {
                address.setDistrict(wardEntity.getGhnDistrictName());
                changed = true;
            }

            String wardName = displayName(wardEntity.getFullName(), wardEntity.getName());
            if (isBlank(address.getWard())) {
                address.setWard(wardName);
                changed = true;
            }
        }

        return changed;
    }

    private JsonNode readArray(String rawJson) throws Exception {
        JsonNode root = objectMapper.readTree(rawJson);
        JsonNode data = root.path("data");
        if (!data.isArray()) {
            throw new IllegalStateException("GHN response does not contain a data array.");
        }
        return data;
    }

    private List<JsonNode> readWardNodes(String rawJson) throws Exception {
        JsonNode root = objectMapper.readTree(rawJson);
        JsonNode data = root.path("data");
        if (data.isArray()) {
            List<JsonNode> nodes = new ArrayList<>();
            data.forEach(nodes::add);
            return nodes;
        }
        if (data.isObject()) {
            if (data.hasNonNull("WardCode") || data.hasNonNull("WardName")) {
                return List.of(data);
            }
            throw new IllegalStateException("GHN ward response does not contain ward data.");
        }
        if (data.isNull() || data.isMissingNode()) {
            throw new IllegalStateException("GHN ward response returned no data.");
        }
        throw new IllegalStateException("GHN ward response does not contain usable data.");
    }

    private Set<String> aliasesForProvince(Province province) {
        return buildAliases(
                province == null ? null : province.getName(),
                province == null ? null : province.getFullName()
        );
    }

    private Set<String> aliasesForWard(Ward ward) {
        return buildAliases(
                ward == null ? null : ward.getName(),
                ward == null ? null : ward.getFullName()
        );
    }

    private Set<String> buildAliases(String baseName, List<String> extraValues) {
        Set<String> aliases = new LinkedHashSet<>();
        addAlias(aliases, baseName);
        if (extraValues != null) {
            for (String value : extraValues) {
                addAlias(aliases, value);
            }
        }
        return aliases;
    }

    private Set<String> buildAliases(String... values) {
        Set<String> aliases = new LinkedHashSet<>();
        if (values == null) {
            return aliases;
        }
        Arrays.stream(values).forEach(value -> addAlias(aliases, value));
        return aliases;
    }

    private void addAlias(Set<String> aliases, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        String normalized = normalizeLocationName(value);
        if (!normalized.isBlank()) {
            aliases.add(normalized);
        }
    }

    private boolean intersects(Set<String> left, Set<String> right) {
        for (String value : left) {
            if (right.contains(value)) {
                return true;
            }
        }
        return false;
    }

    private String normalizeLocationName(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('đ', 'd')
                .replace('Đ', 'd')
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .trim()
                .replaceAll("\\s+", " ");

        return stripAdministrativePrefix(normalized);
    }

    private String stripAdministrativePrefix(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        String normalized = value.trim();
        boolean changed;
        do {
            changed = false;
            for (String prefix : List.of(
                    "thanh pho ",
                    "tp ",
                    "tinh ",
                    "quan ",
                    "q ",
                    "huyen ",
                    "h ",
                    "phuong ",
                    "p ",
                    "xa ",
                    "x ",
                    "thi tran ",
                    "tt ",
                    "thi xa ",
                    "tx ",
                    "dac khu ",
                    "dk "
            )) {
                if (normalized.startsWith(prefix)) {
                    normalized = normalized.substring(prefix.length()).trim();
                    changed = true;
                    break;
                }
            }
        } while (changed);

        return normalized.replaceAll("\\s+", " ");
    }

    private List<String> textValues(JsonNode node, String field) {
        if (node == null || !node.has(field) || node.get(field).isNull()) {
            return List.of();
        }

        JsonNode value = node.get(field);
        if (value.isArray()) {
            List<String> result = new ArrayList<>();
            for (JsonNode child : value) {
                if (child != null && child.isTextual() && !child.asText().isBlank()) {
                    result.add(child.asText());
                } else if (child != null && child.isValueNode() && !child.asText().isBlank()) {
                    result.add(child.asText());
                }
            }
            return result;
        }

        if (value.isTextual() && !value.asText().isBlank()) {
            return List.of(value.asText());
        }

        return List.of();
    }

    private String textValue(JsonNode node, String field) {
        if (node == null || !node.hasNonNull(field)) {
            return null;
        }
        String value = node.get(field).asText();
        return value == null || value.isBlank() ? null : value.trim();
    }

    private Integer intValue(JsonNode node, String field) {
        if (node == null || !node.hasNonNull(field)) {
            return null;
        }
        try {
            return node.get(field).asInt();
        } catch (Exception ignored) {
            return null;
        }
    }

    private String displayName(String fullName, String name) {
        return isBlank(fullName) ? name : fullName;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record GhnProvince(Integer id, String name, Set<String> aliases) {
    }

    private record GhnDistrict(Integer id, String name, Set<String> aliases) {
    }

    private record GhnWard(Integer provinceId, Integer districtId, String districtName, String code, String name,
                           Set<String> aliases) {
    }
}
