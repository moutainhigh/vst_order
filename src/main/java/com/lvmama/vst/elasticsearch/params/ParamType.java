package com.lvmama.vst.elasticsearch.params;

public enum ParamType {
	STRING("java.lang.String"), LONG("java.lang.Long"), DATE("java.util.Date"), CHARACTER("java.lang.Character"), LIST(
			"java.util.List");

	private String name;

	private ParamType(String name) {
		this.name = name;
	}

	public static String getName(String name) {
		for (ParamType c : ParamType.values()) {
			if (c.getName().equalsIgnoreCase(name)) {
				return c.name;
			}
		}
		return null;
	}

	public static ParamType getEnumByName(String name) {
		for (ParamType c : ParamType.values()) {
			if (c.getName().equalsIgnoreCase(name)) {
				return c;
			}
		}
		return STRING;
	}

	// get set 方法
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
