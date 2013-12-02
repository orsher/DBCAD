package dbcad;

import java.util.ArrayList;
import java.util.HashMap;

public class DatabasePluginConfig {

	private String dbPluginType;
	private HashMap<String,String> globalParameterValues;
	private ArrayList<String> instanceParameterNames;
	
	public DatabasePluginConfig(String dbPluginType,
			HashMap<String, String> globalParameterValues,
			ArrayList<String> instanceParameterNames) {
		super();
		this.dbPluginType = dbPluginType;
		this.globalParameterValues = globalParameterValues;
		this.instanceParameterNames = instanceParameterNames;
	}

	public ArrayList<String> getInstanceParameterNames() {
		return instanceParameterNames;
	}

	public void setInstanceParameterNames(ArrayList<String> instanceParameterNames) {
		this.instanceParameterNames = instanceParameterNames;
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
