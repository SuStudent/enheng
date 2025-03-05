package cn.susudad.enheng.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * BlockRejectedExecutionHandler description
 *
 * @author suyiyi@boke.com
 * @version 1.0.0
 * @date 2024-10-22 19:40:05
 */
@Slf4j
public class BlockRejectedExecutionHandler implements RejectedExecutionHandler {
	@Override
	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
		try {
			log.warn("rejected task:{}", executor.getQueue().size());
			if (executor.isShutdown()) {
				log.info("caller thread run");
				r.run();
			} else {
				log.info("queue block put");
				executor.getQueue().put(r);
			}
		} catch (InterruptedException e) {
			log.error("rejected error", e);
		}
	}
}
