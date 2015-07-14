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

	public static boolean persistDelimitedFile(DelimitedFile dataFile) {

		// TODO: should return a msg
		Series serie = createSeries(dataFile);
		CSVParser parser = CSVFileParser.parser(dataFile);
		return persistRecords(serie.getId(), dataFile.getFileFormat(), parser);

	}

	private static boolean persistRecords(long serieID, String fileFormat,
			CSVParser parser) throws NumberFormatException {
		Iterator<CSVRecord> records = parser.iterator();
		while (records.hasNext()) {
			CSVRecord record = records.next();
			if (persistRecord(serieID, fileFormat, record) == 0) {
				return false;
			}
		}
		return true;
	}

	private static long persistRecord(long serieID, String fileFormat,
			CSVRecord record) throws NumberFormatException {

		long locId = 0;

		if (fileFormat.equals(DelimitedFile.APOLLO_ID_FORMAT)) {

			locId = createLocationFromApolloID(Long.parseLong(record
					.get(DelimitedFile.APOLLO_ID_HEADER)));

		} else if (fileFormat.equals(DelimitedFile.COORDINATE_FORMAT)) {

			locId = createLocationFromCoordinates(Double.parseDouble(record
					.get(DelimitedFile.LATITUDE_HEADER)),
					Double.parseDouble(record
							.get(DelimitedFile.LONGITUDE_HEADER)));
		}
		long seriesDataId = createSeriesData(serieID, locId,
				DateTime.parse(record.get(DelimitedFile.TIME_HEADER)).toDate(),
				Double.parseDouble(record.get(DelimitedFile.VALUE_HEADER)));

		return seriesDataId;
	}
	
	private static Series createSeries(DelimitedFile dataFile) {
		return createSeries(dataFile.getTitle(), dataFile.getDescription());
	}

	private static long createLocationFromApolloID(long apolloID) {
		final EntityManager em = JPA.em();
		final Location loc = new Location();
		loc.setAlsId(apolloID);
		em.persist(loc);
		return loc.getId();
	}

	private static Series createSeries(String title, String desc) {
		final EntityManager em = JPA.em();
		final Series serie = new Series();
		serie.setName(title);
		serie.setDescription(desc);
		em.persist(serie);
		return serie;
	}

	private static long createLocationFromCoordinates(Double latitude,
			Double longitude) {

		final EntityManager em = JPA.em();
		final Location loc = new Location();
		loc.setLatitude(latitude);
		loc.setLongitude(longitude);
		em.persist(loc);
		return loc.getId();
	}

	private static long createSeriesData(long serieID, long locID, Date time,
			double value) {
		final EntityManager em = JPA.em();
		final SeriesData seriesData = new SeriesData();
		Location loc = JPA.em().find(Location.class, locID);
		seriesData.setLocation(loc);
		Series series = JPA.em().find(Series.class, serieID);
		seriesData.setSeries(series);
		seriesData.setTimestamp(time);
		seriesData.setValue(value);
		em.persist(seriesData);
		return seriesData.getId();
	}

}
