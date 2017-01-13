package com.devkkh.evio;

import org.junit.Test;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

import static org.junit.Assert.*;

/**
 * Created by netmind on 17. 1. 12.
 */
public class EvSocketTest {
	EvSocket _socket;
	EvTask _task;
	EvTimer _timer = new EvTimer();
	Logger _tlog;

	@Test
	public void UdpTest() throws Exception {
		LoggerUtil.initLogger();
		_tlog = Logger.getLogger("myFirstLog");
		_tlog.info("start test...............");
		_task = new EvTask();
        _task.setOnListener(new EvTask.Listener() {
            @Override
            public void OnEventProc(EvMsg msg) {
                if(msg.msgId == EvTask.EVM_INIT) {
                    _socket = new EvSocket();
                    _socket.open(EvSocket.SOCKET_UDP, new EvSocket.Listener() {
						@Override
						public void OnSocketEvent(EvSocket sockobj, int event) {
							if(event == EvSocket.EVT_READ) {
								ByteBuffer bf = ByteBuffer.allocateDirect(1000);
								sockobj.recv(bf);
//								System.out.println("on recv..........");
								_tlog.info("recv size="+bf.limit());
							}
						}
					});
//					_socket.bind("0.0.0.0", 9010);
					_socket.connect("127.0.0.1", 9000);
//                    ByteBuffer bf = ByteBuffer.allocate(1024);
                    ByteBuffer bf = ByteBuffer.allocateDirect(1024);
                    bf.put("1234".getBytes());
                    bf.flip();
                    _socket.send(bf);
                    _timer.set(100, new EvTimer.Listener() {
						@Override
						public void OnTimer(EvTimer timer, int cnt) {
//							_task.postExit();
//							ByteBuffer bf = ByteBuffer.allocate(1000);
//							bf.put("abcd".getBytes());
//							bf.flip();
//							InetSocketAddress addr = new InetSocketAddress("127.0.0.1", 9002);
//							_socket.sendTo(bf, addr);
						}
					});
                } else if(msg.msgId == EvTask.EVM_CLOSE) {

                }
            }
        });
        _task.start();
        _task.join();
	}

	@Test
	public void BasicStringTest() throws Exception {
		String ts = new String("1234");
		byte[] arr = ts.getBytes();
		assertEquals(4, arr.length);

		ByteBuffer bf = ByteBuffer.wrap(ts.getBytes());
		assertEquals(4, bf.limit());

		ByteBuffer df = ByteBuffer.allocateDirect(4);
		df.put("1234".getBytes());
		String dfs = new String(df.array(), 0, df.limit(),"utf8");
	}
}