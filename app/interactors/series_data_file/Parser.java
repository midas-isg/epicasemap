package interactors.series_data_file;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import models.SeriesDataFile;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class Parser {

	private static boolean Ignore_Empty_Lines = true;
	private static boolean Ignore_Surrounding_Spaces = true;
	private static boolean Skip_Header_Record = true;
	private CSVParser csvParser;
	private String parseError = "";
	private Iterator<CSVRecord> iterator;

	public Parser(SeriesDataFile dataFile) {
		try {
			this.csvParser = parse(dataFile.getDelimiter(), dataFile.getFile());
			this.iterator = csvParser.iterator();
		} catch (RuntimeException e) {
			this.parseError = e.getMessage();
		}
	}

	private CSVParser parse(char delimiter, File file) {
		CSVParser csvParser = null;
		CSVFormat csvFormat = CSVFormat.newFormat(delimiter);
		try {
			csvParser = csvFormat.withHeader()
					.withIgnoreEmptyLines(Ignore_Empty_Lines)
					.withIgnoreSurroundingSpaces(Ignore_Surrounding_Spaces)
					.withSkipHeaderRecord(Skip_Header_Record)
					.parse(new FileReader(file));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return csvParser;
	}

	public boolean hasNext() {
		return iterator.hasNext();
	}

	public class DataPoint {
		private CSVRecord record;

		public DataPoint(CSVRecord record) {
			this.record = record;
		}

		public String get(String columnName) {
			return record.get(columnName);
		}

		public int size() {
			return record.size();
		}
	}

	public DataPoint next() {
		return new DataPoint(iterator.next());
	}

	public String getParseError() {
		return this.parseError;
	}

	public Set<String> getFileHeaders() {
		return csvParser.getHeaderMap().keySet();
	}

	public Long getCurrentLineNumber() {
		return csvParser.getCurrentLineNumber();
	}
}
