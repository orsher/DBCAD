package dbcad;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONArray;
import org.json.JSONObject;

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
	public static final int REP_OK=0;
	public static final int REP_ERR=-1;

	Datasource datasource;
	public RepositoryHandler(){
		try {
			datasource = Datasource.getInstance();
		} catch (Exception e) {
			System.out.println("Failure Connecting to Repository" );
			e.printStackTrace();
		}
//		if (conn == null){
//			try {
//				Class.forName("com.mysql.jdbc.Driver");
//				System.out.println("Starting DB Connection");
//				conn = DriverManager.getConnection("jdbc:mysql://tlv-mysql-dev:3407/dbcad?user=dbcad_user&password=dbcad4u");
//				System.out.println("Connected to DB");
//			} catch (Exception e) {
//				System.out.println("Failure Connecting to Repository" );
//				e.printStackTrace();
//			}
//		}
	}
	
	protected boolean checkDbChanges(String dbChangesId, String lob_id){
		ResultSet rs = null;
		String status = null;
		Connection conn=null;
		try{
			conn = datasource.getConnection();
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
		try{
			conn.close();
		}
		catch(Exception e){
			System.out.println("Error: Could not close connection" );
		}
		return false;
	}
	
	protected ArrayList<String> getDatabaseIds(){
		ResultSet rs = null;
		ArrayList<String> databaseIds = new ArrayList<String>();
		Connection conn=null;
		try{
			conn = datasource.getConnection();
			PreparedStatement preparedStatement = conn.prepareStatement("select db_id from database_instance");
			rs = preparedStatement.executeQuery();
			while (rs.next()){
				databaseIds.add(rs.getString("db_id"));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		try{
			conn.close();
		}
		catch(Exception e){
			System.out.println("Error: Could not close connection" );
		}
		return databaseIds;
	}
	
	protected ArrayList<String> getDatabaseIds(String dbTypeId){
		ResultSet rs = null;
		ArrayList<String> databaseIds = new ArrayList<String>();
		Connection conn=null;
		try{
			conn = datasource.getConnection();
			PreparedStatement preparedStatement = conn.prepareStatement("select db_id from database_instance di, database_groups dg where di.db_group_id = dg.db_group_id and dg.db_type_id=?");
			preparedStatement.setString(1, dbTypeId);
			rs = preparedStatement.executeQuery();
			while (rs.next()){
				databaseIds.add(rs.getString("db_id"));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		try{
			conn.close();
		}
		catch(Exception e){
			System.out.println("Error: Could not close connection" );
		}
		return databaseIds;
	}
	
	protected ArrayList<String> getDatabaseVendors(){
		ResultSet rs = null;
		ArrayList<String> databaseVendors = new ArrayList<String>();
		Connection conn=null;
		try{
			conn = datasource.getConnection();
			PreparedStatement preparedStatement = conn.prepareStatement("select distinct db_vendor from database_type");
			rs = preparedStatement.executeQuery();
			while (rs.next()){
				databaseVendors.add(rs.getString("db_vendor"));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		try{
			conn.close();
		}
		catch(Exception e){
			System.out.println("Error: Could not close connection" );
		}
		return databaseVendors;
	}
	
	protected ArrayList<String> getLobs(){
		ResultSet rs = null;
		ArrayList<String> lobs = new ArrayList<String>();
		Connection conn=null;
		try{
			conn = datasource.getConnection();
			PreparedStatement preparedStatement = conn.prepareStatement("select lob_id from lobs order by sequence_number");
			rs = preparedStatement.executeQuery();
			while (rs.next()){
				lobs.add(rs.getString("lob_id"));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		try{
			conn.close();
		}
		catch(Exception e){
			System.out.println("Error: Could not close connection" );
		}
		return lobs;
	}
	
	protected ArrayList<String> getNextDBChanges(Integer bulkSize, String LastDBReqId){
		ResultSet rs = null;
		String lastDBReqIdQuery = (LastDBReqId == null) ? "" : LastDBReqId;
		ArrayList<String> dbRequestIds = new ArrayList<String>();
		Connection conn=null;
		try{
			conn = datasource.getConnection();
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
		try{
			conn.close();
		}
		catch(Exception e){
			System.out.println("Error: Could not close connection" );
		}
		return dbRequestIds;
	}
	
	protected ArrayList<String> getDatabaseRoles(){
		ResultSet rs = null;
		ArrayList<String> databaseRoles = new ArrayList<String>();
		Connection conn=null;
		try{
			conn = datasource.getConnection();
			PreparedStatement preparedStatement = conn.prepareStatement("select distinct db_role from database_type");
			rs = preparedStatement.executeQuery();
			while (rs.next()){
				databaseRoles.add(rs.getString("db_role"));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		try{
			conn.close();
		}
		catch(Exception e){
			System.out.println("Error: Could not close connection" );
		}
		return databaseRoles;
	}
	
	protected ArrayList<HashMap<String,String>> getDatabaseTypes(){
		ResultSet rs = null;
		ArrayList<HashMap<String,String>> databaseTypes = new ArrayList<HashMap<String,String>>();
		Connection conn=null;
		try{
			conn = datasource.getConnection();
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
		try{
			conn.close();
		}
		catch(Exception e){
			System.out.println("Error: Could not close connection" );
		}
		return databaseTypes;
	}
	
	protected String addDatabaseType(String dbVendor, String dbRole){
		Connection conn=null;
		try{
			conn = datasource.getConnection();
			PreparedStatement preparedStatement = conn.prepareStatement("insert into database_type (db_type_id,db_role,db_vendor) values (?,?,?)");
			preparedStatement.setString(1, dbVendor+" "+dbRole);
			preparedStatement.setString(2, dbRole);
			preparedStatement.setString(3, dbVendor);
			int result = preparedStatement.executeUpdate();
			try{
				conn.close();
			}
			catch(Exception e){
				System.out.println("Error: Could not close connection" );
			}
			return (result > 0) ? dbVendor+" "+dbRole : "";
		}catch(Exception e){
			try{
				conn.close();
			}
			catch(Exception ex){
				System.out.println("Error: Could not close connection" );
			}
			e.printStackTrace();
			return dbVendor+" "+dbRole;
		}
	}
	
	protected boolean deleteDatabaseType(String dbTypeId){
		Connection conn=null;
		try{
			conn = datasource.getConnection();
			PreparedStatement preparedStatement = conn.prepareStatement("delete from database_type where db_type_id = ?");
			preparedStatement.setString(1, dbTypeId);
			int result = preparedStatement.executeUpdate();
			try{
				conn.close();
			}
			catch(Exception e){
				System.out.println("Error: Could not close connection" );
			}
			return (result > 0);
		}catch(Exception e){
			try{
				conn.close();
			}
			catch(Exception ex){
				System.out.println("Error: Could not close connection" );
			}
			e.printStackTrace();
			return false;
		}
	}
	
	protected ArrayList<String> getDatabaseGroupIds(){
		ResultSet rs = null;
		ArrayList<String> databaseGroups = new ArrayList<String>();
		Connection conn=null;
		try{
			conn = datasource.getConnection();
			PreparedStatement preparedStatement = conn.prepareStatement("select distinct db_group_id from database_groups");
			rs = preparedStatement.executeQuery();
			while (rs.next()){
				databaseGroups.add(rs.getString("db_group_id"));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		try{
			conn.close();
		}
		catch(Exception e){
			System.out.println("Error: Could not close connection" );
		}
		return databaseGroups;
	}
	
	protected ArrayList<DBInstance> getDatabaseInstances(String generalFilter, int offset, int bulkSize, AtomicInteger totalRowNumber){
		ResultSet rs = null;
		ArrayList<DBInstance> databaseInstances = new ArrayList<DBInstance>();
		Connection conn=null;
		try{
			conn = datasource.getConnection();
			PreparedStatement preparedStatement = conn.prepareStatement("select SQL_CALC_FOUND_ROWS db_id, db_group_id,host,port,sid from database_instance limit ?,?");
			preparedStatement.setInt(1, offset);
			preparedStatement.setInt(2, bulkSize);
			rs = preparedStatement.executeQuery();
			Statement stmt= conn.createStatement();
			ResultSet numRowsRs = stmt.executeQuery("SELECT FOUND_ROWS()");
            if(numRowsRs.next()){
            	totalRowNumber.set(numRowsRs.getInt(1));
            }
			while (rs.next()){
				HashMap<String,String> pluginInstanceParameters = new HashMap<String,String>();
				PreparedStatement innerPS = conn.prepareStatement("select parameter_name, parameter_value from db_plugin_instance_parameters where db_id=?");
				innerPS.setString(1,rs.getString("db_id"));
				ResultSet innerRS = innerPS.executeQuery();
				while (innerRS.next()){
					pluginInstanceParameters.put(innerRS.getString("parameter_name"), innerRS.getString("parameter_value"));
				}
				DBInstance dbInstance = new DBInstance(rs.getString("db_id"),rs.getString("db_group_id"),rs.getString("host"),rs.getInt("port"),rs.getString("sid"),pluginInstanceParameters);
//				dbInstance.put("db_id", rs.getString("db_id"));
//				dbInstance.put("db_group_id", rs.getString("db_group_id"));
//				dbInstance.put("host", rs.getString("host"));
//				dbInstance.put("port", Integer.toString(rs.getInt("port")));
//				dbInstance.put("sid", rs.getString("sid"));
				databaseInstances.add(dbInstance);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		try{
			conn.close();
		}
		catch(Exception e){
			System.out.println("Error: Could not close connection" );
		}
		return databaseInstances;
	}

	protected int addDatabaseInstance(String dbId,String db_group_id, String host, Integer port, String sid, HashMap<String, String> pluginInstanceParameters ){
		Connection conn=null;
	try{
		conn = datasource.getConnection();
		PreparedStatement preparedStatement = conn.prepareStatement("insert into database_instance (db_id,db_group_id,host,port,sid) values (?,?,?,?,?)");
		preparedStatement.setString(1, dbId);
		preparedStatement.setString(2, db_group_id);
		preparedStatement.setString(3, host);
		preparedStatement.setInt(4, port);
		preparedStatement.setString(5, sid);
		int result = preparedStatement.executeUpdate();
		preparedStatement =  conn.prepareStatement("insert into db_plugin_instance_parameters (db_id,parameter_name,parameter_value) values (?,?,?)");
		preparedStatement.setString(1, dbId);
	    for (String key : pluginInstanceParameters.keySet()){
	    	String val = null;
	    	preparedStatement.setString(2, key);
	    	preparedStatement.setString(3, pluginInstanceParameters.get(key));
	    	result = preparedStatement.executeUpdate();
	    }
		try{
			conn.close();
		}
		catch(Exception e){
			System.out.println("Error: Could not close connection" );
		}
		return (result > 0)  ? REP_OK : REP_ERR;
	}catch(Exception e){
		try{
			conn.close();
		}
		catch(Exception ex){
			System.out.println("Error: Could not close connection" );
		}
		e.printStackTrace();
		return REP_ERR;
	}
  }
	
	protected String saveDatabaseInstance(String dbId, String db_group_id, String host,Integer port, String sid, HashMap<String, String> pluginInstanceParameters ){
		Connection conn=null;
		String newDbId = host+":"+port+":"+sid;
	try{
		conn = datasource.getConnection();
		PreparedStatement preparedStatement = conn.prepareStatement("update database_instance set db_id=?,db_group_id=?,host=?,port=?,sid=? where db_id=?");
		preparedStatement.setString(1, newDbId);
		preparedStatement.setString(2, db_group_id);
		preparedStatement.setString(3, host);
		preparedStatement.setInt(4, port);
		preparedStatement.setString(5, sid);
		preparedStatement.setString(6, dbId);
		int result = preparedStatement.executeUpdate();
		preparedStatement =  conn.prepareStatement("insert into db_plugin_instance_parameters (db_id,parameter_name,parameter_value) values (?,?,?) on duplicate key update parameter_value=?");
		preparedStatement.setString(1, newDbId);
	    for (String key : pluginInstanceParameters.keySet()){
	    	preparedStatement.setString(2, key);
	    	preparedStatement.setString(3, pluginInstanceParameters.get(key));
	    	preparedStatement.setString(4, pluginInstanceParameters.get(key));
	    	result = preparedStatement.executeUpdate();
	    }
		try{
			conn.close();
		}
		catch(Exception e){
			System.out.println("Error: Could not close connection" );
		}
		return (result > 0)  ? newDbId: "";
	}catch(Exception e){
		try{
			conn.close();
		}
		catch(Exception ex){
			System.out.println("Error: Could not close connection" );
		}
		e.printStackTrace();
		return newDbId;
	}
  }
	
	protected boolean deleteDatabaseInstance(String dbInstanceId){
		Connection conn=null;
		try{
			conn = datasource.getConnection();
			PreparedStatement preparedStatement = conn.prepareStatement("delete from database_instance where db_id = ?");
			preparedStatement.setString(1, dbInstanceId);
			int result = preparedStatement.executeUpdate();
			try{
				conn.close();
			}
			catch(Exception e){
				System.out.println("Error: Could not close connection" );
			}
			return (result > 0);
		}catch(Exception e){
			try{
				conn.close();
			}
			catch(Exception ex){
				System.out.println("Error: Could not close connection" );
			}
			e.printStackTrace();
			return false;
		}
	}

	public boolean markDbChangeDeploymentStatus(String dbChangeId, String lobId, String status) {
		Connection conn=null;
		try{
			conn = datasource.getConnection();
			PreparedStatement preparedStatement = conn.prepareStatement("insert into db_request_status (db_request_id,db_group_id,status,update_date) "+
																		"select dbr.db_request_id,dbg.db_group_id,?,now() from db_requests dbr, db_schema dbs, database_groups dbg, lob_group_mapping lobgm "+ 
																		"where dbr.schema_id = dbs.schema_id "+
																		"and dbs.db_type_id = dbg.db_type_id "+
																		"and dbg.db_group_id = lobgm.db_group_id "+ 
																		"and dbr.db_request_id=? and lobgm.lob_id=? on duplicate key update status=?, update_date=now()");
			preparedStatement.setString(1, status);
			preparedStatement.setString(2, dbChangeId);
			preparedStatement.setString(3, lobId);
			preparedStatement.setString(4, status);
			int result = preparedStatement.executeUpdate();
			try{
				conn.close();
			}
			catch(Exception e){
				System.out.println("Error: Could not close connection" );
			}
			return (result > 0) ? true : false;
		}catch(Exception e){
			try{
				conn.close();
			}
			catch(Exception ex){
				System.out.println("Error: Could not close connection" );
			}
			e.printStackTrace();
			return false;
		}
	}

	public ArrayList<String> getDatabaseSchemaIds() {
		ResultSet rs = null;
		ArrayList<String> databaseSchemas = new ArrayList<String>();
		Connection conn=null;
		try{
			conn = datasource.getConnection();
			PreparedStatement preparedStatement = conn.prepareStatement("select schema_id from db_schema");
			rs = preparedStatement.executeQuery();
			while (rs.next()){
				databaseSchemas.add(rs.getString("schema_id"));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		try{
			conn.close();
		}
		catch(Exception e){
			System.out.println("Error: Could not close connection" );
		}
		return databaseSchemas;
	}

	public int addDatabaseChange(String dbChangeId, String schemaId,String dbChangeText) {
		Connection conn=null;
		try{
			conn = datasource.getConnection();
			PreparedStatement preparedStatement = conn.prepareStatement("insert into db_requests (db_request_id,schema_id,code,created_timestamp,last_changed_timestamp) values (?,?,?,?,?)");
			preparedStatement.setString(1, dbChangeId);
			preparedStatement.setString(2, schemaId);
			preparedStatement.setString(3, dbChangeText);
			preparedStatement.setTimestamp(4, new java.sql.Timestamp(System.currentTimeMillis()));
			preparedStatement.setTimestamp(5, new java.sql.Timestamp(System.currentTimeMillis()));
			preparedStatement.executeUpdate();
			try{
				conn.close();
			}
			catch(Exception e){
				System.out.println("Error: Could not close connection" );
			}
			return 0;
		}
		catch(Exception e){
			try{
				conn.close();
			}
			catch(Exception ex){
				System.out.println("Error: Could not close connection" );
			}
			return 1;
		}
	}
	
	public int deleteDatabaseChange(String dbChangeId) {
		Connection conn=null;
		try{
			conn = datasource.getConnection();
			PreparedStatement preparedStatement = conn.prepareStatement("delete from db_requests where db_request_id=?");
			preparedStatement.setString(1, dbChangeId);
			preparedStatement.executeUpdate();
			try{
				conn.close();
			}
			catch(Exception e){
				System.out.println("Error: Could not close connection" );
			}
			return 0;
		}
		catch(Exception e){
			try{
				conn.close();
			}
			catch(Exception ex){
				System.out.println("Error: Could not close connection" );
			}
			return 1;
		}
	}

	public ArrayList<String> getDatabaseTypeIds() {
		ResultSet rs = null;
		ArrayList<String> databaseTypesIds = new ArrayList<String>();
		Connection conn=null;
		try{
			conn = datasource.getConnection();
			PreparedStatement preparedStatement = conn.prepareStatement("select db_type_id from database_type");
			rs = preparedStatement.executeQuery();
			while (rs.next()){
				databaseTypesIds.add(rs.getString("db_type_id"));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		try{
			conn.close();
		}
		catch(Exception e){
			System.out.println("Error: Could not close connection" );
		}
		return databaseTypesIds;
	}

	public ArrayList<HashMap<String, String>> getDatabaseGroups() {
		ResultSet rsGroups = null;
		ResultSet rsLobs = null;
		ArrayList<HashMap<String,String>> databaseGroups = new ArrayList<HashMap<String,String>>();
		Connection conn=null;
		try{
			conn = datasource.getConnection();
			PreparedStatement preparedStatement = conn.prepareStatement("select dg.db_group_id, dg.db_type_id,dt.db_vendor from database_groups dg, database_type dt where dg.db_type_id = dt.db_type_id");
			rsGroups = preparedStatement.executeQuery();
			while (rsGroups.next()){
				HashMap<String,String> dbGroup = new HashMap<String,String>();
				dbGroup.put("db_group_id", rsGroups.getString("db_group_id"));
				dbGroup.put("db_type_id", rsGroups.getString("db_type_id"));
				dbGroup.put("db_plugin_type", rsGroups.getString("db_vendor"));
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
		try{
			conn.close();
		}
		catch(Exception e){
			System.out.println("Error: Could not close connection" );
		}
		return databaseGroups;
	}
	
	public int addDatabaseGroup(String dbGroupId, String dbTypeId) {
		Connection conn=null;
		try{
			conn = datasource.getConnection();
			PreparedStatement preparedStatement = conn.prepareStatement("insert into database_groups (db_group_id,db_type_id) values (?,?)");
			preparedStatement.setString(1, dbGroupId);
			preparedStatement.setString(2, dbTypeId);
			preparedStatement.executeUpdate();
			try{
				conn.close();
			}
			catch(Exception e){
				System.out.println("Error: Could not close connection" );
			}
			return 0;
		}
		catch(Exception e){
			try{
				conn.close();
			}
			catch(Exception ex){
				System.out.println("Error: Could not close connection" );
			}
			e.printStackTrace();
			return 1;
		}
	}

	public int deleteDatabaseGroup(String dbGroupId) {
		Connection conn=null;
		try{
			conn = datasource.getConnection();
			PreparedStatement preparedStatement = conn.prepareStatement("delete from database_groups where db_group_id=?");
			preparedStatement.setString(1, dbGroupId);
			preparedStatement.executeUpdate();
			try{
				conn.close();
			}
			catch(Exception e){
				System.out.println("Error: Could not close connection" );
			}
			return 0;
		}
		catch(Exception e){
			try{
				conn.close();
			}
			catch(Exception ex){
				System.out.println("Error: Could not close connection" );
			}
			e.printStackTrace();
			return 1;
		}
	}

	public int addDatabaseGroupLobMapping(String dbGroupId, String lobId) {
		Connection conn=null;
		try{
			conn = datasource.getConnection();
			PreparedStatement preparedStatement = conn.prepareStatement("insert into lob_group_mapping (lob_id,db_group_id) values (?,?)");
			preparedStatement.setString(1, lobId);
			preparedStatement.setString(2, dbGroupId);
			preparedStatement.executeUpdate();
			try{
				conn.close();
			}
			catch(Exception e){
				System.out.println("Error: Could not close connection" );
			}
			return 0;
		}
		catch(Exception e){
			try{
				conn.close();
			}
			catch(Exception ex){
				System.out.println("Error: Could not close connection" );
			}
			e.printStackTrace();
			return 1;
		}
	}
	
	public ArrayList<HashMap<String, String>> getDatabaseChangeLobsStatus(String generalFilter, int offset, int bulkSize, AtomicInteger totalRowNumber) {
		ResultSet rs = null;
		ArrayList<HashMap<String,String>> databaseChangeLobsStatus = new ArrayList<HashMap<String,String>>();
		Connection conn=null;
		try{
			System.out.println(generalFilter+" "+offset+" "+bulkSize);
			conn = datasource.getConnection();
			PreparedStatement preparedStatement;
			if (generalFilter == null){
				preparedStatement = conn.prepareStatement("select SQL_CALC_FOUND_ROWS db_request_id, schema_id, code from db_requests order by created_timestamp desc limit ?,?");
				preparedStatement.setInt(1, offset);
				preparedStatement.setInt(2, bulkSize);
			}
			else{
				preparedStatement = conn.prepareStatement("select SQL_CALC_FOUND_ROWS db_request_id, schema_id, code from db_requests where upper(db_request_id) like upper(?) or upper(schema_id) like upper(?) or upper(code) like upper(?) order by created_timestamp desc limit ?,?");
				preparedStatement.setString(1, '%'+generalFilter+'%');
				preparedStatement.setString(2, '%'+generalFilter+'%');
				preparedStatement.setString(3, '%'+generalFilter+'%');
				preparedStatement.setInt(4, offset);
				preparedStatement.setInt(5, bulkSize);
			}
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
				dbChangeStatus.put("schema_id", rs.getString("schema_id"));
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
		try{
			conn.close();
		}
		catch(Exception e){
			System.out.println("Error: Could not close connection" );
		}
		return databaseChangeLobsStatus;
	}

	public ArrayList<DBSchema> getDatabaseSchemas(String generalFilter, int offset, int bulkSize, AtomicInteger totalRowNumber) {
		ResultSet rs = null;
		ArrayList<DBSchema> databaseSchemas = new ArrayList<DBSchema>();
		Connection conn=null;
		try{
			conn = datasource.getConnection();
			PreparedStatement preparedStatement = conn.prepareStatement("select SQL_CALC_FOUND_ROWS schema_id, schema_name, db_type_id from db_schema limit ?,?");
			preparedStatement.setInt(1, offset);
			preparedStatement.setInt(2, bulkSize);
			rs = preparedStatement.executeQuery();
			Statement stmt= conn.createStatement();
			ResultSet numRowsRs = stmt.executeQuery("SELECT FOUND_ROWS()");
            if(numRowsRs.next()){
            	totalRowNumber.set(numRowsRs.getInt(1));
            }
            numRowsRs.close();
			PreparedStatement deployablesPreparedStatement = conn.prepareStatement("select db_id from deployable_instance_schema where schema_id=?");
			ResultSet deployablesRs = null;
			while (rs.next()){
				deployablesPreparedStatement.setString(1, rs.getString("schema_id"));
				deployablesRs = deployablesPreparedStatement.executeQuery();
				ArrayList<String> deployableInstances = new ArrayList<String>();
				while (deployablesRs.next()){
					deployableInstances.add(deployablesRs.getString("db_id"));
				}
				DBSchema database_schema = new DBSchema(rs.getString("schema_id"),rs.getString("schema_name"),rs.getString("db_type_id"),deployableInstances);
				databaseSchemas.add(database_schema);
			}
			rs.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		try{
			conn.close();
		}
		catch(Exception e){
			System.out.println("Error: Could not close connection" );
		}
		return databaseSchemas;
	}

	public int addDatabaseSchema(String schemaId, String schemaName,
			String dbTypeId) {
		Connection conn=null;
		try{
			conn = datasource.getConnection();
			PreparedStatement preparedStatement = conn.prepareStatement("insert into db_schema (schema_id,schema_name,db_type_id) values (?,?,?)");
			preparedStatement.setString(1, schemaId);
			preparedStatement.setString(2, schemaName);
			preparedStatement.setString(3, dbTypeId);
			preparedStatement.executeUpdate();
			try{
				conn.close();
			}
			catch(Exception e){
				System.out.println("Error: Could not close connection" );
			}
			return 0;
		}
		catch(Exception e){
			try{
				conn.close();
			}
			catch(Exception ex){
				System.out.println("Error: Could not close connection" );
			}
			e.printStackTrace();
			return 1;
		}
	}

	public int addDeployableDBInstance(String schemaId, String dbId) {
		Connection conn=null;
		try{
			conn = datasource.getConnection();
			PreparedStatement preparedStatement = conn.prepareStatement("insert into deployable_instance_schema (db_id,schema_id) values (?,?)");
			preparedStatement.setString(1, dbId);
			preparedStatement.setString(2, schemaId);
			preparedStatement.executeUpdate();
			try{
				conn.close();
			}
			catch(Exception e){
				System.out.println("Error: Could not close connection" );
			}
			return 0;
		}
		catch(Exception e){
			try{
				conn.close();
			}
			catch(Exception ex){
				System.out.println("Error: Could not close connection" );
			}
			e.printStackTrace();
			return 1;
		}
	}

	public int deleteDatabaseSchema(String dbSchemaId) {
		Connection conn=null;
		try{
			conn = datasource.getConnection();
			PreparedStatement preparedStatement = conn.prepareStatement("delete from db_schema where schema_id=?");
			preparedStatement.setString(1, dbSchemaId);
			preparedStatement.executeUpdate();
			try{
				conn.close();
			}
			catch(Exception e){
				System.out.println("Error: Could not close connection" );
			}
			return 0;
		}
		catch(Exception e){
			try{
				conn.close();
			}
			catch(Exception ex){
				System.out.println("Error: Could not close connection" );
			}
			e.printStackTrace();
			return 1;
		}
	}
	
	public int addDBPluginConfig(String pluginDBType,ArrayList<String> globalParameters){
		Connection conn=null;
		try{
			conn = datasource.getConnection();
			PreparedStatement preparedStatement = conn.prepareStatement("insert into db_plugin_global_parameters (plugin_name,parameter_name) values (?,?) on duplicate key update plugin_name=plugin_name");
			preparedStatement.setString(1, pluginDBType);
			for (String parameter : globalParameters){
				preparedStatement.setString(2, parameter);
				preparedStatement.executeUpdate();
			}
			try{
				conn.close();
			}
			catch(Exception e){
				System.out.println("Error: Could not close connection" );
			}
			return 0;
		}
		catch(Exception e){
			try{
				conn.close();
			}
			catch(Exception ex){
				System.out.println("Error: Could not close connection" );
			}
			e.printStackTrace();
			return 1;
		}
	}

	public int saveDBPluginConfig(String pluginDBType,HashMap<String,String> globalParameters, String dbcadServerHostname){
		Connection conn=null;
		try{
			conn = datasource.getConnection();
			PreparedStatement preparedStatement = conn.prepareStatement("insert into db_plugin_global_parameter_values (plugin_name,parameter_name,parameter_value,dbcad_server) values (?,?,?,?) on duplicate key update parameter_value = ?");
			preparedStatement.setString(1, pluginDBType);
			for (String key : globalParameters.keySet()){
				preparedStatement.setString(2, key);
				preparedStatement.setString(3, globalParameters.get(key));
				preparedStatement.setString(4, dbcadServerHostname);
				preparedStatement.setString(5, globalParameters.get(key));
				preparedStatement.executeUpdate();
			}
			try{
				conn.close();
			}
			catch(Exception e){
				System.out.println("Error: Could not close connection" );
			}
			return 0;
		}
		catch(Exception e){
			try{
				conn.close();
			}
			catch(Exception ex){
				System.out.println("Error: Could not close connection" );
			}
			e.printStackTrace();
			return 1;
		}
	}
	
	public HashMap<String,String> getDBPluginConfig(String pluginDBType, String dbcadServerHostname){
		Connection conn=null;
		HashMap<String,String> dbPluginParamValues = new HashMap<String,String>();
		try{
			conn = datasource.getConnection();
			PreparedStatement preparedStatement = conn.prepareStatement("select p.parameter_name, v.parameter_value from (select plugin_name,parameter_name,parameter_value from db_plugin_global_parameter_values where plugin_name=? and dbcad_server=?) v right outer join (select plugin_name,parameter_name from db_plugin_global_parameters where plugin_name=?) p on (v.plugin_name=p.plugin_name and v.parameter_name=p.parameter_name)");
			preparedStatement.setString(1, pluginDBType);
			preparedStatement.setString(2, dbcadServerHostname);
			preparedStatement.setString(3, pluginDBType);
			ResultSet paramsRS = preparedStatement.executeQuery();
			while (paramsRS.next()){
				dbPluginParamValues.put(paramsRS.getString("parameter_name"), paramsRS.getString("parameter_value"));
			}
			try{
				conn.close();
			}
			catch(Exception e){
				System.out.println("Error: Could not close connection" );
			}
			return dbPluginParamValues;
		}
		catch(Exception e){
			try{
				conn.close();
			}
			catch(Exception ex){
				System.out.println("Error: Could not close connection" );
			}
			e.printStackTrace();
			return null;
		}
	}
	public Boolean isDbChangeDeployed(String dbChangesId, String lob_id){
		Connection conn=null;
		String db_req_status = null;
		try{
			conn = datasource.getConnection();
			PreparedStatement preparedStatement = conn.prepareStatement("select status from db_request_status drs, lob_group_mapping gm where drs.db_group_id=gm.db_group_id and drs.db_request_id=? and lob_id =? ");
			preparedStatement.setString(1, dbChangesId);
			preparedStatement.setString(2, lob_id);
			ResultSet statusRS = preparedStatement.executeQuery();
			if (statusRS.next()){
				db_req_status = statusRS.getString("status");
			}
			else{
				return false;
			}
			try{
				conn.close();
			}
			catch(Exception e){
				System.out.println("Error: Could not close connection" );
			}
			System.out.println("isDbChangeDeployed "+dbChangesId+" "+ lob_id+" "+db_req_status);
			if (db_req_status.equals("DONE")) {
				return true;
			} else {
				return false;
			}
		}
		catch(Exception e){
			try{
				conn.close();
			}
			catch(Exception ex){
				System.out.println("Error: Could not close connection" );
			}
			e.printStackTrace();
			return false;
		}
	}
	public String getDBPluginTypeForDbChange(String dbChangeId){
		Connection conn=null;
		String db_type = null;
		try{
			conn = datasource.getConnection();
			PreparedStatement preparedStatement = conn.prepareStatement("select t.db_vendor from db_requests dbrq, db_schema s, database_type t where dbrq.schema_id = s.schema_id and s.db_type_id =t.db_type_id and dbrq.db_request_id = ?;");
			preparedStatement.setString(1, dbChangeId);
			ResultSet vendorRS = preparedStatement.executeQuery();
			vendorRS.next();
			db_type = vendorRS.getString("db_vendor");
			try{
				conn.close();
			}
			catch(Exception e){
				System.out.println("Error: Could not close connection" );
			}
			return db_type;
		}
		catch(Exception e){
			try{
				conn.close();
			}
			catch(Exception ex){
				System.out.println("Error: Could not close connection" );
			}
			e.printStackTrace();
			return null;
		}
	}
	
	public String getDBPluginTypeForDbID(String dbId){
		Connection conn=null;
		String db_type = null;
		try{
			conn = datasource.getConnection();
			PreparedStatement preparedStatement = conn.prepareStatement("select db_vendor from database_groups dg, database_instance di, database_type dt where dg.db_group_id = di.db_group_id and dg.db_type_id = dt.db_type_id and db_id=?");
			preparedStatement.setString(1, dbId);
			ResultSet vendorRS = preparedStatement.executeQuery();
			vendorRS.next();
			db_type = vendorRS.getString("db_vendor");
			try{
				conn.close();
			}
			catch(Exception e){
				System.out.println("Error: Could not close connection" );
			}
			return db_type;
		}
		catch(Exception e){
			try{
				conn.close();
			}
			catch(Exception ex){
				System.out.println("Error: Could not close connection" );
			}
			e.printStackTrace();
			return null;
		}
	}
	
	public String getDatabaseChangeScript(String dbChangeId){
		Connection conn=null;
		String db_req_code = null;
		try{
			conn = datasource.getConnection();
			PreparedStatement preparedStatement = conn.prepareStatement("select code from db_requests where db_request_id = ?");
			preparedStatement.setString(1, dbChangeId);
			ResultSet codeRS = preparedStatement.executeQuery();
			codeRS.next();
			db_req_code = codeRS.getString("code");
			try{
				conn.close();
			}
			catch(Exception e){
				System.out.println("Error: Could not close connection" );
			}
				return db_req_code;
		}
		catch(Exception e){
			try{
				conn.close();
			}
			catch(Exception ex){
				System.out.println("Error: Could not close connection" );
			}
			e.printStackTrace();
			return null;
		}
	}
	public ArrayList<DBInstance> getDeployableDatabaseInstancesForLobIdAndDbChange(String dbChangeId,String lobId){
		Connection conn=null;
		ArrayList<DBInstance> databaseInstances = new ArrayList<DBInstance>(); ;
		try{
			conn = datasource.getConnection();
			PreparedStatement preparedStatement = conn.prepareStatement("select  a.host, a.port, a.sid, a.db_id, a.db_group_id from( select di.db_id , di.host, di.port, di.sid , di.db_group_id from db_requests r ,  deployable_instance_schema dis,  database_instance di where r.schema_id = dis.schema_id and dis.db_id = di.db_id  and r.db_request_id = ?) a,  lob_group_mapping lgm where a.db_group_id = lgm.db_group_id and lgm.lob_id = ?");
			preparedStatement.setString(1, dbChangeId);
			preparedStatement.setString(2, lobId);
			ResultSet instanceRS = preparedStatement.executeQuery();
			while (instanceRS.next()){
				HashMap<String,String> pluginInstanceParameters = new HashMap<String,String>();
				DBInstance dbInstance = new DBInstance(instanceRS.getString("db_id"),instanceRS.getString("db_group_id"),instanceRS.getString("host"),instanceRS.getInt("port"),instanceRS.getString("sid"),pluginInstanceParameters);		
				PreparedStatement innerPS = conn.prepareStatement("select parameter_name, parameter_value from db_plugin_instance_parameters where db_id=?");
				innerPS.setString(1,instanceRS.getString("db_id"));
				ResultSet innerRS = innerPS.executeQuery();
				while (innerRS.next()){
					pluginInstanceParameters.put(innerRS.getString("parameter_name"), innerRS.getString("parameter_value"));
				}
				
				databaseInstances.add(dbInstance);
			}
			try{
				conn.close();
			}
			catch(Exception e){
				System.out.println("Error: Could not close connection" );
			}
				return databaseInstances;
		}
		catch(Exception e){
			try{
				conn.close();
			}
			catch(Exception ex){
				System.out.println("Error: Could not close connection" );
			}
			e.printStackTrace();
			return null;
		}
	}

	public int addDeploymentLog(String dbChangeId, DBInstance deployableInstance, String log) {
		Connection conn=null;
		try{
			conn = datasource.getConnection();
			PreparedStatement preparedStatement = conn.prepareStatement("insert into deployment_log (db_request_id,db_id,run_date,log) values (?,?,now(),?)");
			preparedStatement.setString(1, dbChangeId);
			preparedStatement.setString(2, deployableInstance.getDbId());
			preparedStatement.setString(3, log);
			preparedStatement.executeUpdate();
			try{
				conn.close();
			}
			catch(Exception e){
				System.out.println("Error: Could not close connection" );
			}
			return 0;
		}
		catch(Exception e){
			try{
				conn.close();
			}
			catch(Exception ex){
				System.out.println("Error: Could not close connection" );
			}
			e.printStackTrace();
			return 1;
		}
	}

	public JSONObject getDeploymentLog(String dbChangeId, String lobId) {
		Connection conn=null;
		JSONObject deploymentLog = new JSONObject();
		try{
			conn = datasource.getConnection();
			PreparedStatement preparedStatement = conn.prepareStatement("select dl.db_id, dl.run_date, dl.log from database_groups dbg, database_instance dbi, lob_group_mapping lgm, deployment_log dl where dbg.db_group_id = dbi.db_group_id and dbg.db_group_id = lgm.db_group_id and dl.db_id = dbi.db_id and dl.db_request_id = ? and lgm.lob_id = ? order by dl.db_id, dl.run_date");
			preparedStatement.setString(1, dbChangeId);
			preparedStatement.setString(2, lobId);
			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()){
				JSONObject logRecord = new JSONObject();
				logRecord.put("run_date", rs.getTimestamp("run_date"));
				logRecord.put("log", rs.getString("log"));
				if (deploymentLog.isNull(rs.getString("db_id"))){
					JSONArray logRecords = new JSONArray();
					logRecords.put(logRecord);
					deploymentLog.put(rs.getString("db_id"),logRecords);
				}
				else {
					((JSONArray)deploymentLog.get(rs.getString("db_id"))).put(logRecord);
				}
			}
			try{
				conn.close();
			}
			catch(Exception e){
				System.out.println("Error: Could not close connection" );
			}
				return deploymentLog;
		}
		catch(Exception e){
			try{
				conn.close();
			}
			catch(Exception ex){
				System.out.println("Error: Could not close connection" );
			}
			e.printStackTrace();
			return null;
		}
	}

	public String getDatabaseChangeSchema(String dbChangeId) {
		Connection conn=null;
		String schemaName = null;
		try{
			conn = datasource.getConnection();
			PreparedStatement preparedStatement = conn.prepareStatement("select schema_name from db_requests dbr, db_schema dbs where dbr.schema_id=dbs.schema_id and db_request_id=?");
			preparedStatement.setString(1, dbChangeId);
			ResultSet rs = preparedStatement.executeQuery();
			rs.next();
			schemaName = rs.getString("schema_name");
			try{
				conn.close();
			}
			catch(Exception e){
				System.out.println("Error: Could not close connection" );
			}
				return schemaName;
		}
		catch(Exception e){
			try{
				conn.close();
			}
			catch(Exception ex){
				System.out.println("Error: Could not close connection" );
			}
			e.printStackTrace();
			return null;
		}
	}
}


/*select di.* from db_requests dr, db_schema ds, database_groups dg,  database_instance di, lob_group_mapping lgm where dr.schema_id=ds.schema_id and ds.db_type_id = dg.db_type_id and dg.db_group_id = di.db_group_id and lgm.db_group_id =  dg.db_group_id and dr.db_request_id =? and  lgm.lob_id =? */