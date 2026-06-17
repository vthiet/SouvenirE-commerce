package nlu.fit.web.souvenirecommerce.common.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import nlu.fit.web.souvenirecommerce.features.shipping.service.GhnService;
import nlu.fit.web.souvenirecommerce.legacy.utils.DBContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.Normalizer;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Synchronises GHN master-data mapping into the local address tables.
 *
 * <p>This job:
 * <ol>
 *   <li>Matches local provinces against GHN provinces and stores the GHN province id/name</li>
 *   <li>Matches local wards against GHN districts/wards and stores the GHN district/ward mapping</li>
 *   <li>Backfills saved addresses with carrier ids/codes when enough local data is available</li>
 * </ol>
 */
public final class GhnAddressSeeder {

    private static final Logger log = LoggerFactory.getLogger(GhnAddressSeeder.class);

    private GhnAddressSeeder() {
    }

    public static SyncReport seed() throws Exception {
        LocalDateTime startedAt = LocalDateTime.now();
        GhnService ghnService = new GhnService();

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(true);

            List<LocalProvinceRow> provinces = loadLocalProvinces(conn);
            Map<Integer, List<LocalWardRow>> wardsByProvince = loadLocalWards(conn);
            List<AddressRow> addresses = loadAddresses(conn);

            List<GhnProvinceOption> ghnProvinces = loadGhnProvinces(ghnService);
            Map<Integer, ProvinceMapping> provinceMappings = mapProvinces(provinces, ghnProvinces);
            WardMappingResult wardMappingResult = mapWards(wardsByProvince, provinceMappings, ghnService);
            AddressBackfillResult addressBackfillResult = buildAddressBackfills(
                    addresses,
                    provinces,
                    wardsByProvince,
                    provinceMappings,
                    wardMappingResult.wardMappingsByLocalWardCode()
            );

            conn.setAutoCommit(false);
            try {
                int provincesUpdated = applyProvinceMappings(conn, provinceMappings);
                int wardsUpdated = applyWardMappings(conn, wardMappingResult.wardMappingsByLocalWardCode());
                int addressesBackfilled = applyAddressBackfills(conn, addressBackfillResult.updates());
                conn.commit();

                LocalDateTime finishedAt = LocalDateTime.now();
                SyncReport report = new SyncReport(
                        provinces.size(),
                        provincesUpdated,
                        wardMappingResult.totalWards(),
                        wardsUpdated,
                        addresses.size(),
                        addressesBackfilled,
                        wardMappingResult.districtsScanned(),
                        wardMappingResult.districtsSkipped(),
                        wardMappingResult.unmappedWards(),
                        addressBackfillResult.unresolvedAddresses(),
                        startedAt,
                        finishedAt,
                        Duration.between(startedAt, finishedAt).toMillis()
                );
                logSummary(report);
                return report;
            } catch (Exception e) {
                rollbackQuietly(conn);
                throw e;
            }
        }
    }

    private static List<LocalProvinceRow> loadLocalProvinces(Connection conn) throws SQLException {
        List<LocalProvinceRow> rows = new ArrayList<>();
        String sql = """
                select code, name, full_name, ghn_province_id, ghn_province_name
                from provinces
                order by code
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new LocalProvinceRow(
                        rs.getInt("code"),
                        rs.getString("name"),
                        rs.getString("full_name"),
                        rs.getObject("ghn_province_id", Integer.class),
                        rs.getString("ghn_province_name")
                ));
            }
        }
        return rows;
    }

    private static Map<Integer, List<LocalWardRow>> loadLocalWards(Connection conn) throws SQLException {
        Map<Integer, List<LocalWardRow>> rows = new LinkedHashMap<>();
        String sql = """
                select code, province_code, name, full_name, ghn_district_id, ghn_district_name, ghn_ward_code, ghn_ward_name
                from wards
                order by province_code, code
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                LocalWardRow ward = new LocalWardRow(
                        rs.getInt("code"),
                        rs.getInt("province_code"),
                        rs.getString("name"),
                        rs.getString("full_name"),
                        rs.getObject("ghn_district_id", Integer.class),
                        rs.getString("ghn_district_name"),
                        rs.getString("ghn_ward_code"),
                        rs.getString("ghn_ward_name")
                );
                rows.computeIfAbsent(ward.provinceCode(), key -> new ArrayList<>()).add(ward);
            }
        }
        return rows;
    }

    private static List<AddressRow> loadAddresses(Connection conn) throws SQLException {
        List<AddressRow> rows = new ArrayList<>();
        String sql = """
                select id,
                       province_code,
                       ward_code,
                       province,
                       city,
                       district,
                       ward,
                       carrier_province_id,
                       carrier_district_id,
                       carrier_ward_code
                from addresses
                where deleted_at is null
                order by id
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new AddressRow(
                        rs.getLong("id"),
                        rs.getObject("province_code", Integer.class),
                        rs.getObject("ward_code", Integer.class),
                        rs.getString("province"),
                        rs.getString("city"),
                        rs.getString("district"),
                        rs.getString("ward"),
                        rs.getObject("carrier_province_id", Integer.class),
                        rs.getObject("carrier_district_id", Integer.class),
                        rs.getString("carrier_ward_code")
                ));
            }
        }
        return rows;
    }

    private static List<GhnProvinceOption> loadGhnProvinces(GhnService ghnService) throws Exception {
        JsonObject response = parseResponse(ghnService.getLocations("province", null));
        JsonArray data = dataArray(response, "data");
        List<GhnProvinceOption> rows = new ArrayList<>();
        for (JsonElement element : data) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject json = element.getAsJsonObject();
            Integer id = intValue(json, "ProvinceID");
            String name = stringValue(json, "ProvinceName");
            if (id == null || isBlank(name)) {
                continue;
            }
            Set<String> aliases = collectAliases(json.get("NameExtension"));
            aliases.add(name);
            rows.add(new GhnProvinceOption(id, name, aliases));
        }
        return rows;
    }

    private static WardMappingResult mapWards(Map<Integer, List<LocalWardRow>> wardsByProvince,
                                              Map<Integer, ProvinceMapping> provinceMappings,
                                              GhnService ghnService) throws Exception {
        Map<Integer, WardMapping> bestByWardCode = new LinkedHashMap<>();
        Map<Integer, Integer> bestScoreByWardCode = new HashMap<>();
        int districtsScanned = 0;
        int districtsSkipped = 0;

        for (ProvinceMapping provinceMapping : provinceMappings.values()) {
            List<LocalWardRow> localWards = wardsByProvince.getOrDefault(provinceMapping.localProvinceCode(), List.of());
            if (localWards.isEmpty()) {
                continue;
            }

            List<GhnDistrictOption> districts = loadGhnDistricts(ghnService, provinceMapping.ghnProvinceId());
            for (GhnDistrictOption district : districts) {
                districtsScanned++;
                List<GhnWardOption> ghnWards = loadGhnWards(ghnService, district.id());
                if (ghnWards.isEmpty()) {
                    districtsSkipped++;
                    continue;
                }

                for (LocalWardRow localWard : localWards) {
                    for (GhnWardOption ghnWard : ghnWards) {
                        int score = scoreWardMatch(localWard, ghnWard);
                        if (score <= 0) {
                            continue;
                        }
                        Integer currentBest = bestScoreByWardCode.get(localWard.code());
                        if (currentBest == null || score > currentBest) {
                            bestScoreByWardCode.put(localWard.code(), score);
                            bestByWardCode.put(localWard.code(), new WardMapping(
                                    localWard.code(),
                                    localWard.provinceCode(),
                                    district.id(),
                                    district.name(),
                                    ghnWard.code(),
                                    ghnWard.name(),
                                    localWard.name(),
                                    localWard.fullName()
                            ));
                        }
                    }
                }
            }
        }

        int totalWards = wardsByProvince.values().stream().mapToInt(List::size).sum();
        int mappedWards = bestByWardCode.size();
        return new WardMappingResult(bestByWardCode, totalWards, mappedWards, districtsScanned, districtsSkipped);
    }

    private static AddressBackfillResult buildAddressBackfills(List<AddressRow> addresses,
                                                               List<LocalProvinceRow> provinces,
                                                               Map<Integer, List<LocalWardRow>> wardsByProvince,
                                                               Map<Integer, ProvinceMapping> provinceMappings,
                                                               Map<Integer, WardMapping> wardMappingsByLocalWardCode) {
        Map<Integer, LocalProvinceRow> provincesByCode = indexProvinces(provinces);
        Map<String, ProvinceMapping> provinceMappingsByName = indexProvinceMappingsByName(provinceMappings, provincesByCode);
        Map<Integer, List<WardMapping>> wardMappingsByProvinceCode = indexWardMappingsByProvinceCode(wardMappingsByLocalWardCode.values());
        Map<Integer, LocalWardRow> localWardsByCode = indexLocalWardsByCode(wardsByProvince);

        List<AddressUpdate> updates = new ArrayList<>();
        int unresolved = 0;

        for (AddressRow address : addresses) {
            ProvinceMapping provinceMapping = resolveProvinceMapping(address, provincesByCode, provinceMappingsByName, provinceMappings);
            WardMapping wardMapping = resolveWardMapping(address, provinceMapping, localWardsByCode, wardMappingsByLocalWardCode, wardMappingsByProvinceCode, wardsByProvince);

            Integer carrierProvinceId = provinceMapping == null ? address.carrierProvinceId() : provinceMapping.ghnProvinceId();
            Integer carrierDistrictId = wardMapping == null ? address.carrierDistrictId() : wardMapping.ghnDistrictId();
            String carrierWardCode = wardMapping == null ? address.carrierWardCode() : wardMapping.ghnWardCode();

            boolean changed = !Objects.equals(address.carrierProvinceId(), carrierProvinceId)
                    || !Objects.equals(address.carrierDistrictId(), carrierDistrictId)
                    || !Objects.equals(normalizeCarrierCode(address.carrierWardCode()), normalizeCarrierCode(carrierWardCode));

            if (!changed) {
                continue;
            }

            if (carrierProvinceId == null && carrierDistrictId == null && isBlank(carrierWardCode)) {
                unresolved++;
                continue;
            }

            updates.add(new AddressUpdate(address.id(), carrierProvinceId, carrierDistrictId, carrierWardCode));
        }

        return new AddressBackfillResult(updates, unresolved);
    }

    private static ProvinceMapping resolveProvinceMapping(AddressRow address,
                                                          Map<Integer, LocalProvinceRow> provincesByCode,
                                                          Map<String, ProvinceMapping> provinceMappingsByName,
                                                          Map<Integer, ProvinceMapping> provinceMappings) {
        if (address.provinceCode() != null) {
            ProvinceMapping byCode = provinceMappings.get(address.provinceCode());
            if (byCode != null) {
                return byCode;
            }
        }

        String provinceText = firstNonBlank(address.province(), address.city());
        if (!isBlank(provinceText)) {
            ProvinceMapping byName = provinceMappingsByName.get(normalizeLocationName(provinceText));
            if (byName != null) {
                return byName;
            }
        }

        if (address.provinceCode() != null) {
            LocalProvinceRow localProvince = provincesByCode.get(address.provinceCode());
            if (localProvince != null) {
                ProvinceMapping byLocal = provinceMappingsByName.get(normalizeLocationName(firstNonBlank(localProvince.fullName(), localProvince.name())));
                if (byLocal != null) {
                    return byLocal;
                }
            }
        }

        return null;
    }

    private static WardMapping resolveWardMapping(AddressRow address,
                                                  ProvinceMapping provinceMapping,
                                                  Map<Integer, LocalWardRow> localWardsByCode,
                                                  Map<Integer, WardMapping> wardMappingsByLocalWardCode,
                                                  Map<Integer, List<WardMapping>> wardMappingsByProvinceCode,
                                                  Map<Integer, List<LocalWardRow>> wardsByProvince) {
        if (provinceMapping == null) {
            return null;
        }

        if (address.wardCode() != null) {
            WardMapping byLocalWardCode = wardMappingsByLocalWardCode.get(address.wardCode());
            if (byLocalWardCode != null) {
                return byLocalWardCode;
            }
        }

        List<WardMapping> provinceWardMappings = wardMappingsByProvinceCode.getOrDefault(provinceMapping.localProvinceCode(), List.of());
        if (provinceWardMappings.isEmpty()) {
            return null;
        }

        LocalWardRow matchedLocalWard = matchLocalWard(address, wardsByProvince.getOrDefault(provinceMapping.localProvinceCode(), List.of()));
        if (matchedLocalWard != null) {
            WardMapping mapping = wardMappingsByLocalWardCode.get(matchedLocalWard.code());
            if (mapping != null) {
                return mapping;
            }
        }

        return null;
    }

    private static LocalWardRow matchLocalWard(AddressRow address, List<LocalWardRow> localWards) {
        if (localWards == null || localWards.isEmpty()) {
            return null;
        }

        String wardText = firstNonBlank(address.ward(), address.district());
        if (isBlank(wardText)) {
            return null;
        }

        LocalWardRow best = null;
        int bestScore = 0;
        for (LocalWardRow localWard : localWards) {
            for (String candidate : candidateNames(localWard.name(), localWard.fullName())) {
                int score = scoreMatch(wardText, candidate);
                if (score > bestScore) {
                    bestScore = score;
                    best = localWard;
                }
            }
        }
        return best;
    }

    private static Map<Integer, LocalProvinceRow> indexProvinces(List<LocalProvinceRow> provinces) {
        Map<Integer, LocalProvinceRow> map = new LinkedHashMap<>();
        for (LocalProvinceRow province : provinces) {
            map.put(province.code(), province);
        }
        return map;
    }

    private static Map<String, ProvinceMapping> indexProvinceMappingsByName(Map<Integer, ProvinceMapping> provinceMappings,
                                                                            Map<Integer, LocalProvinceRow> provincesByCode) {
        Map<String, ProvinceMapping> map = new HashMap<>();
        for (ProvinceMapping mapping : provinceMappings.values()) {
            map.put(normalizeLocationName(mapping.ghnProvinceName()), mapping);
        }
        for (Map.Entry<Integer, LocalProvinceRow> entry : provincesByCode.entrySet()) {
            LocalProvinceRow province = entry.getValue();
            ProvinceMapping mapping = provinceMappings.get(entry.getKey());
            if (mapping != null) {
                map.putIfAbsent(normalizeLocationName(firstNonBlank(province.fullName(), province.name())), mapping);
                map.putIfAbsent(normalizeLocationName(firstNonBlank(province.name(), province.fullName())), mapping);
            }
        }
        return map;
    }

    private static Map<Integer, List<WardMapping>> indexWardMappingsByProvinceCode(Iterable<WardMapping> mappings) {
        Map<Integer, List<WardMapping>> map = new LinkedHashMap<>();
        for (WardMapping mapping : mappings) {
            map.computeIfAbsent(mapping.provinceCode(), key -> new ArrayList<>()).add(mapping);
        }
        return map;
    }

    private static Map<Integer, LocalWardRow> indexLocalWardsByCode(Map<Integer, List<LocalWardRow>> wardsByProvince) {
        Map<Integer, LocalWardRow> map = new LinkedHashMap<>();
        for (List<LocalWardRow> wards : wardsByProvince.values()) {
            for (LocalWardRow ward : wards) {
                map.put(ward.code(), ward);
            }
        }
        return map;
    }

    private static Map<Integer, ProvinceMapping> mapProvinces(List<LocalProvinceRow> provinces, List<GhnProvinceOption> ghnProvinces) {
        Map<Integer, ProvinceMapping> mappings = new LinkedHashMap<>();
        for (LocalProvinceRow province : provinces) {
            GhnProvinceOption match = findBestProvinceMatch(province, ghnProvinces);
            if (match == null) {
                log.info("[GHN] Skip province {} ({}) : no safe match", province.displayName(), province.code());
                continue;
            }
            mappings.put(province.code(), new ProvinceMapping(province.code(), match.id(), match.name(), province.name(), province.fullName()));
        }
        return mappings;
    }

    private static GhnProvinceOption findBestProvinceMatch(LocalProvinceRow province, List<GhnProvinceOption> ghnProvinces) {
        if (province == null || ghnProvinces == null || ghnProvinces.isEmpty()) {
            return null;
        }

        List<String> localCandidates = candidateNames(province.name(), province.fullName());
        GhnProvinceOption best = null;
        int bestScore = 0;
        for (GhnProvinceOption candidate : ghnProvinces) {
            for (String remote : candidate.aliases()) {
                for (String local : localCandidates) {
                    int score = scoreMatch(local, remote);
                    if (score > bestScore) {
                        bestScore = score;
                        best = candidate;
                    }
                }
            }
        }
        return bestScore > 0 ? best : null;
    }

    private static List<GhnDistrictOption> loadGhnDistricts(GhnService ghnService, Integer provinceId) throws Exception {
        if (provinceId == null) {
            return List.of();
        }
        JsonObject response = parseResponse(ghnService.getLocations("district", String.valueOf(provinceId)));
        JsonArray data = dataArray(response, "data");
        List<GhnDistrictOption> rows = new ArrayList<>();
        for (JsonElement element : data) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject json = element.getAsJsonObject();
            Integer id = intValue(json, "DistrictID");
            Integer returnedProvinceId = intValue(json, "ProvinceID");
            String name = stringValue(json, "DistrictName");
            if (id == null || isBlank(name)) {
                continue;
            }
            Set<String> aliases = collectAliases(json.get("NameExtension"));
            aliases.add(name);
            rows.add(new GhnDistrictOption(id, returnedProvinceId == null ? provinceId : returnedProvinceId, name, aliases));
        }
        return rows;
    }

    private static List<GhnWardOption> loadGhnWards(GhnService ghnService, Integer districtId) throws Exception {
        if (districtId == null) {
            return List.of();
        }
        JsonObject response = parseResponse(ghnService.getLocations("ward", String.valueOf(districtId)));
        JsonArray data = dataArray(response, "data");
        List<GhnWardOption> rows = new ArrayList<>();
        for (JsonElement element : data) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject json = element.getAsJsonObject();
            String code = stringValue(json, "WardCode");
            Integer returnedDistrictId = intValue(json, "DistrictID");
            String name = stringValue(json, "WardName");
            if (isBlank(code) || isBlank(name)) {
                continue;
            }
            Set<String> aliases = collectAliases(json.get("NameExtension"));
            aliases.add(name);
            rows.add(new GhnWardOption(code, returnedDistrictId == null ? districtId : returnedDistrictId, name, aliases));
        }
        return rows;
    }

    private static int applyProvinceMappings(Connection conn, Map<Integer, ProvinceMapping> mappings) throws SQLException {
        if (mappings.isEmpty()) {
            return 0;
        }
        String sql = """
                update provinces
                set ghn_province_id = ?,
                    ghn_province_name = ?
                where code = ?
                """;
        int updated = 0;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (ProvinceMapping mapping : mappings.values()) {
                ps.setInt(1, mapping.ghnProvinceId());
                ps.setString(2, mapping.ghnProvinceName());
                ps.setInt(3, mapping.localProvinceCode());
                updated += ps.executeUpdate();
            }
        }
        return updated;
    }

    private static int applyWardMappings(Connection conn, Map<Integer, WardMapping> mappings) throws SQLException {
        if (mappings.isEmpty()) {
            return 0;
        }
        String sql = """
                update wards
                set ghn_district_id = ?,
                    ghn_district_name = ?,
                    ghn_ward_code = ?,
                    ghn_ward_name = ?
                where code = ?
                """;
        int updated = 0;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (WardMapping mapping : mappings.values()) {
                ps.setInt(1, mapping.ghnDistrictId());
                ps.setString(2, mapping.ghnDistrictName());
                ps.setString(3, mapping.ghnWardCode());
                ps.setString(4, mapping.ghnWardName());
                ps.setInt(5, mapping.localWardCode());
                updated += ps.executeUpdate();
            }
        }
        return updated;
    }

    private static int applyAddressBackfills(Connection conn, List<AddressUpdate> updates) throws SQLException {
        if (updates.isEmpty()) {
            return 0;
        }
        String sql = """
                update addresses
                set carrier_province_id = ?,
                    carrier_district_id = ?,
                    carrier_ward_code = ?
                where id = ?
                """;
        int updated = 0;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (AddressUpdate update : updates) {
                if (update.carrierProvinceId() == null && update.carrierDistrictId() == null && isBlank(update.carrierWardCode())) {
                    continue;
                }
                if (update.carrierProvinceId() == null) {
                    ps.setNull(1, Types.INTEGER);
                } else {
                    ps.setInt(1, update.carrierProvinceId());
                }
                if (update.carrierDistrictId() == null) {
                    ps.setNull(2, Types.INTEGER);
                } else {
                    ps.setInt(2, update.carrierDistrictId());
                }
                if (isBlank(update.carrierWardCode())) {
                    ps.setNull(3, Types.VARCHAR);
                } else {
                    ps.setString(3, update.carrierWardCode().trim());
                }
                ps.setLong(4, update.addressId());
                updated += ps.executeUpdate();
            }
        }
        return updated;
    }

    private static void logSummary(SyncReport report) {
        log.info("=================================");
        log.info("GHN mapping seeding completed.");
        log.info("Provinces mapped: {}/{}", report.provincesMapped(), report.provincesTotal());
        log.info("Wards mapped: {}", report.wardsMapped());
        log.info("Addresses backfilled: {}", report.addressesBackfilled());
        log.info("Districts scanned: {}", report.districtsScanned());
        log.info("Districts skipped: {}", report.districtsSkipped());
        log.info("Unresolved addresses: {}", report.addressesUnresolved());
        log.info("=================================");
    }

    private static JsonObject parseResponse(String body) {
        JsonElement element = JsonParser.parseString(body);
        if (element == null || !element.isJsonObject()) {
            throw new IllegalStateException("GHN response does not contain a JSON object.");
        }
        return element.getAsJsonObject();
    }

    private static JsonArray dataArray(JsonObject object, String key) {
        if (object == null || key == null || !object.has(key) || object.get(key) == null || object.get(key).isJsonNull()) {
            return new JsonArray();
        }
        if (!object.get(key).isJsonArray()) {
            return new JsonArray();
        }
        return object.getAsJsonArray(key);
    }

    private static Set<String> collectAliases(JsonElement element) {
        if (element == null || element.isJsonNull() || !element.isJsonArray()) {
            return new LinkedHashSet<>();
        }
        Set<String> aliases = new LinkedHashSet<>();
        for (JsonElement alias : element.getAsJsonArray()) {
            if (alias != null && alias.isJsonPrimitive()) {
                String value = alias.getAsString();
                if (!isBlank(value)) {
                    aliases.add(value);
                }
            }
        }
        return aliases;
    }

    private static int scoreWardMatch(LocalWardRow localWard, GhnWardOption ghnWard) {
        int score = 0;
        for (String localCandidate : candidateNames(localWard.name(), localWard.fullName())) {
            for (String remoteCandidate : ghnWard.aliases()) {
                score = Math.max(score, scoreMatch(localCandidate, remoteCandidate));
            }
        }
        return score;
    }

    private static int scoreMatch(String left, String right) {
        String normalizedLeft = normalizeLocationName(left);
        String normalizedRight = normalizeLocationName(right);
        if (normalizedLeft.isBlank() || normalizedRight.isBlank()) {
            return 0;
        }
        if (normalizedLeft.equals(normalizedRight)) {
            return 3;
        }
        if (normalizedLeft.contains(normalizedRight) || normalizedRight.contains(normalizedLeft)) {
            return 2;
        }
        return 0;
    }

    private static List<String> candidateNames(String... values) {
        List<String> candidates = new ArrayList<>();
        if (values == null) {
            return candidates;
        }
        for (String value : values) {
            if (!isBlank(value)) {
                candidates.add(value);
            }
        }
        return candidates;
    }

    private static String normalizeLocationName(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('đ', 'd')
                .replace('Đ', 'D')
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .replaceAll("\\b(thanh pho|tinh|quan|huyen|phuong|xa|thi xa|thi tran|tp)\\b", " ")
                .replaceAll("\\s+", " ")
                .trim();
        return normalized;
    }

    private static String normalizeCarrierCode(String value) {
        return value == null ? null : value.trim();
    }

    private static Integer intValue(JsonObject object, String key) {
        if (object == null || key == null || !object.has(key) || object.get(key) == null || object.get(key).isJsonNull()) {
            return null;
        }
        try {
            return object.get(key).getAsInt();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String stringValue(JsonObject object, String key) {
        if (object == null || key == null || !object.has(key) || object.get(key) == null || object.get(key).isJsonNull()) {
            return null;
        }
        try {
            return object.get(key).getAsString();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String firstNonBlank(String first, String second) {
        if (!isBlank(first)) {
            return first.trim();
        }
        if (!isBlank(second)) {
            return second.trim();
        }
        return null;
    }

    private static void rollbackQuietly(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            conn.rollback();
        } catch (SQLException ignored) {
            log.debug("Ignoring GHN mapping rollback failure");
        }
    }

    private record LocalProvinceRow(Integer code, String name, String fullName, Integer ghnProvinceId, String ghnProvinceName) {
        String displayName() {
            return firstNonBlank(fullName, name);
        }
    }

    private record LocalWardRow(Integer code, Integer provinceCode, String name, String fullName,
                                Integer ghnDistrictId, String ghnDistrictName,
                                String ghnWardCode, String ghnWardName) {
    }

    private record AddressRow(Long id, Integer provinceCode, Integer wardCode,
                              String province, String city, String district, String ward,
                              Integer carrierProvinceId, Integer carrierDistrictId, String carrierWardCode) {
    }

    private record GhnProvinceOption(Integer id, String name, Set<String> aliases) {
    }

    private record GhnDistrictOption(Integer id, Integer provinceId, String name, Set<String> aliases) {
    }

    private record GhnWardOption(String code, Integer districtId, String name, Set<String> aliases) {
    }

    private record ProvinceMapping(Integer localProvinceCode, Integer ghnProvinceId, String ghnProvinceName,
                                   String localProvinceName, String localProvinceFullName) {
    }

    private record WardMapping(Integer localWardCode, Integer provinceCode, Integer ghnDistrictId,
                               String ghnDistrictName, String ghnWardCode, String ghnWardName,
                               String localWardName, String localWardFullName) {
    }

    private record WardMappingResult(Map<Integer, WardMapping> wardMappingsByLocalWardCode,
                                     int totalWards,
                                     int mappedWards,
                                     int districtsScanned,
                                     int districtsSkipped) {

        int unmappedWards() {
            return Math.max(0, totalWards - mappedWards);
        }
    }

    private record AddressUpdate(Long addressId, Integer carrierProvinceId, Integer carrierDistrictId, String carrierWardCode) {
    }

    private record AddressBackfillResult(List<AddressUpdate> updates, int unresolvedAddresses) {
    }

    public record SyncReport(int provincesTotal,
                             int provincesMapped,
                             int wardsTotal,
                             int wardsMapped,
                             int addressesTotal,
                             int addressesBackfilled,
                             int districtsScanned,
                             int districtsSkipped,
                             int wardsUnmapped,
                             int addressesUnresolved,
                             LocalDateTime startedAt,
                             LocalDateTime finishedAt,
                             long durationMillis) {
    }
}
