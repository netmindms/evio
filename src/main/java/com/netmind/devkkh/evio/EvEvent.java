package com.netmind.devkkh.evio;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

public class EvEvent {
	public static final int EVT_CLOSED = 0;
	public static final int EVT_READ = SelectionKey.OP_READ;
	public static final int EVT_WRITE = SelectionKey.OP_WRITE;
	public static final int EVT_CONNECTED = SelectionKey.OP_CONNECT;
	public static final int EVT_ACCEPT = SelectionKey.OP_ACCEPT;

	EvContext _context;
	EvTask _task;
	SelectableChannel channel;
	SelectionKey mSelectionKey;

	void registerEvent(SelectableChannel ch, int event) {
		_context = EvContext.getEvContext();
		_task = _context.task;
		channel = ch;
		if (ch != null) {
			try {
				ch.configureBlocking(false);
				mSelectionKey = channel.register(_task.selector, event, this);
			} catch (ClosedChannelException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	public void deregisterEvent() {
		if (mSelectionKey != null) mSelectionKey.cancel();
	}

	public void changeEventReg(int event) {
		mSelectionKey.interestOps(event);
	}

	public void OnEvent(int event) {

	}

}
