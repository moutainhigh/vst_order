/**
 * 
 */
package com.lvmama.vst.order.processer;

import java.util.List;

import com.lvmama.vst.comm.jms.Message;

/**
 * @author lancey
 *
 */
public interface IWorkflowProcesser {

	void handle(final Message message);
}
