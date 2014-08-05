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
	
	//If lob_id is null, result is for all lobs
	protected JSONObject getDbChangeDeploymentStatus(String dbChangeId, String lob_id){
		ResultSet rs = null;
		Connection conn=null;
		JSONObject dbChangeDeploymentStatus = new JSONObject();
		try{
			conn = datasource.getConnection();
			StringBuilder query = new StringBuilder(" select db_req_mapping.lob_id, db_req_mapping.db_group_id, db_req_mapping.db_id, dbcs.status  "+
					" from "+
					" db_change_status dbcs "+
					" right outer join  "+
					" (select lgm.db_group_id, lgm.lob_id, dgim.db_id, dbr.db_request_id  "+
					"    from db_requests dbr, "+
					"         group_schema_mapping gsm, "+
					"         lob_group_mapping lgm, "+
					"         database_group_instance_mapping dgim "+
					"	where dbr.schema_id = gsm.schema_id "+
					"	and gsm.db_group_id=dgim.db_group_id "+
					"	and lgm.db_group_id=gsm.db_group_id "+
					"	and dgim.deployable=1) db_req_mapping "+
					"  on  "+
					"  dbcs.db_change_id = db_req_mapping.db_request_id  "+
					"  and dbcs.db_group_id = db_req_mapping.db_group_id "+
					"  and dbcs.db_id = db_req_mapping.db_id "+
					"  where db_req_mapping.db_request_id=? ");
			if (lob_id != null){
				query.append("  and db_req_mapping.lob_id=?");
			}
			PreparedStatement preparedStatement = conn.prepareStatement(query.toString());
			
			preparedStatement.setString(1, dbChangeId);
			if (lob_id != null){
				preparedStatement.setString(2, lob_id);
			}
			rs = preparedStatement.executeQuery();

			while (rs.next()){
				//Case no inserted instances from the same lob id
				if (!dbChangeDeploymentStatus.has(rs.getString("lob_id"))){
					//Create the lob
					dbChangeDeploymentStatus.put(rs.getString("lob_id"), new JSONObject());
					
					//Case no inserted instances from the same group id
					if (!dbChangeDeploymentStatus.getJSONObject(rs.getString("lob_id")).has(rs.getString("db_group_id"))){
						//Create the group
						dbChangeDeploymentStatus.getJSONObject(rs.getString("lob_id")).put(rs.getString("db_group_id"), new JSONObject());
					}
				}
				
				//Add instance status
				dbChangeDeploymentStatus.getJSONObject(rs.getString("lob_id")).getJSONObject(rs.getString("db_group_id")).put(rs.getString("db_id"), rs.getObject("status") == null ? DBCADController.NA_STATUS : rs.getInt("status"));
			}
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
		try{
			conn.close();
		}
		catch(Exception e){
			System.out.println("Error: Could not close connection" );
		}
		return dbChangeDeploymentStatus;
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
	
	protected ArrayList<String> getDatabaseInstanceIds(String dbPluginType){
		ResultSet rs = null;
		ArrayList<String> databaseIds = new ArrayList<String>();
		Connection conn=null;
		try{
			conn = datasource.getConnection();
			PreparedStatement preparedStatement = conn.prepareStatement("select db_id from database_instance where db_plugin_type = ?");
			preparedStatement.setString(1, dbPluginType);
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
	
	protected ArrayList<String> getDatabaseGroupIds(String dbTypeId){
		ResultSet rs = null;
		ArrayList<String> databaseGroups = new ArrayList<String>();
		Connection conn=null;
		try{
			conn = datasource.getConnection();
			PreparedStatement preparedStatement = conn.prepareStatement("select distinct db_group_id from database_groups where db_type_id=?");
			preparedStatement.setString(1, dbTypeId);
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
				dbType.put("db_plugin_type", rs.getString("db_vendor"));
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
	
	protected ArrayList<DBInstance> getDatabaseInstances(String generalFilter, int offset, int bulkSize, AtomicInteger totalRowNumber){
		ResultSet rs = null;
		ArrayList<DBInstance> databaseInstances = new ArrayList<DBInstance>();
		Connection conn=null;
		try{
			PreparedStatement preparedStatement;
			conn = datasource.getConnection();
			if (generalFilter == null || generalFilter.equals("")){
				preparedStatement = conn.prepareStatement("select SQL_CALC_FOUND_ROWS db_id, host,port,db_plugin_type from database_instance limit ?,?");
				preparedStatement.setInt(1, offset);
				preparedStatement.setInt(2, bulkSize);
			}
			else{
				preparedStatement = conn.prepareStatement("select SQL_CALC_FOUND_ROWS db_id, host,port,db_plugin_type from database_instance where db_id like ? or host like ? or port like ? or db_plugin_type like ? limit ?,?");
				preparedStatement.setString(1, "%"+generalFilter+"%");
				preparedStatement.setString(2, "%"+generalFilter+"%");
				preparedStatement.setString(3, "%"+generalFilter+"%");
				preparedStatement.setString(4, "%"+generalFilter+"%");
				preparedStatement.setInt(5, offset);
				preparedStatement.setInt(6, bulkSize);
			}
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
				DBInstance dbInstance = new DBInstance(rs.getString("db_id"),rs.getString("db_plugin_type"),rs.getString("host"),rs.getInt("port"),pluginInstanceParameters);
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

	protected int addDatabaseInstance(String dbId, String dbPluginType, String host, Integer port, HashMap<String, String> pluginInstanceParameters ){
		Connection conn=null;
	try{
		conn = datasource.getConnection();
		PreparedStatement preparedStatement = conn.prepareStatement("insert into database_instance (db_id,host,port,db_plugin_type) values (?,?,?,?)");
		preparedStatement.setString(1, dbId);
		preparedStatement.setString(2, host);
		preparedStatement.setInt(3, port);
		preparedStatement.setString(4, dbPluginType);
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
	
	protected String saveDatabaseInstance(String dbId, String dbPluginType, String host,Integer port, HashMap<String, String> pluginInstanceParameters ){
		Connection conn=null;
	try{
		conn = datasource.getConnection();
		PreparedStatement preparedStatement = conn.prepareStatement("update database_instance set host=?,port=? where db_id=?");
		preparedStatement.setString(1, host);
		preparedStatement.setInt(2, port);
		preparedStatement.setString(3, dbId);
		int result = preparedStatement.executeUpdate();
		preparedStatement =  conn.prepareStatement("insert into db_plugin_instance_parameters (db_id,parameter_name,parameter_value) values (?,?,?) on duplicate key update parameter_value=?");
		preparedStatement.setString(1, dbId);
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
		return (result > 0)  ? dbId:"";
	}catch(Exception e){
		try{
			conn.close();
		}
		catch(Exception ex){
			System.out.println("Error: Could not close connection" );
		}
		e.printStackTrace();
		return dbId;
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

	public boolean markDbChangeDeploymentStatus(String dbChangeId, String dbGroupId, String dbId, int status) {
		Connection conn=null;
		try{
			conn = datasource.getConnection();
			PreparedStatement preparedStatement = conn.prepareStatement("insert into db_change_status (db_change_id,db_group_id,db_id,status,update_date) values (?,?,?,?,now()) on duplicate key update status=?, update_date=now()");
			preparedStatement.setString(1, dbChangeId);
			preparedStatement.setString(2, dbGroupId);
			preparedStatement.setString(3, dbId);
			preparedStatement.setInt(4, status);
			preparedStatement.setInt(5, status);
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
	
	public int saveDatabaseChange(String dbChangeId, String schemaId,String dbChangeText) {
		Connection conn=null;
		try{
			conn = datasource.getConnection();
			PreparedStatement preparedStatement = conn.prepareStatement("update db_requests set code=?, last_changed_timestamp=? where db_request_id = ? and schema_id=?");
			preparedStatement.setString(1, dbChangeText);
			preparedStatement.setTimestamp(2, new java.sql.Timestamp(System.currentTimeMillis()));
			preparedStatement.setString(3, dbChangeId);
			preparedStatement.setString(4, schemaId);
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

	public ArrayList<DBGroup> getDatabaseGroups(String generalFilter, int offset, int bulkSize, AtomicInteger totalRowNumber) {
		ResultSet rsGroups = null;
		ArrayList<DBGroup> databaseGroups = new ArrayList<DBGroup>();
		Connection conn=null;
		try{
			conn = datasource.getConnection();
			PreparedStatement preparedStatement;
			if (generalFilter == null || generalFilter.equals("")){
				preparedStatement = conn.prepareStatement("select SQL_CALC_FOUND_ROWS dg.db_group_id, dg.db_type_id,dt.db_vendor,dt.db_role from database_groups dg, database_type dt where dg.db_type_id = dt.db_type_id limit ?,?");
				preparedStatement.setInt(1, offset);
				preparedStatement.setInt(2, bulkSize);
			}
			else{
				preparedStatement = conn.prepareStatement("select SQL_CALC_FOUND_ROWS dg.db_group_id, dg.db_type_id,dt.db_vendor,dt.db_role from database_groups dg, database_type dt where dg.db_type_id = dt.db_type_id and (lower(dg.db_group_id) like lower(?) or lower(dg.db_type_id) like lower(?) or lower(dt.db_vendor) like lower(?) or lower(dt.db_role) like lower(?) ) limit ?,?");
				preparedStatement.setString(1, "%"+generalFilter+"%");
				preparedStatement.setString(2, "%"+generalFilter+"%");
				preparedStatement.setString(3, "%"+generalFilter+"%");
				preparedStatement.setString(4, "%"+generalFilter+"%");
				preparedStatement.setInt(5, offset);
				preparedStatement.setInt(6, bulkSize);
			}
			rsGroups = preparedStatement.executeQuery();
			Statement stmt= conn.createStatement();
			ResultSet numRowsRs = stmt.executeQuery("SELECT FOUND_ROWS()");
            if(numRowsRs.next()){
            	totalRowNumber.set(numRowsRs.getInt(1));
            }
			while (rsGroups.next()){
				DBGroup dbGroup = new DBGroup();
				dbGroup.setDbGroupId(rsGroups.getString("db_group_id"));
				dbGroup.setDbTypeId(rsGroups.getString("db_type_id"));
				dbGroup.setDbPluginType(rsGroups.getString("db_vendor"));
				
				PreparedStatement lobsPreparedStatement = conn.prepareStatement("select lob_id from lob_group_mapping where db_group_id=?");
				lobsPreparedStatement.setString(1, rsGroups.getString("db_group_id"));
				ResultSet rsLobs = null;
				rsLobs = lobsPreparedStatement.executeQuery();
				ArrayList<String> lobs = new ArrayList<String>();
				while (rsLobs.next()){
					lobs.add(rsLobs.getString("lob_id"));
				}
				dbGroup.setLobs(lobs);
				
				PreparedStatement instancesPreparedStatement = conn.prepareStatement("select db_id,deployable from database_group_instance_mapping where db_group_id=?");
				instancesPreparedStatement.setString(1, rsGroups.getString("db_group_id"));
				ResultSet rsInstances = null;
				rsInstances = instancesPreparedStatement.executeQuery();
				HashMap<String,Boolean> instances = new HashMap<String,Boolean>();
				while (rsInstances.next()){
					instances.put(rsInstances.getString("db_id"), rsInstances.getBoolean("deployable"));
				}
				dbGroup.setDatabaseInstances(instances);
				
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
	
	public int addDatabaseGroup(DBGroup dbGroup) {
		Connection conn=null;
		int returnCode=REP_OK;
		try{
			conn = datasource.getConnection();
			PreparedStatement preparedStatement = conn.prepareStatement("insert into database_groups (db_group_id,db_type_id) values (?,?)");
			preparedStatement.setString(1, dbGroup.getDbGroupId());
			preparedStatement.setString(2, dbGroup.getDbTypeId());
			preparedStatement.executeUpdate();
			try{
				conn.close();
			}
			catch(Exception e){
				System.out.println("Error: Could not close connection" );
			}
			
			for (String lobId : dbGroup.getLobs()) {
				if (addDatabaseGroupLobMapping(dbGroup.getDbGroupId(),lobId) != REP_OK){
					returnCode = REP_ERR;
				}
			}
			
			HashMap<String,Boolean> dbInstances = dbGroup.getDatabaseInstances(); 
			for (String dbInstanceId : dbInstances.keySet()){
				addDatabaseGroupInstanceMapping(dbGroup.getDbGroupId(),dbInstanceId,dbInstances.get(dbInstanceId));
			}
			return returnCode;
		}
		catch(Exception e){
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
			return REP_OK;
		}
		catch(Exception e){
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
	public int addDatabaseGroupInstanceMapping(String dbGroupId, String dbInstanceId, Boolean isDeployable) {
		Connection conn=null;
		try{
			conn = datasource.getConnection();
			PreparedStatement preparedStatement = conn.prepareStatement("insert into database_group_instance_mapping (db_group_id,db_id,deployable) values (?,?,?)");
			preparedStatement.setString(1, dbGroupId);
			preparedStatement.setString(2, dbInstanceId);
			preparedStatement.setBoolean(3, isDeployable);
			preparedStatement.executeUpdate();
			try{
				conn.close();
			}
			catch(Exception e){
				System.out.println("Error: Could not close connection" );
			}
			return REP_OK;
		}
		catch(Exception e){
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
	
	
	public JSONArray getDatabaseChangesDeploymentStatus(String generalFilter, int offset, int bulkSize, AtomicInteger totalRowNumber) {
		ResultSet rs = null;
		//ArrayList<HashMap<String,String>> databaseChangesDeploymentStatus = new ArrayList<HashMap<String,String>>();
		JSONArray databaseChangesDeploymentStatus = new JSONArray();
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
//			PreparedStatement statusPreparedStatement = conn.prepareStatement(
//							"select db_req_mapping.lob_id, db_req_mapping.db_group_id, db_req_mapping.db_id, dbcs.status "+
//							"from "+
//							" db_change_status dbcs "+
//							" right outer join  "+
//							" (select lgm.db_group_id, lgm.lob_id, dgim.db_id, dbr.db_request_id  "+
//							"    from db_requests dbr, "+
//							"         group_schema_mapping gsm, "+
//							"         lob_group_mapping lgm, "+
//							"         database_group_instance_mapping dgim "+
//							"	where dbr.schema_id = gsm.schema_id "+
//							"	and gsm.db_group_id=dgim.db_group_id "+
//							"	and lgm.db_group_id=gsm.db_group_id "+
//							"	and dgim.deployable=1) db_req_mapping "+
//							"  on  "+
//							"  dbcs.db_change_id = db_req_mapping.db_request_id  "+
//							"  and dbcs.db_group_id = db_req_mapping.db_group_id "+
//							"  and dbcs.db_id = db_req_mapping.db_id "+
//							"  where db_req_mapping.db_request_id=?"
//							);
//			ResultSet statusRs = null;
			while (rs.next()){
//				//HashMap<String,String> dbChangeStatus = new HashMap<String,String>();
//				//dbChangeStatus.put("db_request_id", rs.getString("db_request_id"));
//				JSONObject dbChangeDeploymentStatus = new JSONObject();
//				statusPreparedStatement.setString(1, rs.getString("db_request_id"));
//				statusRs = statusPreparedStatement.executeQuery();
//
//				while (statusRs.next()){
//					//Case no inserted instances from the same lob id
//					if (!dbChangeDeploymentStatus.has(statusRs.getString("lob_id"))){
//						//Create the lob
//						dbChangeDeploymentStatus.put(statusRs.getString("lob_id"), new JSONObject());
//						
//						//Case no inserted instances from the same group id
//						if (!dbChangeDeploymentStatus.getJSONObject(statusRs.getString("lob_id")).has(statusRs.getString("db_group_id"))){
//							//Create the group
//							dbChangeDeploymentStatus.getJSONObject(statusRs.getString("lob_id")).put(statusRs.getString("db_group_id"), new JSONObject());
//						}
//					}
//					
//					//Add instance status
//					dbChangeDeploymentStatus.getJSONObject(statusRs.getString("lob_id")).getJSONObject(statusRs.getString("db_group_id")).put(statusRs.getString("db_id"), statusRs.getObject("status") == null ? DBCADController.NA_STATUS : statusRs.getInt("status"));
//
//				}
				JSONObject dbChangeAttributes = new JSONObject();
				dbChangeAttributes.put("db_request_code", rs.getString("code"));
				dbChangeAttributes.put("schema_id", rs.getString("schema_id"));
				dbChangeAttributes.put("db_change_id", rs.getString("db_request_id"));
				dbChangeAttributes.put("deployment_status", getDbChangeDeploymentStatus(rs.getString("db_request_id"),null));
				databaseChangesDeploymentStatus.put(dbChangeAttributes);
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
		return databaseChangesDeploymentStatus;
	}

	public ArrayList<DBSchema> getDatabaseSchemas(String generalFilter, int offset, int bulkSize, AtomicInteger totalRowNumber) {
		ResultSet rs = null;
		ArrayList<DBSchema> databaseSchemas = new ArrayList<DBSchema>();
		Connection conn=null;
		try{
			PreparedStatement preparedStatement;
			conn = datasource.getConnection();
			if (generalFilter == null || generalFilter.equals("")){
				preparedStatement = conn.prepareStatement("select SQL_CALC_FOUND_ROWS schema_id, db_type_id from db_schema limit ?,?");
				preparedStatement.setInt(1, offset);
				preparedStatement.setInt(2, bulkSize);
			}
			else{
				preparedStatement = conn.prepareStatement("select SQL_CALC_FOUND_ROWS schema_id, db_type_id from db_schema where schema_id like ? or db_type_id like ? limit ?,?");
				preparedStatement.setString(1, "%"+generalFilter+"%");
				preparedStatement.setString(2, "%"+generalFilter+"%");
				preparedStatement.setInt(3, offset);
				preparedStatement.setInt(4, bulkSize);
			}
			rs = preparedStatement.executeQuery();
			Statement stmt= conn.createStatement();
			ResultSet numRowsRs = stmt.executeQuery("SELECT FOUND_ROWS()");
            if(numRowsRs.next()){
            	totalRowNumber.set(numRowsRs.getInt(1));
            }
            numRowsRs.close();
			PreparedStatement groupsPreparedStatement = conn.prepareStatement("select db_group_id, schema_name from group_schema_mapping where schema_id=?");
			ResultSet groupsRs = null;
			while (rs.next()){
				groupsPreparedStatement.setString(1, rs.getString("schema_id"));
				groupsRs = groupsPreparedStatement.executeQuery();
				HashMap<String,String> databaseGroups = new HashMap<String,String>();
				while (groupsRs.next()){
					databaseGroups.put(groupsRs.getString("db_group_id"),groupsRs.getString("schema_name"));
				}
				DBSchema database_schema = new DBSchema(rs.getString("schema_id"),rs.getString("db_type_id"),databaseGroups);
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

	public int addDatabaseSchema(String schemaId, String dbTypeId) {
		Connection conn=null;
		try{
			conn = datasource.getConnection();
			PreparedStatement preparedStatement = conn.prepareStatement("insert into db_schema (schema_id,db_type_id) values (?,?)");
			preparedStatement.setString(1, schemaId);
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

	/*public int addDeployableDBInstance(String schemaId, String dbId) {
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
	}*/
	
	public int addSchemaToDBGroup(String dbGroupId, String schemaId, String schemaName) {
		Connection conn=null;
		try{
			conn = datasource.getConnection();
			PreparedStatement preparedStatement = conn.prepareStatement("insert into group_schema_mapping (db_group_id,schema_id,schema_name) values (?,?,?)");
			preparedStatement.setString(1, dbGroupId);
			preparedStatement.setString(2, schemaId);
			preparedStatement.setString(3, schemaName);
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
	public Boolean isDbChangeDeployed(String dbChangesId, String dbGroupId, String dbId){
		Connection conn=null;
		int db_req_status = DBCADController.ERROR_STATUS;
		try{
			conn = datasource.getConnection();
			PreparedStatement preparedStatement = conn.prepareStatement("select status from db_change_status where db_change_id=? and db_group_id=? and db_id=?");
			preparedStatement.setString(1, dbChangesId);
			preparedStatement.setString(2, dbGroupId);
			preparedStatement.setString(3, dbId);
			ResultSet statusRS = preparedStatement.executeQuery();
			if (statusRS.next()){
				db_req_status = statusRS.getInt("status");
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
			if (db_req_status == DBCADController.OK_STATUS) {
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
	public ArrayList<DeploymentDetails> getDeployableDatabaseInstancesAndSchemaNamesForLobIdAndDbChange(String dbChangeId,String lobId){
		Connection conn=null;
		ArrayList<DeploymentDetails> deploymentDetailsList = new ArrayList<DeploymentDetails>(); ;
		try{
			conn = datasource.getConnection();
			PreparedStatement preparedStatement = conn.prepareStatement("select dbi.host, dbi.port,dbi.db_plugin_type, dbi.db_id,lgm.db_group_id,gsm.schema_name from db_requests dbr, group_schema_mapping gsm, lob_group_mapping lgm, database_group_instance_mapping dgim, database_instance dbi where dbr.schema_id = gsm.schema_id and lgm.db_group_id=gsm.db_group_id and dgim.db_group_id = gsm.db_group_id and dbi.db_id = dgim.db_id and dgim.deployable = 1 and lgm.lob_id=? and dbr.db_request_id=?");
			preparedStatement.setString(1, lobId);
			preparedStatement.setString(2, dbChangeId);
			ResultSet instanceRS = preparedStatement.executeQuery();
			while (instanceRS.next()){
				HashMap<String,String> pluginInstanceParameters = new HashMap<String,String>();
				PreparedStatement innerPS = conn.prepareStatement("select parameter_name, parameter_value from db_plugin_instance_parameters where db_id=?");
				innerPS.setString(1,instanceRS.getString("db_id"));
				ResultSet innerRS = innerPS.executeQuery();
				while (innerRS.next()){
					pluginInstanceParameters.put(innerRS.getString("parameter_name"), innerRS.getString("parameter_value"));
				}
				DBInstance dbInstance = new DBInstance(instanceRS.getString("db_id"),instanceRS.getString("db_plugin_type"),instanceRS.getString("host"),instanceRS.getInt("port"),pluginInstanceParameters);
				deploymentDetailsList.add(new DeploymentDetails(dbInstance,instanceRS.getString("db_group_id"),lobId,instanceRS.getString("schema_name"),dbChangeId));
			}
			try{
				conn.close();
			}
			catch(Exception e){
				System.out.println("Error: Could not close connection" );
			}
				return deploymentDetailsList;
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
			PreparedStatement preparedStatement = conn.prepareStatement("select dl.db_id, dl.run_date,dl.log from deployment_log dl, db_requests dbr, group_schema_mapping gcm, database_group_instance_mapping dgim, lob_group_mapping lgm where dl.db_id=dgim.db_id and dbr.db_request_id = dl.db_request_id and gcm.schema_id = dbr.schema_id and gcm.db_group_id = dgim.db_group_id and dgim.db_group_id = lgm.db_group_id and lgm.lob_id=? and dl.db_request_id=?");
			preparedStatement.setString(1, lobId);
			preparedStatement.setString(2, dbChangeId);
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

	public String getDatabaseChangeSchema(String dbChangeId,String dbGroupId) {
		Connection conn=null;
		String schemaName = null;
		try{
			conn = datasource.getConnection();
			PreparedStatement preparedStatement = conn.prepareStatement("select gsm.schema_name from db_requests dbr, db_schema dbs, group_schema_mapping gsm where dbr.schema_id=dbs.schema_id and dbs.schema_id=gsm.schema_id and db_request_id=? and gsm.db_group_id=?");
			preparedStatement.setString(1, dbChangeId);
			preparedStatement.setString(2, dbGroupId);
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

	public ArrayList<String> getDbChangesSortedByCreateDate(ArrayList<String> unsortedDbChangeIds) {
		StringBuilder inClause = new StringBuilder();
		if (unsortedDbChangeIds.size()>0){
			inClause.append("?");
		}
		for (int i=1; i<unsortedDbChangeIds.size();i++){
			inClause.append(",?");
		}
		
		Connection conn=null;
		ArrayList<String> sortedDbChangeIds = new ArrayList<String>();
		try{
			conn = datasource.getConnection();
			PreparedStatement preparedStatement = conn.prepareStatement("select db_request_id from db_requests where db_request_id in ("+inClause+") order by created_timestamp");
			int i=1;
			for (String dbChangeId : unsortedDbChangeIds){
				preparedStatement.setString(i, dbChangeId);
				i++;
			}
			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()){
				sortedDbChangeIds.add(rs.getString("db_request_id"));
			}
			
			try{
				conn.close();
			}
			catch(Exception e){
				System.out.println("Error: Could not close connection" );
			}
				return sortedDbChangeIds;
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