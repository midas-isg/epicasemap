package interactors.series_data_file;

import static org.fest.assertions.Assertions.assertThat;
import integrations.app.App;
import integrations.server.Server;
import interactors.series_data_file.Parser.DataPoint;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
		
		Parser parser = SeriesDataFileHelper.getParser(dataFile);
		DataPoint dataPoint = parser.next();
		
		Validator validator = SeriesDataFileHelper.makeValidator(dataFile);

		String error = validator.getLocationValueError(dataPoint);
		assertThat(error).isEqualTo("als_id: a is not valid.");
		
		dataPoint = parser.next();
		error = validator.getLocationValueError(dataPoint);
		assertThat(error).isEqualTo("als_id: 1234567890 does not exist in ALS.");

	}

	private void testGetDateTimeError() {
		SeriesDataFile dataFile = SeriesDataFileHelper.createTestSeriesDataFileWithAlsIdFormatWithErrors();
		Validator validator = SeriesDataFileHelper.makeValidator(dataFile);
		DataPoint dataPoint = SeriesDataFileHelper.getParser(dataFile).next();
		String error = validator.getDateTimeError(dataPoint);
		assertThat(error).isEqualTo(
				"time: Invalid format: \"2015-01-\" is malformed at \"-\"");

	}

	private void testGetValueError() {
		SeriesDataFile dataFile = SeriesDataFileHelper.createTestSeriesDataFileWithAlsIdFormatWithErrors();
		Validator validator = SeriesDataFileHelper.makeValidator(dataFile);
		DataPoint dataPoint = SeriesDataFileHelper.getParser(dataFile).next();
		String error = validator.getValueError(dataPoint);
		assertThat(error).isEqualTo("VALUE: b is not valid.");

	}
	
	private void testGetRecordSizeError() {
		SeriesDataFile dataFile = SeriesDataFileHelper.createTestSeriesDataFileWithAlsIdFormatWithErrors();
		DataPoint dataPoint = SeriesDataFileHelper.getParser(dataFile).next();
		String error = getRecordSizeError(dataFile,dataPoint);
		assertThat(error).isEqualTo("row has 4 columns. should have 3 columns.");
		
	}

	private String getRecordSizeError(SeriesDataFile dataFile, DataPoint dataPoint) {
		Validator validator = SeriesDataFileHelper.makeValidator(dataFile);
		String error = validator.getRecordSizeError(dataPoint);
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
		SeriesDataFile dataFile = SeriesDataFileHelper.createTestSeriesDataFileWithAlsIdFormatWithErrors();
		Validator validator = SeriesDataFileHelper.makeValidator(dataFile);
		Map<Long, List<String>> error = validator.validateDataFile();
		List<String> expected = Arrays.asList("number of columns is 4. should be 3.","\"lat\" column name is not allowed in alsIdFormat format.");
		assertThat(error.get(1L)).isEqualTo(expected);
		
	}
	
	private void testValidateCoordinateFormatted() {
	
		SeriesDataFile dataFile = SeriesDataFileHelper.createTestSeriesDataFileWithCoordinateFormatWithErrors();
		Validator validator = SeriesDataFileHelper.makeValidator(dataFile);
		Map<Long, List<String>> error = validator.validateDataFile();
		List<String> expected = Arrays.asList("number of columns is 5. should be 4.","\"error\" column name is not allowed in coordinateFormat format.");
		assertThat(error.get(1L)).isEqualTo(expected);
		
	}
	
	private void testGetLocationValueErrorForCoordinateFormat() {
		SeriesDataFile dataFile = SeriesDataFileHelper.createTestSeriesDataFileWithCoordinateFormatWithErrors();
		Validator validator = SeriesDataFileHelper.makeValidator(dataFile);
		DataPoint dataPoint = SeriesDataFileHelper.getParser(dataFile).next();
		String error = validator.getLocationValueError(dataPoint);
		assertThat(error).isEqualTo("latitude: a is not valid. longitude: b is not valid.");
	}
	
	private List<String> getHeaderErrors(SeriesDataFile dataFile)  {
		Validator validator = SeriesDataFileHelper.makeValidator(dataFile);
		List<String> error = validator.getFileConsistencyError();
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
