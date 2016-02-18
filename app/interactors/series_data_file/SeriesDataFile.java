package interactors.series_data_file;

import static java.util.regex.Pattern.compile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.exceptions.NotFound;

import org.apache.commons.codec.digest.DigestUtils;

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
	private String url;
	private String checksum;
	private Character delimiter;
	private String fileFormat;

	private Map<String, String> stdHeaderToFileHeaderMap;

	public SeriesDataFile(File file) {
		this.file = file;
		setFileAttribs(this.file);
	}

	public SeriesDataFile(String url) {
		this.setUrl(url);
		this.file = readFilefromUrl(url);
		this.checksum = md5sum(this.file);
		setFileAttribs(this.file);
	}

	private void setFileAttribs(File file) {
		String headerLine = readFirstLine(file);
		setDelimiter(headerLine);
		setFileFormat(headerLine);
		if(! fileFormat.isEmpty()){
			mapFileHeadersToStdHeaders(headerLine,getDelimiter());	
		}
	}

	private File readFilefromUrl(String StrUrl) {
		String line = "";
		File tempFile;
		try {
			tempFile = File.createTempFile("tempUserFile", ".tmp");
			URL url = new URL(StrUrl);
			URLConnection conn = url.openConnection();
			BufferedReader br = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
			BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile));
			while ((line = br.readLine()) != null) {
				bw.write(line + "\n");
			}
			bw.close();
			br.close();
		} catch (IOException e) {
			throw new NotFound("The requested resource could not be found");
		}
		return tempFile;
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
		String format = getFileFormat();
		List<String> formatHeader = format2UncommonColumns.get(format);
		result.addAll(formatHeader);
		return result;
	}

	public void setStdHeaderToFileHeaderMap(
			Map<String, String> stdHeaderToFileHeaderMap) {
		this.stdHeaderToFileHeaderMap = stdHeaderToFileHeaderMap;
	}

	public String stdHeaderToFileHeader(String header) {
		return stdHeaderToFileHeaderMap.get(header);
	}

	private void setFileFormat(String headerLine) {
		this.fileFormat = findFormat(delimiter, headerLine);
	}

	private void setDelimiter(String headerLine) {

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
		String[] headers = line.split(delimChar + "");
		for (int i = 0; i < headers.length; i++) {
			if (areEqual(headers[i], ALS_ID_HEADER))
				return ALS_ID_FORMAT;
			if (areEqual(headers[i], LATITUDE_HEADER)
					|| areEqual(headers[i], LONGITUDE_HEADER))
				return COORDINATE_FORMAT;
		}
		return "";
	}
	
	private void mapFileHeadersToStdHeaders(String headerLine, Character delimChar) {
		Map<String, String> result = new HashMap<String, String>();
		String[] fileHeaders = headerLine.split(delimChar + "");
		Set<String> stdHeaderSet = this.getHeaders();
		for (String fileHeader : fileHeaders) {
			for (String stdHeader : stdHeaderSet) {
				if (areEqual(fileHeader,stdHeader)) {
					result.put(stdHeader, fileHeader);
				}
			}
		}
		setStdHeaderToFileHeaderMap(result);
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
			throw new RuntimeException(e);
		}
		return line;
	}

	public void deleteFile() {
		this.file.delete();	
	}
	
	private String md5sum(File file) {
		String md5;
		try {
		FileInputStream fis = new FileInputStream(file);
		md5 = DigestUtils.md5Hex(fis);
		fis.close();
		} catch (IOException e){
			throw new RuntimeException(e);
		}
		return md5;
	}

	public String getChecksum() {
		return checksum;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
