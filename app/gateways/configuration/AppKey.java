package gateways.configuration;

public enum AppKey {
	NAME("app.name"),
	VERSION("app.version"),
	;
	
	private final String key;
	
	private AppKey(String key){
		this.key = key;
	}
	
	public String key(){
		return key;
	}
}
