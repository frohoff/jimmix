package jimmix.proxy;

import javax.management.ObjectName;
import org.jboss.invocation.http.interfaces.HttpInvokerProxy;
import org.jboss.invocation.Invoker;
import org.jboss.jmx.adaptor.rmi.RMIAdaptorExt;
import org.jboss.proxy.GenericProxyFactory;
import javax.management.MBeanServerConnection;
import java.util.ArrayList;

import org.jboss.console.remote.RemoteMBeanAttributeInvocation;
import org.jboss.console.remote.RemoteMBeanInvocation;
import jimmix.console.CommandLineParser;
import jimmix.proxy.ProxyFactory;
import jimmix.proxy.ProxyType;
import jimmix.proxy.Proxy;

public class ProxyFactory {

	public static Proxy createProxy(String url, ProxyType type) throws Exception {
		Proxy proxy = null;
		switch(type) {
			case WEB_CONSOLE:
				proxy = new WebConsoleProxy(url);
				break;
			case JMX_INVOKER_SERVLET:
				proxy = new JMXInvokerServletProxy(url);
				break;
			default:
				throw new Exception("Proxy type not found.");
		}
		return proxy;
  	}
}
