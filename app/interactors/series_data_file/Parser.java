package interactors.series_data_file;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import models.SeriesDataFile;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

public class Parser {

	private static boolean Ignore_Empty_Lines = true;
	private static boolean Ignore_Surrounding_Spaces = true;
	private static boolean Skip_Header_Record = true;

	public CSVParser parse(SeriesDataFile dataFile) throws Exception {

		return parse(dataFile.getDelimiter(), dataFile.getFile());

	}

	private CSVParser parse(char delimiter, File file) throws Exception {
		CSVParser csvParser = null;
		CSVFormat csvFormat = CSVFormat.newFormat(delimiter);
			try {
				csvParser = csvFormat
						.withHeader()
						.withIgnoreEmptyLines(Ignore_Empty_Lines)
						.withIgnoreSurroundingSpaces(Ignore_Surrounding_Spaces)
						.withSkipHeaderRecord(Skip_Header_Record)
						.parse(new FileReader(file));
			} catch (IOException e) {
				throw new Exception(e);
			}
	
		return csvParser;
	}
}
