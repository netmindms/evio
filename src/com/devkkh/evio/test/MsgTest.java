package com.devkkh.evio.test;

import static org.junit.Assert.*;

import org.junit.Test;

import com.devkkh.evio.EvMsg;
import com.devkkh.evio.EvTask;
import com.devkkh.evio.EvCtrlMsg;

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
	
	@Test
	public void CtrlMsgTest() throws Exception {

		class CtrlMsgTask extends EvTask {
			EvCtrlMsg _ctrlMsg;
			int _sendMsgCnt =0;
			int _postMsgCnt = 0;
			@Override
			public void OnMsgProc(EvMsg msg) {
				if(msg.msgId == EVM_INIT) {
					_ctrlMsg = new EvCtrlMsg();
					_ctrlMsg.open(new EvCtrlMsg.Listener() {
						@Override
						public void OnCtrlMsg(EvMsg msg) {
							if(msg.ctrlMsgId == 1000) {
								_sendMsgCnt++;
							} else if(msg.ctrlMsgId == 1001) {
								_postMsgCnt++;
							}
						}
					});
				} else if(msg.msgId == EVM_CLOSE) {
					_ctrlMsg.close();
				}
			}

		}

		CtrlMsgTask _task = new CtrlMsgTask();
		_task.start();
		EvCtrlMsg cm = _task._ctrlMsg;
		for(int i=0;i<100;i++) {
			cm.sendMsg(1000);
			cm.postMsg(1001);
		}
		_task.end();
		assertEquals(100, _task._sendMsgCnt);
		assertEquals(100, _task._postMsgCnt);
	}

}
