package interactors;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import models.entities.Location;
import models.entities.LocationFilter;
import models.entities.SeriesData;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.joda.time.DateTime;

import play.db.jpa.JPA;
import controllers.Factory;

public class CSVFilePersister {

	public Long persistCSVFile(CSVFile dataFile, Long seriesId)
			throws Exception {

		CSVFileParser csvParser = new CSVFileParser();
		CSVParser parser = null;
		parser = csvParser.parse(dataFile);
		return persistRecords(seriesId, dataFile, parser);

	}

	private Long persistRecords(Long seriesId, CSVFile dataFile,
			CSVParser parser) throws Exception {
		Long counter = 0L;
		Iterator<CSVRecord> records = parser.iterator();
		while (records.hasNext()) {
			if (isNotTrue(persistRecord(seriesId, dataFile, records.next())))
				return 0L;
			counter++;
		}
		return counter;
	}

	private boolean isNotTrue(boolean result) {
		return !result;
	}

	private boolean persistRecord(Long seriesId, CSVFile dataFile,
			CSVRecord record) throws Exception {

		SeriesData seriesData = csvRecordToSeriesData(seriesId, record,
				dataFile);
		Long seriesDataId = persist(seriesData);
		return (seriesDataId != null) ? true : false;
	}

	private SeriesData csvRecordToSeriesData(Long seriesId, CSVRecord record,
			CSVFile dataFile) throws Exception {
		Long locId = createLocationFromCSVRecord(record, dataFile);
		return createSeriesData(seriesId, locId,
				getTimeStamp(record, dataFile), getValue(record, dataFile));
	}

	private Double getValue(CSVRecord record, CSVFile dataFile)
			throws Exception {
		String header = dataFile.stdHeaderToFileHeader(CSVFile.VALUE_HEADER);

		return stringToDouble(record.get(header));
	}

	private Date getTimeStamp(CSVRecord record, CSVFile dataFile) {
		String header = dataFile.stdHeaderToFileHeader(CSVFile.TIME_HEADER);
		return DateTime.parse(record.get(header)).toDate();
	}

	private Long createLocationFromCSVRecord(CSVRecord record, CSVFile dataFile)
			throws Exception {
		Long locId = null;
		String fileFormat = dataFile.getFileFormat();

		if (fileFormat.equals(CSVFile.ALS_ID_FORMAT)) {
			Long alsId = getAlsId(record, dataFile);
			locId = createLocationFromAlsIdIfNotExists(alsId);

		} else if (fileFormat.equals(CSVFile.COORDINATE_FORMAT)) {
			Double lat = getLatitude(record, dataFile);
			Double lon = getLongitude(record, dataFile);
			locId = createLocationFromCoordinateIfNotExists(lat, lon);
		}
		return locId;
	}

	private Double getLongitude(CSVRecord record, CSVFile dataFile)
			throws Exception {
		String lonHeader = dataFile
				.stdHeaderToFileHeader(CSVFile.LONGITUDE_HEADER);
		Double lon = stringToDouble(record.get(lonHeader));
		return lon;
	}

	private Double stringToDouble(String header) {
		return Double.parseDouble(header);
	}

	private Double getLatitude(CSVRecord record, CSVFile dataFile)
			throws Exception {
		String latHeader = dataFile
				.stdHeaderToFileHeader(CSVFile.LATITUDE_HEADER);
		Double lat = stringToDouble(record.get(latHeader));
		return lat;
	}

	private Long getAlsId(CSVRecord record, CSVFile dataFile)
			throws Exception {
		String header;
		header = dataFile.stdHeaderToFileHeader(CSVFile.ALS_ID_HEADER);
		Long alsId = stringToLong(record.get(header));
		return alsId;
	}

	private long stringToLong(String header) throws Exception {
		return Long.parseLong(header);
	}

	private Long createLocationFromCoordinateIfNotExists(Double lat, Double lon) {
		Long locId;
		if ((locId = findLocation(lat, lon)) != null) {
			return locId;
		} else {
			return persist(createLocation(lat, lon));
		}
	}

	private Long findLocation(Double lat, Double lon) {
		LocationFilter filter = buildLocationFilter(lat, lon);
		List<Location> LocList = makeLocationRule().query(filter);
		if (isNotEmpty(LocList)) {
			return LocList.get(0).getId();
		} else {
			return null;
		}
	}

	private LocationFilter buildLocationFilter(Double lat, Double lon) {
		LocationFilter filter = new LocationFilter();
		filter.setLatitude(lat);
		filter.setLongitude(lon);
		return filter;
	}

	Long createLocationFromAlsIdIfNotExists(Long alsId) {
		Long locId;
		if ((locId = findLocation(alsId)) != null) {
			return locId;
		} else {
			locId = persist(createLocation(alsId));
			return locId;
		}
	}

	private Long findLocation(Long alsId) {
		LocationFilter filter = buildLocationFilter(alsId);
		List<Location> LocList = makeLocationRule().query(filter);
		if (isNotEmpty(LocList)) {
			return LocList.get(0).getId();
		} else {
			return null;
		}
	}

	private boolean isNotEmpty(List<Location> list) {
		return !(list.isEmpty());
	}

	private LocationFilter buildLocationFilter(Long alsId) {
		LocationFilter filter = new LocationFilter();
		filter.setAlsId(alsId);
		return filter;
	}

	Location createLocation(Long alsId) {
		final Location loc = new Location();
		loc.setAlsId(alsId);
		return loc;
	}

	Location createLocation(Double latitude, Double longitude) {

		final Location loc = new Location();
		loc.setLatitude(latitude);
		loc.setLongitude(longitude);
		return loc;
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
