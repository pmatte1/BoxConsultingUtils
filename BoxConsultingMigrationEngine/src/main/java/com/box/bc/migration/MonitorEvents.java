package com.box.bc.migration;

import org.apache.log4j.Logger;

import com.box.bc.exception.AuthorizationException;
import com.box.bc.generator.AuthorizationGenerator;
import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxEvent;
import com.box.sdk.EventListener;
import com.box.sdk.EventStream;

public class MonitorEvents {

	private static Logger logger = Logger.getLogger(MonitorEvents.class);

	public static void main(String[] args) {

		try {
			logger.info("Establishing Connection");
			BoxAPIConnection api = AuthorizationGenerator.getAPIConnection("pmatte+demo@box.com");
			logger.info("Established Connection - " + api.getAccessToken());
			MonitorEvents me = new MonitorEvents();
			EventStream eventStream = new com.box.sdk.EventStream(api);
			EventListener el = me.new MyEventListener();
			eventStream.addListener(el);
			eventStream.start();
			logger.info("Monitoring Established");
			
//			EventLog eventLog = EventLog.getEnterpriseEvents(api, new Date(), null, Type.values());
//			String streamPosition = eventLog.getStreamPosition();


			while(true){
				try {
					Thread.sleep(10000);
//					EventLog eventLog2 = EventLog.getEnterpriseEvents(api, streamPosition, null, null, Type.values());
//					for(int i=0; i<eventLog2.getSize(); i++){
//						Iterator<BoxEvent> iterEvent = eventLog2.iterator();
//						while(iterEvent.hasNext()){
//							BoxEvent event = iterEvent.next();
//							logger.info(event.getType().toString());
//							logger.info(event.getAdditionalDetails());
//
//						}
//						
//					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (AuthorizationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	public class MyEventListener implements EventListener{

		public void onEvent(BoxEvent event) {
			logger.info(event.getType().toString());
			logger.info(event.getSourceJSON());
			logger.info(event.getAdditionalDetails());

		}

		public void onNextPosition(long position) {
			// TODO Auto-generated method stub

		}

		public boolean onException(Throwable e) {
			// TODO Auto-generated method stub
			return false;
		}

	}
}
