package de.mannheim.ids.executor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * BlockingThreadPoolExecutor uses a semaphore to limit the number of task
 * running concurrently and thus also limiting its task queue.
 * 
 * @author margaretha
 * 
 */
public class BlockingThreadPoolExecutor extends ThreadPoolExecutor {

	private final Semaphore semaphore;
	private final List<Callable<?>> runningTasks = new ArrayList<>();

	public BlockingThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
			long keepAliveTime, TimeUnit unit, int queueLength) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit,
				new ArrayBlockingQueue<Runnable>(queueLength));
		this.semaphore = new Semaphore(Math.max(1, queueLength - 1));
	}

	@Override
	protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T result) {
		waitForTaskPermit();
		return new BlockingFutureTask<T>(Executors.callable(runnable, result));
	}

	@Override
	protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
		waitForTaskPermit();
		return new BlockingFutureTask<T>(callable);
	}

	private void waitForTaskPermit() {
		try {
			semaphore.acquire();
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	protected final class BlockingFutureTask<T> extends FutureTask<T> {
		private final Callable<T> callable;

		private BlockingFutureTask(Callable<T> callable) {
			super(callable);
			this.callable = callable;
		}

		@Override
		public void run() {
			synchronized (runningTasks) {
				runningTasks.add(this.callable);
			}
			semaphore.release();
			try {
				super.run();
			}
			finally {
				synchronized (runningTasks) {
					runningTasks.remove(this.callable);
				}
			}
		}
	}
}
