package com.lvmama.vst.order.service.flight.info;

public class PlaneTypeInfo {

	/**
	 * ID
	 */
	private Long id;
	
	/**
	 * 制造商
	 */
	private String manufacturer;
	
	/**
	 * 编号
	 */
	private String code;
	
	/**
	 * 名称
	 */
	private String name;
	
	/**
	 * 类型描述
	 */
	private String typeDesp;
	
	/**
	 * 最少座位数
	 */
	private Long minSeats;
	
	/**
	 * 最大座位数
	 */
	private Long maxSeats;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
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

	public String getTypeDesp() {
		return typeDesp;
	}

	public void setTypeDesp(String typeDesp) {
		this.typeDesp = typeDesp;
	}

	public Long getMinSeats() {
		return minSeats;
	}

	public void setMinSeats(Long minSeats) {
		this.minSeats = minSeats;
	}

	public Long getMaxSeats() {
		return maxSeats;
	}

	public void setMaxSeats(Long maxSeats) {
		this.maxSeats = maxSeats;
	}
	
	
}
