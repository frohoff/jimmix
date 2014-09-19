package jimmix.proxy;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.QueryExp;
import org.jboss.console.remote.RemoteMBeanAttributeInvocation;
import org.jboss.console.remote.RemoteMBeanInvocation;
import org.jboss.console.remote.Util;

import jimmix.util.*;

public class WebConsoleProxy implements Proxy {

	private URL serverURL;

	public WebConsoleProxy(String serverURL) throws MalformedURLException {
		this.serverURL = new URL(serverURL);
	}

	public String invoke(ObjectName name, String actionName, Object[] params, String[] signature) throws Exception {
		String resultToString = "";
		RemoteMBeanInvocation mbeanInvocation = new RemoteMBeanInvocation(name, actionName, params, signature);
		Object result = Util.invoke(this.serverURL, mbeanInvocation);
		if(result != null) {
			resultToString = TypeConvertor.convertObjectToString(result, getObjectClass(result)) + "\n";
		}
		return resultToString;
	}

	public String get(ObjectName name, String attribute) throws Exception {
		String resultToString = "";
		RemoteMBeanAttributeInvocation mbeanInvocation = new RemoteMBeanAttributeInvocation(name, attribute);
		Object result = Util.getAttribute(this.serverURL, mbeanInvocation);
		if(result != null) {
			resultToString = TypeConvertor.convertObjectToString(result, getObjectClass(result)) + "\n";
		}
		return resultToString;
	}

	public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName, Object[] params, String[] signature) throws Exception {
		throw new Exception("WebConsole createMBean not implemented yet.");
	}

	public void unregisterMBean(ObjectName name) throws Exception {
		throw new Exception("WebConsole unregisterMBean not implemented yet.");
	}

	public Set<ObjectName> queryNames(ObjectName name, QueryExp query) throws Exception {
		throw new Exception("WebConsole queryNames not implemented yet.");
	}

	private String getObjectClass(Object object) {
		return object.getClass().toString().split(" ")[1];
	}
}

