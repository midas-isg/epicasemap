package interactors;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CSVFile {
	public static final String APOLLO_ID_FORMAT = "apolloIdFormat";
	public static final String COORDINATE_FORMAT = "coordinateFormat";
	public static final String TIME_HEADER = "time";
	public static final String APOLLO_ID_HEADER = "apollo id";
	public static final String VALUE_HEADER = "value";
	public static final String LATITUDE_HEADER = "latitude";
	public static final String LONGITUDE_HEADER = "longitude";

	private File file;
	private char delimiter;
	private String fileFormat;
	
	private Map<String,String> stdHeaderToFileHeaderMap;

	public File getFile() {
		return file;
	}

	public void setFile(File file) {

		this.file = file;
	}

	public char getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter.charAt(0);
	}

	public String getFileFormat() {
		return fileFormat;
	}

	public void setFileFormat(String fileFormat) {
		this.fileFormat = fileFormat;
	}

	public Set<String> getHeaders() {
		Set<String> result = new HashSet<String>();
		result.add(TIME_HEADER);
		result.add(VALUE_HEADER);
		switch (getFileFormat()) {
		case APOLLO_ID_FORMAT:
			result.add(APOLLO_ID_HEADER);
			break;
		case COORDINATE_FORMAT:
			result.add(LATITUDE_HEADER);
			result.add(LONGITUDE_HEADER);
			break;
		}
		return result;
	}

	public void setStdHeaderToFileHeaderMap(Map<String, String> stdHeaderToFileHeaderMap) {
		this.stdHeaderToFileHeaderMap = stdHeaderToFileHeaderMap;		
	}
	
	public String stdHeaderToFileHeader(String header){
		return stdHeaderToFileHeaderMap.get(header);
	}

}
