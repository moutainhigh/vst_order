package com.lvmama.vst.back.order;

import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath*:applicationContext-vst-order-beans.xml"})
public abstract class OrderTestBase {
	protected ApplicationContext applicationContext = null;
	protected void prepare() {
		String[] contextPaths = {"applicationContext-vst-order-beans.xml"};
		applicationContext = new ClassPathXmlApplicationContext(contextPaths);
	}
}
