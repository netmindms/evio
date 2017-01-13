package com.netmind.devkkh.evio;

/**
 * Created by netmind on 13. 7. 22.
 */
public class dlog {
	public static final int LEVEL_NONE=-1;
	public static final int LEVEL_ERR=0;
	public static final int LEVEL_DEBUG=1;
	public static final int LEVEL_WARNING=2;
	public static final int LEVEL_INFO=3;
	public static final int LEVEL_VERBOSE=4;
	public interface LogOutput {
		void print(String tag, String s);
	}

	private static int _level = LEVEL_WARNING;
	private static LogOutput _logOut = null;

	private static dlog ourInstance = new dlog();

	public static dlog getInstance() {
		return ourInstance;
	}

	private dlog() {
	}

	public static void setOutput(LogOutput out) {
		_logOut = out;
	}

	public static void setLevel(int level) {
		_level = level;
	}


	public static void e(String tag, String s) {
		if(_level >= LEVEL_ERR && _logOut != null) {
			_logOut.print(tag, s);
		}
	}
	public static void w(String tag, String s) {
		if(_level >= LEVEL_WARNING && _logOut != null) {
			_logOut.print(tag, s);
		}
	}
	public static void i(String tag, String s) {
		if(_level >= LEVEL_INFO && _logOut != null) {
			_logOut.print(tag, s);
		}
	}
	public static void d(String tag, String s) {
		if(_level >= LEVEL_DEBUG && _logOut != null) {
			_logOut.print(tag, s);
		}
	}
	public static void v(String tag, String s) {
		if(_level >= LEVEL_VERBOSE && _logOut != null) {
			_logOut.print(tag, s);
		}
	}
}
