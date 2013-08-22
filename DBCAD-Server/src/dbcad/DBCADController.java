package dbcad;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
 
@Controller
public class DBCADController {
	private static RepositoryHandler repHandler;
	
	
	static{
		repHandler = new RepositoryHandler();
	}

    @RequestMapping(value = "/manage", method = RequestMethod.GET)
    public ModelAndView manageDatabases() {
    	HashMap<String,ArrayList<String>> options;
    	ArrayList<HashMap<String,String>> typeTableValues;
    	ArrayList<HashMap<String,String>> instanceTableValues;
    	ModelAndView mav = new ModelAndView ("ManageDatabases") ;
    	options = new HashMap<String,ArrayList<String>>();
    	options.put("db_roles", repHandler.getDatabaseRoles());
    	options.put("db_vendors", repHandler.getDatabaseVendors());
    	options.put("db_groups", repHandler.getDatabaseGroups());
    	typeTableValues = repHandler.getDatabaseTypes();
    	instanceTableValues = repHandler.getDatabaseInstances();
    	mav.addObject ("options", options);
    	mav.addObject ("type_table_values",typeTableValues);
    	mav.addObject ("instance_table_values",instanceTableValues);
    	return mav;
    }
    
    @RequestMapping(value = "/deploy", method = RequestMethod.GET)
    public ModelAndView deployDBChangesView() {
    	HashMap<String,ArrayList<String>> options;
    	options = new HashMap<String,ArrayList<String>>();
    	options.put("lobs", repHandler.getLobs());
    	options.put("db_changes", repHandler.getNextDBRequests(10, null));
        return new ModelAndView("DeployDBChanges" , "options", options);
    }
    
    @RequestMapping(value = "/rest/deploy/{lob_id}", method = RequestMethod.PUT)
    public @ResponseBody String deployDBChangesOnLOB(@PathVariable("lob_id") String lobId,@RequestParam("db_changes") JSONArray dbChanges) {
		String dbChangeId;
		for (int i=0; i < dbChanges.length(); i++){
			dbChangeId = dbChanges.getString(i);
			repHandler.markDbChangeAsDeployed(dbChangeId, lobId);
		}
		return "JOB Was sent";
    }
    
    @RequestMapping(value = "/rest/query/{lob_id}", method = RequestMethod.GET)
    public @ResponseBody String queryLobForDBChanges(@PathVariable("lob_id") String lobId,@RequestParam("query_data") JSONArray queryData) {
		JSONObject jsonResponse = new JSONObject();
		String dbChangeId;
		for (int i=0; i < queryData.length(); i++){
			dbChangeId = queryData.getString(i);
			jsonResponse.put(dbChangeId, repHandler.checkDbChanges(dbChangeId, lobId));
		}
		return jsonResponse.toString();
    }
    
    @RequestMapping(value="/rest/db_type",method=RequestMethod.PUT)
    public @ResponseBody String addDBType(@ModelAttribute(value="db_type") DBType dbType, BindingResult result){
    	String returnText;
        if(!result.hasErrors()){
        	System.out.println("TYPE::: "+dbType.getDbRole() +" "+ dbType.getDbVendor());
        	String dbTypeId = repHandler.addDatabaseType(dbType.getDbVendor(), dbType.getDbRole());
        	if (!dbTypeId.equals("")){
        		returnText = dbTypeId;
        	}
        	else{
        		returnText = "Error: DB Type was not added";
        	}
        }else{
            returnText = "Error: DB Type was not added";
        }
        return returnText;
    }
    
    @RequestMapping(value="/rest/db_type/{db_type_id}",method=RequestMethod.DELETE)
    public @ResponseBody String deleteDBType(@PathVariable("db_type_id") String dbTypeId){
    	String returnText;
    	System.out.println("TYPE::: "+dbTypeId);
    	if (repHandler.deleteDatabaseType(dbTypeId)){
    		returnText = "DB Type Deleted.";
    	}
    	else{
    		returnText = "Error: DB Type was not deleted";
    	}
        return returnText;
    }
    
    @RequestMapping(value="/rest/db_instance",method=RequestMethod.PUT)
    public @ResponseBody String addDBInstance(@ModelAttribute(value="db_instance") DBInstance dbInstance, BindingResult result){
    	String returnText;
        if(!result.hasErrors()){
        	System.out.println("INSTANCE::: "+ dbInstance.getDbGroupId() +" "+ dbInstance.getDbHost() +" "+ dbInstance.getDbSid()+" "+ dbInstance.getDbPort());
        	String dbInstanceId = repHandler.addDatabaseInstance(dbInstance.getDbGroupId(), dbInstance.getDbHost() , dbInstance.getDbPort(), dbInstance.getDbSid());
        	if (!dbInstanceId.equals("")){
        		returnText = dbInstanceId;
        	}
        	else{
        		returnText = "Error: DB Instance was not added";
        	}
        }else{
            returnText = "Error: DB Instance was not added";
        }
        return returnText;
    }
    
    @RequestMapping(value="/rest/db_instance/{db_instance_id}",method=RequestMethod.DELETE)
    public @ResponseBody String deleteDBInstance(@PathVariable("db_instance_id") String dbInstanceId){
    	String returnText;
    	System.out.println("INSTANCE::: "+dbInstanceId);
    	if (repHandler.deleteDatabaseInstance(dbInstanceId)){
    		returnText = "DB instance Deleted.";
    	}
    	else{
    		returnText = "Error: DB Instance was not deleted";
    	}
        return returnText;
    }
     
}
