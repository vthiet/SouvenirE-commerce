package nlu.fit.web.souvenirecommerce.features.user.address;

import nlu.fit.web.souvenirecommerce.features.user.profile.repository.ProfileRepository;
import nlu.fit.web.souvenirecommerce.model.entity.Address;
import nlu.fit.web.souvenirecommerce.model.entity.Province;
import nlu.fit.web.souvenirecommerce.model.entity.User;
import nlu.fit.web.souvenirecommerce.model.entity.Ward;

import java.util.List;
import java.util.Optional;

public class AddressService {
    private final AddressRepository addressRepository = new AddressRepository();
    private final ProfileRepository profileRepository = new ProfileRepository();

    public List<Address> getUserAddresses(Long userId) {
        return addressRepository.findByUserId(userId);
    }

    public List<Province> getProvinces() {
        return profileRepository.findAllProvinces();
    }

    public List<Ward> getWardsByProvinceCode(Integer provinceCode) {
        return profileRepository.findWardsByProvinceCode(provinceCode);
    }

    public boolean addAddress(User user, String addressDetail, Integer provinceCode, Integer wardCode) {
        return createAddress(user, user == null ? null : user.getFullName(), user == null ? null : user.getPhone(),
                addressDetail, provinceCode, wardCode).isPresent();
    }

    public Optional<Address> createAddress(User user,
                                           String receiverName,
                                           String receiverPhone,
                                           String addressDetail,
                                           Integer provinceCode,
                                           Integer wardCode) {
        if (user == null || user.getId() == null || isBlank(addressDetail)) {
            return Optional.empty();
        }

        Optional<Province> province = profileRepository.findProvinceByCode(provinceCode);
        Optional<Ward> ward = profileRepository.findWardByCode(wardCode);
        if (province.isEmpty() || ward.isEmpty()) {
            return Optional.empty();
        }

        Province selectedProvince = province.get();
        Ward selectedWard = ward.get();
        if (selectedWard.getProvince() == null || !selectedProvince.getCode().equals(selectedWard.getProvince().getCode())) {
            return Optional.empty();
        }
        String provinceName = displayName(selectedProvince.getFullName(), selectedProvince.getName());
        String districtName = firstNotBlank(selectedWard.getGhnDistrictName(), "");
        String wardName = displayName(selectedWard.getFullName(), selectedWard.getName());

        Address address = Address.builder()
                .user(user)
                .receiverName(firstNotBlank(receiverName, user.getFullName()))
                .receiverPhone(firstNotBlank(receiverPhone, user.getPhone()))
                .addressDetail(addressDetail.trim())
                .province(provinceName)
                .city(provinceName)
                .district(districtName)
                .ward(wardName)
                .ghnProvinceId(selectedProvince.getGhnProvinceId())
                .ghnDistrictId(selectedWard.getGhnDistrictId())
                .ghnWardCode(selectedWard.getGhnWardCode())
                .provinceEntity(selectedProvince)
                .wardEntity(selectedWard)
                .isDefault(addressRepository.countByUserId(user.getId()) == 0)
                .build();
        return addressRepository.save(address);
    }

    public Optional<Address> createGhnAddress(User user,
                                              String receiverName,
                                              String receiverPhone,
                                              String addressDetail,
                                              Integer provinceId,
                                              Integer districtId,
                                              String wardCode,
                                              String provinceName,
                                              String districtName,
                                              String wardName) {
        if (user == null || user.getId() == null || isBlank(addressDetail)
                || provinceId == null || districtId == null || isBlank(wardCode)
                || isBlank(provinceName) || isBlank(districtName) || isBlank(wardName)) {
            return Optional.empty();
        }

        Optional<Province> provinceEntity = profileRepository.findProvinceByGhnProvinceId(provinceId);
        Optional<Ward> wardEntity = profileRepository.findWardByGhnDistrictIdAndWardCode(districtId, wardCode);

        Address address = Address.builder()
                .user(user)
                .receiverName(firstNotBlank(receiverName, user.getFullName()))
                .receiverPhone(firstNotBlank(receiverPhone, user.getPhone()))
                .addressDetail(addressDetail.trim())
                .province(provinceName.trim())
                .city(provinceName.trim())
                .district(districtName.trim())
                .ward(wardName.trim())
                .ghnProvinceId(provinceId)
                .ghnDistrictId(districtId)
                .ghnWardCode(wardCode.trim())
                .provinceEntity(provinceEntity.orElse(null))
                .wardEntity(wardEntity.orElse(null))
                .isDefault(addressRepository.countByUserId(user.getId()) == 0)
                .build();
        return addressRepository.save(address);
    }

    public Optional<Address> getUserAddress(Long userId, Long addressId) {
        return addressRepository.findByIdAndUserId(addressId, userId);
    }

    public boolean setDefaultAddress(Long userId, Long addressId) {
        if (userId == null || addressId == null) {
            return false;
        }
        return addressRepository.setDefault(addressId, userId);
    }

    public boolean deleteAddress(Long userId, Long addressId) {
        if (userId == null || addressId == null) {
            return false;
        }
        Optional<Address> address = addressRepository.findByIdAndUserId(addressId, userId);
        if (address.isEmpty()) {
            return false;
        }

        boolean deleted = addressRepository.deleteByIdAndUserId(addressId, userId);
        if (!deleted) {
            return false;
        }

        if (address.get().isDefault()) {
            List<Address> remainingAddresses = addressRepository.findByUserId(userId);
            if (!remainingAddresses.isEmpty()) {
                addressRepository.setDefault(remainingAddresses.get(0).getId(), userId);
            }
        }

        return true;
    }

    private String displayName(String fullName, String name) {
        return fullName == null || fullName.isBlank() ? name : fullName;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String firstNotBlank(String preferred, String fallback) {
        return isBlank(preferred) ? fallback : preferred.trim();
    }
}
