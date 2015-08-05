package interactors;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import suites.SeriesDataFileHelper;

public class TestSeriesDataFileParser {

	@Test
	public void testParse() throws Exception{
		SeriesDataFileHelper helper = new SeriesDataFileHelper();
		SeriesDataFile dataFile = helper.createTestSeriesDataFileWithAlsIdFormat();
		SeriesDataFileParser parser = new SeriesDataFileParser();
		int NumOfRecords = parser.parse(dataFile).getRecords().size();
		assertThat(NumOfRecords).isEqualTo(5);
	}
}
