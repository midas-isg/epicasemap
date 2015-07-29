package interactors;

import java.io.FileNotFoundException;
import java.io.IOException;
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

		Map<Long, List<String>> errors = new HashMap<Long, List<String>>();
		List<String> errorList = new ArrayList<String>();
		CSVParser parser = getParseErrors(dataFile, errorList);
		if (!errorList.isEmpty()) {
			errors.put(1L, errorList);
			return errors;
		}
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

	private CSVParser getParseErrors(CSVFile dataFile, List<String> errorList) {
		CSVFileParser csvParser = new CSVFileParser();
		CSVParser parser = null;
		try {
			parser = csvParser.parse(dataFile);
		} catch (IllegalArgumentException e) {
			errorList.add(e.getMessage());
		} catch (FileNotFoundException e) {
			errorList.add(e.getMessage());
		} catch (IOException e) {
			errorList.add(e.getMessage());
		}
		return parser;
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
		List<String> errors = new ArrayList<String>();
		addErrorToList(errors, validateFileHeaders(parser, dataFile));
		addErrorToList(errors, validateDelimiter(parser, dataFile));
		return errors;

	}

	private List<String> validateDelimiter(CSVParser parser, CSVFile dataFile) {
		List<String> errors = new ArrayList<String>();
		return errors;
	}

	List<String> validateFileHeaders(CSVParser parser, CSVFile dataFile) {
		List<String> errors = new ArrayList<String>();
		Set<String> fileHeaderSet = parser.getHeaderMap().keySet();
		Set<String> expectedHeaderSet = dataFile.getHeaders();
		if (fileHeaderSet.size() != expectedHeaderSet.size()) {
			addErrorToList(errors,
					"number of columns is " + fileHeaderSet.size()
							+ ". should be " + expectedHeaderSet.size() + ".");
		}
		for (String header : fileHeaderSet) {
			if (!expectedHeaderSet.contains(header.toLowerCase())) {
				addErrorToList(
						errors,
						"\"" + header + "\""
								+ " column name is not allowed in "
								+ dataFile.getFileFormat() + " format.");
			}

		}
		return errors;
	}

	private List<String> getRecordErrors(CSVRecord record, CSVFile dataFile) {

		List<String> errors = new ArrayList<String>();
		addErrorToList(errors, getRecordSizeError(record, dataFile));
		addErrorToList(errors, getDateTimeError(record, dataFile));
		addErrorToList(errors, getValueError(record, dataFile));
		addErrorToList(errors, getLocationValueError(record, dataFile));

		return errors;
	}

	String getRecordSizeError(CSVRecord record, CSVFile dataFile) {
		String errorMsg = "";
		if (record.size() != dataFile.getHeaders().size()) {
			errorMsg = "row has " + record.size() + " columns. should have "
					+ dataFile.getHeaders().size() + " columns.";
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

	String getLocationValueError(CSVRecord record, CSVFile dataFile) {

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
