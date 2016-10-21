package com.netmind.devkkh.evio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;


public class EvPipe extends EvEvent {

	private static final String tag="evpipe";
	private Pipe mPipe;
	private Pipe.SinkChannel mSinkCh;
	private Pipe.SourceChannel mSourceCh;
	private Listener _lis;
	private int mLastReadCnt;
	
	public EvPipe() {
		super();
	}

	public int open(Listener lis) {
		_lis = lis;
		try {
			mPipe = Pipe.open();
			mSinkCh = mPipe.sink();
			mSourceCh = mPipe.source();

			mSinkCh.configureBlocking(false);
			mSourceCh.configureBlocking(false);

			registerEvent(mSourceCh, EVT_READ);
			return 0;
		} catch (IOException e) {
			e.printStackTrace();

		}
		return -1;
	}

	public void setOnListener(Listener cb) {
		_lis = cb;
	}
	
	
	public void close() {
		try {
			mSourceCh.close();
			mSinkCh.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public int send(ByteBuffer bf) {
		try {
			return mSinkCh.write(bf); // pipe write의 return은 0이 될 수 있다.
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("### sink ch write error");
			return -1;
		}
	}
	
	public int recv(ByteBuffer bf) {
		try {
			mLastReadCnt = mSourceCh.read(bf);
		} catch (IOException e) {
			mLastReadCnt = -1;
			e.printStackTrace();
		}
		return mLastReadCnt;
	}
	
	public void OnRead() {
		if(_lis != null)
			_lis.OnPipeEvent(this, EVT_READ);
	}
	
	public void OnDisconnected() {
		if(_lis != null)
			_lis.OnPipeEvent(this, EVT_CLOSED);
	}
	
	@Override
	public void OnEvent(int event) {
		if(event == EVT_READ) {
			mLastReadCnt = 1;
			OnRead();
			if(mLastReadCnt<0) {
				dlog.d(tag, "### last read cnt error,"+mLastReadCnt);
				OnDisconnected();
				mSelectionKey.cancel();
			}
		} 
	}

	public interface Listener {
		public void OnPipeEvent(EvPipe pipeobj, int event);
	}

}
