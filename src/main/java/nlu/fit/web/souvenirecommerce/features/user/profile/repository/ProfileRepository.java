package nlu.fit.web.souvenirecommerce.features.user.profile.repository;

import nlu.fit.web.souvenirecommerce.common.base.AbsBaseRepository;
import nlu.fit.web.souvenirecommerce.model.entity.Province;
import nlu.fit.web.souvenirecommerce.model.entity.User;
import nlu.fit.web.souvenirecommerce.model.entity.Ward;

import java.util.List;
import java.util.Optional;

public class ProfileRepository extends AbsBaseRepository<Long, User> {

    public List<Province> findAllProvinces() {
        return getSession()
                .createQuery("from Province p order by p.name asc", Province.class)
                .getResultList();
    }

    public List<Ward> findWardsByProvinceCode(Integer provinceCode) {
        if (provinceCode == null) {
            return List.of();
        }

        return getSession()
                .createQuery("from Ward w where w.province.code = :provinceCode order by w.name asc", Ward.class)
                .setParameter("provinceCode", provinceCode)
                .getResultList();
    }

    public Optional<Province> findProvinceByCode(Integer provinceCode) {
        if (provinceCode == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(getSession().find(Province.class, provinceCode));
    }

    public Optional<Ward> findWardByCode(Integer wardCode) {
        if (wardCode == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(getSession().find(Ward.class, wardCode));
    }
}
