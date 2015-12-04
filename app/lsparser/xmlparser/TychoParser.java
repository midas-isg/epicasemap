package lsparser.xmlparser;

import lsparser.tycho.*;
import gateways.webservice.AlsDao;
import models.entities.Location;
import models.entities.NamedLocation;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
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
	
	public void getALSIDs() throws Exception {
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
				//alsDAO = new AlsDao();
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
			
			void setMap(Queue<Request> requestQueue, Map<String, List> possibleIdentities, int seriesSize) {
				Request request = this;
				
				request.promisedLocations.map(locations -> {
					String inputName = locations.get(0).getName();
					possibleIdentities.put(inputName, locations);
					
					synchronized(this) {
						requestQueue.remove(this);
					}
					
System.out.println("Loaded (" + inputName + ") " + (++loadedAmbiguitiesLists) + " of " + seriesSize + " ambiguities lists, but duplicates were ignored\n");
					
					return loadedAmbiguitiesLists;
				});
				
				return;
			}
		}
		
		final int seriesSize = timeSeries.entries.size();
		loadedAmbiguitiesLists = 0;
		AlsDao alsDAO = new AlsDao();
		Map<String, List> possibleIdentities = new HashMap<String, List>();
		Queue<Request> requestQueue = new ConcurrentLinkedQueue<Request>();
		Request request;
		
		int i = 0;
		while(i < seriesSize) {
			if(requestQueue.size() < REQUEST_LIMIT) {
				request = new Request(timeSeries.entries.get(i).alsIDQueryInput);
				
				synchronized(this) {
					requestQueue.add(request);
				}
			
				request.sendRequest(alsDAO);
				request.setMap(requestQueue, possibleIdentities, seriesSize);
				i++;
			}
			/*
			else {
				//check for outstanding requests
				synchronized(this) {
					if(!requestQueue.isEmpty()) {
						Iterator<Request> queueIterator = requestQueue.iterator();
						while(queueIterator.hasNext()) {
							request = queueIterator.next();
							
							if(request.timedOut()) {
								System.out.println("Timed out!");
								request.sendRequest(alsDAO);
							}
						}
					}
				}
			}
			*/
		}
		
		synchronized(this) {
			while(loadedAmbiguitiesLists < seriesSize) {
				/*
				try {
					this.wait(125);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					throw e;
				}
				*/
			}
		}
		
System.out.println("Finished Loading.");
	
//debug output
String inputName;
int ambiguitiesLength = possibleIdentities.size();
NamedLocation currentLocation;
Iterator<String> keyIterator= possibleIdentities.keySet().iterator();

System.out.println("Loaded " + i + " lists for each unique identity");
while(keyIterator.hasNext()) {
	inputName = keyIterator.next();
	
	System.out.println("\n" + inputName);
	for(int j = 0; j < ambiguitiesLength; j++) {
		currentLocation = (NamedLocation)possibleIdentities.get(inputName).get(j);
		System.out.println(currentLocation.getId() + ": " + currentLocation.getLabel());
	}
}
	
		//send HashMap for user selection
		
		return;
	}
	
	public void generateCSV() {
		csvGenerator = new CSVGenerator(timeSeries);
		return;
	}
}
