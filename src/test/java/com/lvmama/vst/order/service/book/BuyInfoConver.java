/**
 * 
 */
package com.lvmama.vst.order.service.book;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.enums.EnumUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.util.ReflectionUtils;

import com.lvmama.vst.comm.utils.DateUtil;
import com.lvmama.vst.comm.vo.order.BuyInfo;

/**
 * @author lancey
 *
 */
public class BuyInfoConver {

	private BuyInfo buyInfo;
	
	public BuyInfo getBuyInfo() {
		return buyInfo;
	}
	private String path = "e:/aabb.txt";
	private Scanner scanner;
	
	public void init(){
		buyInfo = new BuyInfo();
		try {
			scanner = new Scanner(new File(path));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void parse(){
		String key=null;
		String oldkey=null;
		BuyInfo.Item item = null; 
		List<BuyInfo.Item> itemList = null;
		BuyInfo.Product product = null;
		Long productId=null;
		while(scanner.hasNextLine()){
			String line = scanner.nextLine();
			String[] kv = line.split(":");
			if(kv[0].contains(".")){
				if(kv[0].startsWith("productMap")){
					int pos=kv[0].indexOf(".")+1;
					String productKey = kv[0].substring(0,pos-1);
					Matcher m =pattern.matcher(productKey);
					if(m.find()){
						Long number = NumberUtils.toLong(m.group(1));
						if(productId==null||!productId.equals(number)){
							productId = number;
							product = new BuyInfo.Product();
							itemList = new ArrayList<BuyInfo.Item>();
							buyInfo.getProductMap().put(productId,product);
							product.setItemList(itemList);
						}
					}
					int pos2 = kv[0].indexOf(".",pos);
					key =kv[0].substring(pos,pos2);
					if(oldkey==null||!oldkey.equals(key)){
						oldkey = key;
						item = new BuyInfo.Item();
						itemList.add(item);
					}
					String prop = kv[0].substring(pos2+1);
					Field field = ReflectionUtils.findField(BuyInfo.Item.class, prop);
					setValue(item,field,kv[1]);
				}else if(kv[0].startsWith("itemMap")){
					int pos = kv[0].indexOf(".")+1;
					String productKey = kv[0].substring(0,pos-1);
					if(oldkey==null||!oldkey.equals(productKey)){
						if(itemList==null){
							itemList = new ArrayList<BuyInfo.Item>();
							buyInfo.setItemList(itemList);
						}
						oldkey = productKey;
						item = new BuyInfo.Item();
						itemList.add(item);
					}
					String prop = kv[0].substring(pos);
					Field field = ReflectionUtils.findField(BuyInfo.Item.class, prop);
					setValue(item,field,kv[1]);
				}
			}else{
				Field field = ReflectionUtils.findField(BuyInfo.class, kv[0]);
				setValue(buyInfo,field,kv[1]);
			}
		}
		
		scanner.close();
	}
	private Pattern pattern = Pattern.compile("\\[(\\d+)\\]");
	private void setValue(Object target,Field f,String value){
		if(f==null){
			return;
		}
		System.out.println(f);
		Class<?> type = f.getType();
		Object obj=null;
		if(type.equals(Long.class)||type.equals(long.class)){
			obj = NumberUtils.toLong(value);
		}else if(type.equals(Integer.class)||type.equals(int.class)){
			obj = NumberUtils.toInt(value);
		}else if(type.equals(String.class)){
			obj = value;
		}else if(type.equals(Date.class)){
			obj = DateUtil.toSimpleDate(value);
		}else if(type.isEnum()){
			Object vv[] =type.getEnumConstants();
			for(Object v:vv){
				if(((Enum)v).name().equals(value)){
					obj = v;
				}
			}
		}
		f.setAccessible(true);
		try {
			f.set(target, obj);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		BuyInfoConver bic = new BuyInfoConver();
		bic.init();
		bic.parse();
		System.out.println(bic.getBuyInfo());
	}
}
