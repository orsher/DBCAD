package dbcad;

import org.json.JSONObject;

public class MetaDataBean {
	private JSONObject metadataJson;
	
	public JSONObject getMetadataJson(){
		return metadataJson;
	}
	public void setMetadataJson(JSONObject metadataJson){
		this.metadataJson = metadataJson;
	}
}