package lsparser.xmlparser;

import lsparser.tycho.*;
import gateways.webservice.AlsDao;
import models.entities.Location;
import models.entities.NamedLocation;
import interactors.ClientRule;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.function.Function;

import javax.xml.bind.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.corba.se.impl.orbutil.threadpool.TimeoutException;

import akka.dispatch.ExecutionContexts;
import play.Logger;
import play.api.libs.concurrent.Akka;
import play.data.format.Formats.DateTime;
import play.libs.Json;
import play.libs.F.Function0;
import play.libs.F.Promise;
import play.libs.ws.WSResponse;

public class TychoParser {
	private final int REQUEST_LIMIT;
	private final long TIMEOUT;
	private final int TIMEOUT_LIMIT;
	private static final String API_LOCATION_TYPES_URL = "/api/location-types";
	
	private int loadedAmbiguitiesLists;
	
	public Result result;
	static public TimeSeries timeSeries;
	
	public Map<String, Integer> locationTypesMap;
	
	public TychoParser() {
		REQUEST_LIMIT = 15;//15;
		TIMEOUT = 10000;
		TIMEOUT_LIMIT = 5;
		
		getKnownLocationTypes();
		
		return;
	}
	
	private void getKnownLocationTypes() {
		AlsDao alsDao = new AlsDao();
		ClientRule clientRule = alsDao.makeAlsClientRule(alsDao.baseUrl + API_LOCATION_TYPES_URL);
		JsonNode locationTypes = clientRule.get(alsDao.baseUrl + API_LOCATION_TYPES_URL).asJson();
		locationTypesMap = new HashMap<String, Integer>();
		Iterator<JsonNode> locationTypesIterator = locationTypes.elements();
		JsonNode currentType;
		
		while(locationTypesIterator.hasNext()) {
			currentType = locationTypesIterator.next();
			locationTypesMap.put(currentType.get("name").asText().toUpperCase(), currentType.get("id").asInt());
		}
		
		return;
	}
	
	public <T> T unmarshal(Class<T> docClass, InputStream inputStream)
		throws JAXBException {
		String packageName = docClass.getPackage().getName();
		JAXBContext jaxbContext = JAXBContext.newInstance(packageName);
		
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		
		T doc = (T)unmarshaller.unmarshal(inputStream);
		result = (Result)doc;
		
		timeSeries = new TimeSeries(result.getRow());
		
		return doc;
	}
	
	static public class BulkLocationResult {
		public List<NamedLocation> possibleMappings;
		public ALSIDQueryInput alsIDQueryInput;
		public List<Occurrence> occurrences;
		
		public class Occurrence {
			public Date date;
			public long number;
			
			public Occurrence(Date date, long number) {
				this.date = date;
				this.number = number;
				
				return;
			}
		}
		
		BulkLocationResult(ALSIDQueryInput alsIDQueryInput, List<NamedLocation> possibleMappings) {
			this.alsIDQueryInput = alsIDQueryInput;
			this.possibleMappings = possibleMappings;
			occurrences = new ArrayList<Occurrence>();
			
			return;
		}
		
		boolean addOccurrence(ALSIDQueryInput alsIDQueryInput) {
			return occurrences.add(new Occurrence(alsIDQueryInput.date, alsIDQueryInput.number));
		}
	}
	
	static public class Request {
		public ALSIDQueryInput alsIDQueryInput;
		public Promise<List<NamedLocation>> promisedLocations;
		public long timeStamp;
		public int timeouts;
		
		public Request() {
			return;
		}
		
		public Request(ALSIDQueryInput alsIDQueryInput) {
			timeouts = 0;
			this.alsIDQueryInput = alsIDQueryInput;
			
			return;
		}
		
		public List<NamedLocation> sendRequest(AlsDao alsDAO) throws UnsupportedEncodingException, URISyntaxException {
			timeStamp = Calendar.getInstance().getTimeInMillis();
			
			String urlQuery = "?queryTerm=" + (URLEncoder.encode(alsIDQueryInput.locationName, "UTF-8").replaceAll("\\++", "%20") + "&limit=0");
			ClientRule clientRule = alsDAO.makeAlsClientRule();
			
			return alsDAO.toLocations(clientRule.getByQuery(urlQuery).asJson().get("geoJSON"));
		}
		
		public Map<String, BulkLocationResult> sendBulkRequest(List<TychoLocationQuery> bulkRequest, List<ALSIDQueryInput> alsIDQueryInputs) throws UnsupportedEncodingException, URISyntaxException {
			AlsDao alsDAO = new AlsDao();
			ClientRule clientRule = alsDAO.makeAlsClientRule(alsDAO.bulkLocationsUrl);
			timeStamp = Calendar.getInstance().getTimeInMillis();
			
			JsonNode bulkRequestJSON = Json.toJson(bulkRequest);
/*
Iterator<JsonNode> iterator = bulkRequestJSON.elements();
Logger.debug(bulkRequestJSON.toString());
while(iterator.hasNext()){
	JsonNode next = iterator.next();
	Logger.debug(next.toString());
}
*/
/*
Logger.debug("==========");
for(int i = 0; i < alsIDQueryInputs.size(); i++) {
	Logger.debug(alsIDQueryInputs.get(i).toString());
}
*/
			
			JsonNode response = clientRule.post(bulkRequestJSON).asJson();
			Map<String, BulkLocationResult> locations = new HashMap<String, BulkLocationResult>();
			
			int i = 0;
			Iterator<JsonNode> responseIterator = response.iterator();
			while(responseIterator.hasNext()) {
				locations.put(bulkRequest.get(i).queryTerm, new BulkLocationResult(alsIDQueryInputs.get(i), alsDAO.toLocations(responseIterator.next())));
				i++;
			}
			
			for(int c = 0; c < timeSeries.entries.size(); c++) {
				locations.get(timeSeries.entries.get(c).alsIDQueryInput.locationName).addOccurrence(timeSeries.entries.get(c).alsIDQueryInput);
			}
			
			return locations;
		}
	}
	
	class TychoLocationQuery {
		public String queryTerm;
		//public Date startDate;
		//public Date endDate;
		public List<Integer> locationTypeIds;
		
		public TychoLocationQuery() {
			locationTypeIds =  new ArrayList<Integer>();
			
			return;
		}
		
		public TychoLocationQuery(ALSIDQueryInput alsIDQueryInput) {
			locationTypeIds =  new ArrayList<Integer>();
			consumeALSIDQueryInput(alsIDQueryInput);
			
			return;
		}
		
		public void consumeALSIDQueryInput(ALSIDQueryInput alsIDQueryInput) {
			String locationType = alsIDQueryInput.details.get("locationType").toUpperCase();
			queryTerm = alsIDQueryInput.locationName;
			
			if(locationTypesMap.get(locationType) != null) {
				locationTypeIds.add(locationTypesMap.get("STATE"));
				locationTypeIds.add(locationTypesMap.get("DISTRICT"));
				locationTypeIds.add(locationTypesMap.get("COUNTRY"));
			}
			
			/*
			String locationType = alsIDQueryInput.details.get("locationType").toUpperCase();
			
			name = alsIDQueryInput.locationName;
			
			
			if(locationTypesMap.get(locationType) != null) {
				locationTypeIds.add(locationTypesMap.get(locationType));
			}
			*/
			//start = Date(alsIDQueryInput.details.get("startDate"));
			//end = alsIDQueryInput.details.get("");
			/*
			if(locationType != null) {
				switch(locationType) {
					case "STATE":
						locationTypeIds.add(16);
					break;
					
					case "CITY":
						locationTypeIds.add(10);
					break;
					
					case "FEDERAL DISTRICT":
						locationTypeIds.add(90);
					case "DISTRICT":
						locationTypeIds.add(6);
					break;
					
					default:
					break;
				}
			}
			*/
			
			return;
		}
	}
	
	public Map<String, BulkLocationResult> bulkGetALSIDs() throws Exception {
		final int totalEntries = timeSeries.entries.size();
		HashMap<String, ALSIDQueryInput> uniqueEntries = new HashMap<String, ALSIDQueryInput>();
		String entry;
		for(int c = 0; c < totalEntries; c++) {
			entry = timeSeries.entries.get(c).alsIDQueryInput.details.get("country") +
					timeSeries.entries.get(c).alsIDQueryInput.details.get("state") +
					timeSeries.entries.get(c).alsIDQueryInput.details.get("locationType") +
					timeSeries.entries.get(c).alsIDQueryInput.locationName;
			uniqueEntries.put(entry, timeSeries.entries.get(c).alsIDQueryInput);
		}
		
		loadedAmbiguitiesLists = 0;
		Request request;
		
		List<TychoLocationQuery> bulkLocationQueries = new ArrayList<TychoLocationQuery>();
		List<ALSIDQueryInput> alsIDQueryInputs = new ArrayList<ALSIDQueryInput>();
		TychoLocationQuery tychoLocation;
		
		int i = 0;
		Iterator<String> uniqueEntriesIterator = uniqueEntries.keySet().iterator();
		while(uniqueEntriesIterator.hasNext()) {
				alsIDQueryInputs.add(uniqueEntries.get(uniqueEntriesIterator.next()));
				tychoLocation = new TychoLocationQuery(alsIDQueryInputs.get(i));
				bulkLocationQueries.add(tychoLocation);
				i++;
		}
		
		request = new Request();
		
		return request.sendBulkRequest(bulkLocationQueries, alsIDQueryInputs);
	}
	
	public Map<String, List<NamedLocation>> asynchronizedGetALSIDs() throws Exception {
		class Request {
			public ALSIDQueryInput alsIDQueryInput;
			public Promise<List<NamedLocation>> promisedLocations;
			public long timeStamp;
			public int timeouts;
			
			Request(ALSIDQueryInput alsIDQueryInput) {
				timeouts = 0;
				this.alsIDQueryInput = alsIDQueryInput;
				
				return;
			}
			
			void sendRequest(AlsDao alsDAO) throws UnsupportedEncodingException, URISyntaxException {
				timeStamp = Calendar.getInstance().getTimeInMillis();
				promisedLocations = alsDAO.getLocations(alsIDQueryInput);
				
				promisedLocations.onFailure((exception)-> {
					if(exception.getClass() == java.util.concurrent.TimeoutException.class) {
						System.out.println("Timeout...");
						
						if(timeouts > TIMEOUT_LIMIT) {
							System.err.println("Network Error: too many timeouts");
							throw new Exception("Network Error: too many timeouts");
						}
						
						timeouts++;
						
						sendRequest(alsDAO);
					}
					else {
						throw exception;
					}
					
					return;
				});
				
				return;
			}
			
			boolean timedOut() throws Exception {
				if(timeouts > TIMEOUT_LIMIT) {
					System.err.println("Network Error: too many timeouts");
					throw new Exception("Network Error: too many timeouts");
				}
				
				if((Calendar.getInstance().getTimeInMillis() - timeStamp) > TIMEOUT) {
					timeouts++;
					
					return true;
				}
				
				return false;
			}
			
			void setMap(Queue<Request> requestQueue, Map<String, List<NamedLocation>> possibleIdentities, int uniqueListSize) {
				Request request = this;
				
				request.promisedLocations.map(locations -> {
					String inputName = locations.get(0).getALSIDQueryInput().locationName;
					
					if(!locations.isEmpty()) {
						possibleIdentities.put(inputName, locations);
					}
					
					synchronized(this) {
						requestQueue.remove(this);
						loadedAmbiguitiesLists++;
//System.out.println("Loaded (" + inputName + ") " + loadedAmbiguitiesLists + " of " + uniqueListSize + " ambiguities lists\n");
						return loadedAmbiguitiesLists;
					}
				});
				
				return;
			}
		}
		
		final int totalEntries = timeSeries.entries.size();
		HashMap<String, ALSIDQueryInput> uniqueEntries = new HashMap<String, ALSIDQueryInput>();
		String entry;
		for(int c = 0; c < totalEntries; c++) {
			entry = timeSeries.entries.get(c).alsIDQueryInput.details.get("country") +
					timeSeries.entries.get(c).alsIDQueryInput.details.get("state") +
					timeSeries.entries.get(c).alsIDQueryInput.details.get("locationType") +
					timeSeries.entries.get(c).alsIDQueryInput.locationName;
			uniqueEntries.put(entry, timeSeries.entries.get(c).alsIDQueryInput);
		}
		final int uniqueListSize = uniqueEntries.size();
		
		loadedAmbiguitiesLists = 0;
		AlsDao alsDAO = new AlsDao();
		Map<String, List<NamedLocation>> possibleIdentities = new HashMap<String, List<NamedLocation>>();
		Queue<Request> requestQueue = new ConcurrentLinkedQueue<Request>();
		Request request;
		
		Iterator<String> uniqueEntriesIterator = uniqueEntries.keySet().iterator();
		int i = 0;
		while(uniqueEntriesIterator.hasNext()) {
			if(requestQueue.size() < REQUEST_LIMIT) {
				request = new Request(uniqueEntries.get(uniqueEntriesIterator.next()));
				
				synchronized(this) {
					requestQueue.add(request);
					wait(125);
				}
			
				request.sendRequest(alsDAO);
				request.setMap(requestQueue, possibleIdentities, uniqueListSize);
				i++;
			}
		}
		
		/**/
		while(true) {
			synchronized(this) {
				if(loadedAmbiguitiesLists >= uniqueListSize) {
					break;
				}
			}
		}
		/**/
		
		//perform finishing functions
System.out.println("Finished Loading.");
//printAmbiguities(possibleIdentities);
		
		return possibleIdentities;
	}
	
	private void printAmibiguities(Map<String, List<NamedLocation>> possibleIdentities) {
		//debug output
		int ambiguitiesLength;
		String inputName;
		NamedLocation currentLocation;
		Iterator<String> keyIterator = possibleIdentities.keySet().iterator();
		
		System.out.println("Loaded " + loadedAmbiguitiesLists + " lists for each unique identity");
		while(keyIterator.hasNext()) {
			inputName = keyIterator.next();
			
			System.out.println("\n" + inputName);
			ambiguitiesLength = possibleIdentities.get(inputName).size();
			for(int j = 0; j < ambiguitiesLength; j++) {
				currentLocation = (NamedLocation)possibleIdentities.get(inputName).get(j);
				System.out.println(currentLocation.getId() + ": " + currentLocation.getLabel());
			}
		}
		
		return;
	}
}
