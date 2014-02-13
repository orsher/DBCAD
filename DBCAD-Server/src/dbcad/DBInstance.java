package dbcad;

import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONObject;

public class DBInstance {
	private String dbId = null;
	private String dbHost = null;
	private Integer dbPort = 0;
	private String dbPluginType = null;
	private HashMap<String,String> pluginInstanceParameters;
	
	public DBInstance(String dbId, String dbPluginType, String dbHost, Integer dbPort, HashMap<String,String> pluginInstanceParameters) {
		this.dbId = dbId;
		this.dbHost = dbHost;
		this.dbPort = dbPort;
		this.dbPluginType = dbPluginType;
		this.pluginInstanceParameters = pluginInstanceParameters;
	}
	public DBInstance(String dbId, String dbTypeId, String dbHost, Integer dbPort,
			JSONObject pluginInstanceParameters) {
		this.dbHost = dbHost;
		this.dbPort = dbPort;
		this.dbPluginType = dbTypeId;
		Iterator<String> keys = pluginInstanceParameters.keys();
	    while(keys.hasNext()){
	    	String key = keys.next();
	    	String val = null;
	        try{
	             String value = pluginInstanceParameters.getString(key);
	             this.pluginInstanceParameters.put(key, value);
	        }catch(Exception e){
	            e.printStackTrace();
	        }
	    }
	}
	
	public HashMap<String,String> getPluginInstanceParameters() {
		return pluginInstanceParameters;
	}
	public void setPluginInstanceParameters(HashMap<String,String> pluginInstanceParameters) {
		this.pluginInstanceParameters = pluginInstanceParameters;
	}

	public String getDbHost() {
		return dbHost;
	}
	public void setDbHost(String dbHost) {
		this.dbHost = dbHost;
	}
	public String getDbPluginType() {
		return dbPluginType;
	}
	public void setDbPluginType(String dbPluginType) {
		this.dbPluginType = dbPluginType;
	}
	public Integer getDbPort() {
		return dbPort;
	}
	public void setDbPort(Integer dbPort) {
		this.dbPort = dbPort;
	}
	public String getDbId() {
		return dbId;
	}

	public void setDbId(String dbId) {
		this.dbId = dbId;
	}
}


