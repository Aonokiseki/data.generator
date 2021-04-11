package priv.azure.miracle.taskpool;

import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import priv.azure.miracle.data.generator.pojo.Constants;

@Component
@Scope("singleton")
public class RecordsCreatingTaskPool {
	private ThreadPoolExecutor threadPoolExecutor;
	private int currentCoreSize = Constants.DEFAULT_RECORDS_CREATING_THREAD_COUNT;
	private int currentMaxSize = Constants.DEFAULT_RECORDS_CREATING_THREAD_COUNT;
	
	public RecordsCreatingTaskPool() {
		threadPoolExecutor = new ThreadPoolExecutor(
				currentCoreSize, 
				currentMaxSize, 
				0L, 
				TimeUnit.MICROSECONDS, 
				new LinkedBlockingQueue<Runnable>());
	}
	
	public void setPoolSize(int coreSize, int maxSize) {
		if (coreSize > maxSize) {
			int temp = coreSize;
			coreSize = maxSize;
			maxSize = temp;
		}
		int oldCoreSize = threadPoolExecutor.getCorePoolSize();
		this.currentCoreSize = coreSize;
		this.currentMaxSize = maxSize;
		if(maxSize < oldCoreSize) {
			threadPoolExecutor.setCorePoolSize(coreSize);
			threadPoolExecutor.setMaximumPoolSize(maxSize);
			return;
		}
		threadPoolExecutor.setMaximumPoolSize(maxSize);
		threadPoolExecutor.setCorePoolSize(coreSize);
	}
	
	public Future<?> submit(Runnable runnable) {
		return threadPoolExecutor.submit(runnable);
	}
	
	public void shutdown() {
		threadPoolExecutor.shutdown();
	}
	
	public boolean allowSubmitNewTask() {
		if(threadPoolExecutor.getActiveCount() > currentCoreSize || threadPoolExecutor.getQueue().size() > 0)
			return false;
		return true;
	}
}
