package dbcad.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import dbcad.services.api.DBService;

public class MysqlDBService extends DBService {
	private final static String DB_TYPE = "Mysql";
	private String hostname=null;
	private int port;
	private String mysqlClientPath =null;
	
	@Override
	public String getDBType() {
		return DB_TYPE;
	}

	@Override
	public boolean initializeDBService(String hostname, int port , String dbSID, HashMap<String,String> parameters) {
		this.hostname = hostname;
		this.port =port;
		this.mysqlClientPath = parameters.get("mysqlClientPath");
		return true;
	}
	@Override
	
	public boolean runScript(String script) {
		ProcessBuilder processBuilder;
		System.out.println(mysqlClientPath + " " + "-h"+hostname+ " " + "-uayelets"+ " " +  "-P"+port + " -p");
		processBuilder = new ProcessBuilder(mysqlClientPath , "-h"+hostname , "-uayelets",  "-P"+port, "-p" );
        processBuilder.redirectErrorStream(true);
        processBuilder.inheritIO();
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
	                        System.out.println(line);
	                        System.out.flush();
	                    }
	                    reader.close();
	                } catch (final Exception e) {
	                    e.printStackTrace();
	                }
	            }
	        };
	        printOutputThread.start();
	        out.println("orsh");
	        out.println("orsh");
	        out.println("orsh");
	        out.println("orsh");
	        out.flush();
	        System.out.println("Read ended");
        }
        catch(Exception e){
        	e.printStackTrace();
        }
        return true;
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
}



