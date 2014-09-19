package jimmix;

import javax.management.*;
import java.io.*;
import javax.naming.*;
import javax.servlet.*;
import java.util.*;

import org.jboss.console.remote.RemoteMBeanAttributeInvocation;
import org.jboss.console.remote.RemoteMBeanInvocation;
import jimmix.console.CommandLineParser;
import jimmix.proxy.ProxyFactory;
import jimmix.proxy.Proxy;
import jimmix.action.Action;


public class Run {

	public static void main(String args[]) {
		try{
			// To ensure we can modify the Host header
			System.setProperty("sun.net.http.allowRestrictedHeaders", "true");

			Hashtable command = CommandLineParser.parseOptions(args);
			((Action) command.get("action")).invoke();
  	  	} catch (Exception e) {
			e.printStackTrace();
  	  	}
  	}
}
