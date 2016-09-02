package com.devkkh.evio.test;

import static org.junit.Assert.*;

import org.junit.Test;

import com.devkkh.evio.EvMsg;
import com.devkkh.evio.EvTask;
import com.devkkh.evio.EvTimer;

class CntTimer extends EvTimer {
	public CntTimer() {
		this._expireCnt = 0;
	}

	int _expireCnt;
}

public class TimerTest {
	
	@Test
	public void doTimerTest() throws Exception {
		class TimerTask extends EvTask {
			CntTimer _timer1;
			CntTimer _timer2;
			CntTimer _oneShotTimer;
			EvTimer _timerEnd;
			
			@Override
			public void OnMsgProc(EvMsg msg) {
				if(msg.msgId == EVM_INIT) {
					_timer1 = new CntTimer();
					_timer1.set(100, 100, new EvTimer.Listener() {
						@Override
						public void OnTimer(EvTimer timer, int cnt) {
							_timer1._expireCnt++;
						}
					});
					
					_timer2 = new CntTimer();
					_timer2.set(20, 20, new EvTimer.Listener() {
						
						@Override
						public void OnTimer(EvTimer timer, int cnt) {
							_timer2._expireCnt++;
						}
					});
					
					_oneShotTimer = new CntTimer();
					_oneShotTimer.set(100, 0, new EvTimer.Listener() {
						
						@Override
						public void OnTimer(EvTimer timer, int cnt) {
							_oneShotTimer._expireCnt++;
						}
					});
					
					_timerEnd = new EvTimer();
					_timerEnd.set(1010, 1010, new EvTimer.Listener() {
						
						@Override
						public void OnTimer(EvTimer timer, int cnt) {
							EvTask.getCurrentTask().postExit();
						}
					});
					
				} else if(msg.msgId == EVM_CLOSE) {
					
				}
			}
		}
		
		TimerTask _task = new TimerTask();
		_task.start();
		_task.join();
		assertEquals(10, _task._timer1._expireCnt);
		assertEquals(50, _task._timer2._expireCnt);
		assertEquals(1, _task._oneShotTimer._expireCnt);
		
	}

}
