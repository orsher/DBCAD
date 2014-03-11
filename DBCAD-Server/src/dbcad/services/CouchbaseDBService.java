package dbcad.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONObject;

import dbcad.DBInstance;
import dbcad.services.api.DBService;

public class CouchbaseDBService extends DBService {
	private final static String DB_TYPE = "Couchbase";
	private String hostname=null;
	private int port;
	private int apiPort;
	private String username = null;
	private String password = null;
	
	ArrayList<String> globalParameterNames = new ArrayList<String>();
	ArrayList<String> instanceParameterNames = new ArrayList<String>();
	
	public CouchbaseDBService(){
		instanceParameterNames.add("Username");
		instanceParameterNames.add("Password");
		instanceParameterNames.add("API Port");
	}

	@Override
	public ArrayList<String> getGlobalParameterNames() {
		return globalParameterNames;
	}

	@Override
	public ArrayList<String> getInstanceParameterNames() {
		return instanceParameterNames;
	}

	@Override
	public HashMap<String, HashMap<String, String>> getInstanceParameterAttributes() {
		HashMap<String, HashMap<String, String>> instanceParameterAttributes =new HashMap<String, HashMap<String, String>>();
		HashMap<String, String> attributes = new HashMap<String, String>();
		attributes.put("ENCRYPTED", "TRUE");
		instanceParameterAttributes.put("Password",attributes);
		return instanceParameterAttributes;
	}

	@Override
	public String getDBType() {
		return DB_TYPE;
	}

	@Override
	public boolean initializeDBService(DBInstance dbInstance,
			HashMap<String, String> globalParameters) {
		this.hostname = dbInstance.getDbHost();
		this.port =dbInstance.getDbPort();
		this.username = dbInstance.getPluginInstanceParameters().get("Username");
		this.password = dbInstance.getPluginInstanceParameters().get("Password");
		this.apiPort = Integer.parseInt(dbInstance.getPluginInstanceParameters().get("API Port"));
		return true;
	}

	@Override
	public String runScript(String script, String dbSchemaName,
			AtomicInteger exitCode) {
		final StringBuilder output= new StringBuilder();
		BufferedReader rd =null;
		InputStream is=null;
		HttpURLConnection httpCon;
		OutputStreamWriter wr;
		URL url;
		String line;
		try {
			String encoding = javax.xml.bind.DatatypeConverter.printBase64Binary((username+":"+password).getBytes());
			JSONObject jsonScript = new JSONObject(script);
			switch (((String)jsonScript.get("operation"))){
			case "create_update_design_document": 
				url = new URL("http://"+hostname+":"+apiPort+"/"+dbSchemaName+"/_design/"+(String)jsonScript.get("design_document_name"));
			    httpCon = (HttpURLConnection)url.openConnection();
			    httpCon.setDoOutput(true);
			    httpCon.setRequestMethod("PUT");
			    httpCon.setRequestProperty("Authorization", "Basic " + encoding);
			    httpCon.setRequestProperty("Content-Type", "application/json");
			    wr = new OutputStreamWriter(httpCon.getOutputStream());
			    wr.write(jsonScript.get("design_document_defenition").toString());
			    wr.flush();
			    // Get the response
	            if (httpCon.getResponseCode() == 201) {
	                is = httpCon.getInputStream();
	                exitCode.set(0);
	            } else {
	                is = httpCon.getErrorStream();
	                exitCode.set(1);
	            }
			    rd = new BufferedReader(new InputStreamReader(is));
			    while ((line = rd.readLine()) != null) {
			    	output.append(line+"\n");
			    }
			    wr.close();
			    rd.close();
			    break;
			case "create_bucket":
				url = new URL("http://"+hostname+":"+port+"/pools/default/buckets");
			    httpCon = (HttpURLConnection)url.openConnection();
			    httpCon.setDoOutput(true);
			    httpCon.setRequestMethod("POST");
			    httpCon.setRequestProperty("Authorization", "Basic " + encoding);
			    HashMap<String,String> params = new HashMap<String,String>();
			    params.put("name", dbSchemaName);
		        params.put("ramQuotaMB", (String)jsonScript.get("ramQuotaMB"));
		        params.put("authType", (String)jsonScript.get("authType"));
		        params.put("saslPassword", (String)jsonScript.get("saslPassword"));
		        params.put("proxyPort", (String)jsonScript.get("proxyPort"));
		        params.put("replicaNumber", (String)jsonScript.get("replicaNumber"));

		        StringBuilder postData = new StringBuilder();
		        for (Map.Entry<String,String> param : params.entrySet()) {
		            if (postData.length() != 0) postData.append('&');
		            postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
		            postData.append('=');
		            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
		        }
		        
			    wr = new OutputStreamWriter(httpCon.getOutputStream());
			    wr.write(postData.toString());
			    wr.flush();
			    
			    // Get the response
	            if (httpCon.getResponseCode() == 202) {
	                is = httpCon.getInputStream();
	                exitCode.set(0);
	            } else {
	                is = httpCon.getErrorStream();
	                exitCode.set(1);
	            }
			    rd = new BufferedReader(new InputStreamReader(is));
			    while ((line = rd.readLine()) != null) {
			    	output.append(line+"\n");
			    }
			    wr.close();
			    rd.close();
			    break;
			    
			}
		} catch (Exception e) {
			e.printStackTrace();
			output.append(e.toString());
        	exitCode.set(1);
		}
		return output.toString();
	}

	@Override
	public boolean close() {
		// TODO Auto-generated method stub
		return false;
	}

}
