package nlu.fit.web.souvenirecommerce.features.banner.repository;

import nlu.fit.web.souvenirecommerce.common.base.AbsBaseRepository;
import nlu.fit.web.souvenirecommerce.model.entity.Banner;

public class BannerRepository extends AbsBaseRepository<Long, Banner> {
    public BannerRepository() {
        super(Banner.class);
    }
}
