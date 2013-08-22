package dbcad;

public class DBInstance {
	private String dbGroupId = null;
	private String dbHost = null;
	private Integer dbPort = 0;
	private String dbSid = null;
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
}


