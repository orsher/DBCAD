package dbcad.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;







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
		return true;
	}

	@Override
	public boolean runScript(String script) {
		ProcessBuilder processBuilder;
		System.out.println(sqlPlusPath + " "+username+"@(description=(address=(PROTOCOL=TCP)(HOST="+hostname+")(PORT="+port+"))(connect_data=(sid="+dbSID+")))");
		processBuilder = new ProcessBuilder(sqlPlusPath, username+"@(description=(address=(PROTOCOL=TCP)(HOST="+hostname+")(PORT="+port+"))(connect_data=(sid="+dbSID+")))");
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
	                        System.out.println(line);
	                    }
	                    reader.close();
	                } catch (final Exception e) {
	                    e.printStackTrace();
	                }
	            }
	        };
	        printOutputThread.start();
	        out.println(password);
	        out.println(script);
	        out.println("exit");
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
		globalParameterNames.add("SQLplus executable Path");
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
}
