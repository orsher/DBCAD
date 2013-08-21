package dbcad;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

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
		if (status != null && status.equals("Done")){
			return true;
		}
		return false;
	}
	
	protected ArrayList<String> getDatabaseInstances(){
		ResultSet rs = null;
		ArrayList<String> databaseInstances = new ArrayList<String>();
		try{
			PreparedStatement preparedStatement = conn.prepareStatement("select db_id from database_instance");
			rs = preparedStatement.executeQuery();
			while (rs.next()){
				databaseInstances.add(rs.getString("db_id"));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return databaseInstances;
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
	
	protected ArrayList<String> getNextDBRequests(Integer bulkSize, String LastDBReqId){
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

	public void markDbChangeAsDeployed(String dbChangeId, String lobId) {
		// ResultSet rs = null;
		String status = null;
		try{
			PreparedStatement preparedStatement = conn.prepareStatement("insert into db_request_status (db_request_id,db_group_id,status,update_date) values (?,?,?,?)");
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
		if (status != null && status.equals("Done")){
			return true;
		}
		return false;
		
	}
}
