package com.netmind.devkkh.evio;

/**
 * Created by netmind on 16. 9. 5.
 */
public class EvCtrlMsg {
	Listener _lis;
	EvTask _task;
	public interface Listener {
		void OnCtrlMsg(EvMsg msg);
	}

	public int open(Listener lis) {
		_lis = lis;
		_task = EvTask.getCurrentTask();
		return 0;
	}
	public void close() {

	}

	public void sendMsg(int msgid, int arg1, int arg2, Object obj) {
		EvMsg msg = EvMsg.buildMsg(EvTask.EVM_CTRL, arg1, arg2, obj);
		msg.ctrlMsg = this;
		msg.ctrlMsgId = msgid;
		msg.obj = obj;
		_task.sendMsg(msg);
	}

	public void sendMsg(int msgid) {
		sendMsg(msgid, 0, 0, null);
	}

	public void postMsg(int msgid, int arg1, int arg2, Object obj) {
		EvMsg msg = EvMsg.buildMsg(EvTask.EVM_CTRL, arg1, arg2, obj);
		msg.ctrlMsg = this;
		msg.ctrlMsgId = msgid;
		msg.obj = obj;
		_task.postMsg(msg);
	}
	public void postMsg(int msgid) {
		postMsg(msgid, 0, 0, null);
	}

}
