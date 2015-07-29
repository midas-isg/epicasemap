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

	public Map<Long, List<String>> validate(CSVFile dataFile) {

		CSVFileParser csvParser = new CSVFileParser();
		CSVParser parser = csvParser.parse(dataFile);
		Map<Long, List<String>> errors = new HashMap<Long, List<String>>();
		List<String> errorList = new ArrayList<String>();
		if ((errorList = getFileConsistencyError(parser, dataFile)).isEmpty()) {
			mapFileHeadersToStdHeaders(parser, dataFile);
			Iterator<CSVRecord> records = parser.iterator();
			while (records.hasNext()) {
				if (!(errorList = getRecordErrors(records.next(), dataFile))
						.isEmpty()) {
					errors.put(parser.getCurrentLineNumber(), errorList);
				}
			}
		} else {
			errors.put(parser.getCurrentLineNumber(), errorList);
		}
		return errors;
	}

	private void mapFileHeadersToStdHeaders(CSVParser parser, CSVFile dataFile) {
		Map<String, String> result = new HashMap<String, String>();
		Set<String> fileHeaderSet = parser.getHeaderMap().keySet();
		Set<String> stdHeaderSet = dataFile.getHeaders();
		for (String fileHeader : fileHeaderSet) {
			for (String stdHeader : stdHeaderSet) {
				if (fileHeader.equalsIgnoreCase(stdHeader)) {
					result.put(stdHeader, fileHeader);
				}
			}
		}
		dataFile.setStdHeaderToFileHeaderMap(result);
	}

	private List<String> getFileConsistencyError(CSVParser parser,
			CSVFile dataFile) {
		// TODO:
		List<String> errors = new ArrayList<String>();
		addErrorToList(errors, validateFileHeaders(parser, dataFile));
		addErrorToList(errors, validateDelimiter(parser, dataFile));
		return errors;

	}

	private List<String> validateDelimiter(CSVParser parser, CSVFile dataFile) {
		// TODO:
		List<String> errors = new ArrayList<String>();
		return errors;
	}

	private List<String> validateFileHeaders(CSVParser parser, CSVFile dataFile) {
		List<String> errors = new ArrayList<String>();
		Set<String> fileHeaderSet = parser.getHeaderMap().keySet();
		Set<String> expectedHeaderSet = dataFile.getHeaders();
		for (String header : fileHeaderSet) {
			if (!expectedHeaderSet.contains(header.toLowerCase())) {
				addErrorToList(errors,
						"\"" + header + "\"" + " header is not allowed in "
								+ dataFile.getFileFormat() + " format.");
			}

		}
		return errors;
	}

	private List<String> getRecordErrors(CSVRecord record, CSVFile dataFile) {

		List<String> errors = new ArrayList<String>();
		addErrorToList(errors, getConsistencyError(record));
		addErrorToList(errors, getDateTimeError(record, dataFile));
		addErrorToList(errors, getValueError(record, dataFile));
		addErrorToList(errors, getLocationValueError(record, dataFile));

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

	private String getLocationValueError(CSVRecord record, CSVFile dataFile) {

		String errorMsg = "";
		String header;

		switch (dataFile.getFileFormat()) {
		case CSVFile.APOLLO_ID_FORMAT:

			header = dataFile.stdHeaderToFileHeader(CSVFile.APOLLO_ID_HEADER);

			if (!NumberUtils.isNumber(record.get(header))) {
				errorMsg = header + ": " + record.get(header)
						+ " is not valid.";
			}
			break;

		case CSVFile.COORDINATE_FORMAT:

			header = dataFile.stdHeaderToFileHeader(CSVFile.LATITUDE_HEADER);

			if (!NumberUtils.isNumber(record.get(header))) {
				errorMsg = header + ": " + record.get(header)
						+ " is not valid. ";
			}
			header = dataFile.stdHeaderToFileHeader(CSVFile.LONGITUDE_HEADER);
			if (!NumberUtils.isNumber(record.get(header))) {
				errorMsg += header + ": " + record.get(header)
						+ " is not valid.";
			}

			break;
		}
		return errorMsg;
	}

	String getValueError(CSVRecord record, CSVFile dataFile) {
		String errorMsg = "";
		String header = dataFile.stdHeaderToFileHeader(CSVFile.VALUE_HEADER);
		if (!NumberUtils.isNumber(record.get(header))) {
			errorMsg = header + ": " + record.get(header) + " is not valid.";
		}
		return errorMsg;
	}

	String getDateTimeError(CSVRecord record, CSVFile dataFile) {
		String errorMsg = "";
		String header = dataFile.stdHeaderToFileHeader(CSVFile.TIME_HEADER);
		try {
			DateTime.parse(record.get(header));
		} catch (IllegalArgumentException e) {
			errorMsg = header + ": " + e.getMessage();
		}
		return errorMsg;
	}

}
