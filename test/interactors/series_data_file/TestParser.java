package interactors.series_data_file;

import interactors.series_data_file.Parser;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import suites.SeriesDataFileHelper;

public class TestParser {

	@Test
	public void testParse() throws Exception{
		SeriesDataFile dataFile = SeriesDataFileHelper.createTestSeriesDataFileWithAlsIdFormat();
		Parser parser = new Parser(dataFile);
		int fileHeaderSize = parser.getFileHeaders().size();
		assertThat(fileHeaderSize).isEqualTo(3);
	}
}
