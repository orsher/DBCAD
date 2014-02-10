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

public class OracleDBService extends DBService {
	private final static String DB_TYPE = "Oracle";
	private String hostname=null;
	private int port;
	private String dbSID = null;
	private String username = null;
	private String password = null;
	private String sqlPlusPath =null;
	private String oracleHome =null;
	
	@Override
	public String getDBType() {
		return DB_TYPE;
	}

	@Override
	public boolean initializeDBService(DBInstance dbInstance, HashMap<String,String> parameters) {
		this.hostname = dbInstance.getDbHost();
		this.port =dbInstance.getDbPort();
		this.dbSID = dbInstance.getPluginInstanceParameters().get("SID");
		this.username = dbInstance.getPluginInstanceParameters().get("Username");
		this.password = dbInstance.getPluginInstanceParameters().get("Password");
		this.sqlPlusPath = parameters.get("SQLplus executable Path");
		this.oracleHome = parameters.get("Oracle Home");
		return true;
	}

	@Override
	public String runScript(String script, String dbSchemaName, AtomicInteger exitCode) {
		final StringBuilder output= new StringBuilder();
		ProcessBuilder processBuilder;
		processBuilder = new ProcessBuilder(sqlPlusPath, "-L",username+"/"+password+"@(description=(address=(PROTOCOL=TCP)(HOST="+hostname+")(PORT="+port+"))(connect_data=(sid="+dbSID+")))");
        processBuilder.redirectErrorStream(true);
        processBuilder.environment().put("ORACLE_HOME", oracleHome);
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
	        //out.println(password);
	        out.println("set echo on");
	        out.println("whenever SQLERROR exit FAILURE;");
	        out.println(script);
	        out.println("exit");
	        out.flush();
	        exitCode.set(process.waitFor());
	        out.flush();
	        System.out.println("DT: "+exitCode);
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
		globalParameterNames.add("SQLplus executable Path");
		globalParameterNames.add("Oracle Home");
		return globalParameterNames;
	}

	@Override
	public ArrayList<String> getInstanceParameterNames() {
		ArrayList<String> instanceParameterNames = new ArrayList<String>();
		instanceParameterNames.add("SID");
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
