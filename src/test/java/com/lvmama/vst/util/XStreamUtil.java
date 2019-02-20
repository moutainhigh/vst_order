package com.lvmama.vst.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.lvmama.vst.flight.client.order.vo.FlightOrderBookingRequestVO;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class XStreamUtil {

	/**
	 * Bean 2 XML
	 * 
	 * @param obj
	 * @return
	 */
	public static String bean2xml(Object obj) {
		XStream xs = new XStream();
		// xs.alias("root", BaseBean.class); // 类，指定别名
		// xs.aliasField("list", BaseBean.class, "userList"); // 类属性，指定别名
		return xs.toXML(obj);
	}

	/**
	 * XML 2 BEAN
	 * 
	 * @param xmlStr
	 * @return
	 */
	public static Object xml2bean(String xmlStr) {
		XStream xs = new XStream(new DomDriver());
		return xs.fromXML(xmlStr);
	}
	
	public static void main33(String[] args) {
		
		FlightOrderBookingRequestVO flightOrderBookingRequestVO = new FlightOrderBookingRequestVO();
		
		String xml = bean2xml(flightOrderBookingRequestVO);
		System.out.println("javabean转成xml为:\n" + xml);
		
		
	}
	
	public static void main(String[] args) {
		// javabean 转 xml
		List<User> users = new ArrayList<User>();
		users.add(new User("123", "爱边程", "23"));
		users.add(new User("456", "刘大拿", "24"));
		BaseBean base = new BaseBean();
		base.setUserList(users);
		String xml = bean2xml(base);
		System.out.println("javabean转成xml为:\n" + xml);

		// xml转javabean
		base = (BaseBean) xml2bean(xml);
		users = base.getUserList();
		System.out.println("xml转成javabean为:");
		for (User u : users) {
			System.out.println("id=" + u.getId() + ",name=" + u.getName()
					+ ",age=" + u.getAge());
		}
	}

	public static void main2(String[] args) {
		// javabean 转 xml
		List<User> users = new ArrayList<User>();
		users.add(new User("123", "爱边程", "23"));
		users.add(new User("456", "刘大拿", "24"));
		BaseBean base = new BaseBean();
		base.setUserList(users);
		XStream xs = new XStream();
		xs.alias("root", BaseBean.class);
		xs.alias("user", User.class);
		xs.aliasField("list", BaseBean.class, "userList");
		String xml = xs.toXML(base);
		System.out.println("javabean转成xml为:\n" + xml);

		// xml转javabean
		XStream xs1 = new XStream(new DomDriver());
		xs1.alias("root", BaseBean.class);
		xs1.alias("user", User.class);
		xs1.aliasField("list", BaseBean.class, "userList");
		base = (BaseBean) xs1.fromXML(xml);
		users = base.getUserList();
		System.out.println("xml转成javabean为:");
		for (User u : users) {
			System.out.println("id=" + u.getId() + ",name=" + u.getName()
					+ ",age=" + u.getAge());
		}
	}

}

class BaseBean {
	private List<User> userList;

	public BaseBean() {
		userList = new ArrayList<User>();
	}

	public List<User> getUserList() {
		return userList;
	}

	public void setUserList(List<User> userList) {
		this.userList = userList;
	}

	public void addUser(User user) {
		userList.add(user);
	}
}

class User implements Serializable {

	private String id;
	private String name;
	private String age;

	public User() {

	}

	public User(String id, String name, String age) {
		this.id = id;
		this.name = name;
		this.age = age;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAge() {
		return age;
	}

	public void setAge(String age) {
		this.age = age;
	}

}