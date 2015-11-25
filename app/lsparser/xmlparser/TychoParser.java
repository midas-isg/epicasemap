package lsparser.xmlparser;

import lsparser.tycho.*;
import gateways.webservice.AlsDao;
import models.entities.Location;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.*;

public class TychoParser {
	public Result result;
	public TimeSeries timeSeries;
	private CSVGenerator csvGenerator;
	
	public TychoParser() {
		
		return;
	}
	
	public <T> T unmarshal(Class<T> docClass, InputStream inputStream)
		throws JAXBException {
		String packageName = docClass.getPackage().getName();
		JAXBContext jaxbContext = JAXBContext.newInstance(packageName);
		
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		
		//JAXBElement<T> doc = (JAXBElement<T>)unmarshaller.unmarshal(inputStream);
		T doc = (T)unmarshaller.unmarshal(inputStream);
		result = (Result)doc;
		
		timeSeries = new TimeSeries(result.getRow());
		
		//return doc.getValue();
		return doc;
	}
	
	public void getALSIDs() throws URISyntaxException, UnsupportedEncodingException {
		int seriesSize = timeSeries.entries.size();
		AlsDao alsDAO = new AlsDao();
		Map<String, List> possibleIdentities = new HashMap<String, List>();
		List<Location> locations;
		
		for(int i = 0; i < seriesSize; i++) {
			//hash key : list<Locations>
			locations = alsDAO.getLocations(timeSeries.entries.get(i).alsIDQueryInput);
			possibleIdentities.put(timeSeries.entries.get(i).alsIDQueryInput.locationName, locations);
			
System.out.println(timeSeries.entries.get(i).alsIDQueryInput.locationName);
		}
		
		//send HashMap for user selection
		
		return;
	}
	
	public void generateCSV() {
		csvGenerator = new CSVGenerator(timeSeries);
		return;
	}
}
