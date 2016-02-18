package models;

import static org.fest.assertions.Assertions.assertThat;
import interactors.series_data_file.SeriesDataFile;

import org.junit.Test;

import suites.SeriesDataFileHelper;

public class TestSeriesDataFile {
	
	@Test
	public void testAutoReadDelimAndFormat(){
		
		SeriesDataFile dataFile = SeriesDataFileHelper.createTestSeriesDataFileWithTab();
		assertThat(dataFile.getDelimiter()).isEqualTo('\t');
		assertThat(dataFile.getFileFormat()).isEqualTo(SeriesDataFile.ALS_ID_FORMAT);
		
		dataFile = SeriesDataFileHelper.createTestSeriesDataFileWithCoordianteFormat();
		assertThat(dataFile.getDelimiter()).isEqualTo(',');
		assertThat(dataFile.getFileFormat()).isEqualTo(SeriesDataFile.COORDINATE_FORMAT);
		
		dataFile = SeriesDataFileHelper.createTestSeriesDataFileWithUnixLineEnding();
		assertThat(dataFile.getDelimiter()).isEqualTo('\t');
		assertThat(dataFile.getFileFormat()).isEqualTo(SeriesDataFile.ALS_ID_FORMAT);
	}

}
