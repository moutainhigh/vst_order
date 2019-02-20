package com.lvmama.vst.order.service.flight.info;

public class AirLineInfo {

	/**
	 * ID
	 */
	private Long id;
	
	/**
	 * 编码
	 */
	private String code;
	
	/**
	 * 名称
	 */
	private String name;
	
	/**
	 * 国家
	 */
	private String country;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}
	
}
