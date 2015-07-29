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
		return persistRecords(seriesId, dataFile, parser);

	}

	private boolean persistRecords(long seriesId, CSVFile dataFile,
			CSVParser parser) throws NumberFormatException {
		Iterator<CSVRecord> records = parser.iterator();
		while (records.hasNext()) {
			CSVRecord record = records.next();
			if (persistRecord(seriesId, dataFile, record) == 0) {
				return false;
			}
		}
		return true;
	}

	private long persistRecord(long seriesId, CSVFile dataFile, CSVRecord record)
			throws NumberFormatException {

		SeriesData seriesData = csvRecordToSeriesData(seriesId, record,
				dataFile);
		return persist(seriesData);
	}

	private SeriesData csvRecordToSeriesData(long seriesId, CSVRecord record,
			CSVFile dataFile) throws NumberFormatException {
		long locId = createLocationFromCSVRecord(record, dataFile);
		return createSeriesData(seriesId, locId,
				getTimeStamp(record, dataFile), getValue(record, dataFile));
	}

	private double getValue(CSVRecord record, CSVFile dataFile)
			throws NumberFormatException {
		String header = dataFile.stdHeaderToFileHeader(CSVFile.VALUE_HEADER);

		return Double.parseDouble(record.get(header));
	}

	private Date getTimeStamp(CSVRecord record, CSVFile dataFile) {
		String header = dataFile.stdHeaderToFileHeader(CSVFile.TIME_HEADER);
		return DateTime.parse(record.get(header)).toDate();
	}

	private long createLocationFromCSVRecord(CSVRecord record, CSVFile dataFile)
			throws NumberFormatException {
		long locId = 0L;
		String header;
		String fileFormat = dataFile.getFileFormat();

		if (fileFormat.equals(CSVFile.APOLLO_ID_FORMAT)) {
			header = dataFile.stdHeaderToFileHeader(CSVFile.APOLLO_ID_HEADER);
			locId = persist(createLocation(Long.parseLong(record.get(header))));

		} else if (fileFormat.equals(CSVFile.COORDINATE_FORMAT)) {
			String lat = dataFile
					.stdHeaderToFileHeader(CSVFile.LATITUDE_HEADER);
			String lon = dataFile
					.stdHeaderToFileHeader(CSVFile.LONGITUDE_HEADER);

			locId = persist(createLocation(Double.parseDouble(record.get(lat)),
					Double.parseDouble(record.get(lon))));
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
