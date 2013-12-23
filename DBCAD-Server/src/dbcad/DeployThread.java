package dbcad;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;

import dbcad.services.api.DBService;

public class DeployThread extends Thread {
	private RepositoryHandler repHandler;
	private String lobId;
	private JSONArray dbChanges;
	
	public DeployThread(RepositoryHandler repHandler,String lobId,JSONArray dbChanges){
		this.repHandler = repHandler;
		this.lobId = lobId;
		this.dbChanges = dbChanges;
	}
	
	public void run(){
		String dbChangeId;
		String dbPluginType;
		for (int i = 0; i < dbChanges.length(); i++) {
			dbChangeId = dbChanges.getString(i);
			if (!repHandler.isDbChangeDeployed(dbChangeId,lobId)){
				dbPluginType = repHandler.getDBPluginTypeForDbChange(dbChangeId);
				String dbChangeScript = repHandler.getDatabaseChangeScript(dbChangeId);
				DBService dbService = DBService.getDBService(dbPluginType);
				HashMap<String,String> parameters = repHandler.getDBPluginConfig(pluginDBType);
				ArrayList<DBInstance> deployableInstances = repHandler.getDeployableDatabaseInstancesForLobIdAndDbChange(dbChangeId,lobId);
				for (DBInstance deployableInstance : deployableInstances){
					dbService.initializeDBService("dbqa", 1524, "dvlp2",parameters);
					int exitCode = dbService.runScript(dbChangeScript);
					dbService.close();
					if (exitCode != 0){
						
					}
				}
				
				repHandler.markDbChangeAsDeployed(dbChangeId, lobId);
			}
		}

	}
}
