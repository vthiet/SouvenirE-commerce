package nlu.fit.web.souvenirecommerce.features.banner.service;

import nlu.fit.web.souvenirecommerce.common.base.AbsBaseService;
import nlu.fit.web.souvenirecommerce.features.banner.repository.BannerRepository;
import nlu.fit.web.souvenirecommerce.model.entity.Banner;

public class BannerService extends AbsBaseService<Long, Banner> {
    public BannerService() {
        super(new BannerRepository());
    }
}
