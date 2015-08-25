package interactors.series_data_file;

import interactors.series_data_file.Parser;
import models.SeriesDataFile;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import suites.SeriesDataFileHelper;

public class TestParser {

	@Test
	public void testParse() throws Exception{
		SeriesDataFile dataFile = SeriesDataFileHelper.createTestSeriesDataFileWithAlsIdFormat();
		Parser parser = new Parser();
		int NumOfRecords = parser.parse(dataFile).getRecords().size();
		assertThat(NumOfRecords).isEqualTo(5);
	}
}
