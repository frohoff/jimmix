package jimmix.action; 

import java.util.Set;
import java.util.Iterator;
import javax.management.ObjectName;
import javax.management.ObjectInstance;
import javax.management.QueryExp;

import jimmix.util.TypeConvertor;
import jimmix.util.Log;
import jimmix.proxy.ProxyType;

public class QueryNamesAction extends Action {

	private ObjectName mbean = null;
	private QueryExp query = null;
	
	public QueryNamesAction(String url, ProxyType type, String mbean, String query) throws Exception {
		super(url, type);
		this.mbean = new ObjectName(mbean);
		if(query != null) {
			this.query = new ObjectName(query);
		}
	}

	public void invoke() throws Exception {
		Log.log("Trying to find " + this.mbean.getCanonicalName() + "");
		Set<ObjectName> result = this.proxy.queryNames(mbean, query);

		if(result != null) {
			System.out.println();
			Iterator<ObjectName> it = result.iterator();
			while(it.hasNext()) {
				ObjectName objname = it.next();
				System.out.println(objname.getCanonicalName());
			}
		} else {
			Log.error("No match found (Content-Length: " + Integer.parseInt(System.getProperty("jimmX.response.content_length"))  + ")");
		}
	}
}
