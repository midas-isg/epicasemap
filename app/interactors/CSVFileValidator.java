package interactors;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;

public class CSVFileValidator {

	public static ArrayList<String> getFileErrors(DelimitedFile dataFile) {

		CSVParser parser = CSVFileParser.parser(dataFile);
		ArrayList<String> errorMsgList = new ArrayList<String>();
		String errorMsg = "";
		Iterator<CSVRecord> records = parser.iterator();
		while (records.hasNext()) {
			if ((errorMsg = getRecordErrors(records.next(),
					dataFile.getFileFormat())) != null) {
				errorMsgList.add("Line " + parser.getCurrentLineNumber() + ": "
						+ errorMsg);
			}
		}
		return errorMsgList;
	}

	private static String getRecordErrors(CSVRecord record, String fileFormat) {

		String errorMsg = "";

		try {
			DateTime.parse(record.get(DelimitedFile.TIME_HEADER));
		} catch (IllegalArgumentException e) {
			errorMsg += e.getMessage() + '\n';
		}

		if (!NumberUtils.isNumber(record.get(DelimitedFile.VALUE_HEADER))) {
			errorMsg += "value is not valid.\n";
		}

		switch (fileFormat) {
		case DelimitedFile.APOLLO_ID_FORMAT:

			if (!NumberUtils.isNumber(record
					.get(DelimitedFile.APOLLO_ID_HEADER))) {
				errorMsg += "apollo ID is not valid.\n";
			}

		case DelimitedFile.COORDINATE_FORMAT:

			if (!NumberUtils
					.isNumber(record.get(DelimitedFile.LATITUDE_HEADER))
					|| !NumberUtils.isNumber(record
							.get(DelimitedFile.LONGITUDE_HEADER))) {
				errorMsg += "Coordinate is not valid.\n";
			}
		}
		return errorMsg;
	}

}
