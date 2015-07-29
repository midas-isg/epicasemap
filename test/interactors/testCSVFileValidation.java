package interactors;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.Test;

import suites.CSVFileHelper;

public class testCSVFileValidation {

//	validateFileHeaders
//	
//	private String getConsistencyError(CSVRecord record) {
//		
//		
//	}
//	
//	private String getLocationValueError(CSVRecord record, String fileFormat) {
//
//		private String getValueError(CSVRecord record) {

	@Test
	public void testGetDateTimeError() {
		CSVFileValidator validator = new CSVFileValidator();
		CSVFileHelper helper = new CSVFileHelper();
		CSVFile dataFile = helper.createTestDataFileWithApolloIdFormatWithErrors();
		CSVRecord record = helper.getCSVRecord(dataFile);
		String error = validator.getDateTimeError(record,dataFile);
		assertThat(error).isEqualTo("time: Invalid format: \"2015-01-\" is malformed at \"-\"");
		
	}
	
	@Test
	public void testGetValueError() {
		CSVFileValidator validator = new CSVFileValidator();
		CSVFileHelper helper = new CSVFileHelper();
		CSVFile dataFile = helper.createTestDataFileWithApolloIdFormatWithErrors();
		CSVRecord record = helper.getCSVRecord(dataFile);
		String error = validator.getValueError(record,dataFile);
		assertThat(error).isEqualTo("VALUE: b is not valid.");
		
	}
	




}
