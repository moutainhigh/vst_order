package com.lvmama.vst.order.utils;

import java.util.ArrayList;
import java.util.List;

public class WireLessUtil {
	 /**
     * 无线distributionChannel字段的枚举值，通过这个字段判断订单是否来自无线
     * */
    private static final List<Long> wirelessDistributionChannelList = new ArrayList<>(3);
    static {
        wirelessDistributionChannelList.add(10000L);
        wirelessDistributionChannelList.add(10001L);
        wirelessDistributionChannelList.add(10002L);
    }
	 /**
     * 是否是无线
	 *	distributorId = 4, distributionChannel in (10000, 10001, 10002)  or distributorId=6
     * */
    public static boolean isWireless(Long distributionId,Long distributionChannel) {
        return distributionId == 6l||(distributionId==4l&& wirelessDistributionChannelList.contains(distributionChannel));
    }
   
}
