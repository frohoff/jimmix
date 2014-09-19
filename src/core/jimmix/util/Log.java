package jimmix.util;

public class Log {

	public static void log(String msg) {
		System.out.println("[+] " + msg);
	}

	public static void error(String err) {
		System.err.println("[-] " + err);
	}
}
