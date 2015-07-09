package interactors;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.persistence.EntityManager;

import models.entities.Location;
import models.entities.Series;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;

import play.db.jpa.JPA;

public class FileHandler {

	public static boolean persist(DelimitedFile dataFile) {

		// TODO: should return a msg
		
		long serieID = createSeries(dataFile.getTitle(),dataFile.getDescription());
		long locID = 0;
		String fileFormat = dataFile.getFileFormat();
		CSVParser parser = parsFile(dataFile);
		Iterator<CSVRecord> records = parser.iterator();
		records.next();
		
		while (records.hasNext()) {	
			CSVRecord record = records.next();
			if (fileFormat.equals(DelimitedFile.APOLLO_ID_FORMAT)) {
				locID = createLocationFromApolloID(Long.parseLong(record
						.get(DelimitedFile.APOLLO_ID_HEADER)));
			} else if (fileFormat.equals(DelimitedFile.COORDINATE_FORMAT)) {
				locID = createLocationFromCoordinates(
						Double.parseDouble(record
								.get(DelimitedFile.LATITUDE_HEADER)),
						Double.parseDouble(record
								.get(DelimitedFile.LONGITUDE_HEADER)));
			}
			//createSeriesData(serieID,locID,DateTime.parse(record.get(DelimitedFile.TIME_HEADER)).toDate(),Long.parseLong(record.get(DelimitedFile.VALUE_HEADER)));
			
		}
		return true;

	}

	public static ArrayList<String> getFileErrors(DelimitedFile dataFile) {

		CSVParser parser = parsFile(dataFile);
		ArrayList<String> errorMsgList = new ArrayList<String>();
		String errorMsg = "";
		Iterator<CSVRecord> records = parser.iterator();
		records.next();
		while (records.hasNext()) {
			if ((errorMsg = getRecordErrors(records.next(),
					dataFile.getFileFormat())) != null) {
				errorMsgList.add("Line " + parser.getCurrentLineNumber() + ": "
						+ errorMsg);
			}

		}
		return errorMsgList;
	}

	private static String getRecordErrors(CSVRecord record, String fileFormat) {

		String errorMsg = "";

		try {
			DateTime.parse(record.get(DelimitedFile.TIME_HEADER));
		} catch (IllegalArgumentException e) {
			errorMsg += e.getMessage() + '\n';
		}

		if (!NumberUtils.isNumber(record.get(DelimitedFile.VALUE_HEADER))) {
			errorMsg += "value is not valid.\n";
		}

		switch (fileFormat) {
		case DelimitedFile.APOLLO_ID_FORMAT:

			if (!NumberUtils.isNumber(record
					.get(DelimitedFile.APOLLO_ID_HEADER))) {
				errorMsg += "apollo ID is not valid.\n";
			}

		case DelimitedFile.COORDINATE_FORMAT:

			if (!NumberUtils
					.isNumber(record.get(DelimitedFile.LATITUDE_HEADER))
					|| !NumberUtils.isNumber(record
							.get(DelimitedFile.LONGITUDE_HEADER))) {
				errorMsg += "Coordinate is not valid.\n";
			}
		}
		return errorMsg;
	}

	private static CSVParser parsFile(DelimitedFile dataFile) {

		CSVParser records = null;
		try {
			records = dataFile
					.getCSVFormat()
					.withHeader(dataFile.getMetaData().get("headers"))
					// TODO: use a constant instead of headers
					.withIgnoreEmptyLines(false)
					.withIgnoreSurroundingSpaces(true)
					.withSkipHeaderRecord(false)
					.parse(new FileReader(dataFile.getFile()));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return records;

	}

	private static long createLocationFromApolloID(long apolloID) {
		final EntityManager em = JPA.em();
		final Location loc = new Location();
		loc.setAlsId(apolloID);
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

	private static long createLocationFromCoordinates(Double latitude,
			Double longitude) {

		final EntityManager em = JPA.em();
		final Location loc = new Location();
		loc.setLatitude(latitude);
		loc.setLongitude(longitude);
		em.persist(loc);
		return loc.getId();
	}


	/*private static void createSeriesData(long serieID, long locID, Date time,
			long value) {
		final EntityManager em = JPA.em();
		final SeriesData seriesData = new SeriesData();
		Location loc = new Location();
		seriesData.setLocation();
		em.persist(loc);
		return loc.getId();		
	}*/

}
