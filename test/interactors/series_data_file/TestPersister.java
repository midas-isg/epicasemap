package interactors.series_data_file;

import static org.fest.assertions.Assertions.assertThat;
import static suites.Helper.assertAreEqual;
import integrations.app.App;
import interactors.series_data_file.Persister;
import models.SeriesDataFile;
import models.entities.Location;
import models.entities.SeriesData;

import org.apache.commons.csv.CSVRecord;
import org.joda.time.DateTime;
import org.junit.Test;

import play.db.jpa.JPA;
import play.libs.F.Callback0;
import suites.SeriesDataFileHelper;
import controllers.Factory;

public class TestPersister {
	
	final static Long seriesId = 1000_000L;


	@Test
	public void testPersistSeriesDataFile() {
		runWithTransaction(() -> testPersistSeriesData());
	}

	private void testPersistSeriesData() throws Exception {
		SeriesDataFileHelper helper = new SeriesDataFileHelper();
		SeriesDataFile dataFile = helper.createTestSeriesDataFileWithAlsIdFormat();
		helper.setStdToFileHeaderMap(dataFile);
		Long expected = 5L;
		//Long seriesId = 1L;

		Persister persister = new Persister();
		Long created = persister.persistSeriesDataFile(dataFile, seriesId);
		assertThat(created).isEqualTo(expected);

		dataFile = helper.createTestSeriesDataFileWithCoordianteFormat();
		helper.setStdToFileHeaderMap(dataFile);
		persister = new Persister();
		created = persister.persistSeriesDataFile(dataFile, seriesId);
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
		Persister persister = new Persister();
		Location location = getLocationObjectFromCSVRecordWithAlsId();
		Long alsId = location.getAlsId();
		
		Long exsitingLocId = persist(location);
		Long locId = persister.createLocation(alsId);
		assertAreEqual(locId, exsitingLocId);
		
		locId = persister.createLocation(987654321L);
		assertThat(locId).isNull();
		
		location = getLocationObjectFromCSVRecordWithCoordinate();
		Double lat = location.getLatitude();
		Double lon = location.getLongitude();
		exsitingLocId = persist(location);
		
		locId = persister.createLocation(lat,lon);
		assertAreEqual(locId,exsitingLocId);
		
		locId = persister.createLocation(987654321.0,-987654321.0);
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

		SeriesDataFileHelper helper = new SeriesDataFileHelper();
		SeriesDataFile dataFile = helper.createTestSeriesDataFileWithAlsIdFormat();
		CSVRecord csvRecord = helper.getCSVParser(dataFile).iterator().next();
		Persister persister = new Persister();

		SeriesData seriesData = persister.createSeriesData(seriesId, 1L, DateTime
				.parse(get(csvRecord, SeriesDataFile.TIME_HEADER, dataFile)).toDate(),
				Double.parseDouble(get(csvRecord, SeriesDataFile.VALUE_HEADER,
						dataFile)));
		return seriesData;
	}

	private String get(CSVRecord csvRecord, String header, SeriesDataFile dataFile) {
		return csvRecord.get(dataFile.stdHeaderToFileHeader(header));
	}

	private SeriesData getSeriesDataFromCSVRecordwithCoordinateFormat()
			throws Exception {
		SeriesDataFileHelper helper = new SeriesDataFileHelper();
		SeriesDataFile dataFile = helper.createTestSeriesDataFileWithCoordianteFormat();
		CSVRecord csvRecord = helper.getCSVParser(dataFile).iterator().next();

		Persister persister = new Persister();
		SeriesData seriesData = persister.createSeriesData(seriesId, 1L, DateTime
				.parse(get(csvRecord, SeriesDataFile.TIME_HEADER, dataFile)).toDate(),
				Double.parseDouble(get(csvRecord, SeriesDataFile.VALUE_HEADER,
						dataFile)));
		return seriesData;
	}

	private Location getLocationObjectFromCSVRecordWithAlsId()
			throws Exception {

		SeriesDataFileHelper helper = new SeriesDataFileHelper();
		SeriesDataFile dataFile = helper.createTestSeriesDataFileWithAlsIdFormat();
		CSVRecord csvRecord = helper.getCSVParser(dataFile).iterator().next();
		Location location = createLocation(Long.parseLong(get(
				csvRecord, SeriesDataFile.ALS_ID_HEADER, dataFile)));
		return location;
	}

	private Location createLocation(long alsId) {
		Location location = new Location();
		location.setAlsId(alsId);
		return location;
	}

	private Location getLocationObjectFromCSVRecordWithCoordinate()
			throws Exception {

		SeriesDataFileHelper helper = new SeriesDataFileHelper();
		SeriesDataFile dataFile = helper.createTestSeriesDataFileWithCoordianteFormat();
		CSVRecord csvRecord = helper.getCSVParser(dataFile).iterator().next();
		Persister persister = new Persister();
		Location location = persister.createNew(Double.parseDouble(get(
				csvRecord, SeriesDataFile.LATITUDE_HEADER, dataFile)),
				Double.parseDouble(get(csvRecord, SeriesDataFile.LONGITUDE_HEADER,
						dataFile)));
		return location;
	}
	
//	private Location createLocation(double longitude, double latitude) {
//		Location location = new Location();
//		location.setLongitude(longitude);
//		location.setLatitude(latitude);
//		return location;
//	}

	private Long persist(final Location location) {
		return Factory.makeLocationRule(JPA.em()).create(location);
	}

	private static void runWithTransaction(Callback0 callback) {
		App.newWithInMemoryDbWithDbOpen().runWithTransaction(callback);
	}

}
