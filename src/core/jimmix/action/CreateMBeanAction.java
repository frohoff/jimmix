package jimmix.action; 

import javax.management.ObjectName;
import javax.management.ObjectInstance;

import jimmix.util.TypeConvertor;
import jimmix.util.Log;
import jimmix.proxy.ProxyType;

public class CreateMBeanAction extends Action {

	private ObjectName mbean = null;
	private String className = null;
	private ObjectName loaderName = null;
	private Object[] params = null;
	private String[] signature = null;
	
	public CreateMBeanAction(String url, ProxyType type, String mbean, String className, String loaderName, Object[] params, String[] signature) throws Exception {
		super(url, type);
		this.mbean = new ObjectName(mbean);
		this.className = className;
		this.loaderName = new ObjectName(loaderName);
		this.params = params;
		this.signature = signature;
	}

	public void invoke() throws Exception {
		Log.log("creating mbean " + this.mbean.getCanonicalName() + " from " + this.className + "");
		ObjectInstance obj = this.proxy.createMBean(className, mbean, loaderName, params, signature);

		if(obj != null && obj.getObjectName().equals(this.mbean)) {
			Log.log("done");
		} else if(obj == null) {
			Log.log("return object is null (was it a HEAD ?)");
		} else {
			Log.error("something is going wrong");
		}
	}
}
