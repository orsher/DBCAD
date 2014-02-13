package dbcad;

import java.util.ArrayList;
import java.util.HashMap;

public class DBGroup {
	private String dbGroupId;
	private String dbTypeId;
	private String dbPluginType;
	private String dbRole;
	private ArrayList<String> lobs;
	private HashMap<String,Boolean> databaseInstances;
	public DBGroup(String dbGroupId, String dbTypeId, String dbPluginType,String dbRole,ArrayList<String> lobs,HashMap<String,Boolean> databaseInstances) {
		this.dbGroupId = dbGroupId;
		this.dbTypeId = dbTypeId;
		this.dbPluginType = dbPluginType;
		this.dbRole = dbRole;
		this.lobs = lobs;
		this.databaseInstances = databaseInstances;
	}
	
	public DBGroup(){
		
	}
	
	public String getDbGroupId() {
		return dbGroupId;
	}
	public void setDbGroupId(String dbGroupId) {
		this.dbGroupId = dbGroupId;
	}
	public String getDbTypeId() {
		return dbTypeId;
	}
	public void setDbTypeId(String dbTypeId) {
		this.dbTypeId = dbTypeId;
	}
	public String getDbPluginType() {
		return dbPluginType;
	}
	public void setDbPluginType(String dbPluginType) {
		this.dbPluginType = dbPluginType;
	}
	public String getDbRole() {
		return dbRole;
	}
	public void setDbRole(String dbRole) {
		this.dbRole = dbRole;
	}
	public HashMap<String,Boolean> getDatabaseInstances() {
		return databaseInstances;
	}
	public void setDatabaseInstances(HashMap<String,Boolean> databaseInstances) {
		this.databaseInstances = databaseInstances;
	}
	public ArrayList<String> getLobs() {
		return lobs;
	}
	public void setLobs(ArrayList<String> lobs) {
		this.lobs = lobs;
	}
}