package interactors.series_data_file;

import interactors.LocationRule;
import interactors.SeriesRule;

import java.util.Date;
import java.util.Iterator;

import models.SeriesDataFile;
import models.entities.Location;
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
		CSVParser parser = null;
		parser = csvParser.parse(dataFile);
		return persistSeriesData(seriesId, dataFile, parser);

	}

	private Long persistSeriesData(Long seriesId, SeriesDataFile dataFile,
			CSVParser parser) throws Exception {
		Long counter = 0L;
		Iterator<CSVRecord> dataPoints = parser.iterator();
		while (dataPoints.hasNext()) {
			if (isNotTrue(persistDataPoint(seriesId, dataFile, dataPoints.next())))
				return 0L;
			counter++;
		}
		return counter;
	}

	private boolean isNotTrue(boolean result) {
		return !result;
	}

	private boolean persistDataPoint(Long seriesId, SeriesDataFile dataFile,
			CSVRecord dataPoint) throws Exception {

		SeriesData seriesData = csvRecordToSeriesData(seriesId, dataPoint,
				dataFile);
		Long seriesDataId = persist(seriesData);
		return (seriesDataId != null) ? true : false;
	}

	private SeriesData csvRecordToSeriesData(Long seriesId, CSVRecord record,
			SeriesDataFile dataFile) throws Exception {
		Long locId = createLocationFromCSVRecord(record, dataFile);
		return createSeriesData(seriesId, locId,
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

	private Long createLocationFromCSVRecord(CSVRecord record,
			SeriesDataFile dataFile) throws Exception {
		Long locId = null;
		String fileFormat = dataFile.getFileFormat();

		if (fileFormat.equals(SeriesDataFile.ALS_ID_FORMAT)) {
			Long alsId = getAlsId(record, dataFile);
			locId = createLocation(alsId);

		} else if (fileFormat.equals(SeriesDataFile.COORDINATE_FORMAT)) {
			Double lat = getLatitude(record, dataFile);
			Double lon = getLongitude(record, dataFile);
			locId = createLocation(lat, lon);
		}
		return locId;
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

	Long createLocation(Double lat, Double lon) {
		Location location = makeLocationRule().getLocationByCoordinate(lat, lon);
		if (location == null)
			location = createNew(lat,lon);
		return getId(location);
	}

	Location createNew(Double lat,Double lon) {
		Location location = new Location();
		location.setLongitude(lon);
		location.setLatitude(lat);
		return location;
	}

	private Long getId(Location location) {
		if(location ==null)
			return null;
		else if (location.getId() != null)
			return location.getId();
		else
			return persist(location);
	}

	Long createLocation(Long alsId) {
		Location location = makeLocationRule().getLocationByAlsId(alsId);
		return getId(location);
	}

	SeriesData createSeriesData(Long seriesId, Long locId, Date time,
			Double value) {

		final SeriesData seriesData = new SeriesData();
		seriesData.setLocation(makeLocationRule().read(locId));
		seriesData.setSeries(makeSeriesRule().read(seriesId));
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

	private Long persist(final Location location) {
		return makeLocationRule().create(location);
	}

	private Long persist(final SeriesData seriesData) {
		return Factory.makeSeriesDataRule(JPA.em()).create(seriesData);
	}

}
