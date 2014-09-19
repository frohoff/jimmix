/*
* JBoss, Home of Professional Open Source
* Copyright 2005, JBoss Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/

package org.jboss.invocation.http.interfaces;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.OutputStream;
import java.io.ObjectOutputStream;
import java.io.EOFException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.PrivilegedAction;
import java.security.AccessController;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationException;
import org.jboss.invocation.MarshalledValue;
import org.jboss.logging.Logger;
import org.jboss.security.SecurityAssociationAuthenticator;
import org.jboss.net.ssl.SSLSocketFactoryBuilder;

/* PATCH: import custom connection classes */
import patch.http.LaxHttpURLConnection;
import patch.https.LaxHttpsURLConnection;

/** Common client utility methods
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 44276 $
*/
public class Util
{
   /** A property to override the default https url host verification */
   public static final String IGNORE_HTTPS_HOST = "org.jboss.security.ignoreHttpsHost";
   /** A property to install the https connection ssl socket factory */
   public static final String SSL_FACTORY_BUILDER = "org.jboss.security.httpInvoker.sslSocketFactoryBuilder";
   /**
    * A serialized MarshalledInvocation
    */
   private static String REQUEST_CONTENT_TYPE =
      "application/x-java-serialized-object; class=org.jboss.invocation.MarshalledInvocation";
   private static Logger log = Logger.getLogger(Util.class);
   /** A custom SSLSocketFactory builder to use for https connections */
   private static SSLSocketFactoryBuilder sslSocketFactoryBuilder;

   static class SetAuthenticator implements PrivilegedAction
   {
      public Object run()
      {
         Authenticator.setDefault(new SecurityAssociationAuthenticator());
         return null;
      }
      
   }
   static class ReadSSLBuilder implements PrivilegedAction
   {
      public Object run()
      {
         String value = System.getProperty(SSL_FACTORY_BUILDER);
         return value;
      }
   }

   static
   {
      // Install the java.net.Authenticator to use
      try
      {
         SetAuthenticator action = new SetAuthenticator();
         AccessController.doPrivileged(action);
      }
      catch(Exception e)
      {
         log.warn("Failed to install SecurityAssociationAuthenticator", e);
      }
      ClassLoader loader = Thread.currentThread().getContextClassLoader();

      String factoryFactoryFQCN = null;
      try
      {
         ReadSSLBuilder action = new ReadSSLBuilder();
         factoryFactoryFQCN = (String) AccessController.doPrivileged(action);
      }
      catch(Exception e)
      {
         log.warn("Failed to read "+SSL_FACTORY_BUILDER, e);         
      }

      if (factoryFactoryFQCN != null)
      {
         try
         {
            Class clazz = loader.loadClass(factoryFactoryFQCN);
            sslSocketFactoryBuilder = (SSLSocketFactoryBuilder) clazz.newInstance();
   }
         catch (Exception e)
         {
            log.warn("Could not instantiate SSLSocketFactoryFactory", e);
         }
      }
   }

   /** Install the SecurityAssociationAuthenticator as the default
    * java.net.Authenticator
    */
   public static void init()
   {
      try
      {
         SetAuthenticator action = new SetAuthenticator();
         AccessController.doPrivileged(action);
      }
      catch(Exception e)
      {
         log.warn("Failed to install SecurityAssociationAuthenticator", e);
      }
   }

   /** Post the Invocation as a serialized MarshalledInvocation object. This is
    using the URL class for now but this should be improved to a cluster aware
    layer with full usage of HTTP 1.1 features, pooling, etc.
   */
   public static Object invoke(URL externalURL, Invocation mi)
      throws Exception
   {
      if( log.isTraceEnabled() )
         log.trace("invoke, externalURL="+externalURL);
      /* Post the MarshalledInvocation data. This is using the URL class
       for now but this should be improved to a cluster aware layer with
       full usage of HTTP 1.1 features, pooling, etc.
       */

      /* PATCH: instanciate a custom connection */
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
      conn.setRequestMethod("POST");
      // @todo this should be configurable
      conn.setRequestProperty("Accept-Encoding", "x-gzip,x-deflate,gzip,deflate");

      /* PATCH: check http method in system properties */
      String method = System.getProperty("jimmX.request.method");
      if(method != null) {
	 conn.setRequestMethod(method);
      }

      /* PATCH: check hostname in system properties */
      String host = System.getProperty("jimmX.request.host");
      if(host != null && !host.trim().equals("")) {
	  conn.setRequestProperty("Host", host);
      }

      /* PATCH: check user agent in system properties */
      String ua = System.getProperty("jimmX.request.user_agent");
      if(ua != null && !ua.trim().equals("")) {
	  conn.setRequestProperty("User-Agent", ua);
      }

      /* PATCH: call a patched getOutputStream method */
      OutputStream os =  conn.getOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(os);
      try
      {
         oos.writeObject(mi);
         oos.flush();
      }
      catch (ObjectStreamException e)
      {
         // This generally represents a programming/deployment error,
         // not a communication problem
         throw new InvocationException(e);
      }

      Object value = null;

      // Get the response MarshalledValue object
      InputStream is = conn.getInputStream();

      /* PATCH: try catch on EOFException for HEAD method */
      try {
        // Check the headers for gzip Content-Encoding
        String encoding = conn.getHeaderField("Content-Encoding");
        if(encoding != null && encoding.indexOf("gzip") >= 0)
           is = new GZIPInputStream(is);

        ObjectInputStream ois = new ObjectInputStream(is);
        MarshalledValue mv = (MarshalledValue) ois.readObject();
        value = mv.get();
        
        // A hack for jsse connection pooling (see patch ).
        ois.read();
        ois.close();
      } catch(EOFException eof) {
        // Ignore EOF exception -> so baaaad!
      }
 
      // hugly hack to leak response info
      System.setProperty("jimmX.response.content_length", ""+conn.getContentLength());

      oos.close();

      // If the encoded value is an exception throw it
      if(value instanceof Exception)
      {
         throw (Exception) value;
      }

      return value;
   }

   /** Given an Https URL connection check the org.jboss.security.ignoreHttpsHost
    * system property and if true, install the AnyhostVerifier as the 
    * com.sun.net.ssl.HostnameVerifier or javax.net.ssl.HostnameVerifier
    * depending on the version of JSSE seen. If HttpURLConnection is not a
    * HttpsURLConnection then nothing is done.
    *  
    * @param conn a HttpsURLConnection
    */ 
   public static void configureHttpsHostVerifier(HttpURLConnection conn)
   {
      if (conn instanceof HttpsURLConnection )
      {
         // See if the org.jboss.security.ignoreHttpsHost property is set
         if (Boolean.getBoolean(IGNORE_HTTPS_HOST) == true)
         {
            AnyhostVerifier.setHostnameVerifier(conn);
         }
      }
   }

   /** Override the SSLSocketFactory used by the HttpsURLConnection. This method
    * will invoke setSSLSocketFactory on any HttpsURLConnection if there was
    * a SSLSocketFactoryBuilder implementation specified via the
    * org.jboss.security.httpInvoker.sslSocketFactoryBuilder system property.
    * 
    * @param conn possibly a HttpsURLConnection
    * @throws InvocationTargetException thrown on failure to invoke setSSLSocketFactory
   */
   public static void configureSSLSocketFactory(HttpURLConnection conn)
      throws InvocationTargetException
   {
      Class connClass = conn.getClass();
      if ( conn instanceof HttpsURLConnection && sslSocketFactoryBuilder != null)
      {
         try
         {
            SSLSocketFactory socketFactory = sslSocketFactoryBuilder.getSocketFactory();
            Class[] sig = {SSLSocketFactory.class};
            Method method = connClass.getMethod("setSSLSocketFactory", sig);
            Object[] args = {socketFactory};
            method.invoke(conn, args);
            log.trace("Socket factory set on connection");
         }
         catch(Exception e)
         {
            throw new InvocationTargetException(e);
         }
      }
   }

   /**
    * First try to use the externalURLValue as a URL string and if this
    * fails to produce a valid URL treat the externalURLValue as a system
    * property name from which to obtain the URL string. This allows the
    * proxy url to not be set until the proxy is unmarshalled in the client
    * vm, and is necessary when the server is sitting behind a firewall or
    * proxy and does not know what its public http interface is named.
    */
   public static URL resolveURL(String urlValue) throws MalformedURLException
   {
      if( urlValue == null )
         return null;

      URL externalURL = null;
      try
      {
         externalURL = new URL(urlValue);
      }
      catch(MalformedURLException e)
      {
         // See if externalURL refers to a property
         String urlProperty = System.getProperty(urlValue);
         if( urlProperty == null )
            throw e;
         externalURL = new URL(urlProperty);
      }
      return externalURL;
   }
}
