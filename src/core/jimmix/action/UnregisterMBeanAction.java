package jimmix.action; 

import org.jboss.console.remote.RemoteMBeanInvocation;
import javax.management.ObjectName;
import javax.management.ObjectInstance;

import jimmix.util.TypeConvertor;
import jimmix.util.Log;
import jimmix.proxy.ProxyType;

public class UnregisterMBeanAction extends Action {

	private ObjectName mbean = null;
	
	public UnregisterMBeanAction(String url, ProxyType type, String mbean) throws Exception {
		super(url, type);
		this.mbean = new ObjectName(mbean);
	}

	public void invoke() throws Exception {
		Log.log("unregister " + this.mbean.getCanonicalName() + "");
		this.proxy.unregisterMBean(this.mbean);
		Log.log("done");
	}
}
