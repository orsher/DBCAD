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
				repHandler.markDbChangeDeploymentStatus(dbChangeId, lobId, "Running");
				String dbChangeScript = repHandler.getDatabaseChangeScript(dbChangeId);
				String dbSchemaName = repHandler.getDatabaseChangeSchema(dbChangeId);
				dbPluginType = repHandler.getDBPluginTypeForDbChange(dbChangeId);
				DBService dbService = DBService.getDBService(dbPluginType);
				HashMap<String,String> globalParameters=null;
				try {
					String dbcadServerHostname = InetAddress.getLocalHost().getHostName();
					globalParameters = repHandler.getDBPluginConfig(dbPluginType,dbcadServerHostname);
				} catch (UnknownHostException e) {
					 e.printStackTrace();
				}
				ArrayList<DBInstance> deployableInstances = repHandler.getDeployableDatabaseInstancesForLobIdAndDbChange(dbChangeId,lobId);
				boolean isAllDeployedSuccessfully = true;
				for (DBInstance deployableInstance : deployableInstances){
					HashMap<String,HashMap<String,String>> parameterAttributes = dbService.getInstanceParameterAttributes();
					Cipher aes =null;
					try{
						aes = Cipher.getInstance("AES");
						//SecretKeySpec k = new SecretKeySpec(DBCADController.ENCRYPTION_KEY.getBytes(), "AES");
						aes.init(Cipher.DECRYPT_MODE, DBCADController.ENC_KEY);
					
						HashMap<String,String> pluginInstanceParameters =deployableInstance.getPluginInstanceParameters();
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
					
					
					
					
					dbService.initializeDBService(deployableInstance,globalParameters);
					String output;
					AtomicInteger exitCode=new AtomicInteger(0);
					output = dbService.runScript(dbChangeScript,dbSchemaName,exitCode);
					repHandler.addDeploymentLog(dbChangeId,deployableInstance,output);
					System.out.println(output);
					dbService.close();
					System.out.println("DT: "+exitCode);
					if (exitCode.intValue() != 0){
						isAllDeployedSuccessfully = false;
					}
				}
				if (isAllDeployedSuccessfully){
					repHandler.markDbChangeDeploymentStatus(dbChangeId, lobId, "DONE");
				}
				else{
					repHandler.markDbChangeDeploymentStatus(dbChangeId, lobId, "Failed");
				}
			}
		}

	}
}
