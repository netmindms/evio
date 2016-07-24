package com.devkkh.evio;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Vector;

/**
 * Created by Administrator on 2012-07-21.
 */
public class TimerManager {
	private long _minTimerTime=-1;
	private EvTask _task;
	private Vector<FireInfo> _fireList;
	private int _idSeed=0;
	LinkedList<EvTimer> _timerList;
	private static final String tag = "tm";


	private class FireInfo {
		int id;
		int cnt;

		public FireInfo(int id, int cnt) {
			this.id = id;
			this.cnt = cnt;
		}
	}

	long getMinTime() {
		return _minTimerTime;
	}

	void open() {
		_timerList = new LinkedList<>();
		_fireList = new Vector<>();
		_task = EvTask.getCurrentTask();
		_minTimerTime = Long.MAX_VALUE;
	}

	void close() {
		_timerList.clear();
		_fireList.clear();
	}

	void checkTimer() {
		if(_minTimerTime>0) {
//			dlog.d(tag, "checktimer, total timers="+_timerList.size());
			long ct = System.nanoTime();
			if (ct >= _minTimerTime) {
				_fireList.clear();
				// expire된 타이머를 찾는다.
				// expire 된 타이머는 바로 callback을 불러주지 않고 firelist에 일단 먼저 추가하고 loop를 빠져나와서 불러준다.
				// 이렇게 하는 이유는 callback 을 loop 안에서 직접 호출해주면 callback에서 timer를 제거하는 경우가 발생할 때
				// iteration loop가 망가지게 때문이다.
				long nextfiretime=Long.MAX_VALUE;
				for (EvTimer ti : _timerList) {
					int cnt = 0;
					long t;
					for (t = ti._fireTime; t <=ct; ) {
						cnt++;
						if(ti._period==0 ) {
							break;
						}
						t += ti._period;
					}
					if (cnt > 0) {
//						dlog.d(tag, "timer expired, id="+ti._id+", cnt="+cnt);
						ti._fireTime = t;
						_fireList.add(new FireInfo(ti._id, cnt));
					}
					if(ti._fireTime < nextfiretime) {
						nextfiretime = ti._fireTime;
					}
				}

				_minTimerTime = nextfiretime;

				if (_fireList.size() > 0) {
					for (FireInfo fi : _fireList) {
						/*
						expire된 타이머의 callback들을 호출하는 동안 한 callback에서 다른 타이머를 Kill할 수 있다.
						이런 경우를 대비하여 callback을 호출해주어야 할 타이머 object를 id로 찾아야 한다.
						 */
						EvTimer ti = getTimerInfoObj(fi.id);
						if (ti != null) {
							ti.expireTimer(fi.cnt);
						}
					}
					_fireList.clear();
				}
			}
		}
	}

	int newTimer(long period, EvTimer ti) {
		if(++_idSeed==0) ++_idSeed;

		ti._id = _idSeed;
		_timerList.add(ti);
		setTimer(ti, period);
		//dlog.d(tag, "new timer, id=" + ti.id + ", total cnt=" + _timerList.size() +", firetime="+ti.fireTime/1000000+", ct="+ct/1000000);
		return ti._id;
	}

	void setTimer(EvTimer ti, long period) {
		long ct = System.nanoTime();
//		ti._period  = period;
		ti._fireTime = ct + period;
		if(ti._fireTime < _minTimerTime) {
			_minTimerTime = ti._fireTime;
		}
	}

	int getTotalTimerCount() {
		return _timerList.size();
	}

	EvTimer getTimerInfoObj(int id) {
		for(EvTimer ti: _timerList) {
			if(ti._id == id) {
				return ti;
			}
		}
		return null;
	}

	void delTimer(int id) {
		//dlog.d(tag, "delete timer, id=" + id);
		int idx=-1;
		ListIterator<EvTimer> itr = _timerList.listIterator();

		while(itr.hasNext()) {
			if(itr.next()._id  == id) {
				itr.remove();
			}
		}

//		dlog.d(tag, "del timer, total cnt=" + _timerList.size());
	}

	long getWaitTimeMs() {
		if(_minTimerTime >0 ) {
			long ct = System.nanoTime();
			if( _minTimerTime > ct) {
//				long diff =  Math.round( (_minTimerTime - ct)/1000000.0);
				long diff = (_minTimerTime - ct);
//				diff = (diff)/1000000 + ( (diff%1000000)>0?1:0 );
				diff = (diff)/1000000 + ( (diff%1000000)>=500000?1:0 );
//				dlog.d(tag, "wait time ms="+diff+ ", mode="+(diff%1000000));
				return diff;
			} else {
				return 0;
			}
		} else {
			return Long.MAX_VALUE;
		}
	}
}
