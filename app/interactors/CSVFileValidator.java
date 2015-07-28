package interactors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;

public class CSVFileValidator {

	public Map<Long, List<String>> getFileErrors(CSVFile dataFile) {

		CSVFileParser csvParser = new CSVFileParser();
		CSVParser parser = csvParser.parse(dataFile);
		Map<Long, List<String>> errors = new HashMap<Long, List<String>>();
		List<String> errorList = new ArrayList<String>();
		Iterator<CSVRecord> records = parser.iterator();
		if ((errorList = getFileConsistencyError(parser, dataFile)).isEmpty()) {
			while (records.hasNext()) {
				if (!(errorList = getRecordErrors(records.next(),
						dataFile.getFileFormat())).isEmpty()) {
					errors.put(parser.getCurrentLineNumber(), errorList);
				}
			}
		} else {
			errors.put(parser.getCurrentLineNumber(), errorList);
		}
		return errors;
	}

	private List<String> getFileConsistencyError(CSVParser parser, CSVFile dataFile) {
		// TODO:
		List<String> errors = new ArrayList<String>();
		addErrorToList(errors, validateFileHeaders(parser, dataFile));
		addErrorToList(errors, validateDelimiter(parser, dataFile));
		return errors;

	}

	private List<String> validateDelimiter(CSVParser parser, CSVFile dataFile) {
		//TODO:
		List<String> errors = new ArrayList<String>();
		return errors;
	}

	private List<String> validateFileHeaders(CSVParser parser, CSVFile dataFile) {
		List<String> errors = new ArrayList<String>();
		Set<String> fileHeaderSet = parser.getHeaderMap().keySet();
		Set<String> expectedHeaderSet = dataFile.getHeaders();
		for (String header : fileHeaderSet) {
			if (!expectedHeaderSet.contains(header)) {
				addErrorToList(errors, "\"" + header + "\"" + " header is not allowed in "
						+ dataFile.getFileFormat() + " format.");
			}
		}
		return errors;
	}

	private List<String> getRecordErrors(CSVRecord record, String fileFormat) {

		List<String> errors = new ArrayList<String>();
		addErrorToList(errors, getConsistencyError(record));
		addErrorToList(errors, getDateTimeError(record));
		addErrorToList(errors, getValueError(record));
		addErrorToList(errors, getLocationValueError(record, fileFormat));

		return errors;
	}

	private String getConsistencyError(CSVRecord record) {
		String errorMsg = "";
		if (!record.isConsistent()) {
			errorMsg = "The record size does not match the header size.";
		}
		return errorMsg;
	}

	private void addErrorToList(List<String> errors, String err) {
		if (err != "")
			errors.add(err);
	}
	private void addErrorToList(List<String> errors1, List<String> errors2) {
		if (!errors2.isEmpty())
			errors1.addAll(errors2);
	}

	private String getLocationValueError(CSVRecord record, String fileFormat) {

		String errorMsg = "";

		switch (fileFormat) {
		case CSVFile.APOLLO_ID_FORMAT:

			if (!NumberUtils.isNumber(record.get(CSVFile.APOLLO_ID_HEADER))) {
				errorMsg = CSVFile.APOLLO_ID_HEADER + ": "
						+ record.get(CSVFile.APOLLO_ID_HEADER)
						+ " is not valid.";
			}
			break;

		case CSVFile.COORDINATE_FORMAT:

			if (!NumberUtils.isNumber(record.get(CSVFile.LATITUDE_HEADER))) {
				errorMsg = CSVFile.LATITUDE_HEADER + ": "
						+ record.get(CSVFile.LATITUDE_HEADER)
						+ " is not valid. ";
			}
			if (!NumberUtils.isNumber(record.get(CSVFile.LONGITUDE_HEADER))) {
				errorMsg += CSVFile.LONGITUDE_HEADER + ": "
						+ record.get(CSVFile.LONGITUDE_HEADER)
						+ " is not valid.";
			}

			break;
		}
		return errorMsg;
	}

	private String getValueError(CSVRecord record) {
		String errorMsg = "";
		if (!NumberUtils.isNumber(record.get(CSVFile.VALUE_HEADER))) {
			errorMsg = CSVFile.VALUE_HEADER + ": "
					+ record.get(CSVFile.VALUE_HEADER) + " is not valid.";
		}
		return errorMsg;
	}

	private String getDateTimeError(CSVRecord record) {
		String errorMsg = "";
		try {
			DateTime.parse(record.get(CSVFile.TIME_HEADER));
		} catch (IllegalArgumentException e) {
			errorMsg = CSVFile.TIME_HEADER + ": " + e.getMessage();
		}
		return errorMsg;
	}

}
