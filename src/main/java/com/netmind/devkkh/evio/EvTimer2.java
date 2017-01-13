package com.netmind.devkkh.evio;

import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by netmind on 12. 7. 15.
 */
public class EvTimer2 extends EvEvent implements  EvPipe.Listener {

    private Timer _timer;
    private LocalTimerTask _tmtask;

    private EvPipe _cbpipe;
    private Listener _lis;
    private ByteBuffer _cntBf;
    private ByteBuffer _notiRcvBf;
    private static final String tag = "EvTimer2";

    public EvTimer2() {
        super();

        _tmtask = new LocalTimerTask();
        _cntBf = ByteBuffer.allocate(1);
        _cntBf.put((byte)1);


        _notiRcvBf = ByteBuffer.allocate(1);

    }

    public void set(long firstmsec, long periodmsec, Listener lis) {
        if(_cbpipe == null) {
            _cbpipe = new EvPipe();
            _cbpipe.open(this);
        }
        if(_timer==null) {
            _timer = new Timer();
        }
        _lis = lis;
        _timer.scheduleAtFixedRate(_tmtask, firstmsec, periodmsec);

    }

    public void set(long period, Listener lis) {
        set(period, period, lis);
    }

    public void kill() {
        _timer.cancel();
        _timer.purge();
        _cbpipe.close();
        _timer = null;
        _cbpipe = null;
    }

    @Override
    public void OnPipeEvent(EvPipe pipeobj, int event) {
        if(event == EvEvent.EVT_READ) {
            _notiRcvBf.clear();
            _cbpipe.recv(_notiRcvBf);
            if(_lis != null) {
                _lis.OnTimer(this, 1);
            }
        } else {
            _cbpipe.close();
        }

    }

    public interface Listener {
        void OnTimer(EvTimer2 timer, int cnt);
    }

    private class LocalTimerTask extends TimerTask {

        @Override
        public void run() {
            dlog.v(tag, "timer expired, tid="+Thread.currentThread().getId());
            _cntBf.rewind();
            _cbpipe.send(_cntBf);
        }
    }
}
