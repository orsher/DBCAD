package dbcad;

import java.util.ArrayList;
import java.util.HashMap;

public class DatabasePluginConfig {

	private String dbPluginType;
	private HashMap<String,String> globalParameterValues;
	private ArrayList<String> instanceParameterNames;
	private HashMap<String,HashMap<String,String>> instanceParameterAttributes;
	
	public DatabasePluginConfig(String dbPluginType,
			HashMap<String, String> globalParameterValues,
			ArrayList<String> instanceParameterNames,
			HashMap<String,HashMap<String,String>> instanceParameterAttributes) {
		super();
		this.dbPluginType = dbPluginType;
		this.globalParameterValues = globalParameterValues;
		this.instanceParameterNames = instanceParameterNames;
		this.instanceParameterAttributes = instanceParameterAttributes;
	}

	public HashMap<String, HashMap<String, String>> getInstanceParameterAttributes() {
		return instanceParameterAttributes;
	}

	public void setInstanceParameterAttributes(
			HashMap<String, HashMap<String, String>> instanceParameterAttributes) {
		this.instanceParameterAttributes = instanceParameterAttributes;
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
