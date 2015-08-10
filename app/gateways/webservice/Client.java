package gateways.webservice;

import play.libs.ws.WS;
import play.libs.ws.WSResponse;

public class Client {
	private long timeout = 100_000;

	public WSResponse get(String url){
		return WS.url(url).get().get(timeout);
	}

}
