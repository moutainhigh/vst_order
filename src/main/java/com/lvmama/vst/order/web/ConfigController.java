package com.lvmama.vst.order.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.lvmama.config.common.Constant;
import com.lvmama.config.common.ZooKeeperConfigProperties;
import com.lvmama.config.factory.ZooKeeperClientFactory;
import com.lvmama.config.watcher.ZooKeeperCuratorWatcher;
import com.lvmama.vst.comm.utils.DESCoder;
import com.lvmama.vst.comm.utils.DateUtil;

/**
 * 新增修改sweet配置文件参数
 * @author Administrator
 *
 */
@Controller
public class ConfigController {
	
	private static final Logger log = LoggerFactory.getLogger(ConfigController.class);

	@RequestMapping(method = RequestMethod.POST, value = { "/config/saveOrUpdate" })
	@ResponseBody
	public Object saveOrUpdate(String file, String key, String value, HttpServletRequest req) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("code", 200);
		if (!checkUrlValid(req.getParameter("code"))) {
			result.put("code", 500);
			result.put("msg", "亲，你没有权限!");
		}
		if (StringUtils.isEmpty(file) || StringUtils.isEmpty(key)
				|| StringUtils.isEmpty(value)) {
			result.put("code", 500);
			result.put("msg", "必填参数不能为空!");
		}
		log.info("file:" + file + ",key:" + key + ",value:" + value);
		CuratorFramework curator = ZooKeeperClientFactory.getInstance(
				Constant.zookeeperConfigAddress, Constant.NAME_SPACE);
		String basePath = Constant.SEPARATOR + Constant.PROPERTIES
				+ Constant.SEPARATOR + Constant.zookeeperConfigOwner
				+ Constant.SEPARATOR + Constant.zookeeperConfigApplicationName
				+ Constant.SEPARATOR;
		String completePath = basePath + file;
		try {
			if (curator.checkExists().forPath(completePath) == null) {
				curator.create().forPath(completePath);
			}
		} catch (Exception e1) {
			result.put("code", 500);
			result.put("msg", "修改失败,message:" + e1.getMessage());
		}

		log.info("completePath:" + completePath);
		String popPath = completePath + Constant.SEPARATOR + key;
		CuratorWatcher curatorWatcher = new ZooKeeperCuratorWatcher();
		try {
			List<String> propPathList = curator.getChildren().forPath(
					completePath);
			if (propPathList != null && propPathList.contains(key)) {// 参数是否存在
				// 修改
				curator.setData().forPath(popPath, value.getBytes());
			} else {
				// 新增
				curator.create().creatingParentsIfNeeded()
						.forPath(popPath, value.getBytes());
			}
			byte[] data = curator.getData().usingWatcher(curatorWatcher)
					.forPath(popPath);

			log.info("key:" + key + ",value:"
					+ new String(data, Constant.UTF_8));
			result.put("msg", "修改成功, " + key + ":" + ZooKeeperConfigProperties.getProperties(key));
		} catch (Exception e) {
			log.error("修改失败！key:{},value:{},message:{}", key, value, e);
			result.put("code", 500);
			result.put("msg", "修改失败,message:" + e.getMessage());
		}
		return result;
	}

	private boolean checkUrlValid(String code) {
		if (code == null) {
			return false;
		}
		try {
			code = DESCoder.decrypt(code);
			String today = DateUtil.formatSimpleDate(DateUtil.getTodayDate());
			if (today.equalsIgnoreCase(code)) {
				return true;
			}
		} catch (Exception e) {
			log.error("checkUrlValid 异常信息{}", e);
		}
		return false;
	}

}
