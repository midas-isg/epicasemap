package interactors;

import play.libs.ws.WSResponse;
import gateways.webservice.Client;

public class ClientRule {
	
	private Client client = new Client();
	private String baseUrl;
	
	public ClientRule(String baseUrl){
		this.baseUrl = baseUrl;
	}

	public WSResponse getById(Long id){
		String url = append(baseUrl,id+"");
		return client.get(url);
	}

	private String append(final String baseUrl, final String str) {
		return baseUrl + "/" + str;
	}
}
