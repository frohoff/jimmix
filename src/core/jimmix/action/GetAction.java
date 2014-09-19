package jimmix.action; 

import javax.management.ObjectName;

import jimmix.util.TypeConvertor;
import jimmix.util.Log;
import jimmix.proxy.ProxyType;

public class GetAction extends Action {

	private ObjectName mbean = null;
	private String attribute = null;
	
	public GetAction(String url, ProxyType type, String mbean, String attribute) throws Exception {
		super(url, type);
		this.mbean = new ObjectName(mbean);
		this.attribute = attribute;
	}

	public void invoke() throws Exception {
		Log.log("getting the property " + this.attribute+ " from " + this.mbean.getCanonicalName() + "");

		String value = this.proxy.get(this.mbean, this.attribute);
		
		// raw output if not null
		if(value != null && !value.trim().equals("")) {
			System.out.println("\n" + value + "\n");
		} else {
			Log.error("return value is empty (was it a HEAD ?)");
		}
	}
}
