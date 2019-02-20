package com.lvmama.vst.elasticsearch.converter;

import com.lvmama.vst.elasticsearch.params.ESParams;

public interface ParameterConverter<T> {
	ESParams convert(T value);
}
