package interactors.series_data_file;

import static org.fest.assertions.Assertions.assertThat;
import integrations.app.App;
import interactors.SeriesRule;
import interactors.series_data_file.Parser.DataPoint;
import models.SeriesDataFile;
import models.entities.Location;
import models.entities.Series;
import models.entities.SeriesData;

import org.joda.time.DateTime;
import org.junit.BeforeClass;
import org.junit.Test;

import play.db.jpa.JPA;
import play.libs.F.Callback0;
import suites.SeriesDataFileHelper;
import controllers.Factory;

public class TestPersister {
	
	static Long seriesId;

	@BeforeClass
	public static void createTheSeries(){
		runWithTransaction(() -> {
			final SeriesRule sr = Factory.makeSeriesRule(JPA.em());
			Series s = new Series();
			seriesId = sr.create(s);
		});
	}

	@Test
	public void testPersistSeriesDataFile() {
		runWithTransaction(() -> testPersistSeriesData());
	}

	private void testPersistSeriesData() throws Exception {
		SeriesDataFile dataFile = SeriesDataFileHelper.createTestSeriesDataFileWithAlsIdFormat();
		SeriesDataFileHelper.setStdToFileHeaderMap(dataFile);
		int expected = 5;

		Persister persister = SeriesDataFileHelper.makePersister(dataFile);
		int created = persister.persistSeriesDataFile(seriesId);
		assertThat(created).isEqualTo(expected);

		dataFile = SeriesDataFileHelper.createTestSeriesDataFileWithCoordianteFormat();
		SeriesDataFileHelper.setStdToFileHeaderMap(dataFile);
		persister = SeriesDataFileHelper.makePersister(dataFile);
		created = persister.persistSeriesDataFile(seriesId);
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
		SeriesData seriesData = getSeriesDataFromDataPoint(SeriesDataFileHelper.createTestSeriesDataFileWithAlsIdFormat());
		assertThat(seriesData.getValue()).isEqualTo(1);
		assertThat(seriesData.getTimestamp()).isEqualTo(
				DateTime.parse("2015-01-01").toDate());

		seriesData = getSeriesDataFromDataPoint(SeriesDataFileHelper.createTestSeriesDataFileWithCoordianteFormat());
		assertThat(seriesData.getValue().equals(1));
		assertThat(seriesData.getTimestamp()).isEqualTo(
				DateTime.parse("2015-01-01").toDate());

	}

	private SeriesData getSeriesDataFromDataPoint(SeriesDataFile dataFile)
			throws Exception {

		DataPoint dataPoint = SeriesDataFileHelper.getParser(dataFile).next();
		Persister persister = SeriesDataFileHelper.makePersister(dataFile);
		return persister.persistDataPoint(dataPoint);
	}

	private String get(DataPoint dataPoint, String header, SeriesDataFile dataFile) {
		return dataPoint.get(dataFile.stdHeaderToFileHeader(header));
	}

	private Location getLocationObjectFromCSVRecordWithAlsId()
			throws Exception {

		SeriesDataFile dataFile = SeriesDataFileHelper.createTestSeriesDataFileWithAlsIdFormat();
		DataPoint dataPoint = SeriesDataFileHelper.getParser(dataFile).next();
		Location location = createLocation(Long.parseLong(get(
				dataPoint, SeriesDataFile.ALS_ID_HEADER, dataFile)));
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
		DataPoint dataPoint = SeriesDataFileHelper.getParser(dataFile).next();
		Persister persister = SeriesDataFileHelper.makePersister(dataFile);
		Location location = persister.createLocationFromCSVRecord(dataPoint);
		return location;
	}

	private static void runWithTransaction(Callback0 callback) {
		App.newWithInMemoryDbWithDbOpen().runWithTransaction(callback);
	}
}
