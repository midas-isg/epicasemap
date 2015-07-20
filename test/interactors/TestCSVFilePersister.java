package interactors;

import static org.fest.assertions.Assertions.assertThat;
import static suites.Helper.assertAreEqual;
import integrations.app.App;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import models.entities.Location;
import models.entities.Series;
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
		assertThat(persister.persistCSVFile(dataFile) == 1L);

		dataFile = createTestDataFileWithCoordianteFormat();
		persister = new CSVFilePersister();
		assertThat(persister.persistCSVFile(dataFile) == 1L);

	}

	@Test
	public void testCSVFileToSeriesEntityObject() {
		CSVFile dataFile = createTestDataFileWithApolloIdFormat();
		Series series = SeriesObjectFromCSVFile(dataFile);
		assertAreEqual(series.getTitle(), "serie1");
		assertAreEqual(series.getDescription(), "desc");

		dataFile = createTestDataFileWithCoordianteFormat();
		series = SeriesObjectFromCSVFile(dataFile);
		assertAreEqual(series.getTitle(), "serie1");
		assertAreEqual(series.getDescription(), "desc");
	}

	@Test
	public void testCSVRecordToLocationEntityObject() {

		Location location = getLocationObjectFromCSVRecordWithApolloId();
		assertThat(location.getAlsId().equals(1));

	}

	@Test
	public void testCSVRecordToSeriesDataEntityObject() {
		SeriesData seriesData = getSeriesDataFromCSVRecordwithApollloFormat();
		assertThat(seriesData.getValue().equals(1));
		assertThat(seriesData.getTimestamp().equals(
				DateTime.parse("2015-01-01").toDate()));

		seriesData = getSeriesDataFromCSVRecordwithCoordinateFormat();
		assertThat(seriesData.getValue().equals(1));
		assertThat(seriesData.getTimestamp().equals(
				DateTime.parse("2015-01-01").toDate()));

	}

	private SeriesData getSeriesDataFromCSVRecordwithApollloFormat()
			throws NumberFormatException {
		CSVFile dataFile = createTestDataFileWithApolloIdFormat();
		Series series = SeriesObjectFromCSVFile(dataFile);
		series.setId(1L);
		Location location = getLocationObjectFromCSVRecordWithApolloId();
		location.setId(1L);
		CSVRecord csvRecord = getCSVrecordWithApolloIdFormat();
		CSVFilePersister persister = new CSVFilePersister();
		SeriesData seriesData = persister.createSeriesData(series, location,
				DateTime.parse(csvRecord.get(CSVFile.TIME_HEADER)).toDate(),
				Double.parseDouble(csvRecord.get(CSVFile.VALUE_HEADER)));
		return seriesData;
	}

	private SeriesData getSeriesDataFromCSVRecordwithCoordinateFormat()
			throws NumberFormatException {
		CSVFile dataFile = createTestDataFileWithCoordianteFormat();
		Series series = SeriesObjectFromCSVFile(dataFile);
		series.setId(1L);
		Location location = getLocationObjectFromCSVRecordWithCoordinate();
		location.setId(1L);
		CSVRecord csvRecord = getCSVrecordWithCoordinateFormat();
		CSVFilePersister persister = new CSVFilePersister();
		SeriesData seriesData = persister.createSeriesData(series, location,
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

	private Series SeriesObjectFromCSVFile(CSVFile dataFile) {
		CSVFilePersister persister = new CSVFilePersister();
		Series series = persister.createSeries(dataFile);
		return series;
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
		Map<String, String[]> metaData = new HashMap<String, String[]>();
		metaData.put("title", new String[] { "serie1" });
		metaData.put("format", new String[] { CSVFile.APOLLO_ID_FORMAT });
		metaData.put("delimiter", new String[] { "," });
		metaData.put("headers", new String[] { CSVFile.TIME_HEADER,
				CSVFile.APOLLO_ID_HEADER, CSVFile.VALUE_HEADER });
		metaData.put("description", new String[] { "desc" });
		CSVFile dataFile = new CSVFile(csvFile, metaData);
		return dataFile;
	}

	private CSVFile createTestDataFileWithCoordianteFormat() {

		File csvFile = new File("test/resources/test_coordinate_format.txt");
		Map<String, String[]> metaData = new HashMap<String, String[]>();
		metaData.put("title", new String[] { "serie1" });
		metaData.put("format", new String[] { CSVFile.COORDINATE_FORMAT });
		metaData.put("delimiter", new String[] { "," });
		metaData.put("headers", new String[] { CSVFile.TIME_HEADER,
				CSVFile.LATITUDE_HEADER, CSVFile.LONGITUDE_HEADER,
				CSVFile.VALUE_HEADER });
		metaData.put("description", new String[] { "desc" });
		CSVFile dataFile = new CSVFile(csvFile, metaData);
		return dataFile;
	}

	private static void runWithTransaction(Callback0 callback) {
		App.newWithInMemoryDbWithDbOpen().runWithTransaction(callback);
	}

}
