package interactors;

import static org.fest.assertions.Assertions.assertThat;
import integrations.app.App;

import java.io.File;

import models.entities.Location;
import models.entities.SeriesData;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.joda.time.DateTime;
import org.junit.Test;

import play.libs.F.Callback0;

public class TestCSVFilePersister {

	@Test
	public void testPersistCSVFile() {
		runWithTransaction(() -> testPersistFile());
	}

	private void testPersistFile() {
		CSVFile dataFile = createTestDataFileWithApolloIdFormat();
		CSVFilePersister persister = new CSVFilePersister();
		assertThat(persister.persistCSVFile(dataFile,1)).isTrue();

		dataFile = createTestDataFileWithCoordianteFormat();
		persister = new CSVFilePersister();
		assertThat(persister.persistCSVFile(dataFile,1)).isTrue();

	}

	@Test
	public void testCSVRecordToLocationEntityObject() {

		Location location = getLocationObjectFromCSVRecordWithApolloId();
		long id = location.getAlsId();
		assertThat(id).isEqualTo(1);
		
		location = getLocationObjectFromCSVRecordWithCoordinate();
		Double lat = location.getLatitude();
		assertThat(lat).isEqualTo(1.1);
		Double lon = location.getLongitude();
		assertThat(lon).isEqualTo(-1.1);

	}
	
	@Test
	public void testCSVRecordToSeriesDataEntityObject() {
		runWithTransaction(() -> csvRecordToSeriesDataEntityObject());
	}

	private void csvRecordToSeriesDataEntityObject() {
		SeriesData seriesData = getSeriesDataFromCSVRecordwithApollloFormat();
		assertThat(seriesData.getValue()).isEqualTo(1);
		assertThat(seriesData.getTimestamp()).isEqualTo(
				DateTime.parse("2015-01-01").toDate());

		seriesData = getSeriesDataFromCSVRecordwithCoordinateFormat();
		assertThat(seriesData.getValue().equals(1));
		assertThat(seriesData.getTimestamp()).isEqualTo(
				DateTime.parse("2015-01-01").toDate());

	}

	private SeriesData getSeriesDataFromCSVRecordwithApollloFormat()
			throws NumberFormatException {

		CSVRecord csvRecord = getCSVrecordWithApolloIdFormat();
		CSVFilePersister persister = new CSVFilePersister();
		SeriesData seriesData = persister.createSeriesData(1, 1,
				DateTime.parse(csvRecord.get(CSVFile.TIME_HEADER)).toDate(),
				Double.parseDouble(csvRecord.get(CSVFile.VALUE_HEADER)));
		return seriesData;
	}

	private SeriesData getSeriesDataFromCSVRecordwithCoordinateFormat()
			throws NumberFormatException {
		
		CSVRecord csvRecord = getCSVrecordWithCoordinateFormat();
		CSVFilePersister persister = new CSVFilePersister();
		SeriesData seriesData = persister.createSeriesData(1, 1,
				DateTime.parse(csvRecord.get(CSVFile.TIME_HEADER)).toDate(),
				Double.parseDouble(csvRecord.get(CSVFile.VALUE_HEADER)));
		return seriesData;
	}

	private Location getLocationObjectFromCSVRecordWithApolloId()
			throws NumberFormatException {
		CSVRecord csvRecord = getCSVrecordWithApolloIdFormat();
		CSVFilePersister persister = new CSVFilePersister();
		Location location = persister.createLocation(Long.parseLong(csvRecord
				.get(CSVFile.APOLLO_ID_HEADER)));
		return location;
	}

	private Location getLocationObjectFromCSVRecordWithCoordinate()
			throws NumberFormatException {
		CSVRecord csvRecord = getCSVrecordWithCoordinateFormat();
		CSVFilePersister persister = new CSVFilePersister();
		Location location = persister.createLocation(
				Double.parseDouble(csvRecord.get(CSVFile.LATITUDE_HEADER)),
				Double.parseDouble(csvRecord.get(CSVFile.LONGITUDE_HEADER)));
		return location;
	}

	private CSVRecord getCSVrecordWithApolloIdFormat() {
		CSVFile dataFile = createTestDataFileWithApolloIdFormat();
		CSVFileParser fileParser = new CSVFileParser();
		CSVParser parser = fileParser.parse(dataFile);
		return parser.iterator().next();

	}

	private CSVRecord getCSVrecordWithCoordinateFormat() {
		CSVFile dataFile = createTestDataFileWithCoordianteFormat();
		CSVFileParser fileParser = new CSVFileParser();
		CSVParser parser = fileParser.parse(dataFile);
		return parser.iterator().next();

	}

	private CSVFile createTestDataFileWithApolloIdFormat() {
		File csvFile = new File("test/resources/test_apolloId_format.txt");
		String fileFormat = CSVFile.APOLLO_ID_FORMAT;
		String delimiter = "," ;
		CSVFile dataFile = new CSVFile();
		dataFile.setFile(csvFile);
		dataFile.setDelimiter(delimiter);
		dataFile.setFileFormat(fileFormat);
		return dataFile;
	}

	private CSVFile createTestDataFileWithCoordianteFormat() {

		File csvFile = new File("test/resources/test_coordinate_format.txt");
		String fileFormat = CSVFile.COORDINATE_FORMAT;
		String delimiter = "," ;
		CSVFile dataFile = new CSVFile();
		dataFile.setFile(csvFile);
		dataFile.setDelimiter(delimiter);
		dataFile.setFileFormat(fileFormat);
		return dataFile;
	}

	private static void runWithTransaction(Callback0 callback) {
		App.newWithInMemoryDbWithDbOpen().runWithTransaction(callback);
	}

}
