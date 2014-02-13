package dbcad.services.api;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicInteger;

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
	 	
	 	public static ArrayList<String> getAvailableDbPluginTypeNames(){
	 		ServiceLoader<DBService> dbServices = ServiceLoader.load(DBService.class);
	 		ArrayList<String> availableDbPluginTypeName = new ArrayList<String>();
	 		for (DBService dbService : dbServices){
	 			availableDbPluginTypeName.add(dbService.getDBType());
	 		}
	 		return availableDbPluginTypeName; 
	 	}

	 	public abstract ArrayList<String> getGlobalParameterNames();
	 	
	 	public abstract ArrayList<String> getInstanceParameterNames();
	 	
	 	public abstract HashMap<String,HashMap<String,String>> getInstanceParameterAttributes();
	 	
	    public abstract String getDBType();
	    
	    public abstract boolean initializeDBService(DBInstance dbInstance, HashMap<String,String> globalParameters);
	    
	    public abstract String runScript(String script,String dbSchemaName, AtomicInteger exitCode);
	    
	    public abstract boolean close();
}
