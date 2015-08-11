package models;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SeriesDataFile {
	public static final String ALS_ID_FORMAT = "alsIdFormat";
	public static final String COORDINATE_FORMAT = "coordinateFormat";
	
	public static final String TIME_HEADER = "time";
	public static final String ALS_ID_HEADER = "als_id";
	public static final String VALUE_HEADER = "value";
	public static final String LATITUDE_HEADER = "latitude";
	public static final String LONGITUDE_HEADER = "longitude";
	
	private static Map<String, List<String>> format2UncommonColumns = new HashMap<>();
	
	static {
		format2UncommonColumns.put(ALS_ID_FORMAT, list(ALS_ID_HEADER));
		format2UncommonColumns.put(COORDINATE_FORMAT, list(LATITUDE_HEADER, LONGITUDE_HEADER));
	}

	private static List<String> list(String... strings){
		return Arrays.asList(strings);
	}
	
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
		result.addAll(format2UncommonColumns.get(getFileFormat()));
		return result;
	}

	public void setStdHeaderToFileHeaderMap(Map<String, String> stdHeaderToFileHeaderMap) {
		this.stdHeaderToFileHeaderMap = stdHeaderToFileHeaderMap;		
	}
	
	public String stdHeaderToFileHeader(String header){
		return stdHeaderToFileHeaderMap.get(header);
	}

}
