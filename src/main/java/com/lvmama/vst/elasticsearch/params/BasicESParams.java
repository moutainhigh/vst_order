package com.lvmama.vst.elasticsearch.params;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.elasticsearch.common.lang3.StringUtils;

public class BasicESParams implements ESParams {

	private final HashMap<String, String> parameterTypes = new HashMap<String, String>();
	private final HashMap<String, Object> parameters = new HashMap<String, Object>();

	@Override
	public Object getParameter(String name) {
		return this.parameters.get(name);
	}

	@Override
	public String getStringParameter(String name) {
		return (String) this.parameters.get(name);
	}

	@Override
	public ESParams setParameter(String type, String name, Object value) {
		this.parameters.put(name, value);
		this.parameterTypes.put(name, ParamType.getName(type));
		return this;
	}

	@Override
	public boolean removeParameter(String name) {
		if (this.parameters.containsKey(name)) {
			this.parameters.remove(name);
			this.parameterTypes.remove(name);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public ESParams setLongParameter(String name, long value) {
		setParameter(ParamType.LONG.name(), name, new Long(value));
		parameterTypes.put(name, ParamType.LONG.name());
		return this;
	}

	@Override
	public long getLongParameter(String name, long defaultValue) {
		Object param = getParameter(name);
		if (param == null) {
			return defaultValue;
		}
		return ((Long) param).longValue();
	}

	@Override
	public Set<String> getNames() {
		return new HashSet<String>(this.parameters.keySet());
	}

	public boolean isEmpty() {
		Set<String> names = getNames();
		for (String key : names) {
			Object value = this.parameters.get(key);
			if (value != null && StringUtils.isNotBlank(value.toString())) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String getParameterType(String name) {
		return this.parameterTypes.get(name);
	}

	@Override
	public ParamType getEnumParameterType(String name) {
		String paramType = parameterTypes.get(name);
		ParamType paramTypeEnum = ParamType.getEnumByName(paramType);
		return paramTypeEnum;
	}
}
