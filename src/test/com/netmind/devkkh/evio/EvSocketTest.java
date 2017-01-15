package com.netmind.devkkh.evio;

import org.apache.log4j.Logger;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by khkim on 2017-01-14.
 */
public class EvSocketTest {
	EvTask _task;
	Logger _logger;

//	public EvSocketTest() {
//		LoggerUtil.initLogger();
//		_logger = Logger.getLogger("myFirstLog");
//	}

	@Test
	public void udpTest() throws Exception {
		_task = new EvTask();
		_task.setOnListener(new EvTask.Listener() {
			public void OnEventProc(EvMsg msg) {
				if(msg.msgId == EvTask.EVM_INIT) {
					_logger.info("task init");
					_task.postExit();
				} else if(msg.msgId == EvTask.EVM_CLOSE) {
					_logger.info("task closing");
				}
			}
		});
		_task.start();
		_task.join();
	}
}