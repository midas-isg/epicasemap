package models;

import static java.util.regex.Pattern.compile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		format2UncommonColumns.put(COORDINATE_FORMAT,
				list(LATITUDE_HEADER, LONGITUDE_HEADER));
	}

	private static List<String> list(String... strings) {
		return Arrays.asList(strings);
	}

	private File file;
	private Character delimiter;
	private String fileFormat;

	private Map<String, String> stdHeaderToFileHeaderMap;

	public SeriesDataFile(File file) {
		this.file = file;
		setDelimiter();
		setFileFormat();
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {

		this.file = file;
	}

	public char getDelimiter() {
		return delimiter;
	}

	public String getFileFormat() {
		return fileFormat;
	}

	public Set<String> getHeaders() {
		Set<String> result = new HashSet<String>();
		result.add(TIME_HEADER);
		result.add(VALUE_HEADER);
		result.addAll(format2UncommonColumns.get(getFileFormat()));
		return result;
	}

	public void setStdHeaderToFileHeaderMap(
			Map<String, String> stdHeaderToFileHeaderMap) {
		this.stdHeaderToFileHeaderMap = stdHeaderToFileHeaderMap;
	}

	public String stdHeaderToFileHeader(String header) {
		return stdHeaderToFileHeaderMap.get(header);
	}
	
	private void setFileFormat() {
		String headerLine = readFirstLine(file);
		this.fileFormat = findFormat(delimiter, headerLine);		
	}

	private void setDelimiter() {
		String headerLine = readFirstLine(file);
		Character delimChar = findDelimiter(headerLine);
		this.setDelimiter(delimChar);		
	}

	private void setDelimiter(Character delimChar) {
		this.delimiter = delimChar;
	}

	private Character findDelimiter(String line) {
		String regEx = "\\s*\\w+[\\s&&[^\\t]]*(\\W)\\s*\\w*";
		Pattern pattern = compile(regEx);
		Matcher matcher = pattern.matcher(line);
		String delim = "";	
		if (matcher.find())
			delim = matcher.group(1);
			
		return toChar(delim);

	}

	private char toChar(String delim) {
		return delim.isEmpty() ? 0 : delim.charAt(0);
	}

	private String findFormat(Character delimChar, String line) {
		String[] headers = line.split(delimChar+"");
		for(int i=0 ; i < headers.length ; i++){
			if(areEqual(headers[i], ALS_ID_HEADER))
				return ALS_ID_FORMAT;
			if(areEqual(headers[i],LATITUDE_HEADER) || areEqual(headers[i],LONGITUDE_HEADER))
				return COORDINATE_FORMAT;
		}
		return "";
	}

	private boolean areEqual(String header, String string) {
		return polish(header).equals(string);
	}

	private String polish(String str) {
		return str.toLowerCase().trim();
	}

	private String readFirstLine(File file) {
		BufferedReader bf;
		String line = "";
		try {
			bf = new BufferedReader(new FileReader(file));

			line = bf.readLine();
			bf.close();
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
		return line;
	}

}
