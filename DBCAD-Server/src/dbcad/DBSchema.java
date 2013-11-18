package dbcad;

import java.util.ArrayList;

public class DBSchema {
	private String schemaId;
	private String schemaName;
	private String dbTypeId;
	private ArrayList<String> deployableInstances;
	
	
	
	public DBSchema(String schemaId, String schemaName, String dbTypeId,
			ArrayList<String> deployableInstances) {
		this.schemaId = schemaId;
		this.schemaName = schemaName;
		this.dbTypeId = dbTypeId;
		this.deployableInstances = deployableInstances;
	}
	
	public String getSchemaId() {
		return schemaId;
	}
	public void setSchemaId(String schemaId) {
		this.schemaId = schemaId;
	}
	public String getSchemaName() {
		return schemaName;
	}
	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}
	public String getDbTypeId() {
		return dbTypeId;
	}
	public void setDbTypeId(String dbTypeId) {
		this.dbTypeId = dbTypeId;
	}
	public ArrayList<String> getDeployableInstances() {
		return deployableInstances;
	}
	public void setDeployableInstances(ArrayList<String> deployableInstances) {
		this.deployableInstances = deployableInstances;
	}
	
	
}
