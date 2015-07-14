package interactors;

import java.io.File;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;

public class CSVFile {
	public static final String APOLLO_ID_FORMAT = "apolloIdFormat";
	public static final String COORDINATE_FORMAT = "coordinateFormat";
	public static final String TIME_HEADER = "time";
	public static final String APOLLO_ID_HEADER = "apollo ID";
	public static final String VALUE_HEADER = "value";
	public static final String LATITUDE_HEADER = "latitude";
	public static final String LONGITUDE_HEADER = "longitude";

	private File file;
	private Map<String, String[]> metaData;

	public CSVFile(File file, Map<String, String[]> metaData) {
		this.file = file;
		this.metaData = metaData;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public Map<String, String[]> getMetaData() {
		return metaData;
	}

	public void setMetaData(Map<String, String[]> metaData) {
		this.metaData = metaData;
	}

	public CSVFormat getCSVFormat() {
		char del = getMetaData().get("delimiter")[0].charAt(0); // TODO: change
																// string to
																// constant
		return CSVFormat.newFormat(del);

	}

	public String getFileFormat() {
		return getMetaData().get("format")[0]; // TODO: change string to
												// constant
	}

	public String getTitle() {
		return metaData.get("title")[0]; // TODO: change string to constant
	}

	public String getDescription() {
		return (metaData.get("description")[0] == null) ? "" : metaData
				.get("description")[0]; // TODO: change string to constant
	}

	public String[] getHeaders() {
		return metaData.get("headers");
	}
}
