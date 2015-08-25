package interactors.series_data_file;

import interactors.LocationRule;
import interactors.SeriesDataRule;
import interactors.SeriesRule;

import java.util.Date;

import models.SeriesDataFile;
import models.entities.Location;
import models.entities.Series;
import models.entities.SeriesData;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.joda.time.DateTime;

public class Persister {
	
	private LocationRule locationRule;
	private SeriesRule seriesRule;
	private SeriesDataRule seriesDataRule;
	private Parser parser;
	

	public int persistSeriesDataFile(SeriesDataFile dataFile, Long seriesId)
			throws Exception {

		CSVParser csvParser = parser.parse(dataFile);
		Series series = seriesRule.read(seriesId);
		return persistSeriesData(series, dataFile, csvParser);

	}

	private int persistSeriesData(Series series, SeriesDataFile dataFile,
			CSVParser parser) throws Exception {
		int counter = 0;
		for (CSVRecord dataPoint : parser) {
			persistDataPoint(series, dataFile, dataPoint);
			counter++;
		}
		return counter;
	}

	SeriesData persistDataPoint(Series series, SeriesDataFile dataFile,
			CSVRecord dataPoint) throws Exception {

		Location location = createLocationFromCSVRecord(dataPoint, dataFile);
		return seriesDataRule.createNew(series, location,
				getTimeStamp(dataPoint, dataFile), getValue(dataPoint, dataFile));
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

	Location createLocationFromCSVRecord(CSVRecord record,
			SeriesDataFile dataFile) throws Exception {
		Location location = null;
		String fileFormat = dataFile.getFileFormat();

		if (fileFormat.equals(SeriesDataFile.ALS_ID_FORMAT)) {
			Long alsId = getAlsId(record, dataFile);
			location = locationRule.getLocation(alsId);

		} else if (fileFormat.equals(SeriesDataFile.COORDINATE_FORMAT)) {
			Double lat = getLatitude(record, dataFile);
			Double lon = getLongitude(record, dataFile);
			location = locationRule.getLocation(lat, lon);
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

	public void setLocationRule(LocationRule locationRule) {
		this.locationRule = locationRule;
		
	}

	public void setSeriesRule(SeriesRule seriesRule) {
		this.seriesRule = seriesRule;
	}

	public void setSeriesDataRule(SeriesDataRule seriesDataRule) {
		this.seriesDataRule = seriesDataRule;
	}

	public void setParser(Parser parser) {
		this.parser = parser;
		
	}

}
