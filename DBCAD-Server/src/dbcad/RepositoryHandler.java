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
			PreparedStatement preparedStatement = conn.prepareStatement("select db_role,db_vendor from database_type");
			rs = preparedStatement.executeQuery();
			while (rs.next()){
				HashMap<String,String> dbRole = new HashMap<String,String>();
				dbRole.put("db_vendor", rs.getString("db_vendor"));
				dbRole.put("db_role", rs.getString("db_role"));
				databaseTypes.add(dbRole);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return databaseTypes;
	}
	
	protected boolean addDatabaseType(String dbVendor, String dbRole){
		try{
			PreparedStatement preparedStatement = conn.prepareStatement("insert into database_type (db_type_id,db_role,db_vendor) values (?,?,?)");
			preparedStatement.setString(1, dbVendor+" "+dbRole);
			preparedStatement.setString(2, dbRole);
			preparedStatement.setString(3, dbVendor);
			return (preparedStatement.executeUpdate() > 0);
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
}
