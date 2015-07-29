package interactors;

import static org.fest.assertions.Assertions.assertThat;
import integrations.app.App;
import models.entities.Location;
import models.entities.SeriesData;

import org.apache.commons.csv.CSVRecord;
import org.joda.time.DateTime;
import org.junit.Test;

import play.libs.F.Callback0;
import suites.CSVFileHelper;

public class TestCSVFilePersister {

	@Test
	public void testPersistCSVFile() {
		runWithTransaction(() -> testPersistFile());
	}

	private void testPersistFile() {
		CSVFileHelper helper = new CSVFileHelper();
		CSVFile dataFile = helper.createTestDataFileWithApolloIdFormat();
		helper.setStdToFileHeaderMap(dataFile);

		CSVFilePersister persister = new CSVFilePersister();
		assertThat(persister.persistCSVFile(dataFile, 1)).isTrue();

		dataFile = helper.createTestDataFileWithCoordianteFormat();
		helper.setStdToFileHeaderMap(dataFile);
		persister = new CSVFilePersister();
		assertThat(persister.persistCSVFile(dataFile, 1)).isTrue();

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

		CSVFileHelper helper = new CSVFileHelper();
		CSVFile dataFile = helper.createTestDataFileWithApolloIdFormat();
		CSVRecord csvRecord = helper.getCSVRecord(dataFile);
		CSVFilePersister persister = new CSVFilePersister();

		SeriesData seriesData = persister.createSeriesData(1, 1, DateTime
				.parse(get(csvRecord, CSVFile.TIME_HEADER, dataFile)).toDate(),
				Double.parseDouble(get(csvRecord, CSVFile.VALUE_HEADER,
						dataFile)));
		return seriesData;
	}

	private String get(CSVRecord csvRecord, String header, CSVFile dataFile) {
		return csvRecord.get(dataFile.stdHeaderToFileHeader(header));
	}

	private SeriesData getSeriesDataFromCSVRecordwithCoordinateFormat()
			throws NumberFormatException {
		CSVFileHelper helper = new CSVFileHelper();
		CSVFile dataFile = helper.createTestDataFileWithCoordianteFormat();
		CSVRecord csvRecord = helper.getCSVRecord(dataFile);

		CSVFilePersister persister = new CSVFilePersister();
		SeriesData seriesData = persister.createSeriesData(1, 1, DateTime
				.parse(get(csvRecord, CSVFile.TIME_HEADER, dataFile)).toDate(),
				Double.parseDouble(get(csvRecord, CSVFile.VALUE_HEADER,
						dataFile)));
		return seriesData;
	}

	private Location getLocationObjectFromCSVRecordWithApolloId()
			throws NumberFormatException {

		CSVFileHelper helper = new CSVFileHelper();
		CSVFile dataFile = helper.createTestDataFileWithApolloIdFormat();
		CSVRecord csvRecord = helper.getCSVRecord(dataFile);
		CSVFilePersister persister = new CSVFilePersister();
		Location location = persister.createLocation(Long.parseLong(get(
				csvRecord, CSVFile.APOLLO_ID_HEADER, dataFile)));
		return location;
	}

	private Location getLocationObjectFromCSVRecordWithCoordinate()
			throws NumberFormatException {

		CSVFileHelper helper = new CSVFileHelper();
		CSVFile dataFile = helper.createTestDataFileWithCoordianteFormat();
		CSVRecord csvRecord = helper.getCSVRecord(dataFile);
		CSVFilePersister persister = new CSVFilePersister();
		Location location = persister.createLocation(Double.parseDouble(get(
				csvRecord, CSVFile.LATITUDE_HEADER, dataFile)),
				Double.parseDouble(get(csvRecord, CSVFile.LONGITUDE_HEADER,
						dataFile)));
		return location;
	}

	private static void runWithTransaction(Callback0 callback) {
		App.newWithInMemoryDbWithDbOpen().runWithTransaction(callback);
	}

}
