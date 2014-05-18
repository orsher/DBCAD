package dbcad.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dbcad.DBInstance;
import dbcad.services.api.DBService;

public class VerticaDBService extends DBService {
	private final static String DB_TYPE = "Vertica";
	private String hostname=null;
	private int port;
	private String dbName = null;
	private String username = null;
	private String password = null;
	private String vsqlPath = null;
	private ArrayList<String> errorStrings = new ArrayList<String>();
	
	public VerticaDBService(){
		errorStrings.add("ERROR [0-9]*:");
		errorStrings.add("ROLLBACK [0-9]*:");
	}
	
	@Override
	public String getDBType() {
		return DB_TYPE;
	}

	@Override
	public boolean initializeDBService(DBInstance dbInstance, HashMap<String,String> parameters) {
		this.hostname = dbInstance.getDbHost();
		this.port =dbInstance.getDbPort();
		this.dbName = dbInstance.getPluginInstanceParameters().get("DB Name");
		this.username = dbInstance.getPluginInstanceParameters().get("Username");
		this.password = dbInstance.getPluginInstanceParameters().get("Password");
		this.vsqlPath = parameters.get("vSql executable Path");
		return true;
	}

	@Override
	public String runScript(String script, String dbSchemaName, AtomicInteger exitCode) {
		final StringBuilder output= new StringBuilder();
		ProcessBuilder processBuilder;
		processBuilder = new ProcessBuilder(vsqlPath, "-h", hostname, "-p", port+"", "-U", username, "-w", password, "-d", dbName);
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
	        out.println("set search_path='"+dbSchemaName+"';");
	        out.println(script);
	        out.println("\\q");
	        out.flush();
	        exitCode.set(process.waitFor());
	        out.flush();
	        for (String patternStr : errorStrings){
	        	Pattern pattern = Pattern.compile(patternStr);
	        	Matcher m = pattern.matcher(output.toString());
	        	if (m.find()){
	        		exitCode.set(1);
	        	}
	        }
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
		globalParameterNames.add("vSql executable Path");
		return globalParameterNames;
	}

	@Override
	public ArrayList<String> getInstanceParameterNames() {
		ArrayList<String> instanceParameterNames = new ArrayList<String>();
		instanceParameterNames.add("DB Name");
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
