/**
 * 
 */
package com.lvmama.vst.order.service.book;

import java.util.Comparator;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.lvmama.vst.back.order.po.OrdOrderItem;

/**
 * @author lancey
 *
 */
@Component
public class CategoryComparator implements Comparator<OrdOrderItem> {
	
	@Resource(name="category_sort_list")
	private List<Long> categorySortList;

	@Override
	public int compare(OrdOrderItem o1, OrdOrderItem o2) {
		int a=categorySortList.indexOf(o1.getCategoryId());
		int b=categorySortList.indexOf(o2.getCategoryId());
		if(a==-1){
			a=1000;
		}
		if(b==-1){
			b=1000;
		}
		return a-b;
	}
}
