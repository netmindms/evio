package com.netmind.devkkh.evio;

public class EvMsg {
	public int msgId;
	public int param1, param2;
	public Object obj;
	boolean isSync;
	public EvCtrlMsg ctrlMsg;
	public int ctrlMsgId;

	EvMsg(int msgid, int p1, int p2, Object o) {
		msgId = msgid;
		param1 = p1;
		param2 = p2;
		obj = o;
	}
	
	public static EvMsg buildMsg(int msgid) {
		return (new EvMsg(msgid, 0, 0, null));
	}
	
	public static EvMsg buildMsg(int msgid, int p1, int p2) {
		return (new EvMsg(msgid, p1, p2, null));
	}
	
	public static EvMsg buildMsg(int msgid, Object obj) {
		return (new EvMsg(msgid, 0, 0, obj));
	}
	
	public static EvMsg buildMsg(int msgid, int p1, int p2, Object obj) {
		return (new EvMsg(msgid, p1, p2, obj));
	}
}
