package lsparser.xmlparser;

import lsparser.tycho.*;

import java.io.InputStream;
import java.util.*;

import javax.xml.bind.*;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Schema;

public class TychoParser {
	//private ObjectFactory objectFactory;
	public Result result;
	public TimeSeries timeSeries;
	
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
}
