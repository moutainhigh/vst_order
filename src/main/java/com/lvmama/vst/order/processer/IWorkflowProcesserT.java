package com.lvmama.vst.order.processer;

import com.lvmama.vst.comm.jms.Message;

public interface IWorkflowProcesserT<T> {

	void handle(final Message message,T obj);
}
