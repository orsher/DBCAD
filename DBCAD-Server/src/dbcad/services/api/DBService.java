package dbcad.services.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.ServiceLoader;

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
	    
	    public abstract boolean initializeDBService(String hostname, int port, String dbSID,HashMap<String,String> parameters);
	    
	    public abstract boolean runScript(String script);
}
