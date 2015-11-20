package suites.lsparser;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import javax.xml.bind.JAXBException;

import lsparser.xmlparser.*;
import lsparser.tycho.*;

import org.junit.Test;

public class TychoSampleTest {

	@Test
	public void test() {
		assert(true);
		
		return;
	}
	
	@Test
	public void testTychoSampleData() throws FileNotFoundException, JAXBException {
		TychoParser tychoParser = new TychoParser();
		tychoParser.unmarshal(Result.class, new FileInputStream("test/resources/input-files/tycho_sample.xml"));
		assert(tychoParser.result.getCount().intValue() == 6750);
		
		//tychoParser.timeSeries.printSeries();
		
		return;
	}

}
