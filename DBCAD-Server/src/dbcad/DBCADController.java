package dbcad;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.jasypt.util.password.StrongPasswordEncryptor;
import org.jasypt.util.text.BasicTextEncryptor;
import org.jasypt.util.text.StrongTextEncryptor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.access.annotation.Secured;
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.Gson;

import dbcad.services.api.DBService;

@Controller
public class DBCADController {
	public final static String ENCRYPTION_KEY="PBEWITHFPBEWITHF";
	public final static SecretKeySpec ENC_KEY = new SecretKeySpec(ENCRYPTION_KEY.getBytes(), "AES");
	private static RepositoryHandler repHandler;
	private static final int TABLE_MAX_ROWS = 10; 

	static {
		repHandler = new RepositoryHandler();
	}
	
	@PostConstruct
	public void initializeDBCAD(){
		
		for (DBService dbService : DBService.getDBServices()){
			ArrayList<String> parameters = new ArrayList<String>();
			for (String globalParameterName : dbService.getGlobalParameterNames()){
				parameters.add(globalParameterName);
			}
			repHandler.addDBPluginConfig(dbService.getDBType(), parameters);
		}
	}
	
/*	@RequestMapping(value = "/", method = RequestMethod.GET)
	public ModelAndView rootPage() {
		return manageDatabases();
	}*/
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public ModelAndView index() {
		return new ModelAndView("index");
	}
	
	//@PreAuthorize("hasRole('ROLE_ADMIN')")
	//@Secured("ROLE_ADMIN")
	@RequestMapping(value = "/manage-databases", method = RequestMethod.GET)
	public ModelAndView manageDatabases() {
		Gson gson = new Gson();
		HashMap<String, ArrayList<String>> options;
		ArrayList<HashMap<String, String>> typeTableValues;
		ArrayList<DBInstance> instanceTableValues;
		ArrayList<DBGroup> groupTableValues;
		ArrayList<DBSchema> schemaTableValues;
		ModelAndView mav = new ModelAndView("ManageDatabases");
		options = new HashMap<String, ArrayList<String>>();
		options.put("db_roles", repHandler.getDatabaseRoles());
		//options.put("db_vendors", repHandler.getDatabaseVendors());
		options.put("db_groups", repHandler.getDatabaseGroupIds());
		options.put("db_types", repHandler.getDatabaseTypeIds());
		options.put("db_instance_ids",repHandler.getDatabaseIds());
		options.put("db_plugin_types", DBService.getAvailableDbPluginTypeNames());
		options.put("lobs", repHandler.getLobs());
		typeTableValues = repHandler.getDatabaseTypes();
		AtomicInteger instancesTableTotalNumberOfRows = new AtomicInteger();
		instanceTableValues = repHandler.getDatabaseInstances(null,0,TABLE_MAX_ROWS,instancesTableTotalNumberOfRows);
		AtomicInteger groupsTableTotalNumberOfRows = new AtomicInteger();
		groupTableValues = repHandler.getDatabaseGroups(null,0,TABLE_MAX_ROWS,groupsTableTotalNumberOfRows);
		AtomicInteger schemasTableTotalNumberOfRows = new AtomicInteger();
		schemaTableValues = repHandler.getDatabaseSchemas(null,0,TABLE_MAX_ROWS,schemasTableTotalNumberOfRows);
		mav.addObject("options", options);
		mav.addObject("type_table_values", typeTableValues);
		mav.addObject("instance_table_values", instanceTableValues);
		mav.addObject("instance_table_values_json", gson.toJson(instanceTableValues));
		mav.addObject("instancesNumOfPages",Math.ceil(1.0*instancesTableTotalNumberOfRows.intValue()/TABLE_MAX_ROWS));
		mav.addObject("instancesCurrentPage",1);
		mav.addObject("group_table_values", groupTableValues);
		mav.addObject("group_table_values_json", gson.toJson(groupTableValues));
		mav.addObject("groupsNumOfPages", Math.ceil(1.0*groupsTableTotalNumberOfRows.intValue()/TABLE_MAX_ROWS));
		mav.addObject("groupsCurrentPage", 1);
		mav.addObject("schema_table_values", schemaTableValues);
		mav.addObject("schema_table_values_json", gson.toJson(schemaTableValues));
		mav.addObject("schemasNumOfPages",Math.ceil(1.0*schemasTableTotalNumberOfRows.intValue()/TABLE_MAX_ROWS));
		mav.addObject("schemasCurrentPage",1);
		mav.addObject("dbPluginsConfig",getDBPluginsConfig());
		return mav;
	}

	@PreAuthorize("hasRole('ROLE_ADMIN')")
	//@Secured("sss")
	@RequestMapping(value = "/manage-db-changes", method = RequestMethod.GET)
	public ModelAndView ManageDBChangesView() {
		Gson gson = new Gson();
		HashMap<String, ArrayList<String>> options;
		ArrayList<HashMap<String, String>> dbChangesTableValues;
		AtomicInteger totalNumberOfRows = new AtomicInteger();
		dbChangesTableValues= repHandler.getDatabaseChangeLobsStatus(null,0, TABLE_MAX_ROWS,totalNumberOfRows);
		options = new HashMap<String, ArrayList<String>>();
		options.put("lobs", repHandler.getLobs());
		options.put("db_schemas", repHandler.getDatabaseSchemaIds());
		//options.put("db_changes", repHandler.getNextDBChanges(10, null));
		return new ModelAndView("ManageDBChanges", "options", options).addObject("dbChangesTableValues",gson.toJson(dbChangesTableValues)).addObject("dbChangesNoOfPages",Math.ceil(1.0*totalNumberOfRows.intValue()/TABLE_MAX_ROWS)).addObject("dbChangesCurrentPage",1).addObject("dbChangesLobList", gson.toJson(repHandler.getLobs()));
//		HashMap<String, ArrayList<String>> options = new HashMap<String, ArrayList<String>>();
//		options.put("db_schemas", repHandler.getDatabaseSchemaIds());
//		options.put("db_changes", repHandler.getNextDBChanges(10, null));
//		return new ModelAndView("ManageDBChanges", "options", options);
	}
	
/*	@RequestMapping(value = "/deploy", method = RequestMethod.GET)
	public ModelAndView deployDBChangesView() {
		HashMap<String, ArrayList<String>> options;
		ArrayList<HashMap<String, String>> dbChangesTableValues;
		AtomicInteger totalNumberOfRows = new AtomicInteger();
		dbChangesTableValues= repHandler.getDatabaseChangeLobsStatus(null,0, TABLE_MAX_ROWS,totalNumberOfRows);
		options = new HashMap<String, ArrayList<String>>();
		options.put("lobs", repHandler.getLobs());
		options.put("db_schemas", repHandler.getDatabaseSchemaIds());
		//options.put("db_changes", repHandler.getNextDBChanges(10, null));
		return new ModelAndView("DeployDBChanges", "options", options).addObject("dbChangesTableValues",dbChangesTableValues).addObject("dbChangesNoOfPages",Math.ceil(1.0*totalNumberOfRows.intValue()/TABLE_MAX_ROWS)).addObject("currentPage",1);
	}*/
	
	@RequestMapping(value = "/getDbChangesTablePage", method = RequestMethod.POST)
	public @ResponseBody String getDBChangesTablePage(@RequestParam(value = "page") int page, @RequestParam(value = "searchFilter", defaultValue = "{}") JSONObject searchFilterJSON) {
	//public ModelAndView getDBChangesTablePage(@RequestParam(value = "page") int page, @RequestParam(value = "searchFilter", defaultValue = "{}") JSONObject searchFilterJSON) {
		Gson gson = new Gson();
		JSONObject jsonResponse = new JSONObject();
		//HashMap<String, ArrayList<String>> options;
		ArrayList<HashMap<String, String>> dbChangesTableValues;
		AtomicInteger totalNumberOfRows = new AtomicInteger();
		dbChangesTableValues= repHandler.getDatabaseChangeLobsStatus(searchFilterJSON.isNull("generalFilter") ? null : searchFilterJSON.getString("generalFilter"),(page-1)*TABLE_MAX_ROWS, TABLE_MAX_ROWS,totalNumberOfRows);
		jsonResponse.put("dbChangesTableValues", dbChangesTableValues);
		jsonResponse.put("dbChangesLobList", repHandler.getLobs());
		jsonResponse.put("dbChangesNoOfPages",Math.ceil(1.0*totalNumberOfRows.intValue()/TABLE_MAX_ROWS));
		jsonResponse.put("dbChangesCurrentPage",page);
		return jsonResponse.toString();
		//options = new HashMap<String, ArrayList<String>>();
		//options.put("lobs", repHandler.getLobs());
		//options.put("db_changes", repHandler.getNextDBChanges(10, null));
		//return new ModelAndView("ManageDBChangesTable", "options", options).addObject("dbChangesTableValues",dbChangesTableValues).addObject("noOfPages",Math.ceil(1.0*totalNumberOfRows.intValue()/TABLE_MAX_ROWS)).addObject("currentPage",page);
	}
	
	
	@RequestMapping(value = "/getDbSchemasTablePage", method = RequestMethod.POST)
	public @ResponseBody String getDBSchemasTablePage(@RequestParam(value = "page") int page,@RequestParam(value = "searchFilter", defaultValue = "{}") JSONObject searchFilterJSON) {
		Gson gson = new Gson();
		JSONObject jsonResponse = new JSONObject();
		ArrayList<DBSchema> dbSchemasTableValues;
		AtomicInteger totalNumberOfRows = new AtomicInteger();
		dbSchemasTableValues= repHandler.getDatabaseSchemas(searchFilterJSON.isNull("generalFilter") ? null : searchFilterJSON.getString("generalFilter"),(page-1)*TABLE_MAX_ROWS, TABLE_MAX_ROWS,totalNumberOfRows);
		jsonResponse.put("schemaTableValues", gson.toJson(dbSchemasTableValues));
		jsonResponse.put("schemasNumOfPages", Math.ceil(1.0*totalNumberOfRows.intValue()/TABLE_MAX_ROWS));
		jsonResponse.put("schemasCurrentPage",page);
		return jsonResponse.toString();
	}
	
	@RequestMapping(value = "/getDbInstancesTablePage", method = RequestMethod.POST)
	public @ResponseBody String getDBInstancesTablePage(@RequestParam(value = "page") int page,@RequestParam(value = "searchFilter", defaultValue = "{}") JSONObject searchFilterJSON) {
		Gson gson = new Gson();
		JSONObject jsonResponse = new JSONObject();
		ArrayList<DBInstance> dbInstancesTableValues;
		AtomicInteger totalNumberOfRows = new AtomicInteger();
		dbInstancesTableValues= repHandler.getDatabaseInstances(searchFilterJSON.isNull("generalFilter") ? null : searchFilterJSON.getString("generalFilter"),(page-1)*TABLE_MAX_ROWS, TABLE_MAX_ROWS,totalNumberOfRows);
		jsonResponse.put("instanceTableValues", gson.toJson(dbInstancesTableValues));
		jsonResponse.put("instancesNumOfPages", Math.ceil(1.0*totalNumberOfRows.intValue()/TABLE_MAX_ROWS));
		jsonResponse.put("instancesCurrentPage",page);
		return jsonResponse.toString();
	}
	
	@RequestMapping(value = "/getDbGroupsTablePage", method = RequestMethod.POST)
	public @ResponseBody String getDBGroupsTablePage(@RequestParam(value = "page") int page,@RequestParam(value = "searchFilter", defaultValue = "{}") JSONObject searchFilterJSON) {
		Gson gson = new Gson();
		JSONObject jsonResponse = new JSONObject();
		ArrayList<DBGroup> dbGroupsTableValues;
		AtomicInteger totalNumberOfRows = new AtomicInteger();
		dbGroupsTableValues= repHandler.getDatabaseGroups(searchFilterJSON.isNull("generalFilter") ? null : searchFilterJSON.getString("generalFilter"),(page-1)*TABLE_MAX_ROWS, TABLE_MAX_ROWS,totalNumberOfRows);
		jsonResponse.put("groupTableValues", gson.toJson(dbGroupsTableValues));
		jsonResponse.put("groupsNumOfPages", Math.ceil(1.0*totalNumberOfRows.intValue()/TABLE_MAX_ROWS));
		jsonResponse.put("groupsCurrentPage",page);
		return jsonResponse.toString();
	}

	
	@RequestMapping(value = "/rest/deploy/{lob_id}", method = RequestMethod.PUT)
	public @ResponseBody
	String deployDBChangesOnLOB(@PathVariable("lob_id") String lobId,
			@RequestParam("db_changes") JSONArray dbChanges,@RequestParam("mark_only") boolean markOnly) {
		/*String dbChangeId;
		for (int i = 0; i < dbChanges.length(); i++) {
			dbChangeId = dbChanges.getString(i);
			repHandler.markDbChangeAsDeployed(dbChangeId, lobId);
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		DBService dbService = DBService.getDBService("Oracle");
		HashMap<String,String> parameters = new HashMap<String,String>();
		parameters.put("sqlPlusPath", "D:\\Ayelet Backup\\app\\ayelets\\product\\11.1.0\\client_1\\sqlplus.exe");
		dbService.initializeDBService("dbqa", 1524, "dvlp2",parameters);
		dbService.runScript("Select * from v$database;");*/
		/*DBService dbService = DBService.getDBService("Mysql");
		HashMap<String,String> parameters = new HashMap<String,String>();
		parameters.put("mysqlClientPath", "c:\\mysql.exe");
		dbService.initializeDBService("vm-qa-acdb", 3306, "mysql",parameters);
		dbService.runScript("Select * from mysql.user;"); */
		if (markOnly){
			String dbChangeId;
			for (int i = 0; i < dbChanges.length(); i++) {
				dbChangeId = dbChanges.getString(i);
				repHandler.markDbChangeDeploymentStatus(dbChangeId, lobId, "DONE");
			}
		}
		else{
			(new DeployThread(repHandler,lobId,dbChanges)).start();
		}
		
		return "JOB Was sent";
	}

	@RequestMapping(value = "/rest/query/{lob_id}", method = RequestMethod.GET)
	public @ResponseBody
	String queryLobForDBChanges(@PathVariable("lob_id") String lobId,
			@RequestParam("query_data") JSONArray queryData) {
		JSONObject jsonResponse = new JSONObject();
		String dbChangeId;
		for (int i = 0; i < queryData.length(); i++) {
			dbChangeId = queryData.getString(i);
			jsonResponse.put(dbChangeId,
					repHandler.checkDbChanges(dbChangeId, lobId));
		}
		return jsonResponse.toString();
	}

	@RequestMapping(value = "/rest/db_type", method = RequestMethod.PUT)
	public @ResponseBody
	String addDBType(@ModelAttribute(value = "db_type") DBType dbType,
			BindingResult result) {
		String returnText;
		if (!result.hasErrors()) {
			String dbTypeId = repHandler.addDatabaseType(dbType.getDbPluginType(),
					dbType.getDbRole());
			if (!dbTypeId.equals("")) {
				returnText = dbTypeId;
			} else {
				returnText = "Error: DB Type was not added";
			}
		} else {
			returnText = "Error: DB Type was not added";
		}
		return returnText;
	}

	@RequestMapping(value = "/rest/db_type/{db_type_id}", method = RequestMethod.DELETE)
	public @ResponseBody
	String deleteDBType(@PathVariable("db_type_id") String dbTypeId) {
		String returnText;
		System.out.println("TYPE::: " + dbTypeId);
		if (repHandler.deleteDatabaseType(dbTypeId)) {
			returnText = "DB Type Deleted.";
		} else {
			returnText = "Error: DB Type was not deleted";
		}
		return returnText;
	}

	@RequestMapping(value = "/rest/db_instance/{db_instance_id}", method = RequestMethod.PUT)
	public @ResponseBody
	String addDBInstance(@PathVariable("db_instance_id") String dbInstanceId, @RequestParam(value = "dbPluginType") String dbPluginType, @RequestParam(value = "dbHost") String dbHost,
			@RequestParam(value = "dbPort") Integer dbPort,@RequestParam(value = "pluginInstanceParameters") JSONObject pluginInstanceParameters) {
		DBService dbService = DBService.getDBService(dbPluginType);
		HashMap<String,HashMap<String,String>> parameterAttributes = dbService.getInstanceParameterAttributes();

		try{
			BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
			textEncryptor.setPassword(ENCRYPTION_KEY);
		
			Iterator<?> keys = pluginInstanceParameters.keys();
			while( keys.hasNext() ){
	            String key = (String)keys.next();
	            if (parameterAttributes.get(key) != null && parameterAttributes.get(key).get("ENCRYPTED").equals("TRUE") ){
	            	String clearTextValue = (String)pluginInstanceParameters.get(key);
	            	pluginInstanceParameters.put(key,textEncryptor.encrypt(clearTextValue));
	            }
	        }
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		DBInstance dbInstance = new DBInstance(dbInstanceId,dbPluginType,dbHost,dbPort,Utils.jsonToHashMap(pluginInstanceParameters));
		String returnText;
		int retCode = repHandler.addDatabaseInstance(dbInstance.getDbId(),dbInstance.getDbPluginType(), dbInstance.getDbHost(),
				dbInstance.getDbPort(), dbInstance.getPluginInstanceParameters());
		if (retCode == RepositoryHandler.REP_OK) {
			returnText = dbInstanceId;
		} else {
			returnText = "Error: DB Instance was not added";
		}
		return returnText;
	}
	
	@RequestMapping(value = "/rest/db_instance/{db_instance_id}", method = RequestMethod.POST)
	public @ResponseBody
	String saveDBInstance(@PathVariable("db_instance_id") String dbInstanceId, @RequestParam(value = "dbPluginType") String dbPluginType,@RequestParam(value = "dbHost") String dbHost,
			@RequestParam(value = "dbPort") Integer dbPort,@RequestParam(value = "pluginInstanceParameters") JSONObject pluginInstanceParameters) {
		
		DBService dbService = DBService.getDBService(dbPluginType);
		HashMap<String,HashMap<String,String>> parameterAttributes = dbService.getInstanceParameterAttributes();

		try{
			BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
			textEncryptor.setPassword(ENCRYPTION_KEY);
		
			Iterator<?> keys = pluginInstanceParameters.keys();
			while( keys.hasNext() ){
	            String key = (String)keys.next();
	            if (parameterAttributes.get(key) != null && parameterAttributes.get(key).get("ENCRYPTED").equals("TRUE") ){
	            	String clearTextValue = (String)pluginInstanceParameters.get(key);
	            	pluginInstanceParameters.put(key,textEncryptor.encrypt(clearTextValue));
	            }
	        }
			
		}catch(Exception e){
			e.printStackTrace();
		}
		DBInstance dbInstance = new DBInstance(dbInstanceId,dbPluginType,dbHost,dbPort,Utils.jsonToHashMap(pluginInstanceParameters));
		String returnText;
		String newDbInstanceId = repHandler.saveDatabaseInstance(dbInstance.getDbId(),dbInstance.getDbPluginType(),dbInstance.getDbHost(),
				dbInstance.getDbPort(), dbInstance.getPluginInstanceParameters());
		if (!newDbInstanceId.equals("")) {
			returnText = newDbInstanceId;
		} else {
			returnText = "Error: DB Instance was not saved";
		}
		return returnText;
	}

	@RequestMapping(value = "/rest/db_instance/{db_instance_id}", method = RequestMethod.DELETE)
	public @ResponseBody
	String deleteDBInstance(@PathVariable("db_instance_id") String dbInstanceId) {
		String returnText;
		System.out.println("INSTANCE::: " + dbInstanceId);
		if (repHandler.deleteDatabaseInstance(dbInstanceId)) {
			returnText = "DB instance Deleted.";
		} else {
			returnText = "Error: DB Instance was not deleted";
		}
		return returnText;
	}

	@RequestMapping(value = "/rest/db_change/{schema_id}/{db_change_id}", method = RequestMethod.PUT)
	public @ResponseBody
	String addDBChange(
			@RequestParam(value = "db_change_text") String dbChangeText,
			@PathVariable("schema_id") String schemaId,
			@PathVariable("db_change_id") String dbChangeId) {
		String returnText;
		int statusCode = repHandler.addDatabaseChange(dbChangeId, schemaId,
				dbChangeText);
		if (statusCode == 0) {
			returnText = "Database Change added";
		} else {
			returnText = "Error: Database Change was not added";
		}
		return returnText;
	}

	@RequestMapping(value = "/rest/db_change/{schema_id}/{db_change_id}", method = RequestMethod.POST)
	public @ResponseBody
	String saveDBChange(
			@RequestParam(value = "db_change_text") String dbChangeText,
			@PathVariable("schema_id") String schemaId,
			@PathVariable("db_change_id") String dbChangeId) {
		String returnText;
		int statusCode = repHandler.saveDatabaseChange(dbChangeId, schemaId,
				dbChangeText);
		if (statusCode == 0) {
			returnText = "Database Change saved";
		} else {
			returnText = "Error: Database Change was not saved";
		}
		return returnText;
	}
	
	@RequestMapping(value = "/rest/db_change/{db_change_id}", method = RequestMethod.DELETE)
	public @ResponseBody
	String deleteDBChange(@PathVariable("db_change_id") String dbChangeId) {
		String returnText;
		int statusCode = repHandler.deleteDatabaseChange(dbChangeId);
		if (statusCode == 0) {
			returnText = "DB Change Deleted";
		} else {
			returnText = "Error: DB Change was not deleted";
		}
		return returnText;
	}
	
	@RequestMapping(value = "/rest/db_group/{db_group_id}", method = RequestMethod.PUT)
	public @ResponseBody
	String addDBGroup(@RequestParam(value = "dbTypeId") String dbTypeId,@PathVariable("db_group_id") String dbGroupId,@RequestParam("dbLobList") JSONArray dbLobList,@RequestParam("dbInstances") JSONObject dbInstances) {
		String returnText;
		DBGroup newDBGroup = new DBGroup();
		newDBGroup.setDbGroupId(dbGroupId);
		newDBGroup.setDbTypeId(dbTypeId);
		newDBGroup.setLobs(Utils.jsonArrayToArrayList(dbLobList));
		newDBGroup.setDatabaseInstances(Utils.jsonToBooleanHashMap(dbInstances));
		int statusCode = repHandler.addDatabaseGroup(newDBGroup);
		if (statusCode == 0) {
			returnText = "Database group added";
		} else {
			returnText = "Error: Database Group was not added";
		}
		return returnText;
	}

	@RequestMapping(value = "/rest/db_group/{db_group_id}", method = RequestMethod.DELETE)
	public @ResponseBody
	String deleteDBGroup(@PathVariable("db_group_id") String dbGroupId) {
		String returnText;
		int statusCode = repHandler.deleteDatabaseGroup(dbGroupId);
		if (statusCode == 0) {
			returnText = "Database group deleted";
		} else {
			returnText = "Error: Database Group was not deleted";
		}
		return returnText;
	}
	
	@RequestMapping(value = "/rest/db_instance", method = RequestMethod.GET)
	public @ResponseBody
	String findDBInstances(@RequestParam(value = "dbPluginType") String dbPluginType) {
		return (new JSONArray(repHandler.getDatabaseInstanceIds(dbPluginType))).toString();
	}
	
	@RequestMapping(value = "/rest/db_group", method = RequestMethod.GET)
	public @ResponseBody
	String findDBGroups(@RequestParam(value = "dbTypeId") String dbTypeId) {
		return (new JSONArray(repHandler.getDatabaseGroupIds(dbTypeId))).toString();
	}
	
	@RequestMapping(value = "/rest/db_schema/{schema_id}", method = RequestMethod.PUT)
	public @ResponseBody
	String addSchema(@RequestParam(value = "dbTypeId") String dbTypeId,@PathVariable("schema_id") String schemaId,@RequestParam("dbGroupList") JSONArray dbGroupList,@RequestParam(value = "schemaName") String schemaName) {
		String returnText;
		int statusCode = repHandler.addDatabaseSchema(schemaId,dbTypeId);
		if (statusCode == 0) {
			for (int i = 0; i < dbGroupList.length(); i++) {
				statusCode = repHandler.addSchemaToDBGroup(dbGroupList.getString(i),schemaId,schemaName);
			}
			if (statusCode == 0){
				returnText = "Database schema added";
			}
			else {
				returnText = "Error: Database Schema was not added";
			}
		} else {
			returnText = "Error: Database Schema was not added";
		}
		return returnText;
	}
	
	@RequestMapping(value = "/rest/db_schema/{schema_id}", method = RequestMethod.DELETE)
	public @ResponseBody
	String deleteDBSchema(@PathVariable("schema_id") String dbSchemaId) {
		String returnText;
		int statusCode = repHandler.deleteDatabaseSchema(dbSchemaId);
		if (statusCode == 0) {
			returnText = "Database schema deleted";
		} else {
			returnText = "Error: Database Schema was not deleted";
		}
		return returnText;
	}
	
	@RequestMapping(value = "/saveDbPluginConfig", method = RequestMethod.POST)
	public @ResponseBody
	String saveDbPluginConfig(@RequestParam("dbPluginType") String dbPluginType,@RequestParam("params") JSONObject dbPluginParams) {
		HashMap<String,String> dbPluginParamsHash = new HashMap<String,String>();
		Iterator<String> keys = dbPluginParams.keys();
		
		
	    while(keys.hasNext()){
	    	String key = keys.next();
	        try{
	             String value = dbPluginParams.getString(key);
	             dbPluginParamsHash.put(key, value);
	        }catch(Exception e){
	            e.printStackTrace();
	        }
	    }
	    try {
			String dbcadServerHostname = InetAddress.getLocalHost().getHostName();
			if (repHandler.saveDBPluginConfig(dbPluginType, dbPluginParamsHash, dbcadServerHostname) == 0){
				return "DB Plugin Configurations saved.";
			}
			else{
				return "Error: Could not save DB Plugin configurations";
			}
		} catch (UnknownHostException e) {
			return "Error: Could not save DB Plugin configurations";
		}
	}
	
	@RequestMapping(value = "/rest/getLog", method = RequestMethod.POST)
	public @ResponseBody
	String getLog(@RequestParam("db_change_id") String dbChangeId, @RequestParam("lob_id") String lobId){
		return (repHandler.getDeploymentLog(dbChangeId,lobId)).toString();
	}
	
	public ArrayList<DatabasePluginConfig> getDBPluginsConfig(){
		ArrayList<DatabasePluginConfig> pluginsConfig = new ArrayList<DatabasePluginConfig>(); 
		for (DBService dbService : DBService.getDBServices()){
			try {
				String dbcadServerHostname = InetAddress.getLocalHost().getHostName();
				pluginsConfig.add(new DatabasePluginConfig(dbService.getDBType(),repHandler.getDBPluginConfig(dbService.getDBType(),dbcadServerHostname),dbService.getInstanceParameterNames(),dbService.getInstanceParameterAttributes()));
			} catch (UnknownHostException e) {
				return null;
			}
		}
		return pluginsConfig;
	}
}
