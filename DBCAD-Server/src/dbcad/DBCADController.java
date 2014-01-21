package dbcad;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import org.json.JSONArray;
import org.json.JSONObject;
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
	private static RepositoryHandler repHandler;
	private static final int TABLE_MAX_ROWS = 10; 

	static {
		repHandler = new RepositoryHandler();
	}
	
	@PostConstruct
	public void initializeDBCAD(){
		ArrayList<String> parameters = new ArrayList<String>();
		for (DBService dbService : DBService.getDBServices()){
			for (String globalParameterName : dbService.getGlobalParameterNames()){
				parameters.add(globalParameterName);
			}
			repHandler.addDBPluginConfig(dbService.getDBType(), parameters);
		}
	}
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public ModelAndView rootPage() {
		return manageDatabases();
	}
	
	@RequestMapping(value = "/manage-databases", method = RequestMethod.GET)
	public ModelAndView manageDatabases() {
		Gson gson = new Gson();
		HashMap<String, ArrayList<String>> options;
		ArrayList<HashMap<String, String>> typeTableValues;
		ArrayList<DBInstance> instanceTableValues;
		ArrayList<HashMap<String, String>> groupTableValues;
		ArrayList<DBSchema> schemaTableValues;
		ModelAndView mav = new ModelAndView("ManageDatabases");
		options = new HashMap<String, ArrayList<String>>();
		options.put("db_roles", repHandler.getDatabaseRoles());
		options.put("db_vendors", repHandler.getDatabaseVendors());
		options.put("db_groups", repHandler.getDatabaseGroupIds());
		options.put("db_types", repHandler.getDatabaseTypeIds());
		options.put("lobs", repHandler.getLobs());
		typeTableValues = repHandler.getDatabaseTypes();
		AtomicInteger instancesTableTotalNumberOfRows = new AtomicInteger();
		instanceTableValues = repHandler.getDatabaseInstances(null,0,TABLE_MAX_ROWS,instancesTableTotalNumberOfRows);
		groupTableValues = repHandler.getDatabaseGroups();
		AtomicInteger schemasTableTotalNumberOfRows = new AtomicInteger();
		schemaTableValues = repHandler.getDatabaseSchemas(null,0,TABLE_MAX_ROWS,schemasTableTotalNumberOfRows);
		mav.addObject("options", options);
		mav.addObject("type_table_values", typeTableValues);
		mav.addObject("instance_table_values", instanceTableValues);
		mav.addObject("instance_table_values_json", gson.toJson(instanceTableValues));
		mav.addObject("instancesNumOfPages",Math.ceil(1.0*instancesTableTotalNumberOfRows.intValue()/TABLE_MAX_ROWS));
		mav.addObject("instancesCurrentPage",1);
		mav.addObject("group_table_values", groupTableValues);
		mav.addObject("schema_table_values", schemaTableValues);
		mav.addObject("schemasNumOfPages",Math.ceil(1.0*schemasTableTotalNumberOfRows.intValue()/TABLE_MAX_ROWS));
		mav.addObject("schemasCurrentPage",1);
		mav.addObject("dbPluginsConfig",getDBPluginsConfig());
		return mav;
	}

	@RequestMapping(value = "/deploy", method = RequestMethod.GET)
	public ModelAndView deployDBChangesView() {
		HashMap<String, ArrayList<String>> options;
		ArrayList<HashMap<String, String>> dbChangesTableValues;
		AtomicInteger totalNumberOfRows = new AtomicInteger();
		dbChangesTableValues= repHandler.getDatabaseChangeLobsStatus(null,0, TABLE_MAX_ROWS,totalNumberOfRows);
		options = new HashMap<String, ArrayList<String>>();
		options.put("lobs", repHandler.getLobs());
		options.put("db_schemas", repHandler.getDatabaseSchemaIds());
		//options.put("db_changes", repHandler.getNextDBChanges(10, null));
		return new ModelAndView("DeployDBChanges", "options", options).addObject("dbChangesTableValues",dbChangesTableValues).addObject("noOfPages",Math.ceil(1.0*totalNumberOfRows.intValue()/TABLE_MAX_ROWS)).addObject("currentPage",1);
	}
	
	@RequestMapping(value = "/getDbChangesTablePage", method = RequestMethod.POST)
	public ModelAndView getDBChangesTablePage(@RequestParam(value = "page") int page, @RequestParam(value = "searchFilter", defaultValue = "{}") JSONObject searchFilterJSON) {
		HashMap<String, ArrayList<String>> options;
		ArrayList<HashMap<String, String>> dbChangesTableValues;
		AtomicInteger totalNumberOfRows = new AtomicInteger();
		dbChangesTableValues= repHandler.getDatabaseChangeLobsStatus(searchFilterJSON.isNull("generalFilter") ? null : searchFilterJSON.getString("generalFilter"),(page-1)*TABLE_MAX_ROWS, TABLE_MAX_ROWS,totalNumberOfRows);
		options = new HashMap<String, ArrayList<String>>();
		options.put("lobs", repHandler.getLobs());
		//options.put("db_changes", repHandler.getNextDBChanges(10, null));
		return new ModelAndView("ManageDBChangesTable", "options", options).addObject("dbChangesTableValues",dbChangesTableValues).addObject("noOfPages",Math.ceil(1.0*totalNumberOfRows.intValue()/TABLE_MAX_ROWS)).addObject("currentPage",page);
	}
	
	@RequestMapping(value = "/getDbSchemasTablePage", method = RequestMethod.POST)
	public ModelAndView getDBSchemasTablePage(@RequestParam(value = "page") int page,@RequestParam(value = "searchFilter", defaultValue = "{}") JSONObject searchFilterJSON) {
		HashMap<String, ArrayList<String>> options;
		ArrayList<DBSchema> dbSchemasTableValues;
		AtomicInteger totalNumberOfRows = new AtomicInteger();
		dbSchemasTableValues= repHandler.getDatabaseSchemas(searchFilterJSON.isNull("generalFilter") ? null : searchFilterJSON.getString("generalFilter"),(page-1)*TABLE_MAX_ROWS, TABLE_MAX_ROWS,totalNumberOfRows);
		options = new HashMap<String, ArrayList<String>>();
		return new ModelAndView("ManageDatabaseSchemaTable", "options", options).addObject("schema_table_values",dbSchemasTableValues).addObject("schemasNumOfPages",Math.ceil(1.0*totalNumberOfRows.intValue()/TABLE_MAX_ROWS)).addObject("schemasCurrentPage",page);
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

	@RequestMapping(value = "/rest/deploy/{lob_id}", method = RequestMethod.PUT)
	public @ResponseBody
	String deployDBChangesOnLOB(@PathVariable("lob_id") String lobId,
			@RequestParam("db_changes") JSONArray dbChanges) {
		String dbChangeId;
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
	/*	DBService dbService = DBService.getDBService("Oracle");
		HashMap<String,String> parameters = new HashMap<String,String>();
		parameters.put("sqlPlusPath", "D:\\Ayelet Backup\\app\\ayelets\\product\\11.1.0\\client_1\\sqlplus.exe");
		dbService.initializeDBService("dbqa", 1524, "dvlp2",parameters);
		dbService.runScript("Select * from v$database;");*/
		/*DBService dbService = DBService.getDBService("Mysql");
		HashMap<String,String> parameters = new HashMap<String,String>();
		parameters.put("mysqlClientPath", "c:\\mysql.exe");
		dbService.initializeDBService("vm-qa-acdb", 3306, "mysql",parameters);
		dbService.runScript("Select * from mysql.user;"); */
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
			System.out.println("TYPE::: " + dbType.getDbRole() + " "
					+ dbType.getDbVendor());
			String dbTypeId = repHandler.addDatabaseType(dbType.getDbVendor(),
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

	@RequestMapping(value = "/rest/db_instance", method = RequestMethod.PUT)
	public @ResponseBody
	String addDBInstance(@RequestParam(value = "dbGroupId") String dbGroupId,@RequestParam(value = "dbHost") String dbHost,
			@RequestParam(value = "dbPort") Integer dbPort,@RequestParam(value = "dbSid") String dbSid,@RequestParam(value = "pluginInstanceParameters") JSONObject pluginInstanceParameters) {
		DBInstance dbInstance = new DBInstance(null,dbGroupId,dbHost,dbPort,dbSid,Utils.jsonToHashMap(pluginInstanceParameters));
		String returnText;
		String dbInstanceId = repHandler.addDatabaseInstance(
				dbInstance.getDbGroupId(), dbInstance.getDbHost(),
				dbInstance.getDbPort(), dbInstance.getDbSid(), dbInstance.getPluginInstanceParameters());
		if (!dbInstanceId.equals("")) {
			returnText = dbInstanceId;
		} else {
			returnText = "Error: DB Instance was not added";
		}
		return returnText;
	}
	
	@RequestMapping(value = "/rest/db_instance/{db_instance_id}", method = RequestMethod.PUT)
	public @ResponseBody
	String saveDBInstance(@PathVariable("db_instance_id") String dbInstanceId, @RequestParam(value = "dbGroupId") String dbGroupId,@RequestParam(value = "dbHost") String dbHost,
			@RequestParam(value = "dbPort") Integer dbPort,@RequestParam(value = "dbSid") String dbSid,@RequestParam(value = "pluginInstanceParameters") JSONObject pluginInstanceParameters) {
		DBInstance dbInstance = new DBInstance(dbInstanceId,dbGroupId,dbHost,dbPort,dbSid,Utils.jsonToHashMap(pluginInstanceParameters));
		String returnText;
		String newDbInstanceId = repHandler.saveDatabaseInstance(dbInstance.getDbId(),
				dbInstance.getDbGroupId(), dbInstance.getDbHost(),
				dbInstance.getDbPort(), dbInstance.getDbSid(), dbInstance.getPluginInstanceParameters());
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

	@RequestMapping(value = "/manage-db-changes", method = RequestMethod.GET)
	public ModelAndView ManageDBChangesView() {
		HashMap<String, ArrayList<String>> options;
		ArrayList<HashMap<String, String>> dbChangesTableValues;
		AtomicInteger totalNumberOfRows = new AtomicInteger();
		dbChangesTableValues= repHandler.getDatabaseChangeLobsStatus(null,0, TABLE_MAX_ROWS,totalNumberOfRows);
		options = new HashMap<String, ArrayList<String>>();
		options.put("lobs", repHandler.getLobs());
		options.put("db_schemas", repHandler.getDatabaseSchemaIds());
		//options.put("db_changes", repHandler.getNextDBChanges(10, null));
		return new ModelAndView("ManageDBChanges", "options", options).addObject("dbChangesTableValues",dbChangesTableValues).addObject("noOfPages",Math.ceil(1.0*totalNumberOfRows.intValue()/TABLE_MAX_ROWS)).addObject("currentPage",1);
//		HashMap<String, ArrayList<String>> options = new HashMap<String, ArrayList<String>>();
//		options.put("db_schemas", repHandler.getDatabaseSchemaIds());
//		options.put("db_changes", repHandler.getNextDBChanges(10, null));
//		return new ModelAndView("ManageDBChanges", "options", options);
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
	String addDBGroup(@RequestParam(value = "dbTypeId") String dbTypeId,@PathVariable("db_group_id") String dbGroupId,@RequestParam("dbLobList") JSONArray dbLobList) {
		String returnText;
		int statusCode = repHandler.addDatabaseGroup(dbGroupId, dbTypeId);
		if (statusCode == 0) {
			for (int i = 0; i < dbLobList.length(); i++) {
				statusCode = repHandler.addDatabaseGroupLobMapping(dbGroupId,dbLobList.getString(i));
			}
			if (statusCode == 0){
				returnText = "Database group added";
			}
			else {
				returnText = "Error: Database Group was not added";
			}
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
	String findDBInstances(@RequestParam(value = "dbTypeId") String dbTypeId) {
		return (new JSONArray(repHandler.getDatabaseIds(dbTypeId))).toString();
	}
	
	@RequestMapping(value = "/rest/db_schema/{schema_id}", method = RequestMethod.PUT)
	public @ResponseBody
	String addSchema(@RequestParam(value = "dbTypeId") String dbTypeId,@PathVariable("schema_id") String schemaId,@RequestParam("dbDeployableList") JSONArray dbDeployableList,@RequestParam(value = "schemaName") String schemaName) {
		String returnText;
		System.out.println(schemaId + schemaName + dbTypeId + dbDeployableList);
		int statusCode = repHandler.addDatabaseSchema(schemaId,schemaName,dbTypeId);
		if (statusCode == 0) {
			for (int i = 0; i < dbDeployableList.length(); i++) {
				statusCode = repHandler.addDeployableDBInstance(schemaId,dbDeployableList.getString(i));
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
	    	String val = null;
	        try{
	             String value = dbPluginParams.getString(key);
	             dbPluginParamsHash.put(key, value);
	        }catch(Exception e){
	            e.printStackTrace();
	        }
	    }
		if (repHandler.saveDBPluginConfig(dbPluginType, dbPluginParamsHash) == 0){
			return "DB Plugin Configurations saved.";
		}
		else{
			return "Error: Could not save DB Plugin configurations";
		}
	}

	
	public ArrayList<DatabasePluginConfig> getDBPluginsConfig(){
		ArrayList<DatabasePluginConfig> pluginsConfig = new ArrayList<DatabasePluginConfig>(); 
		for (DBService dbService : DBService.getDBServices()){
			pluginsConfig.add(new DatabasePluginConfig(dbService.getDBType(),repHandler.getDBPluginConfig(dbService.getDBType()),dbService.getInstanceParameterNames()));
		}
		return pluginsConfig;
	}
}
