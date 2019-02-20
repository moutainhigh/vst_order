package com.lvmama.vst.order.web.route;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.lvmama.config.common.ZooKeeperConfigProperties;
import com.lvmama.vst.order.route.constant.VstRouteConstants;


/**
 * 订单路由配置检测
 * @author xiaoyulin
 *
 */
public class OrderRouteScaleServlet extends HttpServlet {

	private static final Logger logger = LoggerFactory.getLogger(OrderRouteScaleServlet.class);
	
	private static final long serialVersionUID = -6403807204853890548L;

	@Override
	public void init() throws ServletException {
		logger.info("OrderRouteScaleServlet#init....");
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		this.doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		logger.info("OrderRouteScaleServlet#doPost...");
		String propertiesName = req.getAttribute("name") == null ? "ALL" : req.getAttribute("name").toString();
		logger.info("OrderRouteScaleServlet#doPost propertiesName=" + propertiesName);
		Map<String, Object> result = new HashMap<String, Object>();
		PrintWriter out = resp.getWriter();
		try {
			//TODO:
			if("ALL".equals(propertiesName)){
				result.put(VstRouteConstants.TOTAL_SWITCH, ZooKeeperConfigProperties.getProperties(VstRouteConstants.TOTAL_SWITCH));
				result.put(VstRouteConstants.EMPTY_PARAM_ROUTE_CONFIG, ZooKeeperConfigProperties.getProperties(VstRouteConstants.EMPTY_PARAM_ROUTE_CONFIG));
				result.put(VstRouteConstants.KEY_JOB_SWITCH, ZooKeeperConfigProperties.getProperties(VstRouteConstants.KEY_JOB_SWITCH));
				result.put(VstRouteConstants.CATEGORIES_CONFIG_KEY, ZooKeeperConfigProperties.getProperties(VstRouteConstants.CATEGORIES_CONFIG_KEY));
				result.put(VstRouteConstants.MODULO_CONFIG_PREFIX + 1, ZooKeeperConfigProperties.getProperties(VstRouteConstants.MODULO_CONFIG_PREFIX + 1));
				result.put(VstRouteConstants.MODULO_CONFIG_PREFIX + 11, ZooKeeperConfigProperties.getProperties(VstRouteConstants.MODULO_CONFIG_PREFIX + 11));
				result.put(VstRouteConstants.MODULO_CONFIG_PREFIX + 12, ZooKeeperConfigProperties.getProperties(VstRouteConstants.MODULO_CONFIG_PREFIX + 12));
				result.put(VstRouteConstants.MODULO_CONFIG_PREFIX + 13, ZooKeeperConfigProperties.getProperties(VstRouteConstants.MODULO_CONFIG_PREFIX + 13));
			}else{
				result.put(propertiesName, ZooKeeperConfigProperties.getProperties(propertiesName));
			}
			out.write(JSON.toJSONString(result));
		} catch (Exception e) {
			e.printStackTrace();
			out.write("\n" + e.getMessage() + "\n");
			out.write(printStackTraceMsg(e.getStackTrace()));
		} finally {
			out.flush();
			if(out != null){
				try {
					out.close();
					logger.info("out closed!");
				} catch (Exception e2) {
					out = null;
					e2.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * @Title: printStackTraceMsg 
	 * @Description: 高仿异常信息格式
	 * @param @param stackElements
	 * @param @return    设定文件 
	 * @return String    返回类型 
	 * @throws 
	 * @author ltwangwei   
	 * @date 2016-11-15 上午10:24:14
	 */
	private String printStackTraceMsg(StackTraceElement[] stackElements){
		if(stackElements != null && stackElements.length > 0){
			StringBuffer msg = new StringBuffer();
			for (int i = 0; i < stackElements.length; i++) {
				msg.append(stackElements[i].getClassName());
				msg.append("."); 
				msg.append(stackElements[i].getMethodName());
				msg.append("(");
				msg.append(stackElements[i].getFileName());
				msg.append(":");
				msg.append(stackElements[i].getLineNumber());
				msg.append(")\n");
			}
			return msg.toString();
		}
		return "";
	}
	
}
