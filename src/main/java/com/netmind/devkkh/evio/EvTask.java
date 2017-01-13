package com.netmind.devkkh.evio;

import java.io.IOException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;



public class EvTask extends Thread {
	private static final String tag = "EvTask";
	private boolean mLoopExit=false;

	public static final int EVM_INIT =0;
	public static final int EVM_CLOSE =1;
	public static final int EVM_EXIT =2;
//	public static final int EVM_TIMER =3;
//	public static final int EVM_SOCKET =4;
	public static final int EVM_CTRL=5;

	public static final int EVM_USER =1000;

	private Listener _lis;

	Selector selector;
	LinkedList<EvMsg> mMsgQue;

	// timer
	private TimerManager _timerManager;

	public EvTask() {
		mMsgQue = new LinkedList<EvMsg>();

		try {
			selector = Selector.open();
			//mRecvCmdCh = mPipe.source();
			//mSendCmdCh = mPipe.sink();
			//mRecvCmdCh.configureBlocking(false);
			//mSendCmdCh.configureBlocking(false);
			//mRecvCmdCh.register(selector, SelectionKey.OP_READ);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}


	public void postMsg(EvMsg msg) {
		synchronized (mMsgQue) {
			msg.isSync = false;
			mMsgQue.add(msg);
		}
		selector.wakeup();
	}

	public void sendMsg(EvMsg msg) {
		synchronized (msg) {
			synchronized (mMsgQue) {
				msg.isSync = true;
				mMsgQue.add(msg);
			}
			selector.wakeup();
			try {
				msg.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
	}

	public void postMsg(int msgid) {
		postMsg(EvMsg.buildMsg(msgid));
	}


	public void end() {
		postMsg(EvMsg.buildMsg(EVM_EXIT));
		try {
			join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void postExit() {
		postMsg(EvMsg.buildMsg(EVM_EXIT));
	}

	public void OnMsgProc(EvMsg msg) {
		if(_lis !=null) {
			_lis.OnEventProc(msg);
		}
	}

	public void setOnListener(Listener lis) {
		_lis = lis;
	}


	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
//		dlog.d(tag, "EvTask started...");
		EvContext._currentEvContext.set(new EvContext());
		EvContext._currentEvContext.get().task = this;
		try {
			process_loop();
		} catch (IOException e) {
//			dlog.e(tag, e.getMessage());
		}


//		dlog.d(tag, "EvTask ended....");
	}


	public void start() {
		super.start();
//		initTimerContext();
		sendMsg(EvMsg.buildMsg(EVM_INIT));
	}

	private void procMsgs() {
		EvMsg msg;
		for(;;) {
			synchronized (mMsgQue) {
				msg = mMsgQue.poll();
			}
			if(msg == null)
				break;
			if(msg.msgId == EVM_CTRL) {
				msg.ctrlMsg._lis.OnCtrlMsg(msg);
			} else if(msg.msgId == EVM_EXIT) {
				mLoopExit = true;
			} else {
				OnMsgProc(msg);
			}

			if(msg.isSync == true) {
				synchronized(msg) {
					msg.notify();
				}
			}
		}
	}

	private void process_loop() throws IOException {
		//dlog.v(tag, "process loop start");
		_timerManager = new TimerManager();
		_timerManager.open();
		while (!mLoopExit) {
			try {
//				int chs = selector.select();
//				dlog.d(tag, "select timeout time="+_timerManager.getWaitTimeMs());
				int chs;
				long wtime = _timerManager.getWaitTimeMs();
				if( wtime !=0) {
//					dlog.d(tag, "start selet..., selector="+selector);
					chs = selector.select(wtime);
//					dlog.d(tag, "    select end, chs="+chs);
				} else {
					chs = 0;
				}
				
				// EdEvent에 의해 깨어 날 수도 있으므로 항상 타이머와 메시지큐를 검사해야 함. 
				_timerManager.checkTimer();
				procMsgs();
				
				if(chs > 0) {
					Set<SelectionKey> selectedKeys = selector.selectedKeys();
					dlog.v(tag, "task="+this+", chs="+chs+", keysize="+selectedKeys.size());
					Iterator<SelectionKey> itr = selectedKeys.iterator();
					while (itr.hasNext()) {
						SelectionKey key = itr.next();
						// rs = (RemoteSys)key.attachment();
						//SelectableChannel sch = key.channel();

						// dlog.d(tag, "key ch="+sch.toString());
						// dlog.d(tag, "    key ="+key.toString());

						//int ops = key.readyOps();
						// dlog.d(tag, " read ops = " + ops);
						if (key.isValid() && key.isReadable()) {
							// dlog.d(tag, "read ready....");
							// ByteBuffer buf = ByteBuffer.allocate(128);
							EvEvent evobj = (EvEvent) key.attachment();
							evobj.OnEvent(EvEvent.EVT_READ);
						}

						if (key.isValid() && key.isConnectable()) {
//						dlog.d(tag, "connectable ......");
							EvEvent evobj = (EvEvent) key.attachment();
							evobj.OnEvent(EvEvent.EVT_CONNECTED);
						}

						if (key.isValid() && key.isAcceptable()) {
							EvEvent evobj = (EvEvent) key.attachment();
							evobj.OnEvent(EvEvent.EVT_ACCEPT);
						}
						itr.remove(); // very, very important

					}
				}
			} catch (ClosedSelectorException sele) {
				// break;

			} /*
			 * catch(Exception e) { //e.printStackTrace(); dlog.d(tag,
			 * "### excepton = "+e.getMessage()); //break; }
			 */
		}

		OnMsgProc(EvMsg.buildMsg(EVM_CLOSE));
		//close_allsock();
	}


	public static void msleep(int msec) {
		try {
			Thread.sleep(msec);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	

	
	int setTimer(int period, EvTimer timer) {
		int id = _timerManager.newTimer(period*1000*1000, timer);
		return id;
	}

	void killTimer(int id) {
		_timerManager.delTimer(id);
	}

	public int getTotalTimerCount() {
		return _timerManager.getTotalTimerCount();
	}

	TimerManager getTimerManager() {
		return _timerManager;
	}
	
	public interface Listener {
		void OnEventProc(EvMsg msg);
	}

	public static EvTask getCurrentTask() {
		return EvContext.getCurrentTask();
	}


}
