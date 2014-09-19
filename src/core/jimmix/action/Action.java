package jimmix.action;

import jimmix.proxy.*; 

public abstract class Action {

	protected Proxy proxy = null;

	public Action(String url, ProxyType type) throws Exception {
		proxy = ProxyFactory.createProxy(url, type);
	}
	
	public abstract void invoke() throws Exception;
}
