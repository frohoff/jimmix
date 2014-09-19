package org.jboss.console.remote;

import java.net.HttpURLConnection;
import java.io.EOFException;

/* PATCH: import custom connection classes */
import patch.http.LaxHttpURLConnection;
import patch.https.LaxHttpsURLConnection;

// patch the org.jboss.console.remote.Util class
// to allow all http methods 
public class Util {
	private static String REQUEST_CONTENT_TYPE = "application/x-java-serialized-object; class=org.jboss.console.remote.RemoteMBeanInvocation";
	private static org.jboss.logging.Logger log = org.jboss.logging.Logger.getLogger(Util.class);

	/** Post the Invocation as a serialized MarshalledInvocation object. This is
	  using the URL class for now but this should be improved to a cluster aware
	  layer with full usage of HTTP 1.1 features, pooling, etc.
	 */
	public static Object invoke (java.net.URL externalURL, RemoteMBeanInvocation mi) throws Exception {
		HttpURLConnection conn = null;
		
		// http or https / proxy ?
		if(externalURL.getProtocol().equals("https")) {
			String proxyHost = System.getProperty("https.proxyHost");
			String proxyPort = System.getProperty("https.proxyPort");

			if(proxyHost != null && proxyPort != null) {
				conn = new LaxHttpsURLConnection(externalURL, proxyHost, Integer.parseInt(proxyPort), null);
			} else {
				conn = new LaxHttpsURLConnection(externalURL, null);
			}
		} else {
			String proxyHost = System.getProperty("https.proxyHost");
			String proxyPort = System.getProperty("https.proxyPort");

			if(proxyHost != null && proxyPort != null) {
				conn = new LaxHttpURLConnection(externalURL, proxyHost, Integer.parseInt(proxyPort));
			} else {
				conn = new LaxHttpURLConnection(externalURL);
			}
		}

		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setRequestProperty("ContentType", REQUEST_CONTENT_TYPE);
		
		String method = System.getProperty("jimmX.request.method");
		if(method != null) {
			conn.setRequestMethod(method);
		} else {
			conn.setRequestMethod("POST");
		}

		java.io.OutputStream os = conn.getOutputStream();
		java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(os);
		oos.writeObject(mi);
		oos.flush();

		// Get the response MarshalledValue object
		/* PATCH: try catch on EOFException for HEAD method */
		Object value = null;
		try {
			java.io.InputStream is = conn.getInputStream();
			java.io.ObjectInputStream ois = new java.io.ObjectInputStream(is);
			org.jboss.invocation.MarshalledValue mv = (org.jboss.invocation.MarshalledValue) ois.readObject();
			ois.close();
			oos.close();
	
			// If the encoded value is an exception throw it
			value = mv.get();
	
			if( value instanceof org.jboss.invocation.InvocationException )
				throw (Exception) (((org.jboss.invocation.InvocationException)value).getTargetException ());
	
			if( value instanceof Exception )
				throw (Exception) value;
		} catch(EOFException eof) {
			// Ignore EOF exception -> so baaaad!
		}

		// hugly hack to leak response info
		System.setProperty("jimmX.response.content_length", ""+conn.getContentLength());
		oos.close();
	
		return value;
	}

	public static Object getAttribute (java.net.URL externalURL, RemoteMBeanAttributeInvocation mi) throws Exception {
                HttpURLConnection conn = null;

                // http or https / proxy ?
                if(externalURL.getProtocol().equals("https")) {
                        String proxyHost = System.getProperty("https.proxyHost");
                        String proxyPort = System.getProperty("https.proxyPort");

                        if(proxyHost != null && proxyPort != null) {
                                conn = new LaxHttpsURLConnection(externalURL, proxyHost, Integer.parseInt(proxyPort), null);
                        } else {
                                conn = new LaxHttpsURLConnection(externalURL, null);
                        }
                } else {
                        String proxyHost = System.getProperty("https.proxyHost");
                        String proxyPort = System.getProperty("https.proxyPort");

                        if(proxyHost != null && proxyPort != null) {
                                conn = new LaxHttpURLConnection(externalURL, proxyHost, Integer.parseInt(proxyPort));
                        } else {
                                conn = new LaxHttpURLConnection(externalURL);
                        }
                }

                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestProperty("ContentType", REQUEST_CONTENT_TYPE);

                String method = System.getProperty("jimmX.request.method");
                if(method != null) {
                        conn.setRequestMethod(method);
                } else {
                        conn.setRequestMethod("POST");
                }

                java.io.OutputStream os = conn.getOutputStream();
                java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(os);
                oos.writeObject(mi);
                oos.flush();

                // Get the response MarshalledValue object
                /* PATCH: try catch on EOFException for HEAD method */
                Object value = null;
		try {
                        java.io.InputStream is = conn.getInputStream();
                        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(is);
                        org.jboss.invocation.MarshalledValue mv = (org.jboss.invocation.MarshalledValue) ois.readObject();
                        ois.close();
                        oos.close();

                        // If the encoded value is an exception throw it
                        value = mv.get();

                        if( value instanceof org.jboss.invocation.InvocationException )
                                throw (Exception) (((org.jboss.invocation.InvocationException)value).getTargetException ());

                        if( value instanceof Exception )
                                throw (Exception) value;
                } catch(EOFException eof) {
                        // Ignore EOF exception -> so baaaad!
                }

                // hugly hack to leak response info
                System.setProperty("jimmX.response.content_length", ""+conn.getContentLength());
                oos.close();

                return value;
	}
}
