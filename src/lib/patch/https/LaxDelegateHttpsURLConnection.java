/*
 * Copyright 2001-2005 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package patch.https;

import java.net.URL;
import java.net.Proxy;
import java.io.IOException;
import java.io.OutputStream;

import sun.net.www.protocol.https.AbstractDelegateHttpsURLConnection;
import sun.net.www.protocol.http.Handler;
import javax.net.ssl.*;
import java.security.cert.X509Certificate;

/**
 * This class was introduced to provide an additional level of
 * abstraction between javax.net.ssl.HttpURLConnection and
 * com.sun.net.ssl.HttpURLConnection objects. <p>
 *
 * javax.net.ssl.HttpURLConnection is used in the new sun.net version
 * of protocol implementation (this one)
 * com.sun.net.ssl.HttpURLConnection is used in the com.sun version.
 *
 */
public class LaxDelegateHttpsURLConnection extends AbstractDelegateHttpsURLConnection {

	TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
        	public void checkClientTrusted( final X509Certificate[] chain, final String authType) {
        	}
        	public void checkServerTrusted( final X509Certificate[] chain, final String authType) {
        	}
        	public X509Certificate[] getAcceptedIssuers() {
        	    return null;
        	}
	}};
    
    // we need a reference to the HttpsURLConnection to get
    // the properties set there
    // we also need it to be public so that it can be referenced
    // from sun.net.www.protocol.http.HttpURLConnection
    // this is for ResponseCache.put(URI, URLConnection)
    // second parameter needs to be cast to javax.net.ssl.HttpsURLConnection
    // instead of AbstractDelegateHttpsURLConnection
    public javax.net.ssl.HttpsURLConnection httpsURLConnection;

    public LaxDelegateHttpsURLConnection(URL url,
            sun.net.www.protocol.http.Handler handler,
            javax.net.ssl.HttpsURLConnection httpsURLConnection)
            throws Exception {
        this(url, null, handler, httpsURLConnection);
    }

    public LaxDelegateHttpsURLConnection(URL url, Proxy p,
            sun.net.www.protocol.http.Handler handler,
            javax.net.ssl.HttpsURLConnection httpsURLConnection)
            throws Exception {
        super(url, p, handler);
	this.httpsURLConnection = httpsURLConnection;

	// Install the all-trusting trust manager
    	final SSLContext sslContext = SSLContext.getInstance( "SSL" );
    	sslContext.init( null, trustAllCerts, new java.security.SecureRandom() );
    	// Create an ssl socket factory with our all-trusting manager
    	final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
    	
    	// Tell the url connection object to use our socket factory which bypasses security checks
    	this.httpsURLConnection.setSSLSocketFactory(sslSocketFactory);

	this.httpsURLConnection.setHostnameVerifier(new HostnameVerifier() {
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	});
    }

    protected javax.net.ssl.SSLSocketFactory getSSLSocketFactory() {
        return httpsURLConnection.getSSLSocketFactory();
    }

    protected javax.net.ssl.HostnameVerifier getHostnameVerifier() {
        return httpsURLConnection.getHostnameVerifier();
    }

    /*
     * Called by layered delegator's finalize() method to handle closing
     * the underlying object.
     */
    protected void dispose() throws Throwable {
        super.finalize();
    }

    public synchronized OutputStream getOutputStream() throws IOException {
        String old_method = this.method;
        // just to bypass some checks, we set the method to POST 
        // -> allows content body in HEAD requests for example :)
        this.method = "POST";
        OutputStream os = super.getOutputStream();
	this.method = old_method;
	return os; 
   }  
}
