package interactors.series_data_file;

import interactors.LocationRule;
import interactors.SeriesRule;

import java.util.Date;

import models.SeriesDataFile;
import models.entities.Location;
import models.entities.Series;
import models.entities.SeriesData;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.joda.time.DateTime;

import play.db.jpa.JPA;
import controllers.Factory;

public class Persister {

	public Long persistSeriesDataFile(SeriesDataFile dataFile, Long seriesId)
			throws Exception {

		Parser csvParser = new Parser();
		CSVParser parser = csvParser.parse(dataFile);
		Series series = makeSeriesRule().read(seriesId);
		return persistSeriesData(series, dataFile, parser);

	}

	private long persistSeriesData(Series series, SeriesDataFile dataFile,
			CSVParser parser) throws Exception {
		long counter = 0L; // Long -> long; Don't use Long if long can be used because it'll affect performance.
		//Iterator<CSVRecord> dataPoints = parser.iterator();
		//while (dataPoints.hasNext()) {
		for (CSVRecord dataPoint : parser) {
			persistDataPoint(series, dataFile, dataPoint);
			counter++;
		}
		return counter;
	}

	/*private boolean isNotTrue(boolean result) { // Just use !
		return !result;
	}*/

	private void persistDataPoint(Series series, SeriesDataFile dataFile,
			CSVRecord dataPoint) throws Exception {

		SeriesData seriesData = csvRecordToSeriesData(series, dataPoint,
				dataFile);
		/*Long seriesDataId = */persist(seriesData);
		// return (seriesDataId != null) ? true : false; // an exception will be thrown if something wrong (not set id to null)
	}

	private SeriesData csvRecordToSeriesData(Series series, CSVRecord record,
			SeriesDataFile dataFile) throws Exception {
		Location location = createLocationFromCSVRecord(record, dataFile);
		return newSeriesData(series, location,
				getTimeStamp(record, dataFile), getValue(record, dataFile));
	}

	private Double getValue(CSVRecord record, SeriesDataFile dataFile)
			throws Exception {
		String header = dataFile
				.stdHeaderToFileHeader(SeriesDataFile.VALUE_HEADER);

		return stringToDouble(record.get(header));
	}

	private Date getTimeStamp(CSVRecord record, SeriesDataFile dataFile) {
		String header = dataFile
				.stdHeaderToFileHeader(SeriesDataFile.TIME_HEADER);
		return DateTime.parse(record.get(header)).toDate();
	}

	private Location createLocationFromCSVRecord(CSVRecord record,
			SeriesDataFile dataFile) throws Exception {
		Location location = null;
		String fileFormat = dataFile.getFileFormat();

		if (fileFormat.equals(SeriesDataFile.ALS_ID_FORMAT)) {
			Long alsId = getAlsId(record, dataFile);
			location = makeLocation(alsId);

		} else if (fileFormat.equals(SeriesDataFile.COORDINATE_FORMAT)) {
			Double lat = getLatitude(record, dataFile);
			Double lon = getLongitude(record, dataFile);
			location = makeLocation(lat, lon);
		}
		return location;
	}

	private Double getLongitude(CSVRecord record, SeriesDataFile dataFile)
			throws Exception {
		String lonHeader = dataFile
				.stdHeaderToFileHeader(SeriesDataFile.LONGITUDE_HEADER);
		Double lon = stringToDouble(record.get(lonHeader));
		return lon;
	}

	private Double stringToDouble(String header) {
		return Double.parseDouble(header);
	}

	private Double getLatitude(CSVRecord record, SeriesDataFile dataFile)
			throws Exception {
		String latHeader = dataFile
				.stdHeaderToFileHeader(SeriesDataFile.LATITUDE_HEADER);
		Double lat = stringToDouble(record.get(latHeader));
		return lat;
	}

	private Long getAlsId(CSVRecord record, SeriesDataFile dataFile)
			throws Exception {
		String header;
		header = dataFile.stdHeaderToFileHeader(SeriesDataFile.ALS_ID_HEADER);
		Long alsId = stringToLong(record.get(header));
		return alsId;
	}

	private long stringToLong(String header) throws Exception {
		return Long.parseLong(header);
	}

	Location makeLocation(Double lat, Double lon) {
		Location location = makeLocationRule().getLocationByCoordinate(lat, lon);
		if (location == null)
			location = createNew(lat, lon);
		//return getId(location);
		return location;
	}

	Location createNew(Double lat, Double lon) {
		Location location = new Location();
		location.setLongitude(lon);
		location.setLatitude(lat);
		persist(location);
		return location;
	}

	/*private Long getId(Location location) {
		if(location == null)
			return null;
		else if (location.getId() != null)
			return location.getId();
		else
			return persist(location);
	}*/

	Location makeLocation(Long alsId) {
		Location location = makeLocationRule().getLocationByAlsId(alsId);
		//return getId(location);
		if (location != null && location.getId() == null)
			persist(location);
		return location;
	}

	SeriesData newSeriesData(Series series, Location location, Date time,
			Double value) {

		final SeriesData seriesData = new SeriesData();
		seriesData.setLocation(location);
		seriesData.setSeries(series);
		seriesData.setTimestamp(time);
		seriesData.setValue(value);
		return seriesData;
	}

	private SeriesRule makeSeriesRule() {
		return Factory.makeSeriesRule(JPA.em());

	}

	private LocationRule makeLocationRule() {
		return Factory.makeLocationRule(JPA.em());
	}

	private long persist(final Location location) {
		return makeLocationRule().create(location);
	}

	private long persist(final SeriesData seriesData) {
		return Factory.makeSeriesDataRule(JPA.em()).create(seriesData);
	}

}
