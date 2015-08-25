package interactors.series_data_file;

import static org.fest.assertions.Assertions.assertThat;
import integrations.app.App;
import models.SeriesDataFile;
import models.entities.Location;
import models.entities.SeriesData;

import org.apache.commons.csv.CSVRecord;
import org.joda.time.DateTime;
import org.junit.Test;

import play.libs.F.Callback0;
import suites.SeriesDataFileHelper;

public class TestPersister {
	
	final static Long seriesId = 1000_000L;


	@Test
	public void testPersistSeriesDataFile() {
		runWithTransaction(() -> testPersistSeriesData());
	}

	private void testPersistSeriesData() throws Exception {
		SeriesDataFile dataFile = SeriesDataFileHelper.createTestSeriesDataFileWithAlsIdFormat();
		SeriesDataFileHelper.setStdToFileHeaderMap(dataFile);
		int expected = 5;

		Persister persister = SeriesDataFileHelper.makePersister();
		int created = persister.persistSeriesDataFile(dataFile, seriesId);
		assertThat(created).isEqualTo(expected);

		dataFile = SeriesDataFileHelper.createTestSeriesDataFileWithCoordianteFormat();
		SeriesDataFileHelper.setStdToFileHeaderMap(dataFile);
		persister = SeriesDataFileHelper.makePersister();
		created = persister.persistSeriesDataFile(dataFile, seriesId);
		assertThat(created).isEqualTo(expected);

	}
	
	@Test
	public void testCSVRecordToLocationEntityObject() {
		runWithTransaction(() -> testCSVRecordToLocation());
	}

	private void testCSVRecordToLocation() throws Exception {

		Location location = getLocationObjectFromCSVRecordWithAlsId();
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

	private void csvRecordToSeriesDataEntityObject() throws Exception {
		SeriesData seriesData = getSeriesDataFromCSVRecord(SeriesDataFileHelper.createTestSeriesDataFileWithAlsIdFormat());
		assertThat(seriesData.getValue()).isEqualTo(1);
		assertThat(seriesData.getTimestamp()).isEqualTo(
				DateTime.parse("2015-01-01").toDate());

		seriesData = getSeriesDataFromCSVRecord(SeriesDataFileHelper.createTestSeriesDataFileWithCoordianteFormat());
		assertThat(seriesData.getValue().equals(1));
		assertThat(seriesData.getTimestamp()).isEqualTo(
				DateTime.parse("2015-01-01").toDate());

	}

	private SeriesData getSeriesDataFromCSVRecord(SeriesDataFile dataFile)
			throws Exception {

		CSVRecord csvRecord = SeriesDataFileHelper.getCSVParser(dataFile).iterator().next();
		Persister persister = SeriesDataFileHelper.makePersister();
		return persister.persistDataPoint(null, dataFile, csvRecord);
	}

	private String get(CSVRecord csvRecord, String header, SeriesDataFile dataFile) {
		return csvRecord.get(dataFile.stdHeaderToFileHeader(header));
	}

	private Location getLocationObjectFromCSVRecordWithAlsId()
			throws Exception {

		SeriesDataFile dataFile = SeriesDataFileHelper.createTestSeriesDataFileWithAlsIdFormat();
		CSVRecord csvRecord = SeriesDataFileHelper.getCSVParser(dataFile).iterator().next();
		Location location = createLocation(Long.parseLong(get(
				csvRecord, SeriesDataFile.ALS_ID_HEADER, dataFile)));
		return location;
	}

	private Location createLocation(long alsId) {
		Location location = SeriesDataFileHelper.makeLocation();
		location.setAlsId(alsId);
		return location;
	}

	private Location getLocationObjectFromCSVRecordWithCoordinate()
			throws Exception {

		SeriesDataFile dataFile = SeriesDataFileHelper.createTestSeriesDataFileWithCoordianteFormat();
		CSVRecord csvRecord = SeriesDataFileHelper.getCSVParser(dataFile).iterator().next();
		Persister persister = SeriesDataFileHelper.makePersister();
		Location location = persister.createLocationFromCSVRecord(csvRecord, dataFile);
		return location;
	}

	private static void runWithTransaction(Callback0 callback) {
		App.newWithInMemoryDbWithDbOpen().runWithTransaction(callback);
	}
}
