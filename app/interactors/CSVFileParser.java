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

	public CSVParser parse(CSVFile dataFile) {

		return  parse(dataFile.getDelimiter(), dataFile.getHeaders(),
				dataFile.getFile());

	}

	private CSVParser parse(char delimiter, String[] headers,
			File file) {
		CSVParser csvParser = null;
		CSVFormat csvFormat = CSVFormat.newFormat(delimiter);
		try {
			csvParser = csvFormat
					.withHeader(headers)
					.withIgnoreEmptyLines(Ignore_Empty_Lines)
					.withIgnoreSurroundingSpaces(Ignore_Surrounding_Spaces)
					.withSkipHeaderRecord(Skip_Header_Record)
					.parse(new FileReader(file));
			//csvParser.close(); //TODO: does it need to be closed??
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return csvParser;
	}

}
