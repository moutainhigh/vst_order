//package com.lvmama.vst.flight.client.order.service.impl;
//
//import java.util.Date;
//import java.util.List;
//
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.springframework.stereotype.Service;
//
//import com.lvmama.vst.comm.vo.ResultHandle;
//import com.lvmama.vst.flight.client.order.service.FlightOrderService;
//import com.lvmama.vst.flight.client.order.vo.FlightOrderBookingRequestVO;
//import com.lvmama.vst.flight.client.order.vo.FlightOrderCancelRequestVO;
//import com.lvmama.vst.flight.client.order.vo.FlightOrderPayRequestVO;
//import com.thoughtworks.xstream.XStream;
//import com.thoughtworks.xstream.io.xml.DomDriver;
//
///**
// * 仅作机票测试用，上线前需删除
// * @author wanglvsong
// *
// */
////@Service("flightOrderService")
//public class FlightOrderServiceImpl implements FlightOrderService {
//	private static final Log LOG = LogFactory.getLog(FlightOrderServiceImpl.class);
//	
//	public String bean2xml(Object obj) {
//		XStream xs = new XStream();
//		return xs.toXML(obj);
//	}
//
//	/**
//	 * XML 2 BEAN
//	 * 
//	 * @param xmlStr
//	 * @return
//	 */
//	public Object xml2bean(String xmlStr) {
//		XStream xs = new XStream(new DomDriver());
//		return xs.fromXML(xmlStr);
//	}
//	
//	@Override
//	public ResultHandle bookingOrder(
//			FlightOrderBookingRequestVO flightOrderBookingRequestVO)
//			throws Exception {
//		long time = new Date().getTime();
//		if(flightOrderBookingRequestVO != null) {
//			LOG.info(bean2xml(flightOrderBookingRequestVO));
//		}
//		ResultHandle result = new ResultHandle();
////		if(time % 2 == 0) {
////			result.setMsg("payFlightOrder fail");
////		}
//		return result;
//	}
//
//	@Override
//	public ResultHandle cancelFlightOrder(
//			FlightOrderCancelRequestVO flightOrderCancelRequestVO)
//			throws Exception {
//		long time = new Date().getTime();
//		if(flightOrderCancelRequestVO != null) {
//			LOG.info(bean2xml(flightOrderCancelRequestVO));
//		}
//		ResultHandle result = new ResultHandle();
//		if(time % 2 == 0) {
//			result.setMsg("cancelFlightOrder fail");
//		}
//		return result;
//	}
//
//	@Override
//	public ResultHandle payFlightOrder(
//			List<FlightOrderPayRequestVO> flightOrderPayRequestVOs)
//			throws Exception {
//		long time = new Date().getTime();
//		if(flightOrderPayRequestVOs != null) {
//			LOG.info(bean2xml(flightOrderPayRequestVOs));
//		}
//		ResultHandle result = new ResultHandle();
////		if(time % 2 == 0) {
//			result.setMsg("payFlightOrder fail");
////		}
//		return result;
//	}
//
//}
