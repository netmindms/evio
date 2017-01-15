package com.netmind.devkkh.evio;

import java.nio.ByteBuffer;


public class EvTcpMsg16 {

	int _pktLen=0;
	ByteBuffer _recvBuf;
	EvSocket _sock;
	Listener _lis;
	
	public interface Listener {
		void OnTcpMsg(ByteBuffer bf);
	}
	
	void open(EvSocket sock, int bufsize) {
		if(_sock != null) {
			return;
		}
		_recvBuf = ByteBuffer.allocate(bufsize);
		
		_sock = sock;
		_sock.setOnListener(new EvSocket.Listener() {
			
			public void OnSocketEvent(EvSocket sockobj, int event) {
				if(event == EvSocket.EVT_READ) {
					if(_pktLen!=0) {
						sockobj.recv(_recvBuf);
						if(_recvBuf.limit()==2) {
							_pktLen = _recvBuf.getShort();
							_recvBuf.clear();
						}
					} else {
						sockobj.recv(_recvBuf);
						if(_recvBuf.limit()==_pktLen) {
							if(_lis != null) {
								_lis.OnTcpMsg(_recvBuf);
							}
							_pktLen = 0;
							_recvBuf.clear();
							_recvBuf.limit(2);
						} else {
							_recvBuf.position( _recvBuf.limit());
						}
					}
				} else if(event == EvSocket.EVT_CLOSED) {
					
				}
			}
		});
	}
	
	

}
