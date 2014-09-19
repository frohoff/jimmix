package jimmix.action; 

import javax.management.ObjectName;

import jimmix.util.TypeConvertor;
import jimmix.util.Log;
import jimmix.proxy.ProxyType;

public class InvokeAction extends Action {

	private ObjectName mbean = null;
	private String action = null;
	private Object[] params = null;
	private String[] signature = null;
	
	public InvokeAction(String url, ProxyType type, String mbean, String action, Object[] params, String[] signature) throws Exception {
		super(url, type);
		this.mbean = new ObjectName(mbean);
		this.action = action;
		this.params = params;
		this.signature = signature;
	}

	public void invoke() throws Exception {
		Log.log("invoking " + this.action + " on " + this.mbean.getCanonicalName() + "");

		String output = this.proxy.invoke(this.mbean, this.action, this.params, this.signature);

		// raw output
		if(output != null && !output.trim().equals("")) {
			System.out.println("\n" + output + "\n");
		} else {
			System.err.println("return value is empty");
		}
	}
}
