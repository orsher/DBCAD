package dbcad;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.jasypt.util.text.BasicTextEncryptor;
import org.json.JSONArray;

import dbcad.services.api.DBService;

public class DeployThread extends Thread {
	private RepositoryHandler repHandler;
	private String lobId;
	private JSONArray dbChanges;
	private boolean markOnly;
	public DeployThread(RepositoryHandler repHandler,String lobId,JSONArray dbChanges,boolean markOnly){
		this.repHandler = repHandler;
		this.lobId = lobId;
		this.dbChanges = dbChanges;
		this.markOnly = markOnly;
	}
	
	public void run(){
		String dbChangeId;
		String dbPluginType;
		//Order db changes by create date before starting to run them
		ArrayList<String> unsortedDbChangeIds = new ArrayList<String>();
		ArrayList<String> sortedDbChangeIds = new ArrayList<String>();
		for (int i = 0; i < dbChanges.length(); i++) {
			unsortedDbChangeIds.add(dbChanges.getString(i));
		}
		sortedDbChangeIds = repHandler.getDbChangesSortedByCreateDate(unsortedDbChangeIds);
		for (int i = 0; i < sortedDbChangeIds.size(); i++) {
			dbChangeId = sortedDbChangeIds.get(i);
			String dbChangeScript = repHandler.getDatabaseChangeScript(dbChangeId);
			dbPluginType = repHandler.getDBPluginTypeForDbChange(dbChangeId);
			DBService dbService = DBService.getDBService(dbPluginType);
			HashMap<String,String> globalParameters=null;
			try {
				String dbcadServerHostname = InetAddress.getLocalHost().getHostName();
				globalParameters = repHandler.getDBPluginConfig(dbPluginType,dbcadServerHostname);
			} catch (UnknownHostException e) {
				 e.printStackTrace();
			}
			ArrayList<DeploymentDetails> deploymentDetailsList = repHandler.getDeployableDatabaseInstancesAndSchemaNamesForLobIdAndDbChange(dbChangeId,lobId);
			for (DeploymentDetails deploymentDetails : deploymentDetailsList){
				if (!repHandler.isDbChangeDeployed(dbChangeId, deploymentDetails.getDbGroupId(), deploymentDetails.getDbInstance().getDbId())){
					if (!markOnly){
						repHandler.markDbChangeDeploymentStatus(dbChangeId, deploymentDetails.getDbGroupId(), deploymentDetails.getDbInstance().getDbId(), DBCADController.RUNNING_STATUS);
						HashMap<String,HashMap<String,String>> parameterAttributes = dbService.getInstanceParameterAttributes();
						String dbSchemaName = deploymentDetails.getSchemaName();
						Cipher aes =null;
						try{
							aes = Cipher.getInstance("AES");
							//SecretKeySpec k = new SecretKeySpec(DBCADController.ENCRYPTION_KEY.getBytes(), "AES");
							aes.init(Cipher.DECRYPT_MODE, DBCADController.ENC_KEY);
						
							HashMap<String,String> pluginInstanceParameters = deploymentDetails.getDbInstance().getPluginInstanceParameters();
							for( Map.Entry<String, String> entry : pluginInstanceParameters.entrySet() ){
					            String key = entry.getKey();
					            if (parameterAttributes.get(key) != null && parameterAttributes.get(key).get("ENCRYPTED").equals("TRUE") ){
					            	BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
					    			textEncryptor.setPassword(DBCADController.ENCRYPTION_KEY);
					            	String encTextValue = (String)pluginInstanceParameters.get(key);
					            	pluginInstanceParameters.put(key,textEncryptor.decrypt(encTextValue));
					            }
					        }
							
						}catch(Exception e){
							e.printStackTrace();
						}
						
						
						
						
						dbService.initializeDBService(deploymentDetails.getDbInstance(),globalParameters);
						String output;
						AtomicInteger exitCode=new AtomicInteger(0);
						output = dbService.runScript(dbChangeScript,dbSchemaName,exitCode);
						repHandler.addDeploymentLog(dbChangeId,deploymentDetails.getDbInstance(),output);
						dbService.close();
						
						if (exitCode.intValue() == 0){
							repHandler.markDbChangeDeploymentStatus(dbChangeId, deploymentDetails.getDbGroupId(), deploymentDetails.getDbInstance().getDbId(), DBCADController.OK_STATUS);
						}
						else{
							repHandler.markDbChangeDeploymentStatus(dbChangeId, deploymentDetails.getDbGroupId(), deploymentDetails.getDbInstance().getDbId(), DBCADController.ERROR_STATUS);
						}
					}
					else{
						repHandler.addDeploymentLog(dbChangeId,deploymentDetails.getDbInstance(),"-- MARK ONLY --");
						repHandler.markDbChangeDeploymentStatus(dbChangeId, deploymentDetails.getDbGroupId(), deploymentDetails.getDbInstance().getDbId(), DBCADController.OK_STATUS);
					}
				}
			}
		}

	}
}
