package com.box.bc.util;

public class StopWatch {
	
	private long startTimeInMillis = 0L;
	private boolean isRunning;
	private long stopTimeInMillis = 0L;

	public void start(){
		this.startTimeInMillis = System.currentTimeMillis();
		isRunning = true;
	}
	
	public void stop() throws StopWatchNotRunningException{
		if(!isRunning){
			throw new StopWatchNotRunningException("The Stop Watch is not running, cannot stop");
		}
		
		this.stopTimeInMillis  = System.currentTimeMillis();
		isRunning=false;
	}
	
	public long getElapsedTime() throws StopWatchNeverStartedException{
		if(!this.wasStarted()){
			throw new StopWatchNeverStartedException("The Stopwatch Was Never Started");
		}
		
		if(isRunning){
			return (System.currentTimeMillis() - startTimeInMillis);
		}else{
			return stopTimeInMillis - startTimeInMillis;
		}
	}

	protected boolean wasStarted() {
		if(startTimeInMillis == 0L){
			return false;
		}
		
		return true;
	}

	public class StopWatchException extends Exception{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		StopWatchException(String message){
			super(message);
		}
	}

	public class StopWatchNotRunningException extends StopWatchException{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		StopWatchNotRunningException(String message){
			super(message);
		}
	}
	
	public class StopWatchNeverStartedException extends StopWatchException{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		StopWatchNeverStartedException(String message){
			super(message);
		}
	}
}
