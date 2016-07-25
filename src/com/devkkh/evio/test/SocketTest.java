package com.devkkh.evio.test;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

import com.devkkh.evio.EvMsg;
import com.devkkh.evio.EvServerSocket;
import com.devkkh.evio.EvSocket;
import com.devkkh.evio.EvTask;
import com.devkkh.evio.dlog;

public class SocketTest {
	
	public SocketTest() {
		super();
		dlog.setOutput(new dlog.LogOutput() {
			
			@Override
			public void print(String tag, String s) {
				System.out.println(tag+": "+s);
				
			}
		});
	}

	class ChildSock extends EvSocket {
		int _id;
		ServerTask _svrTask;
		public ChildSock(int id, ServerTask task) {
			super();
			this._id = id;
			this._svrTask = task;
		}
		
		@Override
		public void close() {
			super.close();
			_svrTask.removeChild(_id);
		}

	}

	class ServerTask extends EvTask {
		EvServerSocket _svrSock;
		HashMap<Integer, ChildSock> _clientsMap;
		int _idSeed = 0;

		@Override
		public void OnMsgProc(EvMsg msg) {
			if (msg.msgId == EVM_INIT) {
				_clientsMap = new HashMap<>();
				_svrSock = new EvServerSocket();
				int r = _svrSock.listen("127.0.0.1", 9090);
				Assert.assertEquals(0, r);
				_svrSock.setOnListener(new EvServerSocket.Listener() {

					@Override
					public void OnServerSocketEvent(EvServerSocket sockobj) {
						procIncoming();
					}
				});
			} else if(msg.msgId == EVM_CLOSE) {
//				dlog.d("server", "server task closing...");
		        for( Map.Entry<Integer, ChildSock> elem : _clientsMap.entrySet() ){
		            elem.getValue().close();
		        }
		        _clientsMap.clear(); 
				_svrSock.close();
			}

		}

		void procIncoming() {
			SocketChannel ch = _svrSock.accept();
			if (ch != null) {
				if (++_idSeed == 0)
					++_idSeed;
				ChildSock sock = new ChildSock(_idSeed, this);
				sock.open(ch, new EvSocket.Listener() {
					@Override
					public void OnSocketEvent(EvSocket sockobj, int event) {
						if (event == EvSocket.EVT_READ) {
							ByteBuffer bf = ByteBuffer.allocate(1);
							sockobj.recv(bf);
							sockobj.send(bf);
						} else if(event == EvSocket.EVT_CLOSED) {
							sockobj.close();
						}
					}
				});
			}

		}
		
		void removeChild(int id) {
			_clientsMap.remove(id);
		}

	}
	
	class ClientTask extends EvTask {

		EvSocket _sock1;
		EvSocket _sock2;
		byte sock1val=(byte)100;
		byte sock2val=(byte)50;
		byte r1=0;
		byte r2=0;
		
		@Override
		public void OnMsgProc(EvMsg msg) {
			if(msg.msgId == EVM_INIT) {
				_sock1 = new EvSocket();
				_sock2 = new EvSocket();
				_sock1.open(EvSocket.SOCKET_TCP, new EvSocket.Listener() {
					
					@Override
					public void OnSocketEvent(EvSocket sockobj, int event) {
						if(event == EvSocket.EVT_READ) {
							ByteBuffer bf = ByteBuffer.allocate(1);
							sockobj.recv(bf);
							if(bf.limit()>0) {
								r1 = bf.get();
//								dlog.d("cl1", Integer.toString(r1));
							} else {
								postExit();
							}
						} else if(event == EvSocket.EVT_CONNECTED) {
							ByteBuffer bf = ByteBuffer.allocate(1);
							bf.put(sock1val);
							bf.flip();
							sockobj.send(bf);
						} else if(event == EvSocket.EVT_CLOSED) {
							postExit();
						}
					}
				});
				_sock1.connect("127.0.0.1", 9090);
				
				
				_sock2.open(EvSocket.SOCKET_TCP, new EvSocket.Listener() {
					
					@Override
					public void OnSocketEvent(EvSocket sockobj, int event) {
						if(event == EvSocket.EVT_READ) {
							ByteBuffer bf = ByteBuffer.allocate(1);
							sockobj.recv(bf);
							if(bf.limit()>0) {
								r2 = bf.get();
//								dlog.d("cl2", Integer.toString(r2));
							} 
							postExit();
						} else if(event == EvSocket.EVT_CONNECTED) {
							ByteBuffer bf = ByteBuffer.allocate(1);
							bf.put(sock2val);
							bf.flip();
							sockobj.send(bf);
						} else if(event == EvSocket.EVT_CLOSED) {
							postExit();
						}
					}
						
				});
				_sock2.connect("127.0.0.1", 9090);
			} else if(msg.msgId == EVM_CLOSE) {
//				dlog.d("ctask", "client task closing...");
			}
		
		}
		
	}

	@Test
	public void test() throws Exception {
		ServerTask _svrTask = new ServerTask();
		_svrTask.start();
		ClientTask _clTask = new ClientTask();
		_clTask.start();
		
		_clTask.join();
		_svrTask.end();
		Assert.assertEquals((byte)100, _clTask.r1);
		Assert.assertEquals((byte)50, _clTask.r2);
	}

}
