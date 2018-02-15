package com.box.bc.migration.util;

import org.apache.log4j.Logger;

public class MemoryMonitor extends Thread {
	private static Logger logger = Logger.getLogger(MemoryMonitor.class);


	private int frequency;
	private boolean keepRunning=true;

	public MemoryMonitor(int frequency){
		this.setName("MemoryMonitor");
		if(frequency<1000){
			this.frequency=frequency*1000;
		}else{
			this.frequency=frequency;
		}
	}

	public void run(){
		while(this.keepRunning){
			MemoryStats memStats = new MemoryStats();
			if(memStats.getPercentOfMax()>85){
				logger.warn("Memory Utilization is >85%.  Currently at " + memStats.getPercentOfMax());
			}else{
				String output  = "";
				for(float i=0; i<memStats.getPercentOfMax(); i++){
					output += "*";
				}
				output = buffer(output, 100);

				logger.info(output + "| (" + memStats.getUsed() + " of " + memStats.getMax() + " - "+ memStats.getPercentOfMax() + " %)");
			}

			try {
				Thread.sleep(frequency);
			} catch (InterruptedException e) {
				logger.error(e.getMessage(),e);
			}

		}
	}

	private String buffer(String output, int bufferedLength) {
		for(int i=output.length(); i<bufferedLength; i++){
			output = output + " ";
		}
		return output;
	}

	public class MemoryStats{
		private float used;
		private float free;
		private float total;
		private float max;
		private float percentUsed;
		private float percentOfMax;

		public MemoryStats(){
			this.setUsed((Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/1024/1024);
			this.setTotal((Runtime.getRuntime().totalMemory())/1024/1024);
			this.setMax((Runtime.getRuntime().maxMemory())/1024/1024);
			this.setFree((Runtime.getRuntime().freeMemory())/1024/1024);
			this.setPercentUsed((this.getUsed()/this.getTotal())*100);
			this.setPercentOfMax((this.getUsed()/this.getMax())*100);

		}

		public float getUsed() {
			return used;
		}
		public void setUsed(float used) {
			this.used = used;
		}
		public float getFree() {
			return free;
		}
		public void setFree(float free) {
			this.free = free;
		}
		public float getTotal() {
			return total;
		}
		public void setTotal(float total) {
			this.total = total;
		}
		public float getMax() {
			return max;
		}
		public void setMax(float max) {
			this.max = max;
		}
		public float getPercentUsed() {
			return percentUsed;
		}
		public void setPercentUsed(float percentUsed) {
			this.percentUsed = percentUsed;
		}

		public float getPercentOfMax() {
			return percentOfMax;
		}

		public void setPercentOfMax(float percentOfMax) {
			this.percentOfMax = percentOfMax;
		}



	}

	public void stopRunning() {
		this.keepRunning=false;

	}

}
