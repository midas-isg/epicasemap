package models;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;

import org.junit.Test;

public class TestSeriesDataFile {
	
	@Test
	public void testAutoReadDelimAndFormat(){
		File file = new File("test\\resources\\input-files\\test_alsId_format_tab.txt");
		SeriesDataFile dataFile = new SeriesDataFile(file);
		assertThat(dataFile.getDelimiter()).isEqualTo('\t');
		assertThat(dataFile.getFileFormat()).isEqualTo(SeriesDataFile.ALS_ID_FORMAT);
		
		file = new File("test\\resources\\input-files\\test_coordinate_format.txt");
		dataFile = new SeriesDataFile(file);
		assertThat(dataFile.getDelimiter()).isEqualTo(',');
		assertThat(dataFile.getFileFormat()).isEqualTo(SeriesDataFile.COORDINATE_FORMAT);
		
		file = new File("test\\resources\\input-files\\test_alsId_format_unix_line_ending.txt");
		dataFile = new SeriesDataFile(file);
		assertThat(dataFile.getDelimiter()).isEqualTo('\t');
		assertThat(dataFile.getFileFormat()).isEqualTo(SeriesDataFile.ALS_ID_FORMAT);
		
		
	}

}
