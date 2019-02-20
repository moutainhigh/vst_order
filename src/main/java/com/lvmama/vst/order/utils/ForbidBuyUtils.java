package com.lvmama.vst.order.utils;

import com.lvmama.vst.back.order.po.OrdOrder;
import com.lvmama.vst.back.order.po.OrdOrderItem;
import com.lvmama.vst.back.order.po.OrdPerson;
import com.lvmama.vst.back.prom.po.PromForbidBuy;

import com.lvmama.vst.order.redis.JedisTemplate2;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhouguoliang on 2016/4/28.
 */
public class ForbidBuyUtils {

	private static final Log LOG = LogFactory.getLog(ForbidBuyUtils.class);
	public static final String COMMA = ",";

	public static Date getDate(PromForbidBuy pb, Date visdate, Date createDate) {

		if ("CREATEDATE".equals(pb.getPeriodType())) {

			return createDate;
		}
		if ("VISITDATE".equals(pb.getPeriodType())) {
			return visdate;
		}
		if ("N".equals(pb.getPeriodType())) {

			if (pb.getVisitBeginDate() != null && pb.getVisitEndDate() != null) {
				return visdate;
			} else {
				return createDate;
			}

		}
		return null;
	}

	public static String getPeriodType(PromForbidBuy pb) {

		if ("CREATEDATE".equals(pb.getPeriodType())) {
			return "C";
		}
		if ("VISITDATE".equals(pb.getPeriodType())) {
			return "V";
		}
		return "N";
	}

	public static String getObjectTypeKey(PromForbidBuy pb) {
		String objectTypekey = "";
		if ("GOODS".equals(pb.getObjectType())) {
			objectTypekey = "G";
		}
		if ("PRODUCT".equals(pb.getObjectType())) {
			objectTypekey = "P";
		}
		if ("CATEGORY".equals(pb.getObjectType())) {
			objectTypekey = "C";
		}
		if("GROUPGOODS".equals(pb.getObjectType())){
			objectTypekey="GG";
		}
		return objectTypekey;
	}

	public static String getQuantityType(PromForbidBuy pb) {
		String op = pb.getQuantityType();
		if ("ORDER".equals(op)) {
			return "O";
		}

		else if ("PRODUCT_GOODS".equals(op)) {
			return "P";
		}
		return "";
	}

	public static String formartdate(Date date) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		return simpleDateFormat.format(date);
	}

	public static List<Long> getGoodsIdsbyItem(List<OrdOrderItem> item) {
		ArrayList<Long> ids = new ArrayList<Long>();
		for (OrdOrderItem ordOrderItem : item) {
			ids.add(ordOrderItem.getSuppGoodsId());
		}
		return ids;
	}

	public static Long getProductId(OrdOrder order) {
		return order.getProductId();
	}

	public static String getQuantity(OrdOrderItem item) {
		if (item == null) {
			return "0";
		} else {
			return item.getQuantity().toString();
		}
	}

	public static List<String> getphonesIds(OrdOrder order,
											List<OrdPerson> personlist) {
		ArrayList<String> ids = new ArrayList<String>();
		for (OrdPerson ordPerson : personlist) {
			String mobile = ordPerson.getMobile();
			ids.add(mobile);
		}
		LOG.info("THE PHONE LIST ORDER IS " + personlist);
		return ids;
	}

	public static String getUserId(OrdOrder order) {

		return String.valueOf(order.getUserNo());
	}

	public static List<String> getIdtypeAndIds(OrdOrder order,
											   List<OrdPerson> personlist) {
		ArrayList<String> ids = new ArrayList<String>();
		for (OrdPerson ordPerson : personlist) {
			String idtype = ordPerson.getIdType();
			String idNo = ordPerson.getIdNo();
			ids.add(idtype + idNo);
		}
		LOG.info("THE ID AND TYPE IS " + ids);
		return ids;
	}

	public static String getMobileEquipmentNo(OrdOrder order) {
		LOG.info("mobileequipmentno is" + order.getMobileEquipmentNo());
		return order.getMobileEquipmentNo();
	}

	public static List<String> allids14(PromForbidBuy pb, OrdOrder order) {
		ArrayList<String> ids = new ArrayList<String>();
		String ptype = pb.getPtype();
		if (ptype.contains("1")) {
			String userId = getUserId(order);
			ids.add(userId);
		}
		if (ptype.contains("4")) {
			ids.add(getMobileEquipmentNo(order));
		}
		return ids;
	}

	public static List<String> allids23(PromForbidBuy pb,
										List<OrdPerson> personTraller, OrdOrder order) {
		ArrayList<String> ids = new ArrayList<String>();
		String ptype = pb.getPtype();
		if (ptype.contains("2")) {
			List<String> phoneslist = getphonesIds(order, personTraller);
			ids.addAll(phoneslist);
		}
		if (ptype.contains("3")) {
			List<String> idtypeAndidlist = getIdtypeAndIds(order, personTraller);
			ids.addAll(idtypeAndidlist);
		}
		return ids;
	}

	public static List<String> allids2(PromForbidBuy pb,
									   List<OrdPerson> personTraller, OrdOrder order) {
		ArrayList<String> ids = new ArrayList<String>();
		String ptype = pb.getPtype();
		if (ptype.contains("2")) {
			List<String> phoneslist = getphonesIds(order, personTraller);
			ids.addAll(phoneslist);
		}
		return ids;
	}

	public static List<String> allids3(PromForbidBuy pb,
									   List<OrdPerson> personTraller, OrdOrder order) {
		ArrayList<String> ids = new ArrayList<String>();
		String ptype = pb.getPtype();
		if (ptype.contains("3")) {
			List<String> idtypeAndidlist = getIdtypeAndIds(order, personTraller);
			ids.addAll(idtypeAndidlist);
		}
		return ids;
	}

	public static String exceptPercent(String input) {
		// 专门针对限购redis里面的数量 所以直接设置成0
		if (input == null || ("\"\"").equals(input)) {
			return "0";
		}
		Pattern pattern = Pattern.compile("^\".*\"$");
		Matcher matcher = pattern.matcher(input);
		if (matcher.matches()) {
			input = input.substring(1, input.length() - 1);
		}
		return input;
	}

	public static void findRedisAndMinusQuantity(
			Map<String, Integer> serchekeyMap, JedisTemplate2 jedisTemplate2) {
		try {
			if (serchekeyMap != null && serchekeyMap.isEmpty()==false) {
				for (Map.Entry<String, Integer> serchekey : serchekeyMap.entrySet()) {
					// 判断存在与否。
					String key = serchekey.getKey();
					boolean exists = jedisTemplate2.exists(key);
					// 如果存在则做减法。 先拿到redis里面的value 在拿到map里面的value
					// 然后把redis里面的value
					// 减去map里面的value
					LOG.info("exitsts status is=" +exists);
					if (exists) {
						Integer needTomin = serchekey.getValue();

						Integer quantity= Integer.valueOf(exceptPercent(jedisTemplate2.get(key)));

						Integer nowquantity = quantity - needTomin;
						LOG.info("REDIS CANLE KEY AND QUANTITY IS"
								+ serchekey.getKey() + "--" + nowquantity);
						if (nowquantity <= 0) {
							nowquantity = 0;
							jedisTemplate2.set(serchekey.getKey(),
									String.valueOf(nowquantity),60*60*24);
						}else{
							jedisTemplate2.set(serchekey.getKey(),
									String.valueOf(nowquantity),60*60*24*30*12);
						}
					}
					// 不存在则啥也不干
				}
			}
		} catch (Exception e) {
			LOG.info("REDIS EXCEPTION=="+ ExceptionUtils.getFullStackTrace(e));
		}
	}


}
