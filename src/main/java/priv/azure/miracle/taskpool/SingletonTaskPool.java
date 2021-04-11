package priv.azure.miracle.taskpool;

import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import priv.azure.miracle.data.generator.pojo.Constants;

@Component
public class SingletonTaskPool {
	private ThreadPoolExecutor threadPoolExecutor;
	
	public SingletonTaskPool() {
		threadPoolExecutor = new ThreadPoolExecutor(
				Constants.SINGLETON_TASK_POOL_SIZE,
				Integer.MAX_VALUE,
				60L,
				TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>()
				);
	}
	
	public boolean isShutdown() {
		return this.threadPoolExecutor.isShutdown();
	}
	public Future<?> submit(Runnable runnable){
		return threadPoolExecutor.submit(runnable);
	}
	public void shutdown(){
		threadPoolExecutor.shutdown();
	}
}
