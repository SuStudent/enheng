package cn.susudad.enheng.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * ExecutorService description
 * <p>
 *
 * @author yiyi.su
 * @version 1.0.0
 * @date 2023-08-09 15:38:33
 */
@Slf4j
public class ExecutorService {

	private ThreadPoolExecutor executor = null;

	public static ExecutorService getInstance() {
		return ExecutorServiceHolder.SERVICE;
	}

	private static class ExecutorServiceHolder {
		private static final ExecutorService SERVICE = new ExecutorService(100000);
	}

	private ExecutorService(int queueSize) {
		this.executor = new ThreadPoolExecutor(
				Runtime.getRuntime().availableProcessors(),
				Runtime.getRuntime().availableProcessors() * 2,
				60L,
				TimeUnit.SECONDS,
				new LinkedBlockingQueue<>(queueSize),
				new CustomizableThreadFactory("executorService-"),
				new BlockRejectedExecutionHandler()
		);
	}

	public void shutdown(){
		if(executor.isShutdown()) {
			return;
		}
		log.info("shutdown ExecutorService executor");
		executor.shutdown();
		try {
			int i = 1;
			while (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
				log.info("ExecutorService 等待关闭。{}", i++);
			}
		} catch (InterruptedException e) {
			log.info("线程中断。", e);
			Thread.currentThread().interrupt();
		}
		log.info("ExecutorService executor 关闭");
	}

	public void execute(Runnable runnable) {
		try {
			executor.execute(runnable);
		} catch (Exception e) {
			log.warn("ExecutorService execute exception.", e);
		}
	}

	public Future<?> submit(Runnable runnable) {
		return executor.submit(runnable);
	}
}
