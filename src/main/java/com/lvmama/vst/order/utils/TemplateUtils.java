/**
 * 
 */
package com.lvmama.vst.order.utils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author lancey
 *
 */
public  class TemplateUtils {

	public static String replace(String content,Map<String,Object> params){
		Pattern pattern = Pattern.compile("\\$\\{(.*?)\\}");
		Matcher m = pattern.matcher(content);
		StringBuffer sb = new StringBuffer();
		while(m.find()){
			String key = m.group(1);
			String value = String.valueOf(params.get(key));
			m.appendReplacement(sb, value);
		}
		m.appendTail(sb);
		return sb.toString();
	}
	
	public static void main(String[] args){
		String str="aa${aa},${ef}";
		Map<String,Object> obj = new java.util.HashMap<String,Object>();
		obj.put("aa", "efeeefe");
		obj.put("ef", "rrrrr");
		System.out.println(replace(str,obj));
		
	}
	
}
