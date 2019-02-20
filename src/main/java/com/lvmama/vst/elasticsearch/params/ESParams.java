package com.lvmama.vst.elasticsearch.params;

import java.util.Set;

public interface ESParams {
	Object getParameter(String name);

	String getStringParameter(String name);

	ESParams setParameter(String type, String name, Object value);

	boolean removeParameter(String name);

	ESParams setLongParameter(String name, long value);

	long getLongParameter(String name, long defaultValue);

	Set<String> getNames();

	boolean isEmpty();

	String getParameterType(String name);

	ParamType getEnumParameterType(String name);
}
