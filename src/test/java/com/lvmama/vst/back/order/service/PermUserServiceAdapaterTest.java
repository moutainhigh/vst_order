package com.lvmama.vst.back.order.service;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.lvmama.vst.back.order.OrderTestBase;
import com.lvmama.vst.pet.adapter.PermUserServiceAdapter;

/**
 * 单元测试
 * 
 * @author wenzhengtao
 * 
 */
public class PermUserServiceAdapaterTest extends OrderTestBase{
	//用户权限业务测试
	private PermUserServiceAdapter permUserServiceAdapter = null;

	@Before
	public void prepare() {
		super.prepare();
		if(null != applicationContext){
			permUserServiceAdapter = (PermUserServiceAdapter) applicationContext.getBean(PermUserServiceAdapter.class);
		}
	}

//	@Test
//	public void testIsExistsUser() {
//	}
//
//	@Test
//	public void testQueryPermUserByParam() {
//	}
//
//	@Test
//	public void testQueryPermUserByParamCount() {
//	}
//
//	@Test
//	public void testFindUserByDepId() {
//	}
//
//	@Test
//	public void testGetPermUserByUserName() {
//	}

	@Test
	public void testFindUserPermission() {
		try {
			List<Long> permissionIds = new ArrayList<Long>();
			permissionIds.add(1428L);
			permissionIds.add(1429L);
			permissionIds.add(1430L);
			permissionIds.add(1431L);
			permissionIds.add(1432L);

			String userName = "lv1007";

			List<Long> realPermissionIds = permUserServiceAdapter.findUserPermission(permissionIds, userName);
			//[1428, 1429, 1432, 1431, 1430]
			System.out.println(realPermissionIds);

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("服务器内部异常！");
		}
	}

}
