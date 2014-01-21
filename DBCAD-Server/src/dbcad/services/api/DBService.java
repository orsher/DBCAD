package dbcad.services.api;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.ServiceLoader;

import dbcad.DBInstance;

public abstract class DBService {
	
	 	public static DBService getDBService(String dbType) {
	        ServiceLoader<DBService> dbServices = ServiceLoader.load(DBService.class);
	        for (DBService dbService : dbServices) {
	        	if (dbService.getDBType().equals(dbType)){	        		
	        		return dbService;
	        	}
	        }
	        throw new Error ("No DB Service of type "+dbType+" registered");
	    }
	 	
	 	public static ServiceLoader<DBService> getDBServices() {
	        ServiceLoader<DBService> dbServices = ServiceLoader.load(DBService.class);
	        return dbServices; 
	    }

	 	public abstract ArrayList<String> getGlobalParameterNames();
	 	
	 	public abstract ArrayList<String> getInstanceParameterNames();
	 	
	    public abstract String getDBType();
	    
	    public abstract boolean initializeDBService(DBInstance dbInstance, HashMap<String,String> globalParameters);
	    
	    public abstract boolean runScript(String script);
	    
	    public abstract boolean close();
}
