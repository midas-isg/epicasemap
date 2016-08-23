package interactors;

import com.fasterxml.jackson.databind.JsonNode;

import play.libs.F.Promise;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;

public class ClientRule {
	
	private long timeout = 100_000;

	private String baseUrl;
	
	public ClientRule(String baseUrl){
		this.baseUrl = baseUrl;
	}

	public WSResponse getById(Long id){
		String url = append(baseUrl, id + "");
		return get(url);
	}

	private String append(final String baseUrl, final String str) {
		return baseUrl + "/" + str;
	}
	
	public WSResponse get(String url){
		return WS.url(url).get().get(timeout);
	}
	
	public WSResponse getByQuery(String urlQuery){
		return WS.url(baseUrl + urlQuery).get().get(timeout);
	}
	
	public Promise<WSResponse> getAsynchronouslyByQuery(String urlQuery) {
		return WS.url(baseUrl + urlQuery).setTimeout((int) 10000/*timeout*/).get();
	}
	
	public WSResponse post(JsonNode body) {
		return WS.url(baseUrl).post(body).get(timeout);
	}
}
