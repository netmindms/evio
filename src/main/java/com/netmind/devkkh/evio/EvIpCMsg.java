package com.netmind.devkkh.evio;

import java.nio.ByteBuffer;
import java.util.LinkedList;

/**
 * Created by netmind on 16. 9. 2.
 */
public class EvIpCMsg {
	private EvPipe _pipe;
	LinkedList<EvMsg> _msgQue= new LinkedList<>();
	Listener _lis;
	ByteBuffer _sendBuf=ByteBuffer.allocate(1);
	ByteBuffer _recvBuf = ByteBuffer.allocate(10);

	public interface Listener {
		void OnIpcMsg(EvMsg msg);
	}

	public int open(Listener lis) {
		_lis = lis;
		_pipe = new EvPipe();
		return _pipe.open(new EvPipe.Listener() {
			@Override
			public void OnPipeEvent(EvPipe pipeobj, int event) {
				_recvBuf.clear();
				int rcnt = pipeobj.recv(_recvBuf);
				if(rcnt>0) {
//					tlog.info("pipe read cnt="+rcnt);
//					System.out.println("pipe read cnt="+rcnt);
					EvMsg msg;
//					for(int i=0;i<rcnt;i++) {
					for(;_msgQue.size()>0;) {
						synchronized (_msgQue) {
							msg = _msgQue.pop();
						}
//						if(msg==null) {
//							break;
//						}
						synchronized (msg) {
//							System.out.println("sync enter");
							_lis.OnIpcMsg(msg);
							if (msg.isSync == true) {
								msg.notify();
//								System.out.println("noti end");
							}
						}
					}
				} else {
					assert rcnt>0:"### pipe error";
				}

			}
		});
	}

	public int postMsg(EvMsg msg) {
		synchronized (_msgQue) {
			_msgQue.add(msg);
			if(_msgQue.size() > 1) {
				return 0; // 다른 msg에 의해 이미 pipe가 write된 상태이기 때문에 큐에 넣기만 하고 나온다.
			}
		}
		_sendBuf.position(0);
		_sendBuf.limit(1);
		return _pipe.send(_sendBuf); // pipe write의 return은 0이 될 수 있다.
	}

	public int sendMsg(EvMsg msg) {
		synchronized (msg) {
			msg.isSync = true;
			synchronized (_msgQue) {
				_msgQue.add(msg);
				if(_msgQue.size()>1) {
					return 0;// 다른 msg에 의해 이미 pipe가 write된 상태이기 때문에 큐에 넣기만 하고 나온다.
				}
			}
			_sendBuf.position(0);
			_sendBuf.limit(1);
			int wcnt = _pipe.send(_sendBuf); // 이 때 pipe read 대기 중인 스레드로 switching 될 수 있다.
			if(wcnt>0) {
				try {
//				System.out.println(" wait enter");
					msg.wait(); // wait 하면 synchronized가 풀리고, 다른 스레드가 notify 해 줘서 나오면 다시 synchronzied 가 걸린다.
//				System.out.println(" wait end");
				} catch (InterruptedException e) {
					e.printStackTrace();
					return -1;
				}
			} else {
				System.out.println("#### pipe write error, wcnt="+wcnt);
			}

		}
		return 0;
	}

	void close() {
		_pipe.close();
	}

}
