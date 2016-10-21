package com.netmind.devkkh.evio;

/**
 * Created by netmind on 12. 7. 19.
 */
public class EvContext {
//    private final static String tag="EvContext";
    EvTask task;

    public static EvContext getEvContext() {
        return _currentEvContext.get();
    }
    public static EvTask getCurrentTask() {
        return _currentEvContext.get().task;
    }

    static final ThreadLocal<EvContext> _currentEvContext=new ThreadLocal<EvContext>();
}
