package com.lvmama.vst.order.factory;

import com.lvmama.vst.back.order.po.OrderEnum;
import com.lvmama.vst.order.service.apportion.particle.ApportionParticleService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Created by zhouyanqun on 2017/4/18.
 * OrderDetailApportionCompleteService服务的工厂
 * 已经提前注入好所有的OrderDetailApportionCompleteService服务，
 * 使用时根据分摊粒度获取对应的OrderDetailApportionCompleteService服务即可
 */
@Component
public class ApportionParticleServiceFactory {
    /**
     * 分摊粒度为子单的服务
     * */
    @Resource(name = "apportionComplete4ItemServiceImpl")
    private ApportionParticleService apportionComplete4ItemService;
    /**
     * 分摊粒度为入住日期的服务
     * */
    @Resource(name = "apportionComplete4CheckInDateServiceImpl")
    private ApportionParticleService apportionComplete4CheckInDateService;
    /**
     * 分摊粒度为价格类型(含房差，即成人价、儿童价、房差)的服务
     * */
    @Resource(name = "apportionComplete4PriceTypeSpreadServiceImpl")
    private ApportionParticleService apportionComplete4PriceTypeSpreadService;
    /**
     * 分摊粒度为价格类型的服务
     * */
    @Resource(name = "apportionComplete4PriceTypeNoSpreadServiceImpl")
    private ApportionParticleService apportionComplete4PriceTypeNoSpreadService;

    /**
     * 根据分摊粒度，获取对应的补全服务
     * */
    public ApportionParticleService catchOrderDetailApportionCompleteService(OrderEnum.ORDER_APPORTION_PARTICLE orderApportionParticle){
        switch (orderApportionParticle) {
            case apportion_particle_check_in_date: return apportionComplete4CheckInDateService;
            case apportion_particle_price_type_with_spread: return apportionComplete4PriceTypeSpreadService;
            case apportion_particle_price_type_without_spread:return apportionComplete4PriceTypeNoSpreadService;
            default: return apportionComplete4ItemService;
        }
    }
}
