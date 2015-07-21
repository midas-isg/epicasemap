package interactors;

import java.util.Date;
import java.util.Iterator;

import models.entities.Location;
import models.entities.SeriesData;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.joda.time.DateTime;

import play.db.jpa.JPA;
import controllers.Factory;

public class CSVFilePersister {

	public boolean persistCSVFile(CSVFile dataFile, long seriesId) {

		CSVFileParser csvParser = new CSVFileParser();
		CSVParser parser = csvParser.parse(dataFile);
		return persistRecords(seriesId, dataFile.getFileFormat(), parser);
		
	}

	private boolean persistRecords(long seriesId, String fileFormat,
			CSVParser parser) throws NumberFormatException {
		Iterator<CSVRecord> records = parser.iterator();
		while (records.hasNext()) {
			CSVRecord record = records.next();
			if (persistRecord(seriesId, fileFormat, record) == 0) {
				return false;
			}
		}
		return true;
	}

	private long persistRecord(long seriesId, String fileFormat,
			CSVRecord record) throws NumberFormatException {

		SeriesData seriesData = csvRecordToSeriesData(seriesId, record,
				fileFormat);
		return persist(seriesData);
	}

	private SeriesData csvRecordToSeriesData(long seriesId, CSVRecord record,
			String fileFormat) throws NumberFormatException {
		long locId = createLocationFromCSVRecord(record, fileFormat);
		return createSeriesData(seriesId, locId, getTimeStamp(record),
				getValue(record));
	}

	private double getValue(CSVRecord record) throws NumberFormatException {
		return Double.parseDouble(record.get(CSVFile.VALUE_HEADER));
	}

	private Date getTimeStamp(CSVRecord record) {
		return DateTime.parse(record.get(CSVFile.TIME_HEADER)).toDate();
	}

	private long createLocationFromCSVRecord(CSVRecord record,
			String fileFormat) throws NumberFormatException {
		long locId = 0L;

		if (fileFormat.equals(CSVFile.APOLLO_ID_FORMAT)) {

			locId = persist(createLocation(Long.parseLong(record
					.get(CSVFile.APOLLO_ID_HEADER))));

		} else if (fileFormat.equals(CSVFile.COORDINATE_FORMAT)) {

			locId = persist(createLocation(
					Double.parseDouble(record.get(CSVFile.LATITUDE_HEADER)),
					Double.parseDouble(record.get(CSVFile.LONGITUDE_HEADER))));
		}
		return locId;
	}

	Location createLocation(long apolloId) {
		final Location loc = new Location();
		loc.setAlsId(apolloId);
		return loc;
	}

	Location createLocation(double latitude, double longitude) {

		final Location loc = new Location();
		loc.setLatitude(latitude);
		loc.setLongitude(longitude);
		return loc;
	}

	SeriesData createSeriesData(long seriesId, long locId, Date time,
			double value) {

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

	private long persist(final Location location) {
		return makeLocationRule().create(location);
	}

	private long persist(final SeriesData seriesData) {
		return Factory.makeSeriesDataRule(JPA.em()).create(seriesData);
	}

}
