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

import com.sun.corba.se.impl.orbutil.threadpool.TimeoutException;

import akka.dispatch.ExecutionContexts;
import play.api.libs.concurrent.Akka;
import play.data.format.Formats.DateTime;
import play.libs.F.Function0;
import play.libs.F.Promise;
import play.libs.ws.WSResponse;

public class TychoParser {
	private final int REQUEST_LIMIT;
	private final long TIMEOUT;
	private final int TIMEOUT_LIMIT;
	
	private int loadedAmbiguitiesLists;
	private CSVGenerator csvGenerator;
	
	public Result result;
	public TimeSeries timeSeries;
	
	public TychoParser() {
		REQUEST_LIMIT = 15;//15;
		TIMEOUT = 10000;
		TIMEOUT_LIMIT = 5;
		
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
	
	public Map<String, List<NamedLocation>> synchronizedGetALSIDs() throws Exception {
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
			
			List<NamedLocation> sendRequest(AlsDao alsDAO) throws UnsupportedEncodingException, URISyntaxException {
				timeStamp = Calendar.getInstance().getTimeInMillis();
				
				String urlQuery = "?q=" + (URLEncoder.encode(alsIDQueryInput.locationName, "UTF-8").replaceAll("\\++", "%20"));
				ClientRule clientRule = alsDAO.makeAlsClientRule();
				
				return alsDAO.toLocations(clientRule.getByQuery(urlQuery).asJson().get("geoJSON"), alsIDQueryInput.locationName);
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
		List<NamedLocation> resolutionList;
		Map<String, List<NamedLocation>> possibleIdentities = new HashMap<String, List<NamedLocation>>();
		Request request;
		
		Iterator<String> uniqueEntriesIterator = uniqueEntries.keySet().iterator();
		while(uniqueEntriesIterator.hasNext()) {
				request = new Request(uniqueEntries.get(uniqueEntriesIterator.next()));
				resolutionList = request.sendRequest(alsDAO);
				possibleIdentities.put(request.alsIDQueryInput.locationName, resolutionList);
				loadedAmbiguitiesLists++;
				System.out.println("Loaded (" + request.alsIDQueryInput.locationName + ") " + loadedAmbiguitiesLists + " of " + uniqueListSize + " ambiguities lists\n");
		}
		
		//perform finishing functions
System.out.println("Finished Loading.");

		//printAmibiguities()
		{
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
		}	
		
		//send HashMap for user selection
		
		return possibleIdentities;
	}
	
	public Map<String, List<NamedLocation>> getALSIDs() throws Exception {
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
					String inputName = locations.get(0).getInputName();
					possibleIdentities.put(inputName, locations);
					
					synchronized(this) {
						requestQueue.remove(this);
						loadedAmbiguitiesLists++;
System.out.println("Loaded (" + inputName + ") " + loadedAmbiguitiesLists + " of " + uniqueListSize + " ambiguities lists\n");
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

		//printAmibiguities()
		{
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
		}	
		
		//send HashMap for user selection
		
		return possibleIdentities;
	}
	
	public void generateCSV() {
		csvGenerator = new CSVGenerator(timeSeries);
		return;
	}
}
