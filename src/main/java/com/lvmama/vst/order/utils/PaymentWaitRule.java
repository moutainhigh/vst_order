package com.lvmama.vst.order.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

/**
 * 支付等待时间规则
 */
public class PaymentWaitRule {
	/**
	 * 晚上六点
	 */
	private static final LocalTime EIGHTEEN_TIME = new LocalTime(18, 0);

	/**
	 * 12小时
	 */
	public static final Integer TWELVE = 12 * 60;

	/**
	 * 2小时
	 */
	public static final Integer TWO = 2 * 60;

	/**
	 * 有时间规则的品类
	 */
	private static final String[] categorys = { "category_route_group_FOREIGNLINE" };// 跟团游
																						// 出境/港澳台

	/**
	 * 根据品类key值和订单创建时间获取支付等待时间
	 * 
	 * @param key
	 *            品类key
	 * @param createOrderTime
	 *            订单创建时间
	 * @return 支付等待时间
	 */
	public static Integer getPaymentMinute(String key, Date createOrderTime) {
		// 不在时间控制规则内的直接从map中获取
		if (!StringUtils.startsWithAny(key, categorys)) {
			return RULEMAP.get(key);
		}

		LocalTime createTime = LocalTime.fromDateFields(createOrderTime);
		// 在时间控制规则内  晚上六点及六点以后的订单
		if (EIGHTEEN_TIME.compareTo(createTime) <= 0) {
			return getPaymentWait(key, createOrderTime);
		}

		// 0点到晚上6点之间的订单12小时
		return TWELVE;
	}

	/**
	 * 获取有时间阶段支付等待时间
	 * 
	 * @param key
	 *            品类key
	 * @param createOrderTime
	 *            生成订单时间
	 * @return 支付等待时间
	 */
	private static Integer getPaymentWait(String key, Date createOrderTime) {
		// 默认12小时
		Integer waitTime = TWELVE;

		// 订单创建时间
		DateTime createOrderDateTime = LocalDateTime.fromDateFields(createOrderTime).toDateTime();
		int createMinute = createOrderDateTime.getMinuteOfDay();
		// 该天最大时间
		DateTime endOfDay = createOrderDateTime.millisOfDay().withMaximumValue();
		int endMinute = endOfDay.getMinuteOfDay();
		// 距离今天结束还有多少分钟
		int endOfDayMinute = endMinute - createMinute;

		// 支付等待时间 (第二天中午12点) = endOfDayMinute + 12*60;
		waitTime = endOfDayMinute + TWELVE;

		return waitTime;
	}

	/**
	 * 不分时间阶段品类下单支付等待时间
	 */
	private static final Map<String, Integer> RULEMAP = new HashMap<String, Integer>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = -2047140942985069115L;

		{
			// 酒店
			put("category_hotel", 120);
			// 景点门票
			put("category_single_ticket", 1440);
			// 其它票
			put("category_other_ticket", 1440);
			// 组合套餐票
			put("category_comb_ticket", 1440);
			// 玩乐演出票
			put("category_show_ticket", 1440);
			// 其它机票
			put("category_traffic_aero_other", 1800);
			// 其它火车票
			put("category_traffic_train_other", 1800);
			// 其它巴士
			put("category_traffic_bus_other", 1800);
			// 其它船票
			put("category_traffic_ship_other", 1800);
			// 岸上观光
			put("category_sightseeing", 720);
			// 邮轮组合产品
			put("category_comb_cruise", 720);
			// 签证
			put("category_visa", 120);
			// 保险
			put("category_insurance", 1440);
			// 邮轮
			put("category_cruise", 720);
			// 邮轮附加项
			put("category_cruise_addition", 720);

			// 跟团游 国内短线
			put("category_route_group_INNERSHORTLINE", 120);
			// 跟团游 国内长线
			put("category_route_group_INNERLONGLINE", 120);

			// 当地游 国内
			put("category_route_local_INNERLINE", 120);
			// 当地游 出境/港澳台
			put("category_route_local_FOREIGNLINE", 720);

			// 酒店套餐 国内
			put("category_route_hotelcomb_INNERLINE", 120);
			// 酒店套餐 出境/港澳台
			put("category_route_hotelcomb_FOREIGNLINE", 720);

			// 自由行 国内
			put("category_route_freedom_INNERLINE", 120);
			// 自由行 出境/港澳台
			put("category_route_freedom_FOREIGNLINE", 720);

			// wifi
			put("category_wifi", 120);
			// 交通接驳
			put("category_connects", 120);
			// 美食
			put("category_food", 120);
			// 娱乐
			put("category_sport", 120);
			// 购物
			put("category_shop", 120);
		}
	};

}
