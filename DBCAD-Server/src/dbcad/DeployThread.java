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
	
	public void run_dbreq_on_lob(){
		String dbChangeId;
		String dbPluginType;
		for (int i = 0; i < dbChanges.length(); i++) {
			dbChangeId = dbChanges.getString(i);
			if (!repHandler.isDbChangeDeployed(dbChangeId,lobId)){
				String dbChangeScript = repHandler.getDatabaseChangeScript(dbChangeId);
				dbPluginType = repHandler.getDBPluginTypeForDbChange(dbChangeId);
				DBService dbService = DBService.getDBService(dbPluginType);
				HashMap<String,String> globalParameters = repHandler.getDBPluginConfig(dbPluginType);
				ArrayList<DBInstance> deployableInstances = repHandler.getDeployableDatabaseInstancesForLobIdAndDbChange(dbChangeId,lobId);
				for (DBInstance deployableInstance : deployableInstances){
					dbService.initializeDBService(deployableInstance,globalParameters);
					boolean exitCode = dbService.runScript(dbChangeScript);
					dbService.close();
					if (exitCode != true){
						
					}
				}
				
				repHandler.markDbChangeAsDeployed(dbChangeId, lobId);
			}
		}

	}
}
