package patch.http;

import sun.net.www.protocol.http.*;
import sun.net.www.http.*;
import sun.net.*;
import sun.net.www.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.net.*;

public class LaxHttpURLConnection extends sun.net.www.protocol.http.HttpURLConnection {

	public LaxHttpURLConnection(URL u, String host, int port) {
		super(u, new Proxy(Proxy.Type.HTTP, InetSocketAddress.createUnresolved(host, port)));
    	}

    	public LaxHttpURLConnection(URL u, Proxy p) {
		super(u, p, new Handler());
    	}

    	public LaxHttpURLConnection(URL u) {
		super(u, null, new Handler());
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
