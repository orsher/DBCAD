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

public class CassandraDBService extends DBService {
	private final static String DB_TYPE = "Cassandra";
	private String hostname=null;
	private int port;
	private String username = null;
	private String password = null;
	private String cassandraClientPath =null;
	private String javaHome = null;
	private ArrayList<String> errorStrings = new ArrayList<String>();
	
	public CassandraDBService(){
		errorStrings.add("Syntax error at position");
		errorStrings.add("Not authenticated to a working keyspace");
		errorStrings.add("Keyspace.*not found");
		errorStrings.add("Keyspace names must be case-insensitively unique");
		errorStrings.add("already exists in keyspace");
		errorStrings.add("Warning: unreachable nodes");
		errorStrings.add("java.lang.IllegalArgumentException");
		errorStrings.add("java.net.UnknownHostException");
		errorStrings.add("Not connected to a cassandra instance");
	}
	
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
		this.cassandraClientPath = GlobalParameters.get("Cassandra client executable Path");
		this.javaHome = GlobalParameters.get("Java Home");
		return true;
	}
	@Override
	
	public String runScript(String script, String dbSchemaName,AtomicInteger exitCode) {
		final StringBuilder output= new StringBuilder();
		ProcessBuilder processBuilder;
		
		if (username != null && password != null){
			processBuilder = new ProcessBuilder(cassandraClientPath , "-h",hostname , "-p",port+"", "-u",username,  "-pw",password,"-k",dbSchemaName);
		}
		else{
			processBuilder = new ProcessBuilder(cassandraClientPath , "-h",hostname , "-p",port+"","-k",dbSchemaName);
		}
        processBuilder.redirectErrorStream(true);
        processBuilder.environment().put("JAVA_HOME", javaHome);
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
	        out.println(script);
	        out.println("exit;");
	        out.println("exit;");
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
		globalParameterNames.add("Cassandra client executable Path");
		globalParameterNames.add("Java Home");
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



