package jimmix.action; 

import javax.management.ObjectName;
import javax.management.ObjectInstance;

import jimmix.util.TypeConvertor;
import jimmix.util.Log;
import jimmix.proxy.ProxyType;

public class BruteforceObjectNameAction extends Action {

	private String pattern = null;
	private int notFoundContentLength = 0;
	private String charList = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890/-.:";
	
	public BruteforceObjectNameAction(String url, ProxyType type, String pattern) throws Exception {
		super(url, type);
		// Ex: jboss.classloader:id=JIMMX/server/default/deploy/management/console-mgr.sar*,* 
		this.pattern = pattern;
	}

	public void invoke() throws Exception {
		Log.log("starting bruteforce... (it's really experimental, so be clement :]).");

		// get the "not found" content length
		Log.log("guessing queryNames not found length...");
		this.notFoundContentLength = getNotFoundContentLength();
		Log.log("queryNames not found length is " + this.notFoundContentLength);

		// replace JIMMX with *<char>
		String start = this.pattern;
		String end = "";
		if(pattern.contains("JIMMX")) {
			String jimmxray[] = this.pattern.split("JIMMX");
			start = jimmxray[0];
			if (jimmxray.length > 1) {
				end = jimmxray[1];
			}
			else {
				end = "";
			}
		}
	
		boolean done = false;
		Log.log("bruteforce in progress, please wait...");
		while(!done) {
			// test if start + end = found
		        Log.log("Bruteforcing path => " + start + "*" + end);
			this.proxy.queryNames(new ObjectName(start + "*" + end), null);
			if(Integer.parseInt(System.getProperty("jimmX.response.content_length")) == this.notFoundContentLength) {
				Log.log("No need to bruteforce as it is not a valid path: " + start + "*" + end + " <=> " + this.notFoundContentLength + " against " + Integer.parseInt(System.getProperty("jimmX.response.content_length")) );
				return;
			}
			this.proxy.queryNames(new ObjectName(start+end), null);
			if(Integer.parseInt(System.getProperty("jimmX.response.content_length")) != this.notFoundContentLength) {
				done = true;
			} else {
				// else -> bruteforce the next char from the end to the start
				for(int i = 0; i < charList.length(); i++) {
					char currentChar = charList.charAt(i);
					Log.log("Bruteforcing: " + start + "*" + currentChar + end);
					this.proxy.queryNames(new ObjectName(start+ "*" + currentChar + end), null);
					if(Integer.parseInt(System.getProperty("jimmX.response.content_length")) != this.notFoundContentLength) {
						// we found a char
						end = currentChar + end;
						break;
					}

				}
			}
		}
		Log.log("objectName found: " + start + end);	
	}

	private int getNotFoundContentLength() throws Exception {
		this.proxy.queryNames(new ObjectName("YOUDONOTEXIST:*"), null);
		return Integer.parseInt(System.getProperty("jimmX.response.content_length"));
	}

}
