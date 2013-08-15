package dbcad;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
    
    @RequestMapping(value = "/add_db_type.html", method = RequestMethod.GET)
    public ModelAndView addDBType() {
    	options = new HashMap<String,ArrayList<String>>();
    	options.put("db_roles", repHandler.getDatabaseRoles());
    	options.put("db_vendors", repHandler.getDatabaseVendors());
    	tableValues = repHandler.getDatabaseTypes();
        return new ModelAndView("AddDBTypes" , "options", options).addObject("table_values",tableValues);
    }
    
    @RequestMapping(value="/submit_db_type.html",method=RequestMethod.POST)
    public @ResponseBody String addDBType(@ModelAttribute(value="db_type") DBType dbType, BindingResult result ){
        String returnText;
        if(!result.hasErrors()){
        	System.out.println("TYPE::: "+dbType.getDbRole() +" "+ dbType.getDbVendor());
        	if (repHandler.addDatabaseType(dbType.getDbVendor(), dbType.getDbRole())){
        		returnText = "DB Type added.";
        	}
        	else{
        		returnText = "Error: DB Type was not added";
        	}
        }else{
            returnText = "Error: DB Type was not added";
        }
        return returnText;
    }
     
}
