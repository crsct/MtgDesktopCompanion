package org.magic.services;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.utils.patterns.observer.Observable;
import org.utils.patterns.observer.Observer;

public class MTGAppender extends AppenderSkeleton {

	List<LoggingEvent> events;
	MyObservable obs;

	
	public List<LoggingEvent> getEvents() {
		return events;
	}
	
		
	public Observable getObservable()
	{
		return obs;
	}
	
	public MTGAppender() {
		events=new ArrayList<>();
		obs=new MyObservable();
	}
	
	public void addObserver(Observer viewer)
	{
		obs.addObserver(viewer);
	}
	
	

	@Override
	public boolean requiresLayout() {
		return false;
	}

	@Override
	protected void append(LoggingEvent event) {
		events.add(event);
		obs.setChanged();
		try{
			obs.notifyObservers(event.getMessage());
		}catch(Exception e)
		{
			MTGLogger.printStackTrace(e);
		}
	}


	@Override
	public void close() {
		//Do nothing because no stream
		
	}
}

class MyObservable extends Observable
{
	
	@Override
	public synchronized void setChanged() {
		super.setChanged();
	}
	
}

