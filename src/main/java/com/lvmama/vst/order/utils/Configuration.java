package com.lvmama.vst.order.utils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import com.lvmama.config.common.ZooKeeperConfigProperties;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 反射加载配置
 * @author xuxueli
 */
public class Configuration {	
	private static transient Logger logger = LoggerFactory.getLogger(Configuration.class);
	static {
		init();
	}
	/** Configuration初始化 **/
	public static void init() {
		
		String prop = "configuration.properties";
		Properties properties = new Properties();
		try {
			InputStream ins = Configuration.class.getClassLoader().getResourceAsStream("/" + prop);
			properties.load(ins);
		} catch (Throwable e) {
			logger.warn("Cannot load configuration.properties file,Check to see if the file has been moved in sweet!");
		}
		try {
			logger.info("===================Configuration init start==========================");
			Field[] allFields = Configuration.class.getDeclaredFields();
			for (Field field : allFields) {
				if (Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers())) {
					field.setAccessible(true);
					
					Class<?> clazz = field.getType();
					String value = getValue(field.getName(),properties);
					if (clazz == String.class) {
						field.set(Configuration.class, value);
					} else if (clazz == Integer.TYPE){
						field.set(Configuration.class, Integer.parseInt(value));
					} else if (clazz == Long.TYPE) {
						field.set(Configuration.class, Long.parseLong(value));
					} else if (clazz == Date.class) {
						SimpleDateFormat bartDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						Date date = bartDateFormat.parse(value);
						field.set(Configuration.class, date );
					}
					
					
					logger.info("[Configuration init filed: {} = {}]", field.getName(), field.get(Configuration.class));
				}			
			}
			logger.info("===================Configuration init end==========================");
			
		} catch (ParseException e) {
			logger.error("[vst_order load {} ParseException...]", prop, e);
		} catch (IllegalArgumentException e) {
			logger.error("[vst_order load {} IllegalArgumentException...]", prop, e);
		} catch (IllegalAccessException e) {
			logger.error("[vst_order load {} IllegalAccessException...]", prop, e);
		}
		
	}

	public static String getValue(String key,Properties properties){
		if(StringUtils.isNotBlank(ZooKeeperConfigProperties.getProperties(key))){
			return ZooKeeperConfigProperties.getProperties(key);
		}
		String value = "";
		if(properties != null){
			value = properties.getProperty(key);
		}

		return value;
	}

	// 分销渠道：仅淘宝
	public static String distribution_taobao;
	// 分销渠道：不含淘宝、旅途、wap、微信、秒杀、团购
	public static String distribution_other;
	// 分销渠道：其他.主站(1997微信小站)
	public static String distribution_neither;
	
}
