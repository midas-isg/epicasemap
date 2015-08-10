package interactors.series_data_file;

import interactors.AlsResponseHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.SeriesDataFile;
import models.entities.Location;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;

public class Validator {

	public Map<Long, List<String>> validate(SeriesDataFile dataFile) {

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

	private CSVParser getParseErrors(SeriesDataFile dataFile,
			List<String> errorList) {
		Parser csvParser = new Parser();
		CSVParser parser = null;
		try {
			parser = csvParser.parse(dataFile);
		} catch (Exception e) {
			errorList.add(e.getMessage());
		}
		return parser;
	}

	private void mapFileHeadersToStdHeaders(CSVParser parser,
			SeriesDataFile dataFile) {
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
			SeriesDataFile dataFile) {
		List<String> errors = new ArrayList<String>();
		addErrorToList(errors, validateFileHeaders(parser, dataFile));
		addErrorToList(errors, validateDelimiter(parser, dataFile));
		return errors;

	}

	private List<String> validateDelimiter(CSVParser parser,
			SeriesDataFile dataFile) {
		List<String> errors = new ArrayList<String>();
		return errors;
	}

	List<String> validateFileHeaders(CSVParser parser, SeriesDataFile dataFile) {
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

	private List<String> getRecordErrors(CSVRecord record,
			SeriesDataFile dataFile) {

		List<String> errors = new ArrayList<String>();
		addErrorToList(errors, getRecordSizeError(record, dataFile));
		addErrorToList(errors, getDateTimeError(record, dataFile));
		addErrorToList(errors, getValueError(record, dataFile));
		addErrorToList(errors, getLocationValueError(record, dataFile));

		return errors;
	}

	String getRecordSizeError(CSVRecord record, SeriesDataFile dataFile) {
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

	String getLocationValueError(CSVRecord record, SeriesDataFile dataFile) {

		String errorMsg = "";
		String header;

		switch (dataFile.getFileFormat()) {
		case SeriesDataFile.ALS_ID_FORMAT:

			header = dataFile
					.stdHeaderToFileHeader(SeriesDataFile.ALS_ID_HEADER);

			if (! isNumber(record.get(header))) {
				errorMsg = header + ": " + record.get(header)
						+ " is not valid.";
			} else if(! existInAls(record.get(header))){
				errorMsg = header + ": " + record.get(header)
						+ " does not exist in ALS.";
			}
			break;

		case SeriesDataFile.COORDINATE_FORMAT:

			header = dataFile
					.stdHeaderToFileHeader(SeriesDataFile.LATITUDE_HEADER);

			if (! isNumber(record.get(header))) {
				errorMsg = header + ": " + record.get(header)
						+ " is not valid. ";
			}
			header = dataFile
					.stdHeaderToFileHeader(SeriesDataFile.LONGITUDE_HEADER);
			if (! isNumber(record.get(header))) {
				errorMsg += header + ": " + record.get(header)
						+ " is not valid.";
			}

			break;
		}
		return errorMsg;
	}

	private boolean existInAls(String alsId) {
		Location location = getAlsLocation(Long.parseLong(alsId));
		if (location == null)
			return false;
		return true;
	}

	private Location getAlsLocation(long alsId) throws NumberFormatException {
		AlsResponseHelper helper = new AlsResponseHelper();
		Location location = helper.getAlsLocation(alsId);
		return location;
	}

	private boolean isNumber(String val) {
		return NumberUtils.isNumber(val);
	}

	String getValueError(CSVRecord record, SeriesDataFile dataFile) {
		String errorMsg = "";
		String header = dataFile
				.stdHeaderToFileHeader(SeriesDataFile.VALUE_HEADER);
		if (!isNumber(record.get(header))) {
			errorMsg = header + ": " + record.get(header) + " is not valid.";
		}
		return errorMsg;
	}

	String getDateTimeError(CSVRecord record, SeriesDataFile dataFile) {
		String errorMsg = "";
		String header = dataFile
				.stdHeaderToFileHeader(SeriesDataFile.TIME_HEADER);
		try {
			DateTime.parse(record.get(header));
		} catch (IllegalArgumentException e) {
			errorMsg = header + ": " + e.getMessage();
		}
		return errorMsg;
	}

}
