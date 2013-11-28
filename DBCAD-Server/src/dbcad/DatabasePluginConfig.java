package dbcad;

import java.util.HashMap;

public class DatabasePluginConfig {

	private String dbPluginType;
	private HashMap<String,String> globalParameterValues;
	
	public DatabasePluginConfig(String dbPluginType,
			HashMap<String, String> globalParameterValues) {
		this.dbPluginType = dbPluginType;
		this.globalParameterValues = globalParameterValues;
	}

	public String getDbPluginType() {
		return dbPluginType;
	}

	public void setDbPluginType(String dbPluginType) {
		this.dbPluginType = dbPluginType;
	}

	public HashMap<String, String> getGlobalParameterValues() {
		return globalParameterValues;
	}

	public void setGlobalParameterValues(
			HashMap<String, String> globalParameterValues) {
		this.globalParameterValues = globalParameterValues;
	}
}
