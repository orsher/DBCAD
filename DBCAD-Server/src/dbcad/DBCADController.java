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
	private HashMap<String,ArrayList<String>> options;
	private ArrayList<HashMap<String,String>> tableValues;
	
	static{
		repHandler = new RepositoryHandler();
	}

    @RequestMapping(value = "/manage", method = RequestMethod.GET)
    public ModelAndView manageDatabasesView() {
    	options = new HashMap<String,ArrayList<String>>();
    	options.put("db_roles", repHandler.getDatabaseRoles());
    	options.put("db_vendors", repHandler.getDatabaseVendors());
    	tableValues = repHandler.getDatabaseTypes();
        return new ModelAndView("ManageDatabases" , "options", options).addObject("table_values",tableValues);
    }
    
    @RequestMapping(value = "/deploy", method = RequestMethod.GET)
    public ModelAndView deployDBChangesView() {
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
     
}
