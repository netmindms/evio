package com.netmind.devkkh.evio;




import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;


public class EvSocket extends EvEvent {
	
	private static final String tag="EvSock";
	public static final int SOCKET_TCP=0;
	public static final int SOCKET_UDP=1;

	private Listener _lis;
	protected SelectableChannel mChannel;
	private int mLastReadCnt;
	private int _type;
	private ByteBuffer _dummyBuf;

	EvSocket(SocketChannel ch) {
		super();
		mChannel = ch;
		_lis = null;
		_type = SOCKET_TCP;
	}
	
	public EvSocket() {
		super();
//		try {
//			mChannel = SocketChannel.open();
//			mChannel.configureBlocking(false);
//
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		_lis = null;
	}
	

	public int open(int type, Listener lis) {
		if(mChannel != null) {
			return -1;
		}

		_type = type;
		if(lis != null) {
			_lis = lis;
		}
		try {
			if(type == SOCKET_TCP) {
				mChannel = SocketChannel.open();
			} else if(type == SOCKET_UDP) {
				mChannel = DatagramChannel.open();
			}
			mChannel.configureBlocking(false);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if(mChannel != null) {
			return 0;
		} else {
			return -1;
		}
	}
	
	public int open(SocketChannel ch, Listener lis) {
		registerEvent(ch, EVT_READ);
		_lis = lis;
		_type = SOCKET_TCP;
		mChannel = ch;
		return 0;
	}
	
	public int bind(String ip, int port) {
		InetSocketAddress localaddr  = new InetSocketAddress(ip, port);
		try {
			if(_type == SOCKET_TCP) {
				((SocketChannel)mChannel).socket().bind(localaddr);
			} else if(_type == SOCKET_UDP) {
				((DatagramChannel)mChannel).socket().bind(localaddr);
			}
			registerEvent(mChannel, EVT_READ);
			return 0;
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	public void setOnListener(Listener lis) {
		_lis = lis;
	}
	
	public void OnConnected() {
		if(_lis != null) {
			_lis.OnSocketEvent(this, EVT_CONNECTED);
		}
	}
	
	public void OnDisconnected() {
		if(_lis != null)
			_lis.OnSocketEvent(this, EVT_CLOSED);
	}
	
	public void OnRead() {
		if(_lis != null) {
			_lis.OnSocketEvent(this, EVT_READ);
		} else {
			if(_dummyBuf==null) {
				_dummyBuf = ByteBuffer.allocate(128);
			}
			_dummyBuf.clear();
			recv(_dummyBuf);
		}
	}
	
	public void OnWrite() {
		if(_lis != null)
			_lis.OnSocketEvent(this, EVT_WRITE);
	}
	
	public int connect(String ip, int port) {
		InetSocketAddress address = new InetSocketAddress(ip, port);
		boolean cret;
		try {
			if(_type == SOCKET_TCP) {
				registerEvent(mChannel, EVT_CONNECTED);
				cret = ((SocketChannel)mChannel).connect(address);
			} else if(_type == SOCKET_UDP) {
				registerEvent(mChannel, EVT_READ);
				((DatagramChannel)mChannel).connect(address);
//				((DatagramChannel)mChannel).socket().connect(address);

				cret = true;
			} else {
				cret = false;
			}
		} catch (IOException e) {
			cret = false;
			e.printStackTrace();
		}
		return cret? 0:-1;
	}
	
	public void close() {
		try {
			mChannel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public SocketAddress recvFrom(ByteBuffer buf) {
		SocketAddress fromaddr = null;
		try {
			if(_type == SOCKET_UDP) {
				mLastReadCnt = 0;
				fromaddr = ((DatagramChannel)mChannel).receive(buf);
				mLastReadCnt = buf.position();
			}
			if(mLastReadCnt>=0) {
				buf.flip();
			}
		} catch (IOException e) {
			mLastReadCnt = -1;
			e.printStackTrace();
		}
		return fromaddr;
	}
	
	public int recv(ByteBuffer buf) {
		try {
			mLastReadCnt = 0;
			if(_type == SOCKET_TCP) {
				mLastReadCnt = ((SocketChannel)mChannel).read(buf);
				if(mLastReadCnt<=0) {
					dlog.e(tag, "#### read error, mLastReadCnt="+mLastReadCnt);
				}
			} else if(_type == SOCKET_UDP) {
				((DatagramChannel)mChannel).receive(buf);
				mLastReadCnt = buf.position();
			}
			if(mLastReadCnt>0) {
				buf.flip();
			}
		} catch (IOException e) {
			mLastReadCnt = -1;
			e.printStackTrace();
		}
		return mLastReadCnt;
	}
	
	public int sendTo(ByteBuffer buf, SocketAddress destaddr) {
		try {
			return ((DatagramChannel)mChannel).send(buf, destaddr);
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	public int send(ByteBuffer buf) {
		try {
			if(_type == SOCKET_TCP) {
				return ((SocketChannel)mChannel).write(buf);
			} else if(_type == SOCKET_UDP) {
				return ((DatagramChannel)mChannel).write(buf);
			} else {
				return -1;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
	}

	
	@Override
	public void OnEvent(int event) {
		if(event == EVT_CONNECTED) {

			try {
				if(_type == SOCKET_TCP) {
					boolean r = ((SocketChannel)mChannel).finishConnect();
					if(r) {
						dlog.d(tag, "finishConnect, true");
						mSelectionKey.interestOps(SelectionKey.OP_READ);
//						registerEvent(mChannel, EVT_READ);
						OnConnected();
					} else {
						// error
					}
				}
			} catch (IOException e) {
				dlog.e(tag, "### finish connect error...");
				OnDisconnected();
				mSelectionKey.cancel();
			}
		} else if(event == EVT_READ) {
			mLastReadCnt = 1;
			OnRead();
			if(mLastReadCnt < 0) {
				dlog.d(tag, "### last read cnt error,"+mLastReadCnt);
				OnDisconnected();
				mSelectionKey.cancel();
			}
				
		} else if(event == EVT_WRITE) {
			OnWrite();
		} else if(event == EVT_ACCEPT) {
			
		}
	}

	public int getLocalPort() {
		if(_type == SOCKET_TCP) {
			return ((SocketChannel)mChannel).socket().getLocalPort();
		} else if(_type == SOCKET_UDP) {
			return ((DatagramChannel)mChannel).socket().getLocalPort();
		} else {
			return 0;
		}
	}
	public interface Listener {
		public void OnSocketEvent(EvSocket sockobj, int event);
	}

}
