package dbcad;

public class DBGroup {
	private String dbGroupId;
	public DBGroup(String dbGroupId, String dbTypeId, String dbPluginName,
			String dbRole) {
		this.dbGroupId = dbGroupId;
		this.dbTypeId = dbTypeId;
		this.dbPluginName = dbPluginName;
		this.dbRole = dbRole;
	}
	private String dbTypeId;
	private String dbPluginName;
	private String dbRole;
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
	public String getDbPluginName() {
		return dbPluginName;
	}
	public void setDbPluginName(String dbPluginName) {
		this.dbPluginName = dbPluginName;
	}
	public String getDbRole() {
		return dbRole;
	}
	public void setDbRole(String dbRole) {
		this.dbRole = dbRole;
	}
}