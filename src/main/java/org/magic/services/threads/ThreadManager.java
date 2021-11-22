package org.magic.services.threads;

import java.beans.PropertyChangeEvent;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.apache.log4j.Logger;
import org.magic.services.MTGControler;
import org.magic.services.MTGLogger;
import org.magic.services.threads.ThreadInfo.STATE;
import org.magic.services.threads.ThreadInfo.TYPE;
import org.magic.tools.Chrono;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class ThreadManager {

	private static ThreadManager inst;
	private ThreadPoolExecutor executor;
	protected Logger logger = MTGLogger.getLogger(this.getClass());
	private ThreadFactory factory;
	private List<ThreadInfo> tasksMap;
	
	
	public static ThreadManager getInstance() {
		if (inst == null)
			inst = new ThreadManager();

		return inst;
	}

	public void executeThread(MTGRunnable task, String name) {
		
		if(task==null)
		{
			logger.error("task is null for " + name);
			return;
		}
		
		task.getInfo().setName(name);
		
		tasksMap.add(task.getInfo());
		
		executor.execute(task);
	}
	
	public Future<?> submitThread(MTGRunnable task, String name) {
		
		task.getInfo().setName(name);
		tasksMap.add(task.getInfo());
		return submitCallable(Executors.callable(task), name);
	}
	
	public <V> Future<V> submitCallable(Callable<V> task,String name) {
		return executor.submit(task);
	}
	
	
	public void invokeLater(MTGRunnable task, String name) {
		
		task.getInfo().setName(name);
		tasksMap.add(task.getInfo());
		SwingUtilities.invokeLater(task);
	}
	
	public void runInEdt(SwingWorker<?, ?> runnable,String name) {
		
		var info = new ThreadInfo(runnable);
			  info.setName(name);
			  info.setType(TYPE.WORKER);
		tasksMap.add(info);			
		
		runnable.execute();
		var c = new Chrono();
		
		runnable.addPropertyChangeListener((PropertyChangeEvent ev)->{
			if(ev.getNewValue().toString().equals("STARTED"))
			{ 
				info.setStartDate(Instant.now());
				info.setStatus(STATE.STARTED);
				c.start();
			}
			
			if(ev.getNewValue().toString().equals("DONE")) {
				info.setEndDate(Instant.now());
				info.setDuration(c.stopInMillisecond());
				info.setStatus(STATE.FINISHED);
			}
			
			if(ev.getNewValue().toString().equals("CANCELED")) {
				info.setEndDate(Instant.now());
				info.setDuration(c.stopInMillisecond());
				info.setStatus(STATE.CANCELED);
			}
		});
	}
	
	private ThreadManager() {
		
		var tpc = MTGControler.getInstance().getThreadPoolConfig();
		
		tasksMap = new ArrayList<>();
		
		
		factory = new ThreadFactoryBuilder()
						.setNameFormat(tpc.getNameFormat())
						.setDaemon(tpc.isDaemon())
						.build();
		
		switch (tpc.getThreadPool())
		{
			case CACHED:executor = (ThreadPoolExecutor) Executors.newCachedThreadPool(factory);break;
			case FIXED: executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(tpc.getCorePool(),factory);break;
			case SCHEDULE:executor = (ThreadPoolExecutor) Executors.newScheduledThreadPool(tpc.getCorePool(),factory);break;
			case SINGLE : executor = (ThreadPoolExecutor) Executors.newSingleThreadExecutor(factory);break;
			default :  executor = (ThreadPoolExecutor) Executors.newCachedThreadPool(factory);break;
		}
		logger.debug("init ThreadManager config="+tpc);
	}
	
	public void stop()
	{
		executor.shutdown();
	}
	
	public List<ThreadInfo> listTasks()
	{
		return tasksMap;
	}

	public ThreadFactory getFactory() {
		return factory;
	}
	
	public ThreadPoolExecutor getExecutor() {
		return executor;
	}

	public void clean() {
		tasksMap.removeIf(t->t.getStatus()==STATE.FINISHED);
		
	}
	
	
	
}


