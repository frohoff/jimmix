package jimmix.console;

import jargs.gnu.CmdLineParser;
import java.util.Hashtable;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import org.jboss.console.remote.RemoteMBeanAttributeInvocation;
import org.jboss.console.remote.RemoteMBeanInvocation;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Properties;

import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimplePrincipal;
import org.jboss.console.manager.DeploymentFileRepository;

import jimmix.util.*;
import jimmix.proxy.*;
import jimmix.action.*;

public class CommandLineParser {
	// hashtable where parsed command is located
	private static Hashtable parsedCommand = new Hashtable();

	// internal command parser
    	private static CmdLineParser parser = new CmdLineParser();
    	private static CmdLineParser.Option helpOption;
    	private static CmdLineParser.Option proxyOption;
    	private static CmdLineParser.Option urlOption;
    	private static CmdLineParser.Option userOption;
    	private static CmdLineParser.Option passwordOption;
    	private static CmdLineParser.Option methodOption;
    	private static CmdLineParser.Option hostOption;
    	private static CmdLineParser.Option uaOption;
    	private static CmdLineParser.Option invokeSignatureOption;
    	private static CmdLineParser.Option proxyTypeOption;

	public static Hashtable parseOptions(String args[]) throws Exception {

  	    	helpOption = parser.addBooleanOption('h', "help");
    	    	proxyOption = parser.addStringOption('P', "proxy");
    	    	urlOption = parser.addStringOption('i', "invoker");
    	    	userOption = parser.addStringOption('u', "user");
    	    	passwordOption = parser.addStringOption('p', "password");
    	    	methodOption = parser.addStringOption('m', "method");
    	    	hostOption = parser.addStringOption('H', "host");
    	    	uaOption = parser.addStringOption('U', "ua");
    	    	invokeSignatureOption = parser.addStringOption('s', "signature");
    	    	proxyTypeOption = parser.addStringOption('t', "type");

    	    	try {
			parser.parse(args);
    	    	} catch(CmdLineParser.OptionException e) {
			printParsingError(e.getMessage());
    	    	}

    	    	// help
    	    	Boolean help = (Boolean)parser.getOptionValue(helpOption, Boolean.valueOf(false));
    	    	if(help.booleanValue()) {
			printUsage();
			System.exit(1);
    	    	}

    	    	// setting the proxy
    	    	String proxy = (String)parser.getOptionValue(proxyOption, null);
    	    	if(proxy != null) {
			Pattern pattern = Pattern.compile("^([^:\\/\\s]+):([0-9]+)$", Pattern.CASE_INSENSITIVE);
    	    	  	Matcher matcher = pattern.matcher(proxy);
    	    	  	if(matcher.matches()) {
    	    	  	  	String proxyHost = matcher.group(1);
    	    	  	  	String proxyPort = matcher.group(2);

    	    	  	  	System.setProperty("http.proxyHost", proxyHost);
    	    	  	  	System.setProperty("http.proxyPort", proxyPort);
    	    	  	  	System.setProperty("https.proxyHost", proxyHost);
    	    	  	  	System.setProperty("https.proxyPort", proxyPort);
    	    	  	} else {
				printParsingError("Proxy is not correctly defined (ex: host:port).");
    	    	  	}
    	    	}

    	    	// setting the remote url
    	    	String url = (String)parser.getOptionValue(urlOption, "http://localhost:8080/invoker/JMXInvokerServlet");
    	    	parsedCommand.put("url", url);
    	   
    	    	// configure user / password 
    	    	String user = (String)parser.getOptionValue(userOption, null);
    	    	String password = (String)parser.getOptionValue(passwordOption, null);
    	    	if(user != null && password != null) {
			SecurityAssociation.setPrincipal(new SimplePrincipal(user));
    	    	  	SecurityAssociation.setCredential(password);
    	    	}

    	    	// setting the http method
    	    	System.setProperty("jimmX.request.method", (String)parser.getOptionValue(methodOption, "POST"));

		// setting the proxy type
		String typestr = (String) parser.getOptionValue(proxyTypeOption, "jmxinvokerservlet");
		ProxyType type = null;
		if(typestr.equals("jmxinvokerservlet")) {
			type = ProxyType.JMX_INVOKER_SERVLET;
		} else if(typestr.equals("webconsole")) { 
			type = ProxyType.WEB_CONSOLE;
		} else {
			printParsingError("Invalid proxy type \"" + type + "\". Expecting \"webconsole\" or \"jmxconsole\""); 
		}

    	    	// setting the host
		String host = (String)parser.getOptionValue(hostOption, null);
		if(host != null) {
    	    		System.setProperty("jimmX.request.host", host);
		}

		// setting the useragent
		String ua = (String)parser.getOptionValue(uaOption, "Mozilla/5.0");
		if(ua != null) {
    	    		System.setProperty("jimmX.request.user_agent", ua);
		}

		// create the action
		createAction(type);

    	    	// return the command hash table
    	    	return parsedCommand;
    	}

	private static void createAction(ProxyType type) throws Exception {
		String[] args = parser.getRemainingArgs();
        	if( args.length < 2) {
			printParsingError("Too few arguments.");
        	}   

		String action = args[0];
		String mbean = args[1];
		
		// invoke
		if(action.equals("invoke")) {
			if(args.length < 3) {
				printParsingError("Too few arguments.");
            		}   
            		String invokeMethod = args[2];
            		String invokeSignature = (String)parser.getOptionValue(invokeSignatureOption, null);
    
            		Object[] invokeParamsArray = null;
            		String[] invokeSignatureArray = null;

            		if(args.length > 3) {
				String[] invokeParamsStringArray = new String[args.length-3];
            		  	System.arraycopy(args, 3, invokeParamsStringArray, 0, invokeParamsStringArray.length);

            		  	// Default signature is java.lang.String
            		  	if (invokeSignature == null) {
					invokeSignatureArray = new String[invokeParamsStringArray.length];
            		  	    	for (int i=0; i<invokeParamsStringArray.length; i++) {
            		  	    	  invokeSignatureArray[i] = "java.lang.String";
            		  	    	}   
            		  	} else {
					invokeSignatureArray = invokeSignature.split(",");
            		  	}   

            		  	if (invokeSignatureArray.length != invokeParamsStringArray.length) {
					System.err.println("Mismatch between parameters and signature.");
            		  	    	printUsage();
            		  	    	System.exit(2);
            		  	}

				// Object conversion
				invokeParamsArray = TypeConvertor.convertObjectsFromString(invokeParamsStringArray, invokeSignatureArray);
			}   
			parsedCommand.put("action", new InvokeAction((String) parsedCommand.get("url"), type, mbean, invokeMethod, invokeParamsArray, invokeSignatureArray));

		// get
		} else if(action.equals("get")) {
			// check arguments
			if(args.length != 3) {
				printParsingError("Invalid arguments.");
			}
			parsedCommand.put("action", new GetAction((String) parsedCommand.get("url"), type, mbean, args[2]));	
	
		// createMBean
		} else if(action.equals("createMBean")) {
			if(args.length < 4) {
                                printParsingError("Too few arguments.");
                        }   
                        String className = args[2];
			String loaderName = args[3];
                        String signatureRaw = (String)parser.getOptionValue(invokeSignatureOption, null);
    
                        Object[] params = null;
                        String[] signature = null;

                        if(args.length > 4) {
                                String[] paramsRaw = new String[args.length-3];
                                System.arraycopy(args, 3, paramsRaw, 0, paramsRaw.length);

                                // Default signature is java.lang.String
                                if (signatureRaw == null) {
                                        signature = new String[paramsRaw.length];
                                        for (int i=0; i<paramsRaw.length; i++) {
                                          signature[i] = "java.lang.String";
                                        }   
                                } else {
                                        signature = signatureRaw.split(",");
                                }   

                                if (signature.length != paramsRaw.length) {
                                        System.err.println("Mismatch between parameters and signature.");
                                        printUsage();
                                        System.exit(2);
                                }   

                                // Object conversion
                                params = TypeConvertor.convertObjectsFromString(paramsRaw, signature);
                        } 
			parsedCommand.put("action", new CreateMBeanAction((String) parsedCommand.get("url"), type, mbean, className, loaderName, params, signature));

		} else if(action.equals("unregisterMBean")) {
			parsedCommand.put("action", new UnregisterMBeanAction((String) parsedCommand.get("url"), type, mbean));

		// queryNames
		} else if(action.equals("queryNames")) {
			String query = null;
			if(args.length > 2) {
				query = args[2];
			}
			parsedCommand.put("action", new QueryNamesAction((String) parsedCommand.get("url"), type, mbean, query));

		// bfobjectname
		} else if(action.equals("bfobjectname")) {
			parsedCommand.put("action", new BruteforceObjectNameAction((String) parsedCommand.get("url"), type, mbean));

		// no match
		} else {
			printParsingError(action + " is not a valid action.");
		}
	}

    	private static void printParsingError(String msg) {
		System.err.println(msg);
    	    	printUsage();
    	    	System.exit(2);
    	}

    	private static void printUsage() {
		System.err.println("\nUsage: jimmix.sh [options] <operation> <mbean> <params>\n" +
    	    		    "\t-m, --method method\t\tHTTP method\n" +
    	    		    "\t-t, --type type\t\t\tProxy type (default is jmxinvokerservlet)\n" +
    	    		    "\t-P, --proxy proxy\t\tHTTP(S) proxy server.\n" +
    	    		    "\t-H, --host host\t\t\tHost header\n" +
    	    		    "\t-U, --ua ua\t\t\tUser-Agent header (default is Mozilla/5.0)\n" +
    	    		    "\t-i, --invoker url\t\tThe JMX Invoker Servlet URL.\n" +
    	    		    "\t-u, --user user\t\t\tAuthentication username.\n" +
    	    		    "\t-p, --password password\t\tAuthentication password.\n" +
    	    		    "\t-s, --signature signature\tThe invocation signature, comma separated (default is java.lang.String).\n");
    	}
}
