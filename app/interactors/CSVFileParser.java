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

	public static CSVParser parser(CSVFile dataFile) {

		return  parser(dataFile.getCSVFormat(), dataFile.getHeaders(),
				dataFile.getFile());

	}

	private static CSVParser parser(CSVFormat csvFormat, String[] headers,
			File file) {
		CSVParser records = null;
		try {
			records = csvFormat
					.withHeader(headers)
					.withIgnoreEmptyLines(Ignore_Empty_Lines)
					.withIgnoreSurroundingSpaces(Ignore_Surrounding_Spaces)
					.withSkipHeaderRecord(Skip_Header_Record)
					.parse(new FileReader(file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return records;
	}

}
