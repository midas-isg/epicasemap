package gateways.configuration;

public enum AppKey {
	NAME("app.name"),
	VERSION("app.version"),
	ALS_LOCATION_WS_URL("app.servers.als.ws.location.url")
	;
	
	private final String key;
	
	private AppKey(String key){
		this.key = key;
	}
	
	public String key(){
		return key;
	}
}
