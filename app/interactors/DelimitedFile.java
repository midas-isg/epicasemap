package interactors;

import java.io.File;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public class DelimitedFile {
	public static final String APOLLO_ID_FORMAT = "apolloIdFormat";
	public static final String COORDINATE_FORMAT = "coordinateFormat";
	
	private File file;
	private Map<String, String[]> metaData;
	private Iterable<CSVRecord> records;

	public DelimitedFile(File file, Map<String, String[]> metaData) {
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
		char del = getMetaData().get("delimiter")[0].charAt(0);
		return CSVFormat.newFormat(del);

	}

	public Iterable<CSVRecord> getRecords() {
		return records;
	}

	public void setRecords(Iterable<CSVRecord> records) {
		this.records = records;
	}

}
