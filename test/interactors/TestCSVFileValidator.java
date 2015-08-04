package interactors;

import static org.fest.assertions.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.Test;

import suites.CSVFileHelper;

public class TestCSVFileValidator {

	@Test
	public void testGetDateTimeError() throws Exception {
		CSVFileValidator validator = new CSVFileValidator();
		CSVFileHelper helper = new CSVFileHelper();
		CSVFile dataFile = helper
				.createTestDataFileWithAlsIdFormatWithErrors();
		CSVRecord record = helper.getCSVRecord(dataFile);
		String error = validator.getDateTimeError(record, dataFile);
		assertThat(error).isEqualTo(
				"time: Invalid format: \"2015-01-\" is malformed at \"-\"");

	}

	@Test
	public void testGetValueError() throws Exception {
		CSVFileValidator validator = new CSVFileValidator();
		CSVFileHelper helper = new CSVFileHelper();
		CSVFile dataFile = helper
				.createTestDataFileWithAlsIdFormatWithErrors();
		CSVRecord record = helper.getCSVRecord(dataFile);
		String error = validator.getValueError(record, dataFile);
		assertThat(error).isEqualTo("VALUE: b is not valid.");

	}

	@Test
	public void testGetLocationValueError() throws Exception {
		CSVFileValidator validator = new CSVFileValidator();
		CSVFileHelper helper = new CSVFileHelper();
		CSVFile dataFile = helper
				.createTestDataFileWithAlsIdFormatWithErrors();
		CSVRecord record = helper.getCSVRecord(dataFile);
		String error = validator.getLocationValueError(record, dataFile);
		assertThat(error).isEqualTo("als_id: a is not valid.");

	}
	
	@Test
	public void testGetRecordSizeError() throws Exception {
		CSVFileValidator validator = new CSVFileValidator();
		CSVFileHelper helper = new CSVFileHelper();
		CSVFile dataFile = helper.createTestDataFileWithAlsIdFormatWithErrors();
		CSVRecord record = helper.getCSVRecord(dataFile);
		String error = validator.getRecordSizeError(record,dataFile);
		assertThat(error).isEqualTo("row has 4 columns. should have 3 columns.");
		
	}
	
	@Test
	public void testValidateFileHeader() throws Exception {
		CSVFileValidator validator = new CSVFileValidator();
		CSVFileHelper helper = new CSVFileHelper();
		CSVFile dataFile = helper.createTestDataFileWithAlsIdFormatWithErrors();
		CSVFileParser fileParser = new CSVFileParser();
		CSVParser parser = fileParser.parse(dataFile);
		List<String> error = validator.validateFileHeaders(parser,dataFile);
		List<String> expected = Arrays.asList("number of columns is 4. should be 3.","\"lat\" column name is not allowed in alsIdFormat format.");
		assertThat(error).isEqualTo(expected);
		
	}
	
	@Test
	public void testValidateAlsIdFormatted() {
		CSVFileValidator validator = new CSVFileValidator();
		CSVFileHelper helper = new CSVFileHelper();
		CSVFile dataFile = helper.createTestDataFileWithAlsIdFormatWithErrors();
		Map<Long, List<String>> error = validator.validate(dataFile);
		List<String> expected = Arrays.asList("number of columns is 4. should be 3.","\"lat\" column name is not allowed in alsIdFormat format.");
		assertThat(error.get(1L)).isEqualTo(expected);
		
	}
	
	@Test
	public void testValidateCoordinateFormatted() {
		CSVFileValidator validator = new CSVFileValidator();
		CSVFileHelper helper = new CSVFileHelper();
		CSVFile dataFile = helper.createTestDataFileWithCoordinateFormatWithErrors();
		Map<Long, List<String>> error = validator.validate(dataFile);
		List<String> expected = Arrays.asList("number of columns is 5. should be 4.","\"error\" column name is not allowed in coordinateFormat format.");
		assertThat(error.get(1L)).isEqualTo(expected);
		
	}
	
	@Test
	public void testGetLocationValueErrorForCoordinateFormat() throws Exception {
		CSVFileValidator validator = new CSVFileValidator();
		CSVFileHelper helper = new CSVFileHelper();
		CSVFile dataFile = helper
				.createTestDataFileWithCoordinateFormatWithErrors();
		CSVRecord record = helper.getCSVRecord(dataFile);
		String error = validator.getLocationValueError(record, dataFile);
		assertThat(error).isEqualTo("latitude: a is not valid. longitude: b is not valid.");
	}

}
