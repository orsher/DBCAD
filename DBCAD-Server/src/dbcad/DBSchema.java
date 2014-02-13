package dbcad;

import java.util.HashMap;

public class DBSchema {
	private String schemaId;
	private String dbTypeId;
	private HashMap<String,String> databaseGroups;
	
	
	
	public DBSchema(String schemaId, String dbTypeId,
			HashMap<String,String> databaseGroups) {
		this.schemaId = schemaId;
		this.dbTypeId = dbTypeId;
		this.databaseGroups = databaseGroups;
	}
	
	public String getSchemaId() {
		return schemaId;
	}
	public void setSchemaId(String schemaId) {
		this.schemaId = schemaId;
	}
	public String getDbTypeId() {
		return dbTypeId;
	}
	public void setDbTypeId(String dbTypeId) {
		this.dbTypeId = dbTypeId;
	}
	public HashMap<String,String> getDatabaseGroups() {
		return databaseGroups;
	}
	public void setDatabaseGroups(HashMap<String,String> databaseGroups) {
		this.databaseGroups = databaseGroups;
	}
	
	
}
