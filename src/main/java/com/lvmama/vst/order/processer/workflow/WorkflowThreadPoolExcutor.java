package com.lvmama.vst.order.processer.workflow;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WorkflowThreadPoolExcutor {
	private static ExecutorService instance;

	private WorkflowThreadPoolExcutor() {

	}

	private static ExecutorService getInstance() {
		if (null == instance || instance.isShutdown()) {
			instance = Executors.newFixedThreadPool(8);
		}
		return instance;
	}

	public static void execute(Thread... tasks) {
		for (Thread task : tasks) {
			getInstance().execute(task);
		}
	}
}
