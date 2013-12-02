package dbcad;

import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONObject;

public class DBInstance {
	private String dbId = null;
	private String dbGroupId = null;
	private String dbHost = null;
	private Integer dbPort = 0;
	private String dbSid = null;
	private HashMap<String,String> pluginInstanceParameters;
	
	public DBInstance(String dbId,String dbGroupId, String dbHost, Integer dbPort,
			String dbSid, HashMap<String,String> pluginInstanceParameters) {
		this.dbGroupId = dbGroupId;
		this.dbHost = dbHost;
		this.dbPort = dbPort;
		this.dbSid = dbSid;
		this.pluginInstanceParameters = pluginInstanceParameters;
	}
	public DBInstance(String dbId,String dbGroupId, String dbHost, Integer dbPort,
			String dbSid, JSONObject pluginInstanceParameters) {
		this.dbGroupId = dbGroupId;
		this.dbHost = dbHost;
		this.dbPort = dbPort;
		this.dbSid = dbSid;
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
	public String getDbGroupId() {
		return dbGroupId;
	}
	public void setDbGroupId(String dbGroupId) {
		this.dbGroupId = dbGroupId;
	}
	public String getDbHost() {
		return dbHost;
	}
	public void setDbHost(String dbHost) {
		this.dbHost = dbHost;
	}
	public String getDbSid() {
		return dbSid;
	}
	public void setDbSid(String dbSid) {
		this.dbSid = dbSid;
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


