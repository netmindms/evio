package com.devkkh.evio;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class EvTcpChunk extends EvTcpChannel {
	private static final String tag = "EvTcpChunk";
	int _packetSize;
	ByteBuffer mCurBuf;
	ByteBuffer _sizeBuf;
	int mBufSize;
	boolean isDynamicBuf=false;
	private IEvTcpChunkCb mCallback=null;
	
	public EvTcpChunk(int maxpacket) {
		super();
		mBufSize = maxpacket;

		mCurBuf = ByteBuffer.allocate(mBufSize);

		_sizeBuf = ByteBuffer.allocate(4);
		_sizeBuf.order(ByteOrder.LITTLE_ENDIAN);
		_packetSize = 0;
	}

	public void setCallback(IEvTcpChunkCb cb) {
		mCallback = cb;
	}
	
	@Override
	public void OnRead() {
		int rdcnt;
		if (_packetSize <= 0) {
			//int r = 4 - mSizeBuf.position();
			rdcnt = read(_sizeBuf);
			dlog.d(tag, "=== read=" + rdcnt);
			if (rdcnt > 0) {
				if (_sizeBuf.position() == 4) {
					_sizeBuf.rewind();
					_packetSize = _sizeBuf.getInt();
					//dlog.d(tag, "get len=" + packetSize);
					_sizeBuf.rewind();
					//mCurBuf.limit(packetSize);
					if (isDynamicBuf) {
						dlog.d(tag, "alloc chunk buf,size=" + _packetSize);
						mCurBuf = ByteBuffer.allocate(_packetSize);
					} else {
						mCurBuf.position(0);
						mCurBuf.limit(_packetSize);
					}
				} else {
					dlog.d(tag, "=== p=" + _sizeBuf.position());
					return;
				}
			}
		}

		if (_packetSize > 0) {
			// int rdcnt = packetSize - mCurBuf.position();
			rdcnt = read(mCurBuf);
			//dlog.e(tag, "=== read data ="+rdcnt);
			if (rdcnt > 0) {
				if (mCurBuf.remaining() == 0) {
					mCurBuf.position(0);
					OnChunk(mCurBuf);
					_packetSize = 0; // reset
				}
			}
		}
	}
	
	
	public void sendChunk(byte[] buf, int size) {
		ByteBuffer bf = ByteBuffer.allocate(4+size);
		bf.order(ByteOrder.LITTLE_ENDIAN);
		bf.putInt(size);
		bf.put(buf, 0, size);
		bf.flip();
		sendPacket(bf);
	}
	
	public boolean OnChunk(ByteBuffer bf) {
		if(mCallback != null)
			return mCallback.IOnTcpChunk(this, bf);
		return false;
	}

	@Override
	public void OnConnected() {
		if(mCallback != null)
			mCallback.IOnTcpChunkStatu(this, EvEvent.EVT_CONNECTED);
	}

	@Override
	public void OnDisconnected() {
		if(mCallback != null)
			mCallback.IOnTcpChunkStatu(this, EvEvent.EVT_CLOSED);
	}

	public interface IEvTcpChunkCb {
		public boolean IOnTcpChunk(EvTcpChunk chunkobj, ByteBuffer bf);
		public void IOnTcpChunkStatu(EvTcpChunk chunkobj, int event);
	}

}
