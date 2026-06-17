package nlu.fit.web.souvenirecommerce.model.entity;

import jakarta.persistence.*;
import lombok.*;
import nlu.fit.web.souvenirecommerce.common.base.AbsBaseEntity;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "addresses")
@SQLDelete(sql = "UPDATE addresses SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address extends AbsBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "receiver_name", length = 50, nullable = false)
    private String receiverName;

    @Column(name = "receiver_phone", length = 15, nullable = false)
    private String receiverPhone;

    @Column(name = "address_detail", nullable = false, length = 255)
    private String addressDetail;

    @Column(nullable = false, length = 100)
    private String ward;

    @Column(nullable = false, length = 100)
    private String district;

    @Column(nullable = false, length = 100)
    private String province;

    @Column(nullable = false, length = 100)
    private String city;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "province_code")
    private Province provinceEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ward_code")
    private Ward wardEntity;

    @Column(name = "ghn_province_id")
    private Integer ghnProvinceId;

    @Column(name = "ghn_district_id")
    private Integer ghnDistrictId;

    @Column(name = "ghn_ward_code", length = 20)
    private String ghnWardCode;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Integer getEffectiveGhnProvinceId() {
        if (ghnProvinceId != null) {
            return ghnProvinceId;
        }
        return provinceEntity == null ? null : provinceEntity.getGhnProvinceId();
    }

    public String getEffectiveGhnProvinceName() {
        if (provinceEntity != null && provinceEntity.getGhnProvinceName() != null && !provinceEntity.getGhnProvinceName().isBlank()) {
            return provinceEntity.getGhnProvinceName();
        }
        return null;
    }

    public Integer getEffectiveGhnDistrictId() {
        if (ghnDistrictId != null) {
            return ghnDistrictId;
        }
        return wardEntity == null ? null : wardEntity.getGhnDistrictId();
    }

    public String getEffectiveGhnWardCode() {
        if (ghnWardCode != null && !ghnWardCode.isBlank()) {
            return ghnWardCode;
        }
        return wardEntity == null ? null : wardEntity.getGhnWardCode();
    }

    public String getEffectiveGhnDistrictName() {
        if (wardEntity != null && wardEntity.getGhnDistrictName() != null && !wardEntity.getGhnDistrictName().isBlank()) {
            return wardEntity.getGhnDistrictName();
        }
        return null;
    }

    public String getEffectiveGhnWardName() {
        if (wardEntity != null && wardEntity.getGhnWardName() != null && !wardEntity.getGhnWardName().isBlank()) {
            return wardEntity.getGhnWardName();
        }
        return null;
    }

    public String getEffectiveProvinceName() {
        if (province != null && !province.isBlank()) {
            return province;
        }
        if (city != null && !city.isBlank()) {
            return city;
        }
        if (provinceEntity != null) {
            if (provinceEntity.getFullName() != null && !provinceEntity.getFullName().isBlank()) {
                return provinceEntity.getFullName();
            }
            return provinceEntity.getName();
        }
        return null;
    }

    public String getEffectiveDistrictName() {
        if (district != null && !district.isBlank()) {
            return district;
        }
        return wardEntity == null ? null : wardEntity.getGhnDistrictName();
    }

    public String getEffectiveWardName() {
        if (ward != null && !ward.isBlank()) {
            return ward;
        }
        if (wardEntity != null) {
            if (wardEntity.getFullName() != null && !wardEntity.getFullName().isBlank()) {
                return wardEntity.getFullName();
            }
            return wardEntity.getName();
        }
        return null;
    }
}
