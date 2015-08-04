package interactors;

import static org.fest.assertions.Assertions.assertThat;
import static suites.Helper.assertAreEqual;
import integrations.app.App;
import models.entities.Location;
import models.entities.SeriesData;

import org.apache.commons.csv.CSVRecord;
import org.joda.time.DateTime;
import org.junit.Test;

import play.db.jpa.JPA;
import play.libs.F.Callback0;
import suites.CSVFileHelper;
import controllers.Factory;

public class TestCSVFilePersister {

	@Test
	public void testPersistCSVFile() {
		runWithTransaction(() -> testPersistFile());
	}

	private void testPersistFile() throws Exception {
		CSVFileHelper helper = new CSVFileHelper();
		CSVFile dataFile = helper.createTestDataFileWithAlsIdFormat();
		helper.setStdToFileHeaderMap(dataFile);
		Long expected = 5L;
		Long seriesId = 1L;

		CSVFilePersister persister = new CSVFilePersister();
		Long created = persister.persistCSVFile(dataFile, seriesId);
		assertThat(created).isEqualTo(expected);

		dataFile = helper.createTestDataFileWithCoordianteFormat();
		helper.setStdToFileHeaderMap(dataFile);
		persister = new CSVFilePersister();
		created = persister.persistCSVFile(dataFile, seriesId);
		assertThat(created).isEqualTo(expected);

	}

	@Test
	public void testCSVRecordToLocationEntityObject() throws Exception {

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
	public void testCreateLocationIfNotExists() {
		runWithTransaction(() -> createLocationIfNotExists());
	}
	
	private void createLocationIfNotExists() throws Exception{
		CSVFilePersister persister = new CSVFilePersister();
		Location location = getLocationObjectFromCSVRecordWithAlsId();
		Long alsId = location.getAlsId();
		
		Long exsitingLocId = persist(location);
		Long locId = persister.createLocationFromAlsIdIfNotExists(alsId);
		
		assertAreEqual(locId, exsitingLocId);
		
		locId = persister.createLocationFromAlsIdIfNotExists(987654321L);
		
		assertThat(locId).isNotEqualTo(exsitingLocId);
	}

	@Test
	public void testCSVRecordToSeriesDataEntityObject() {
		runWithTransaction(() -> csvRecordToSeriesDataEntityObject());
	}

	private void csvRecordToSeriesDataEntityObject() throws Exception {
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
			throws Exception {

		CSVFileHelper helper = new CSVFileHelper();
		CSVFile dataFile = helper.createTestDataFileWithAlsIdFormat();
		CSVRecord csvRecord = helper.getCSVRecord(dataFile);
		CSVFilePersister persister = new CSVFilePersister();

		SeriesData seriesData = persister.createSeriesData(1L, 1L, DateTime
				.parse(get(csvRecord, CSVFile.TIME_HEADER, dataFile)).toDate(),
				Double.parseDouble(get(csvRecord, CSVFile.VALUE_HEADER,
						dataFile)));
		return seriesData;
	}

	private String get(CSVRecord csvRecord, String header, CSVFile dataFile) {
		return csvRecord.get(dataFile.stdHeaderToFileHeader(header));
	}

	private SeriesData getSeriesDataFromCSVRecordwithCoordinateFormat()
			throws Exception {
		CSVFileHelper helper = new CSVFileHelper();
		CSVFile dataFile = helper.createTestDataFileWithCoordianteFormat();
		CSVRecord csvRecord = helper.getCSVRecord(dataFile);

		CSVFilePersister persister = new CSVFilePersister();
		SeriesData seriesData = persister.createSeriesData(1L, 1L, DateTime
				.parse(get(csvRecord, CSVFile.TIME_HEADER, dataFile)).toDate(),
				Double.parseDouble(get(csvRecord, CSVFile.VALUE_HEADER,
						dataFile)));
		return seriesData;
	}

	private Location getLocationObjectFromCSVRecordWithAlsId()
			throws Exception {

		CSVFileHelper helper = new CSVFileHelper();
		CSVFile dataFile = helper.createTestDataFileWithAlsIdFormat();
		CSVRecord csvRecord = helper.getCSVRecord(dataFile);
		CSVFilePersister persister = new CSVFilePersister();
		Location location = persister.createLocation(Long.parseLong(get(
				csvRecord, CSVFile.ALS_ID_HEADER, dataFile)));
		return location;
	}

	private Location getLocationObjectFromCSVRecordWithCoordinate()
			throws Exception {

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
	
	private Long persist(final Location location) {
		return Factory.makeLocationRule(JPA.em()).create(location);
	}

	private static void runWithTransaction(Callback0 callback) {
		App.newWithInMemoryDbWithDbOpen().runWithTransaction(callback);
	}

}
