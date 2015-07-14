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

	public static boolean persistCSVFile(CSVFile dataFile) {

		// TODO: should return a msg
		long serieId = createSeries(dataFile);
		CSVParser parser = CSVFileParser.parser(dataFile);
		return persistRecords(serieId, dataFile.getFileFormat(), parser);

	}

	private static boolean persistRecords(long serieId, String fileFormat,
			CSVParser parser) throws NumberFormatException {
		Iterator<CSVRecord> records = parser.iterator();
		while (records.hasNext()) {
			CSVRecord record = records.next();
			if (persistRecord(serieId, fileFormat, record) == 0) {
				return false;
			}
		}
		return true;
	}

	private static long persistRecord(long serieId, String fileFormat,
			CSVRecord record) throws NumberFormatException {

		long locId = 0;

		if (fileFormat.equals(CSVFile.APOLLO_ID_FORMAT)) {

			locId = createLocation(Long.parseLong(record
					.get(CSVFile.APOLLO_ID_HEADER)));

		} else if (fileFormat.equals(CSVFile.COORDINATE_FORMAT)) {

			locId = createLocation(Double.parseDouble(record
					.get(CSVFile.LATITUDE_HEADER)),
					Double.parseDouble(record
							.get(CSVFile.LONGITUDE_HEADER)));
		}
		long seriesDataId = createSeriesData(serieId, locId,
				DateTime.parse(record.get(CSVFile.TIME_HEADER)).toDate(),
				Double.parseDouble(record.get(CSVFile.VALUE_HEADER)));

		return seriesDataId;
	}
	
	private static long createSeries(CSVFile dataFile) {
		return createSeries(dataFile.getTitle(), dataFile.getDescription());
	}

	private static long createLocation(long apolloId) {
		final EntityManager em = JPA.em();
		final Location loc = new Location();
		loc.setAlsId(apolloId);
		
		em.persist(loc);
		return loc.getId();
	}

	private static long createSeries(String title, String desc) {
		final EntityManager em = JPA.em();
		final Series serie = new Series();
		serie.setName(title);
		serie.setDescription(desc);
		em.persist(serie);
		return serie.getId();
	}

	private static long createLocation(Double latitude,
			Double longitude) {

		final EntityManager em = JPA.em();
		final Location loc = new Location();
		loc.setLatitude(latitude);
		loc.setLongitude(longitude);
		em.persist(loc);
		return loc.getId();
	}

	private static long createSeriesData(long serieId, long locId, Date time,
			double value) {
		final EntityManager em = JPA.em();
		final SeriesData seriesData = new SeriesData();
		Location loc = JPA.em().find(Location.class, locId);
		seriesData.setLocation(loc);
		Series series = JPA.em().find(Series.class, serieId);
		seriesData.setSeries(series);
		seriesData.setTimestamp(time);
		seriesData.setValue(value);
		em.persist(seriesData);
		return seriesData.getId();
	}

}
