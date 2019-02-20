/**
 * 
 */
package com.lvmama.vst.order.web.backorder;

import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.lvmama.comm.search.vst.vo.AutoIndexBean;
import com.lvmama.vst.comm.utils.json.JSONOutput;
import com.lvmama.vst.search.client.service.AutoCompleteNewService;

/**
 * @author lancey
 *
 */
@Controller
public class AutoCompleteAction {

	@Autowired
	private AutoCompleteNewService autoCompleteNewService;
	
	@RequestMapping("/ord/productQuery/searchComplete.do")
	public void searchComplete(HttpServletResponse res,String searchType,String search){
		JSONArray array = new JSONArray();
		Set<AutoIndexBean> set = autoCompleteNewService.getAutoCompleteInfo(search, searchType, 20);
		if(CollectionUtils.isNotEmpty(set)){
			for(AutoIndexBean bean:set){
				JSONObject obj = new JSONObject();
				obj.put("text", bean.getSearchValue());
				obj.put("id", bean.getSearchValue());
				array.add(obj);
			}
		}
		JSONOutput.writeJSON(res, array);
	}
}
