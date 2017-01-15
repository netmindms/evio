package com.devkkh.evio;

import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;

/**
 * Created by netmind on 12. 7. 19.
 */
public class EvSSLSocket extends  EvSocket {
    private SSLEngine _sslEngine;
    public int openClient(SSLContext ctx) {
        try {
            _sslEngine = SSLContext.getDefault().createSSLEngine();
            _sslEngine.setUseClientMode(true);
            _sslEngine.beginHandshake();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return -1;
        } catch (SSLException e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }
}
