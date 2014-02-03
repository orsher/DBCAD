package dbcad.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import dbcad.DBInstance;
import dbcad.services.api.DBService;

public class MysqlDBService extends DBService {
	private final static String DB_TYPE = "Mysql";
	private String hostname=null;
	private int port;
	private String username = null;
	private String password = null;
	private String mysqlClientPath =null;
	
	@Override
	public String getDBType() {
		return DB_TYPE;
	}

	@Override
	public boolean initializeDBService(DBInstance dbInstance, HashMap<String,String> GlobalParameters) {
		this.hostname = dbInstance.getDbHost();
		this.port = dbInstance.getDbPort();
		this.username = dbInstance.getPluginInstanceParameters().get("Username");
		this.password = dbInstance.getPluginInstanceParameters().get("Password");
		this.mysqlClientPath = GlobalParameters.get("mysql client executable Path");
		return true;
	}
	@Override
	
	public String runScript(String script, String dbSchemaName,AtomicInteger exitCode) {
		final StringBuilder output= new StringBuilder();
		ProcessBuilder processBuilder;
		
		System.out.println(mysqlClientPath + " " + "-h"+hostname+ " " + "-u"+username+ " -P"+port+ " -p"+password);
		processBuilder = new ProcessBuilder(mysqlClientPath , "-h"+hostname , "-u"+username,  "-P"+port, "-p"+password,"-vvv","-D"+dbSchemaName,"-e",script);
        processBuilder.redirectErrorStream(true);
        try {
	        final Process process = processBuilder.start();
	        BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
	        PrintWriter out = new PrintWriter(process.getOutputStream());
	        String currentLine = null;
	        final Thread printOutputThread = new Thread() {
	            @Override
	            public void run() {
	                try {
	                    final BufferedReader reader = new BufferedReader(
	                            new InputStreamReader(process.getInputStream()));
	                    String line = null;
	                    while ((line = reader.readLine()) != null) {
	                    	output.append(line+"\n");
	                    }
	                    reader.close();
	                } catch (final Exception e) {
	                    e.printStackTrace();
	                }
	            }
	        };
	        printOutputThread.start();
	        exitCode.set(process.waitFor());
	        System.out.println("Read ended exit code: "+exitCode);
        }
        catch(Exception e){
        	e.printStackTrace();
        	exitCode.set(1);
        }
        return output.toString();
	}
	
	@Override
	public ArrayList<String> getGlobalParameterNames() {
		ArrayList<String> globalParameterNames = new ArrayList<String>();
		globalParameterNames.add("mysql client executable Path");
		return globalParameterNames;
	}

	@Override
	public ArrayList<String> getInstanceParameterNames() {
		ArrayList<String> instanceParameterNames = new ArrayList<String>();
		instanceParameterNames.add("Username");
		instanceParameterNames.add("Password");
		return instanceParameterNames;
	}
	
	public  boolean close(){
		hostname=null;
		port = 0;
		return true;
	}
	
	@Override
	public HashMap<String, HashMap<String, String>> getInstanceParameterAttributes() {
		HashMap<String, HashMap<String, String>> instanceParameterAttributes =new HashMap<String, HashMap<String, String>>();
		HashMap<String, String> attributes = new HashMap<String, String>();
		attributes.put("ENCRYPTED", "TRUE");
		instanceParameterAttributes.put("Password",attributes);
		return instanceParameterAttributes;
	}
}



