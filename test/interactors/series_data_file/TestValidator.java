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
	public void testGetDateTimeError() throws Exception {
		Validator validator = new Validator();
		SeriesDataFileHelper helper = new SeriesDataFileHelper();
		SeriesDataFile dataFile = helper
				.createTestSeriesDataFileWithAlsIdFormatWithErrors();
		CSVRecord record = helper.getCSVParser(dataFile).iterator().next();
		String error = validator.getDateTimeError(record, dataFile);
		assertThat(error).isEqualTo(
				"time: Invalid format: \"2015-01-\" is malformed at \"-\"");

	}

	@Test
	public void testGetValueError() throws Exception {
		Validator validator = new Validator();
		SeriesDataFileHelper helper = new SeriesDataFileHelper();
		SeriesDataFile dataFile = helper
				.createTestSeriesDataFileWithAlsIdFormatWithErrors();
		CSVRecord record = helper.getCSVParser(dataFile).iterator().next();
		String error = validator.getValueError(record, dataFile);
		assertThat(error).isEqualTo("VALUE: b is not valid.");

	}
	
	@Test
	public void test() throws Exception {
		Runnable test = testLocationError();
		Server.run(test);
	}

	@Test
	public void testGetLocationValueError() {
		runWithTransaction(() -> getLocationValueError());
	}
	private void getLocationValueError() throws Exception {
		SeriesDataFileHelper helper = new SeriesDataFileHelper();
		SeriesDataFile dataFile = helper
				.createTestSeriesDataFileWithAlsIdFormatWithErrors();
		
		CSVParser parser = helper.getCSVParser(dataFile);
		CSVRecord record = parser.iterator().next();
		
		Validator validator = new Validator();

		String error = validator.getLocationValueError(record, dataFile);
		assertThat(error).isEqualTo("als_id: a is not valid.");
		
		record = parser.iterator().next();
		error = validator.getLocationValueError(record, dataFile);
		assertThat(error).isEqualTo("als_id: 1234567890 does not exist in ALS.");

	}
	
	@Test
	public void testGetRecordSizeError() throws Exception {
		SeriesDataFileHelper helper = new SeriesDataFileHelper();
		SeriesDataFile dataFile = helper.createTestSeriesDataFileWithAlsIdFormatWithErrors();
		CSVRecord record = helper.getCSVParser(dataFile).iterator().next();
		String error = getRecordSizeError(dataFile, record);
		assertThat(error).isEqualTo("row has 4 columns. should have 3 columns.");
		
	}

	private String getRecordSizeError(SeriesDataFile dataFile, CSVRecord record) {
		Validator validator = new Validator();
		String error = validator.getRecordSizeError(record,dataFile);
		return error;
	}
	
	@Test
	public void testValidateFileHeader() throws Exception {
		
		SeriesDataFileHelper helper = new SeriesDataFileHelper();
		SeriesDataFile dataFile = helper.createTestSeriesDataFileWithAlsIdFormatWithErrors();
		List<String> error = getHeaderErrors(dataFile);
		List<String> expected = Arrays.asList("number of columns is 4. should be 3.","\"lat\" column name is not allowed in alsIdFormat format.");
		assertThat(error).isEqualTo(expected);
		
	}
	
	@Test
	public void testValidateFormat() throws Exception {
		SeriesDataFileHelper helper = new SeriesDataFileHelper();
		SeriesDataFile dataFile = helper.creatDataSeriesFileWithDelimiterError();
		List<String> error = getHeaderErrors(dataFile);
		List<String> expected = Arrays.asList("number of columns is 4. should be 3.","\"\" column name is not allowed in alsIdFormat format.");
		assertThat(error).isEqualTo(expected);
	}
	
	@Test
	public void testValidateHeader() throws Exception {
		SeriesDataFileHelper helper = new SeriesDataFileHelper();
		SeriesDataFile dataFile = helper.creatDataSeriesFileWithHeaderError();
		List<String> error = getHeaderErrors(dataFile);
		List<String> expected = Arrays.asList("column names are not valid.");
		assertThat(error).isEqualTo(expected);
	}
		
	@Test
	public void testValidateAlsIdFormatted() {
		Validator validator = new Validator();
		SeriesDataFileHelper helper = new SeriesDataFileHelper();
		SeriesDataFile dataFile = helper.createTestSeriesDataFileWithAlsIdFormatWithErrors();
		Map<Long, List<String>> error = validator.validate(dataFile);
		List<String> expected = Arrays.asList("number of columns is 4. should be 3.","\"lat\" column name is not allowed in alsIdFormat format.");
		assertThat(error.get(1L)).isEqualTo(expected);
		
	}
	
	@Test
	public void testValidateCoordinateFormatted() {
	
		SeriesDataFileHelper helper = new SeriesDataFileHelper();
		SeriesDataFile dataFile = helper.createTestSeriesDataFileWithCoordinateFormatWithErrors();
		Validator validator = new Validator();
		Map<Long, List<String>> error = validator.validate(dataFile);
		List<String> expected = Arrays.asList("number of columns is 5. should be 4.","\"error\" column name is not allowed in coordinateFormat format.");
		assertThat(error.get(1L)).isEqualTo(expected);
		
	}
	
	@Test
	public void testGetLocationValueErrorForCoordinateFormat() throws Exception {
		Validator validator = new Validator();
		SeriesDataFileHelper helper = new SeriesDataFileHelper();
		SeriesDataFile dataFile = helper
				.createTestSeriesDataFileWithCoordinateFormatWithErrors();
		CSVRecord record = helper.getCSVParser(dataFile).iterator().next();
		String error = validator.getLocationValueError(record, dataFile);
		assertThat(error).isEqualTo("latitude: a is not valid. longitude: b is not valid.");
	}
	
	private List<String> getHeaderErrors(SeriesDataFile dataFile) throws Exception {
		Parser fileParser = new Parser();
		CSVParser parser = fileParser.parse(dataFile);
		Validator validator = new Validator();
		List<String> error = validator.getFileConsistencyError(parser,dataFile);
		return error;
	}
	
	private static Runnable testLocationError() {
		return () -> wrapped();
	}

	private static void wrapped() {
		try {
			newInstance().testGetLocationValueError();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private static TestValidator newInstance() {
		return new TestValidator();
	}
	
	private static void runWithTransaction(Callback0 callback) {
		App.newWithInMemoryDbWithDbOpen().runWithTransaction(callback);
	}


}
