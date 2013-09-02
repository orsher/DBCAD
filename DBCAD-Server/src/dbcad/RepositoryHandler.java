package dbcad;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/*
 * Filling dbcad:
 * 
 * use dbcad;
 * insert into database_type values ('Mysql Replicated','mysql','replicated');
 * insert into database_groups values ('Mysql Replicated Dev','Mysql Replicated');
 * insert into database_instance values ('tlv-mysql-dev3407','Mysql Replicated Dev','tlv-mysql-dev',3407,null);
 * insert into db_schema values ('account_config','account_config','Mysql Replicated');
 * insert into lob_group_mapping values ('DEV','Mysql Replicated Dev');
 * insert into deployable_instance_schema values ('tlv-mysql-dev3407','account_config');
 * insert into db_requests values ('DBAR-22','account_config','create table d');
 * insert into db_request_status values ('DBAR-22','Mysql Replicated Dev','Done',now());
 * 
 */
public class RepositoryHandler {
	private static Connection conn = null;
	public RepositoryHandler(){
		if (conn == null){
			try {
				Class.forName("com.mysql.jdbc.Driver");
				System.out.println("Starting DB Connection");
				conn = DriverManager.getConnection("jdbc:mysql://tlv-mysql-dev:3407/dbcad?user=dbcad_user&password=dbcad4u");
				System.out.println("Connected to DB");
			} catch (Exception e) {
				System.out.println("Failure Connecting to Repository" );
				e.printStackTrace();
			}
		}
	}
	
	protected boolean checkDbChanges(String dbChangesId, String lob_id){
		ResultSet rs = null;
		String status = null;
		try{
			PreparedStatement preparedStatement = conn.prepareStatement("select * from db_request_status dbrs, lob_group_mapping lobgm where dbrs.db_group_id = lobgm.db_group_id and dbrs.db_request_id=? and lobgm.lob_id=?");
			preparedStatement.setString(1, dbChangesId);
			preparedStatement.setString(2, lob_id);
			rs = preparedStatement.executeQuery();
			if (rs.next()){
				status = rs.getString("status");
				System.out.println(status);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		if (status != null && status.equals("DONE")){
			return true;
		}
		return false;
	}
	
	protected ArrayList<String> getDatabaseIds(){
		ResultSet rs = null;
		ArrayList<String> databaseIds = new ArrayList<String>();
		try{
			PreparedStatement preparedStatement = conn.prepareStatement("select db_id from database_instance");
			rs = preparedStatement.executeQuery();
			while (rs.next()){
				databaseIds.add(rs.getString("db_id"));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return databaseIds;
	}
	
	protected ArrayList<String> getDatabaseVendors(){
		ResultSet rs = null;
		ArrayList<String> databaseVendors = new ArrayList<String>();
		try{
			PreparedStatement preparedStatement = conn.prepareStatement("select distinct db_vendor from database_type");
			rs = preparedStatement.executeQuery();
			while (rs.next()){
				databaseVendors.add(rs.getString("db_vendor"));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return databaseVendors;
	}
	
	protected ArrayList<String> getLobs(){
		ResultSet rs = null;
		ArrayList<String> lobs = new ArrayList<String>();
		try{
			PreparedStatement preparedStatement = conn.prepareStatement("select distinct lob_id from lob_group_mapping");
			rs = preparedStatement.executeQuery();
			while (rs.next()){
				lobs.add(rs.getString("lob_id"));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return lobs;
	}
	
	protected ArrayList<String> getNextDBChanges(Integer bulkSize, String LastDBReqId){
		ResultSet rs = null;
		String lastDBReqIdQuery = (LastDBReqId == null) ? "" : LastDBReqId;
		ArrayList<String> dbRequestIds = new ArrayList<String>();
		try{
			PreparedStatement preparedStatement = conn.prepareStatement("select distinct db_request_id from db_requests where db_request_id > ? limit ?");
			preparedStatement.setString(1, lastDBReqIdQuery);
			preparedStatement.setInt(2, bulkSize);
			rs = preparedStatement.executeQuery();
			while (rs.next()){
				dbRequestIds.add(rs.getString("db_request_id"));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return dbRequestIds;
	}
	
	protected ArrayList<String> getDatabaseRoles(){
		ResultSet rs = null;
		ArrayList<String> databaseRoles = new ArrayList<String>();
		try{
			PreparedStatement preparedStatement = conn.prepareStatement("select distinct db_role from database_type");
			rs = preparedStatement.executeQuery();
			while (rs.next()){
				databaseRoles.add(rs.getString("db_role"));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return databaseRoles;
	}
	
	protected ArrayList<HashMap<String,String>> getDatabaseTypes(){
		ResultSet rs = null;
		ArrayList<HashMap<String,String>> databaseTypes = new ArrayList<HashMap<String,String>>();
		try{
			PreparedStatement preparedStatement = conn.prepareStatement("select db_type_id,db_role,db_vendor from database_type");
			rs = preparedStatement.executeQuery();
			while (rs.next()){
				HashMap<String,String> dbType = new HashMap<String,String>();
				dbType.put("db_type_id", rs.getString("db_type_id"));
				dbType.put("db_vendor", rs.getString("db_vendor"));
				dbType.put("db_role", rs.getString("db_role"));
				databaseTypes.add(dbType);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return databaseTypes;
	}
	
	protected String addDatabaseType(String dbVendor, String dbRole){
		try{
			PreparedStatement preparedStatement = conn.prepareStatement("insert into database_type (db_type_id,db_role,db_vendor) values (?,?,?)");
			preparedStatement.setString(1, dbVendor+" "+dbRole);
			preparedStatement.setString(2, dbRole);
			preparedStatement.setString(3, dbVendor);
			return (preparedStatement.executeUpdate() > 0) ? dbVendor+" "+dbRole : "";
		}catch(Exception e){
			e.printStackTrace();
			return dbVendor+" "+dbRole;
		}
	}
	
	protected boolean deleteDatabaseType(String dbTypeId){
		try{
			PreparedStatement preparedStatement = conn.prepareStatement("delete from database_type where db_type_id = ?");
			preparedStatement.setString(1, dbTypeId);
			return (preparedStatement.executeUpdate() > 0);
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	
	protected ArrayList<String> getDatabaseGroupIds(){
		ResultSet rs = null;
		ArrayList<String> databaseGroups = new ArrayList<String>();
		try{
			PreparedStatement preparedStatement = conn.prepareStatement("select distinct db_group_id from database_groups");
			rs = preparedStatement.executeQuery();
			while (rs.next()){
				databaseGroups.add(rs.getString("db_group_id"));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return databaseGroups;
	}
	
	protected ArrayList<HashMap<String,String>> getDatabaseInstances(){
		ResultSet rs = null;
		ArrayList<HashMap<String,String>> databaseInstances = new ArrayList<HashMap<String,String>>();
		try{
			PreparedStatement preparedStatement = conn.prepareStatement("select db_id, db_group_id,host,port,sid from database_instance");
			rs = preparedStatement.executeQuery();
			while (rs.next()){
				HashMap<String,String> dbInstance = new HashMap<String,String>();
				dbInstance.put("db_id", rs.getString("db_id"));
				dbInstance.put("db_group_id", rs.getString("db_group_id"));
				dbInstance.put("host", rs.getString("host"));
				dbInstance.put("port", Integer.toString(rs.getInt("port")));
				dbInstance.put("sid", rs.getString("sid"));
				databaseInstances.add(dbInstance);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return databaseInstances;
	}

	protected String addDatabaseInstance(String db_group_id, String host,Integer port, String sid ){
	try{
		PreparedStatement preparedStatement = conn.prepareStatement("insert into database_instance (db_id,db_group_id,host,port,sid) values (?,?,?,?,?)");
		preparedStatement.setString(1, host+":"+port+":"+sid);
		preparedStatement.setString(2, db_group_id);
		preparedStatement.setString(3, host);
		preparedStatement.setInt(4, port);
		preparedStatement.setString(5, sid);
		return (preparedStatement.executeUpdate() > 0)  ? host+":"+port+":"+sid: "";
	}catch(Exception e){
		e.printStackTrace();
		return host+":"+port+":"+sid;
	}
  }
	protected boolean deleteDatabaseInstance(String dbInstanceId){
		try{
			PreparedStatement preparedStatement = conn.prepareStatement("delete from database_instance where db_id = ?");
			preparedStatement.setString(1, dbInstanceId);
			return (preparedStatement.executeUpdate() > 0);
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}

	public boolean markDbChangeAsDeployed(String dbChangeId, String lobId) {
		try{
			PreparedStatement preparedStatement = conn.prepareStatement("insert into db_request_status (db_request_id,db_group_id,status,update_date) "+
																		"select dbr.db_request_id,dbg.db_group_id,'DONE',now() from db_requests dbr, db_schema dbs, database_groups dbg, lob_group_mapping lobgm "+ 
																		"where dbr.schema_id = dbs.schema_id "+
																		"and dbs.db_type_id = dbg.db_type_id "+
																		"and dbg.db_group_id = lobgm.db_group_id "+ 
																		"and dbr.db_request_id=? and lobgm.lob_id=?");
			preparedStatement.setString(1, dbChangeId);
			preparedStatement.setString(2, lobId);
			System.out.println(preparedStatement.toString());
			return (preparedStatement.executeUpdate() > 0) ? true : false;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}

	public ArrayList<String> getDatabaseSchemas() {
		ResultSet rs = null;
		ArrayList<String> databaseSchemas = new ArrayList<String>();
		try{
			PreparedStatement preparedStatement = conn.prepareStatement("select schema_id from db_schema");
			rs = preparedStatement.executeQuery();
			while (rs.next()){
				databaseSchemas.add(rs.getString("schema_id"));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return databaseSchemas;
	}

	public int addDatabaseChange(String dbChangeId, String schemaId,String dbChangeText) {
		try{
			PreparedStatement preparedStatement = conn.prepareStatement("insert into db_requests (db_request_id,schema_id,code) values (?,?,?)");
			preparedStatement.setString(1, dbChangeId);
			preparedStatement.setString(2, schemaId);
			preparedStatement.setString(3, dbChangeText);
			preparedStatement.executeUpdate();
			return 0;
		}
		catch(Exception e){
			return 1;
		}
	}
	
	public int deleteDatabaseChange(String dbChangeId) {
		try{
			PreparedStatement preparedStatement = conn.prepareStatement("delete from db_requests where db_request_id=?");
			preparedStatement.setString(1, dbChangeId);
			preparedStatement.executeUpdate();
			return 0;
		}
		catch(Exception e){
			return 1;
		}
	}

	public ArrayList<String> getDatabaseTypeIds() {
		ResultSet rs = null;
		ArrayList<String> databaseTypesIds = new ArrayList<String>();
		try{
			PreparedStatement preparedStatement = conn.prepareStatement("select db_type_id from database_type");
			rs = preparedStatement.executeQuery();
			while (rs.next()){
				databaseTypesIds.add(rs.getString("db_type_id"));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return databaseTypesIds;
	}

	public ArrayList<HashMap<String, String>> getDatabaseGroups() {
		ResultSet rsGroups = null;
		ResultSet rsLobs = null;
		ArrayList<HashMap<String,String>> databaseGroups = new ArrayList<HashMap<String,String>>();
		try{
			PreparedStatement preparedStatement = conn.prepareStatement("select db_group_id, db_type_id from database_groups");
			rsGroups = preparedStatement.executeQuery();
			while (rsGroups.next()){
				HashMap<String,String> dbGroup = new HashMap<String,String>();
				dbGroup.put("db_group_id", rsGroups.getString("db_group_id"));
				dbGroup.put("db_type_id", rsGroups.getString("db_type_id"));
				PreparedStatement lobsPreparedStatement = conn.prepareStatement("select lob_id from lob_group_mapping where db_group_id=?");
				lobsPreparedStatement.setString(1, rsGroups.getString("db_group_id"));
				rsLobs = lobsPreparedStatement.executeQuery();
				StringBuilder sb = new StringBuilder();
				if (rsLobs.next()){
					sb.append(rsLobs.getString("lob_id"));
				}
				while (rsLobs.next()){
					sb.append(", "+rsLobs.getString("lob_id"));
				}
				dbGroup.put("db_group_lob_mapping", sb.toString());
				databaseGroups.add(dbGroup);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return databaseGroups;
	}
	
	public int addDatabaseGroup(String dbGroupId, String dbTypeId) {
		try{
			PreparedStatement preparedStatement = conn.prepareStatement("insert into database_groups (db_group_id,db_type_id) values (?,?)");
			preparedStatement.setString(1, dbGroupId);
			preparedStatement.setString(2, dbTypeId);
			preparedStatement.executeUpdate();
			return 0;
		}
		catch(Exception e){
			e.printStackTrace();
			return 1;
		}
	}

	public int deleteDatabaseGroup(String dbGroupId) {
		try{
			PreparedStatement preparedStatement = conn.prepareStatement("delete from database_groups where db_group_id=?");
			preparedStatement.setString(1, dbGroupId);
			preparedStatement.executeUpdate();
			return 0;
		}
		catch(Exception e){
			e.printStackTrace();
			return 1;
		}
	}

	public int addDatabaseGroupLobMapping(String dbGroupId, String lobId) {
		try{
			PreparedStatement preparedStatement = conn.prepareStatement("insert into lob_group_mapping (lob_id,db_group_id) values (?,?)");
			preparedStatement.setString(1, lobId);
			preparedStatement.setString(2, dbGroupId);
			preparedStatement.executeUpdate();
			return 0;
		}
		catch(Exception e){
			e.printStackTrace();
			return 1;
		}
	}
	
	public ArrayList<HashMap<String, String>> getDatabaseChangeLobsStatus(int offset, int bulkSize, AtomicInteger totalRowNumber) {
		ResultSet rs = null;
		ArrayList<HashMap<String,String>> databaseChangeLobsStatus = new ArrayList<HashMap<String,String>>();
		try{
			PreparedStatement preparedStatement = conn.prepareStatement("select SQL_CALC_FOUND_ROWS db_request_id, code from db_requests limit ?,?");
			preparedStatement.setInt(1, offset);
			preparedStatement.setInt(2, bulkSize);
			rs = preparedStatement.executeQuery();
			Statement stmt= conn.createStatement();
			ResultSet numRowsRs = stmt.executeQuery("SELECT FOUND_ROWS()");
            if(numRowsRs.next()){
            	totalRowNumber.set(numRowsRs.getInt(1));
            }
            numRowsRs.close();
			PreparedStatement statusPreparedStatement = conn.prepareStatement("select lobgm.lob_id, dbrs.status from db_request_status dbrs, lob_group_mapping lobgm where dbrs.db_group_id = lobgm.db_group_id and dbrs.db_request_id=?");
			ResultSet statusRs = null;
			while (rs.next()){
				HashMap<String,String> dbChangeStatus = new HashMap<String,String>();
				dbChangeStatus.put("db_request_id", rs.getString("db_request_id"));
				dbChangeStatus.put("db_request_code", rs.getString("code"));
				statusPreparedStatement.setString(1, rs.getString("db_request_id"));
				statusRs = statusPreparedStatement.executeQuery();
				while (statusRs.next()){
					dbChangeStatus.put(statusRs.getString("lob_id"), statusRs.getString("status"));
				}
				databaseChangeLobsStatus.add(dbChangeStatus);
			}
			rs.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return databaseChangeLobsStatus;
	}

}
