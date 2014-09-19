package jimmix.util;

import java.lang.*;
import java.net.*;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;

public class TypeConvertor {

	public static String getObjectClass(Object object) {
		return object.getClass().toString().split(" ")[1];
    	}

    	public static Object convertObjectFromString(String object, String type) throws Exception {
		Object convertedObject = null;
    	
    	    	if(type.equals("java.lang.String")) {
			convertedObject = createStringFromString(object);
    	    	} 
 
    	    	if(type.equals("javax.management.ObjectName")) {
			convertedObject = createObjectNameFromString(object);
    	    	}   

    	    	if(type.equals("java.net.URL")) {
			convertedObject = createURLFromString(object);
    	    	}   

    	    	if(type.equals("java.lang.Integer")) {
			convertedObject = createIntegerFromString(object);
    	    	}  

    	    	if(type.equals("boolean")) {
			convertedObject = createBooleanFromString(object); 
    	    	}
    	    	  
    	    	return convertedObject;
    	}

    	public static String convertObjectToString(Object object, String type) throws Exception {
		String result = null;
    	
    	    	if(type.equals("java.lang.String")) {
			result = ((String) object).toString();
    	    	}
   
    	    	if(type.equals("javax.management.ObjectName")) {
			result = ((ObjectName) object).toString();
    	    	}   

    	    	if(type.equals("java.net.URL")) {
			result = ((URL) object).toString();
    	    	}   

    	    	if(type.equals("java.lang.Integer")) {
			result = ((Integer) object).toString();
    	    	}  
 
    	    	if(type.equals("[Ljava.lang.String;")) {
			result = "[";
    	    	    	String[] tmpObject = (String[]) object;
    	    	    	for (int i = 0; i<tmpObject.length-1; i++) {
				result += tmpObject[i] + ", ";
    	    	    	}
    	    	    	result += tmpObject[tmpObject.length-1];
    	    	    	result += "]";
    	    	}

    	    	// if result is null
    	    	if(result == null) {
			result = object.toString();
    	    	}
    	    	
    	    	return result;
    	}

    	public static Object[] convertObjectsFromString(String[] objects, String[] types) throws Exception {
		Object[] convertedObjects = new Object[objects.length];

    	    	for (int i = 0; i<objects.length; i++) {
			convertedObjects[i] = convertObjectFromString(objects[i], (String)types[i]);
    	    	}

    	    	return convertedObjects;
    	}
    	
    	
    	private static Object createStringFromString(String string) {
		return (Object) (new String(string));
    	}

    	private static Object createObjectNameFromString(String string) throws MalformedObjectNameException {
		return (Object) (new ObjectName(string));
    	}

    	private static Object createURLFromString(String string) throws MalformedURLException {
		return (Object) (new URL(string));
    	}

    	private static Object createIntegerFromString(String string) throws NumberFormatException {
		return (Object) (new Integer(string));
    	}

    	private static Object createBooleanFromString(String string) {
		return (Object) (new Boolean(string));
    	}
}
