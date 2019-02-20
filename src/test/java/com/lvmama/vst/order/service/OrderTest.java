package com.lvmama.vst.order.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OrderTest implements Runnable{
	
	private Map<String,Integer> map;
	
	void init(){
		map = new LinkedHashMap<String, Integer>();
		for(int i=0;i<200;i++){
			map.put("key:"+i, i);
		}
	}

	public static void main(String[] args) {
		List<Thread> list = new ArrayList<Thread>();
		OrderTest ot = new OrderTest();
		ot.init();
		for(int i = 0;i<3;i++){
			list.add(new Thread(ot));
		}
		
		for(Thread t:list){
			t.start();
		}
	}
	
	List<String> getAll(){
		List<String> list = new ArrayList<String>();
		list.addAll(map.keySet());
		return list;
	}

	@Override
	public void run() {
		List<String> list= getAll();
		System.out.println(list);
	}
}
