package interactors.series_data_file;

import interactors.LocationRule;
import interactors.SeriesDataRule;
import interactors.SeriesDataUrlRule;
import interactors.SeriesRule;
import interactors.series_data_file.Parser.DataPoint;

import java.util.Date;
import java.util.List;

import models.entities.Location;
import models.entities.Series;
import models.entities.SeriesData;
import models.entities.SeriesDataUrl;

import org.joda.time.DateTime;

public class Persister {

	private LocationRule locationRule;
	private SeriesRule seriesRule;
	private SeriesDataRule seriesDataRule;
	private SeriesDataUrlRule seriesDataUrlRule;
	private Parser parser;
	private SeriesDataFile dataFile;
	private Series series;

	public int persistSeriesDataFile(Long seriesId) {

		this.series = seriesRule.read(seriesId);
		int numCreated = persistSeriesData();
		if (dataFile.getUrl() != null)
			updateOrCreateUrl(series, dataFile.getUrl(), dataFile.getChecksum());
		return numCreated;
	}

	private int persistSeriesData() {
		int counter = 0;
		while (parser.hasNext()) {
			persistDataPoint(parser.next());
			counter++;
		}
		return counter;
	}

	SeriesData persistDataPoint(DataPoint dataPoint) {
		Location location = createLocationFromCSVRecord(dataPoint);
		return seriesDataRule.createNew(series, location,
				getTimeStamp(dataPoint), getValue(dataPoint));
	}

	private Double getValue(DataPoint dataPoint) {
		String header = dataFile
				.stdHeaderToFileHeader(SeriesDataFile.VALUE_HEADER);

		return stringToDouble(dataPoint.get(header));
	}

	private Date getTimeStamp(DataPoint dataPoint) {
		String header = dataFile
				.stdHeaderToFileHeader(SeriesDataFile.TIME_HEADER);
		return DateTime.parse(dataPoint.get(header)).toDate();
	}

	Location createLocationFromCSVRecord(DataPoint dataPoint) {
		Location location = null;
		String fileFormat = dataFile.getFileFormat();

		if (fileFormat.equals(SeriesDataFile.ALS_ID_FORMAT)) {
			Long alsId = getAlsId(dataPoint);
			location = locationRule.getLocation(alsId);

		} else if (fileFormat.equals(SeriesDataFile.COORDINATE_FORMAT)) {
			Double lat = getLatitude(dataPoint);
			Double lon = getLongitude(dataPoint);
			location = locationRule.getLocation(lat, lon);
		}
		return location;
	}

	private Double getLongitude(DataPoint dataPoint) {
		String lonHeader = dataFile
				.stdHeaderToFileHeader(SeriesDataFile.LONGITUDE_HEADER);
		Double lon = stringToDouble(dataPoint.get(lonHeader));
		return lon;
	}

	private Double stringToDouble(String header) {
		return Double.parseDouble(header);
	}

	private Double getLatitude(DataPoint dataPoint) {
		String latHeader = dataFile
				.stdHeaderToFileHeader(SeriesDataFile.LATITUDE_HEADER);
		Double lat = stringToDouble(dataPoint.get(latHeader));
		return lat;
	}

	private Long getAlsId(DataPoint dataPoint) {
		String header;
		header = dataFile.stdHeaderToFileHeader(SeriesDataFile.ALS_ID_HEADER);
		Long alsId = stringToLong(dataPoint.get(header));
		return alsId;
	}

	private long stringToLong(String header) {
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

	public void setSeriesDataFile(SeriesDataFile dataFile) {
		this.dataFile = dataFile;

	}

	public void setSeriesDataUrlRule(SeriesDataUrlRule seriesDataUrlRule) {
		this.seriesDataUrlRule = seriesDataUrlRule;
	}

	private void updateOrCreateUrl(Series series, String url, String checksum) {
		List<SeriesDataUrl> seriesDataUrl = seriesDataUrlRule.query(series
				.getId());
		if (seriesDataUrl.isEmpty())
			seriesDataUrlRule.createNew(series, url, checksum);
		else {
			seriesDataUrl.get(0).setUrl(url);
			seriesDataUrl.get(0).setChecksum(checksum);
		}
	}
}
