package com.lvmama.vst.order.job.workflow;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PayWorkflowThreadPoolExcutor {
	private static ExecutorService instance;

	private PayWorkflowThreadPoolExcutor() {

	}

	private static ExecutorService getInstance() {
		if (null == instance || instance.isShutdown()) {
			instance = Executors.newFixedThreadPool(10);
		}
		return instance;
	}

	public static void execute(Thread... tasks) {
		for (Thread task : tasks) {
			getInstance().execute(task);
		}
	}
}
