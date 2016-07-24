package com.devkkh.evio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class EvServerSocket extends EvEvent {
	ServerSocketChannel mChannel;
	Listener mCallback;
	int mPort;
	
	public EvServerSocket() {
		super();
		mCallback = null;
		try {
			mChannel = ServerSocketChannel.open();
			mChannel.configureBlocking(false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setOnListener(Listener cb) {
		mCallback = cb;
	}
	
	public int bind(String ip, int port) {
		mPort = port;
		InetSocketAddress sockaddr = new InetSocketAddress(ip, port);
		try {
			mChannel.socket().bind(sockaddr);
			registerEvent(mChannel, EVT_ACCEPT);
			return 0;
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	public int listen(String ip, int port) {
		return bind(ip, port);
	}
	
	public int listen(int port) {
		return bind("0.0.0.0", port);
	}
	
	
	public EvSocket accept() {
		try {
			SocketChannel childch = mChannel.accept();
			childch.configureBlocking(false);
			EvSocket evsock = new EvSocket(childch);
			evsock.registerEvent(childch, EVT_READ);
			return evsock;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void reject() {
		EvSocket sch = accept();
		sch.close();
	}
	
	public void close() {
		try {
			mChannel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void OnAccept() {
		if(mCallback != null) {
			mCallback.OnServerSocketEvent(this);
		}
	}

	public int getLocalPort() {
		return mChannel.socket().getLocalPort();
	}

	@Override
	public void OnEvent(int event) {
		OnAccept();
	}


	public interface Listener {
		public void OnServerSocketEvent(EvServerSocket sockobj);
	}

}
