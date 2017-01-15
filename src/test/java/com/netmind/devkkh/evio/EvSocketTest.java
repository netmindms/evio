package com.netmind.devkkh.evio;

import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * Created by khkim on 2017-01-14.
 */
public class EvSocketTest {
	EvTask _task;
	EvSocket _svrSock;
	EvSocket _clientSock;
	Logger _logger;

	public EvSocketTest() {
		LoggerUtil.initLogger();
		_logger = Logger.getLogger("console");
	}

	@Test
	public void udpTest() throws Exception {
		_task = new EvTask();
		_task.setOnListener(new EvTask.Listener() {
			public void OnEventProc(EvMsg msg) {
				if(msg.msgId == EvTask.EVM_INIT) {
					_logger.info("task init");
					_svrSock = new EvSocket();
					_svrSock.open(EvSocket.SOCKET_UDP, new EvSocket.Listener() {
						@Override
						public void OnSocketEvent(EvSocket sockobj, int event) {
							if(event == EvSocket.EVT_READ) {
								_logger.info("server read event");
								ByteBuffer bf = ByteBuffer.allocate(100);
								InetSocketAddress rmtaddr = (InetSocketAddress)sockobj.recvFrom(bf);
								try {
									if(bf.limit()>0) {
										String rs = new String(bf.array(), 0, bf.limit(), "utf8");
										_logger.info("server rs: " + rs);
										String backmsg = "server back msg";
										ByteBuffer bbf = ByteBuffer.allocate(100);
										bbf.put(backmsg.getBytes());
										bbf.flip();
										int wcnt = sockobj.sendTo(bbf, rmtaddr);
										_logger.info("server back wcnt="+wcnt);
									}
								} catch (UnsupportedEncodingException e) {
									e.printStackTrace();
								}
							}
						}
					});

				} else if(msg.msgId == EvTask.EVM_CLOSE) {
					_logger.info("task closing");
				}
				_svrSock.bind("0.0.0.0", 10000);

				_clientSock = new EvSocket();
				_clientSock.open(EvSocket.SOCKET_UDP, new EvSocket.Listener() {
					@Override
					public void OnSocketEvent(EvSocket sockobj, int event) {
						if(event == EvSocket.EVT_READ) {
							_logger.info("client read event");
							ByteBuffer bf = ByteBuffer.allocate(100);
							sockobj.recv(bf);
							try {
								String rs = new String(bf.array(), 0, bf.limit(), "utf8");
								_logger.info("client rs: "+rs);
							} catch (UnsupportedEncodingException e) {
								e.printStackTrace();
							}
						}
					}
				});

				_clientSock.bind("0.0.0.0", 10010);
//				_clientSock.connect("127.0.0.1", 10000);
				String ts = "client send msg";
				ByteBuffer sf = ByteBuffer.allocate(100);
				sf.put(ts.getBytes());
				sf.flip();
				int wcnt;
				_clientSock.connect("127.0.0.1", 10000);
				wcnt = _clientSock.send(sf);
				//wcnt = _clientSock.sendTo(sf, new InetSocketAddress("127.0.0.1", 10000));
				_logger.info("client wcnt="+wcnt);
			}
		});
		_task.start();
		_task.join();
	}

	@Test
	public void tcpTest() throws Exception {

	}

}