/**
 * 
 */
package com.lvmama.vst.order.utils;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author lancey1
 *
 */
public abstract class EnumUtilsEx {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T extends Enum> T getEnum(Class<T> clazz,String enumName){
		if(StringUtils.isEmpty(enumName)){
			return null;
		}
		return (T)EnumUtils.getEnum(clazz, enumName);
	}
}
