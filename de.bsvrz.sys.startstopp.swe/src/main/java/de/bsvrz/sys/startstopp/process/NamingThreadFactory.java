package de.bsvrz.sys.startstopp.process;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class NamingThreadFactory implements ThreadFactory {

	private String name;

	public NamingThreadFactory(String name) {
		this.name = name;
	}

	@Override
	public Thread newThread(Runnable r) {
		Thread thread = Executors.defaultThreadFactory().newThread(r);
		if( thread != null) {
			thread.setName(name + "_" + thread.getId());
		}
		return thread;
	}

}
