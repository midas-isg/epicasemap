package interactors;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;

public class FileHandler {

	public static boolean saveFile(DelimitedFile dataFile) {
		if (isValid(dataFile)) {
			// TODO:
			return true;
		} else {
			return false;
		}
	}

	private static boolean isValid(DelimitedFile dataFile) {

		String fileFormat = dataFile.getMetaData().get("format")[0];
		for (CSVRecord record : dataFile.getRecords()) {
			if (!isValidRecord(record, fileFormat)) {
				return false;
			}
		}
		return true;
	}

	private static boolean isValidRecord(CSVRecord record, String fileFormat) {

		try {
			DateTime.parse(record.get("time"));
		} catch (IllegalArgumentException e) {
			// TODO:
			e.printStackTrace();
			return false;
		}

		switch (fileFormat) {
		case DelimitedFile.APOLLO_ID_FORMAT:

			return NumberUtils.isNumber(record.get("apollo ID"))
					&& NumberUtils.isNumber(record.get("value"));

		case DelimitedFile.COORDINATE_FORMAT:

			return NumberUtils.isNumber(record.get("Lat"))
					&& NumberUtils.isNumber(record.get("Long"))
					&& NumberUtils.isNumber(record.get("value"));

		default:
			return false;
		}
	}

	public static DelimitedFile parsFile(DelimitedFile dataFile) {

		try {
			dataFile.setRecords(dataFile.getCSVFormat()
					.withHeader(dataFile.getMetaData().get("headers"))
					.withIgnoreEmptyLines(true)
					.withIgnoreSurroundingSpaces(true)
					.withSkipHeaderRecord(true)
					.parse(new FileReader(dataFile.getFile())));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return dataFile;

	}

}
