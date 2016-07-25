package com.devkkh.evio.test;

import static org.junit.Assert.*;

import org.junit.Test;

import com.devkkh.evio.EvMsg;
import com.devkkh.evio.EvTask;

public class MsgTest {

	class MsgTask extends EvTask {
		public static final int UM_TEST = EVM_USER+1;
		int _msgRecvCnt=0;
		
		@Override
		public void OnMsgProc(EvMsg msg) {
			if(msg.msgId == EVM_INIT) {
				
			} else if(msg.msgId == EVM_CLOSE) {
				
			} else if(msg.msgId == UM_TEST) {
				_msgRecvCnt++;
			}
		}
		
	}
	
	@Test
	public void test() throws Exception {
		MsgTask _task = new MsgTask();
		_task.start();
		for(int i=0;i<1000;i++) {
			EvMsg msg = EvMsg.buildMsg(MsgTask.UM_TEST);
			_task.postMsg(msg);
		}
		_task.postExit();
		_task.join();
		assertEquals(1000, _task._msgRecvCnt);
	}

}
