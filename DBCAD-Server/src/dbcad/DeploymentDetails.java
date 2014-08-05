package dbcad;

public class DeploymentDetails {
	private DBInstance dbInstance;
	private String dbGroupId;
	private String lobId;
	private String schemaName;
	private String dbChangeId;
	
	public DeploymentDetails(DBInstance dbInstance, String dbGroupId, String lobId, String schemaName, String dbChangeId) {
		this.dbInstance = dbInstance;
		this.dbGroupId = dbGroupId;
		this.lobId = lobId;
		this.schemaName = schemaName;
		this.dbChangeId = dbChangeId;
	}

	public DBInstance getDbInstance() {
		return dbInstance;
	}

	public void setDbInstance(DBInstance dbInstance) {
		this.dbInstance = dbInstance;
	}

	public String getDbGroupId() {
		return dbGroupId;
	}

	public void setDbGroupId(String dbGroupId) {
		this.dbGroupId = dbGroupId;
	}

	public String getLobId() {
		return lobId;
	}

	public void setLobId(String lobId) {
		this.lobId = lobId;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

	public String getDbChangeId() {
		return dbChangeId;
	}

	public void setDbChangeId(String dbChangeId) {
		this.dbChangeId = dbChangeId;
	}
	
	
	
}
