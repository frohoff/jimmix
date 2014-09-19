package jimmix.proxy;

import java.util.Set;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.QueryExp;
import org.jboss.console.remote.RemoteMBeanAttributeInvocation;
import org.jboss.console.remote.RemoteMBeanInvocation;

public interface Proxy {

  	public String invoke(ObjectName name, String actionName, Object[] params, String[] signature) throws Exception;
  	public String get(ObjectName name, String attribute) throws Exception;
  	public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName, Object[] params, String[] signature) throws Exception;
	public void unregisterMBean(ObjectName name) throws Exception;
	public Set<ObjectName> queryNames(ObjectName name, QueryExp query) throws Exception;

}
