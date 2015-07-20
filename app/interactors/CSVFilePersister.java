package interactors;

import java.util.Date;
import java.util.Iterator;

import javax.persistence.EntityManager;

import models.entities.Location;
import models.entities.Series;
import models.entities.SeriesData;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.joda.time.DateTime;

import play.db.jpa.JPA;

public class CSVFilePersister {
	// private SeriesRule seriesRule = Factory.makeSeriesRule(em)

	public long persistCSVFile(CSVFile dataFile) {

		// TODO: should return a msg
		Series series = persist(createSeries(dataFile));
		CSVFileParser csvParser = new CSVFileParser();
		CSVParser parser = csvParser.parse(dataFile);
		if(persistRecords(series, dataFile.getFileFormat(), parser))
			return series.getId();
		else
			return 0L;

	}

	private boolean persistRecords(Series series, String fileFormat,
			CSVParser parser) throws NumberFormatException {
		Iterator<CSVRecord> records = parser.iterator();
		while (records.hasNext()) {
			CSVRecord record = records.next();
			if (persistRecord(series, fileFormat, record) == 0) {
				return false;
			}
		}
		return true;
	}

	private long persistRecord(Series series, String fileFormat,
			CSVRecord record) throws NumberFormatException {

		long seriesDataId = persist(csvRecordToSeriesData(series, record,
				fileFormat));

		return seriesDataId;
	}

	private  SeriesData csvRecordToSeriesData(Series series,
			CSVRecord record, String fileFormat) throws NumberFormatException {
		Location location = createLocationFromCSVRecord(record, fileFormat);
		return createSeriesData(series, location,
				DateTime.parse(record.get(CSVFile.TIME_HEADER)).toDate(),
				Double.parseDouble(record.get(CSVFile.VALUE_HEADER)));
	}

	private Location createLocationFromCSVRecord(CSVRecord record, String fileFormat)
			throws NumberFormatException {
		Location location = null;

		if (fileFormat.equals(CSVFile.APOLLO_ID_FORMAT)) {

			location = persist(createLocation(Long.parseLong(record
					.get(CSVFile.APOLLO_ID_HEADER))));

		} else if (fileFormat.equals(CSVFile.COORDINATE_FORMAT)) {

			location = persist(createLocation(
					Double.parseDouble(record.get(CSVFile.LATITUDE_HEADER)),
					Double.parseDouble(record.get(CSVFile.LONGITUDE_HEADER))));
		}
		return location;
	}

	Series createSeries(CSVFile dataFile) {
		return createSeries(dataFile.getTitle(), dataFile.getDescription());
	}

	Location createLocation(long apolloId) {
		final Location loc = new Location();
		loc.setAlsId(apolloId);
		return loc;
	}

	private  Series createSeries(String title, String desc) {
		final Series series = new Series();
		series.setTitle(title);
		series.setDescription(desc);
		return series;
	}

	Location createLocation(Double latitude, Double longitude) {

		final Location loc = new Location();
		loc.setLatitude(latitude);
		loc.setLongitude(longitude);
		return loc;
	}

	SeriesData createSeriesData(Series series, Location location,
			Date time, double value) {

		final SeriesData seriesData = new SeriesData();
		seriesData.setLocation(location);
		seriesData.setSeries(series);
		seriesData.setTimestamp(time);
		seriesData.setValue(value);
		return seriesData;
	}

	/*
	 * private  long persist(Entity entity) { EntityRule rule =
	 * Factory.makeEntityRule(JPA.em()); return rule.save(entity); }
	 */

	private  Series persist(final Series series) {
		// SeriesRule seriesRule = Factory.makeSeriesRule(JPA.em());

		final EntityManager em = JPA.em();
		em.persist(series);
		// seriesRule.create(serie);
		return series;
	}

	private  Location persist(final Location location) {
		final EntityManager em = JPA.em();
		em.persist(location); // TODO: use FActory.makeRule
		return location;
	}

	private  long persist(final SeriesData seriesData) {
		final EntityManager em = JPA.em();
		em.persist(seriesData);
		return seriesData.getId();
	}

}
