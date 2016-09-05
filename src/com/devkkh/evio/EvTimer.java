package com.devkkh.evio;

/**
 * Created by netmind on 13. 7. 22.
 */
public class EvTimer extends EvEvent {
	long _fireTime;
	int _id=0;
	long _period=0;
	boolean _fisrtExpired;
	Listener _lis;
	TimerManager _tm;
	int _userInt;

	private final static String tag = "EvTimer";

	public interface Listener {
		void OnTimer(EvTimer timer, int cnt);
	}
	
	public int setUs(long firstus, long periodus, Listener lis) {
		_tm = EvContext.getCurrentTask().getTimerManager();
		_fisrtExpired = false;
		_period = periodus * 1000;
		_context = EvContext.getEvContext();
		_task = _context.task;
		_lis = lis;
		_id = _tm.newTimer(firstus*1000, this);

		return _id;
	}
	
	public int set(long firstms, long periodms, Listener lis) {
//		dlog.d(tag, "set timer,="+ this);
//		_tm = EvContext.getCurrentTask().getTimerManager();
//
//		_fisrtExpired = false;
//		_period = periodms * 1000000;
//		_context = EvContext.getEvContext();
//		_task = _context.task;
//		_lis = lis;
//		_id = _tm.newTimer(firstms*1000000, this);

		return setUs(1000*firstms, 1000*periodms, lis);
	}
	

	public int set(int periodms, Listener lis) {
		return set(0, periodms, lis);
	}
	
	public void kill() {
		if(_id >0) {
			_task.killTImer(_id);
		}
	}

	void expireTimer(int cnt) {
//		dlog.d(tag, "expire timer,="+ this);
		if(_lis != null ) {
			if(_fisrtExpired == false) {
				_fisrtExpired = true;
				_lis.OnTimer(this, cnt);
				if (_period > 0) {
					_tm.setTimer(this, _period);
				}  else {
					kill();
				}
			} else {
				_lis.OnTimer(this, cnt);
			}
		}
	}

	void setUserInt(int val) {
		_userInt = val;
	}
	int getUserInt() {
		return _userInt;
	}

}
