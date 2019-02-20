package com.lvmama.vst.order.utils;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.lvmama.vst.comm.vo.order.BuyInfo;

public class MemoryTestObject {

	
	public static void saveObjcet(Object objcet,String objectName){
		
		try {
			ObjectOutputStream outs=new ObjectOutputStream(new FileOutputStream("D:\\test\\"+objectName+".txt"));
			outs.writeObject(objcet);	
			
			ByteArrayOutputStream byteArrayOut=new ByteArrayOutputStream();
			ObjectOutputStream outs1=new ObjectOutputStream(byteArrayOut);
			outs1.writeObject(objcet);
			byte[] byteArr=byteArrayOut.toByteArray();
			System.out.println(byteArr.length);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public static Object getObject(String objectName){
		Object object=null;
		try {
			ObjectInputStream ins=new ObjectInputStream(new FileInputStream("D:\\test\\"+objectName+".txt"));
			object=ins.readObject();			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 		
		return object;
	}
	
	public static void main(String[] args){
		MemoryTestObject test=new MemoryTestObject();
		BuyInfo buyInfo=new BuyInfo();
		buyInfo.setProductId(10000L);
		test.saveObjcet(buyInfo,"BuyInfo");
		Object obj=test.getObject(BuyInfo.class.getSimpleName());
		System.out.println(((BuyInfo)obj).getProductId());
	}
}
