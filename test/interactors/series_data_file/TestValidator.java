package interactors.series_data_file;

import static org.fest.assertions.Assertions.assertThat;
import integrations.app.App;
import integrations.server.Server;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import models.SeriesDataFile;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.Test;

import play.libs.F.Callback0;
import suites.SeriesDataFileHelper;

public class TestValidator {
	
	@Test
	public void testValidator() {
		runWithTransaction(() -> testGetErrors());
	}
	
	private void testGetErrors() {
		testGetDateTimeError();
		testGetValueError();
		testGetRecordSizeError();
		testValidateHeader1();
		testValidateHeader2();
		testValidateFormat();
		testValidateAlsIdFormatted();
		testValidateCoordinateFormatted();
		testGetLocationValueErrorForCoordinateFormat();
		
	}
	
	@Test
	public void test() {
		Runnable test = testLocationError();
		Server.run(test);
	}

	private void testGetLocationValueError() {
		runWithTransaction(() -> getAlsLocationError());
	}
	private void getAlsLocationError()  {
		SeriesDataFile dataFile = SeriesDataFileHelper.createTestSeriesDataFileWithAlsIdFormatWithErrors();
		
		CSVParser parser = SeriesDataFileHelper.getCSVParser(dataFile);
		CSVRecord record = parser.iterator().next();
		
		Validator validator = SeriesDataFileHelper.makeValidator();

		String error = validator.getLocationValueError(record, dataFile);
		assertThat(error).isEqualTo("als_id: a is not valid.");
		
		record = parser.iterator().next();
		error = validator.getLocationValueError(record, dataFile);
		assertThat(error).isEqualTo("als_id: 1234567890 does not exist in ALS.");

	}

	private void testGetDateTimeError() {
		Validator validator = SeriesDataFileHelper.makeValidator();
		SeriesDataFile dataFile = SeriesDataFileHelper.createTestSeriesDataFileWithAlsIdFormatWithErrors();
		CSVRecord record = SeriesDataFileHelper.getCSVParser(dataFile).iterator().next();
		String error = validator.getDateTimeError(record, dataFile);
		assertThat(error).isEqualTo(
				"time: Invalid format: \"2015-01-\" is malformed at \"-\"");

	}

	private void testGetValueError() {
		Validator validator = SeriesDataFileHelper.makeValidator();
		SeriesDataFile dataFile = SeriesDataFileHelper.createTestSeriesDataFileWithAlsIdFormatWithErrors();
		CSVRecord record = SeriesDataFileHelper.getCSVParser(dataFile).iterator().next();
		String error = validator.getValueError(record, dataFile);
		assertThat(error).isEqualTo("VALUE: b is not valid.");

	}
	
	private void testGetRecordSizeError() {
		SeriesDataFile dataFile = SeriesDataFileHelper.createTestSeriesDataFileWithAlsIdFormatWithErrors();
		CSVRecord record = SeriesDataFileHelper.getCSVParser(dataFile).iterator().next();
		String error = getRecordSizeError(dataFile, record);
		assertThat(error).isEqualTo("row has 4 columns. should have 3 columns.");
		
	}

	private String getRecordSizeError(SeriesDataFile dataFile, CSVRecord record) {
		Validator validator = SeriesDataFileHelper.makeValidator();
		String error = validator.getRecordSizeError(record,dataFile);
		return error;
	}
	
	private void testValidateHeader1() {
		
		SeriesDataFile dataFile = SeriesDataFileHelper.createTestSeriesDataFileWithAlsIdFormatWithErrors();
		List<String> error = getHeaderErrors(dataFile);
		List<String> expected = Arrays.asList("number of columns is 4. should be 3.","\"lat\" column name is not allowed in alsIdFormat format.");
		assertThat(error).isEqualTo(expected);
		
	}
	
	private void testValidateFormat() {
		SeriesDataFile dataFile = SeriesDataFileHelper.creatDataSeriesFileWithDelimiterError();
		List<String> error = getHeaderErrors(dataFile);
		List<String> expected = Arrays.asList("number of columns is 4. should be 3.","\"\" column name is not allowed in alsIdFormat format.");
		assertThat(error).isEqualTo(expected);
	}
	
	private void testValidateHeader2() {
		SeriesDataFile dataFile = SeriesDataFileHelper.creatDataSeriesFileWithHeaderError();
		List<String> error = getHeaderErrors(dataFile);
		List<String> expected = Arrays.asList("column names are not valid.");
		assertThat(error).isEqualTo(expected);
	}
		
	private void testValidateAlsIdFormatted() {
		Validator validator = SeriesDataFileHelper.makeValidator();
		SeriesDataFile dataFile = SeriesDataFileHelper.createTestSeriesDataFileWithAlsIdFormatWithErrors();
		Map<Long, List<String>> error = validator.validate(dataFile);
		List<String> expected = Arrays.asList("number of columns is 4. should be 3.","\"lat\" column name is not allowed in alsIdFormat format.");
		assertThat(error.get(1L)).isEqualTo(expected);
		
	}
	
	private void testValidateCoordinateFormatted() {
	
		SeriesDataFile dataFile = SeriesDataFileHelper.createTestSeriesDataFileWithCoordinateFormatWithErrors();
		Validator validator = SeriesDataFileHelper.makeValidator();
		Map<Long, List<String>> error = validator.validate(dataFile);
		List<String> expected = Arrays.asList("number of columns is 5. should be 4.","\"error\" column name is not allowed in coordinateFormat format.");
		assertThat(error.get(1L)).isEqualTo(expected);
		
	}
	
	private void testGetLocationValueErrorForCoordinateFormat() {
		Validator validator = SeriesDataFileHelper.makeValidator();
		SeriesDataFile dataFile = SeriesDataFileHelper.createTestSeriesDataFileWithCoordinateFormatWithErrors();
		CSVRecord record = SeriesDataFileHelper.getCSVParser(dataFile).iterator().next();
		String error = validator.getLocationValueError(record, dataFile);
		assertThat(error).isEqualTo("latitude: a is not valid. longitude: b is not valid.");
	}
	
	private List<String> getHeaderErrors(SeriesDataFile dataFile)  {
		Parser fileParser = new Parser();
		CSVParser parser = fileParser.parse(dataFile);
		Validator validator = SeriesDataFileHelper.makeValidator();
		List<String> error = validator.getFileConsistencyError(parser,dataFile);
		return error;
	}
	
	private static Runnable testLocationError() {
		return () -> newInstance().testGetLocationValueError();
	}

	private static TestValidator newInstance() {
		return new TestValidator();
	}
	
	private static void runWithTransaction(Callback0 callback) {
		App.newWithInMemoryDbWithDbOpen().runWithTransaction(callback);
	}


}
