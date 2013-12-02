package dbcad;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import org.json.JSONObject;

/**
* Set of common utilities.
* @author faheem
*
*/
public class Utils {

    /**
    * Read a properties file from the classpath and return a Properties object
    * @param filename
    * @return
    * @throws IOException
    */
    static public Properties readProperties(String filename) throws IOException{
        Properties props = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream stream = loader.getResourceAsStream(filename);
        props.load(stream);
        return props;
    }
    
    static public HashMap<String,String> jsonToHashMap(JSONObject json){
    	HashMap<String,String> hashMap = new HashMap<String,String>(); 
    	Iterator<String> keys = json.keys();
	    while(keys.hasNext()){
	    	String key = keys.next();
	    	String val = null;
	        try{
	             String value = json.getString(key);
	             hashMap.put(key, value);
	        }catch(Exception e){
	            e.printStackTrace();
	            return hashMap;
	        }
	    }
	    return hashMap;
    }
}