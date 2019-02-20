/**
 * 
 */
package com.lvmama.vst.logback;

import ch.qos.logback.core.filter.EvaluatorFilter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * @author chenlizhao
 *
 */
public class OrderEvalFilter extends EvaluatorFilter {

//	private static Log log = LogFactory.getLog(OrderEvalFilter.class);
	
	public FilterReply decide(Object event) {
		FilterReply reply = super.decide(event);
		//当判断返回NEUTRAL时，返回onMismatch
		if(FilterReply.NEUTRAL.equals(reply)) {
			reply = this.onMismatch;
		}
		return reply;
	}
}
