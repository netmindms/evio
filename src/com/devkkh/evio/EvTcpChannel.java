package com.devkkh.evio;

import java.nio.ByteBuffer;

public class EvTcpChannel extends EvSocket {
	
	public EvTcpChannel() {
		super();
		
	}
	
	public int sendPacket(ByteBuffer bf) {
		int wcnt = send(bf);
		
		return wcnt;
	}
	
	public void setSocket(EvSocket socket) {
		mChannel = socket.mChannel;
	}
	
}
