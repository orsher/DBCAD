package dbcad;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import org.json.JSONArray;
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
        FileInputStream stream = new FileInputStream(filename);
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
    
    static public HashMap<String,Boolean> jsonToBooleanHashMap(JSONObject json){
    	HashMap<String,Boolean> hashMap = new HashMap<String,Boolean>(); 
    	Iterator<String> keys = json.keys();
	    while(keys.hasNext()){
	    	String key = keys.next();
	    	String val = null;
	        try{
	             Boolean value = json.getBoolean(key);
	             hashMap.put(key, value);
	        }catch(Exception e){
	            e.printStackTrace();
	            return hashMap;
	        }
	    }
	    return hashMap;
    }
    
    static public ArrayList<String> jsonArrayToArrayList(JSONArray jsonArray){
    	ArrayList<String> arrayList = new ArrayList<String>(); 
    	if (jsonArray != null) { 
    		   for (int i=0;i<jsonArray.length();i++){ 
    			   arrayList.add(jsonArray.get(i).toString());
    		   } 
    	} 
	    return arrayList;
    }
}