package interactors;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

public class CSVFileParser {

	private static boolean Ignore_Empty_Lines = true;
	private static boolean Ignore_Surrounding_Spaces = true;
	private static boolean Skip_Header_Record = true;

	public CSVParser parse(CSVFile dataFile) throws IllegalArgumentException, FileNotFoundException, IOException {

		return parse(dataFile.getDelimiter(), dataFile.getFile());

	}

	private CSVParser parse(char delimiter, File file) throws IllegalArgumentException, FileNotFoundException, IOException{
		CSVParser csvParser = null;
		CSVFormat csvFormat = CSVFormat.newFormat(delimiter);
			csvParser = csvFormat
					.withHeader()
					.withIgnoreEmptyLines(Ignore_Empty_Lines)
					.withIgnoreSurroundingSpaces(Ignore_Surrounding_Spaces)
					.withSkipHeaderRecord(Skip_Header_Record)
					.parse(new FileReader(file));
	
		return csvParser;
	}

}
