package interactors.series_data_file;

import interactors.LocationRule;
import interactors.series_data_file.Parser.DataPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;

public class Validator {

	private LocationRule locationRule;
	private Parser parser;
	private SeriesDataFile dataFile;
	private Map<Long, List<String>> errors = new HashMap<Long, List<String>>();

	public Map<Long, List<String>> validateDataFile() {

		List<String> errorList = new ArrayList<String>();
		addErrorToList(errorList, parser.getParseError());
		if (!errorList.isEmpty()) {
			errors.put(1L, errorList);
			return this.errors;
		}
		if ((errorList = getFileConsistencyError()).isEmpty()) {
			while (parser.hasNext()) {
				if (!(errorList = getRecordErrors(parser.next())).isEmpty()) {
					errors.put(parser.getCurrentLineNumber(), errorList);
				}
			}
		} else {
			errors.put(parser.getCurrentLineNumber(), errorList);
		}
		return errors;
	}

	List<String> getFileConsistencyError() {
		List<String> errors = new ArrayList<String>();
		addErrorToList(errors, validateFormat());
		if (errors.isEmpty())
			addErrorToList(errors, validateFileHeaders());
		return errors;

	}

	private String validateFormat() {
		String format = dataFile.getFileFormat();
		if (format.isEmpty())
			return "column names are not valid.";
		else
			return "";
	}

	private List<String> validateFileHeaders() {
		List<String> errors = new ArrayList<String>();
		Set<String> fileHeaderSet = parser.getFileHeaders();
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

	private List<String> getRecordErrors(DataPoint dataPoint) {

		List<String> errors = new ArrayList<String>();
		addErrorToList(errors, getRecordSizeError(dataPoint));
		addErrorToList(errors, getDateTimeError(dataPoint));
		addErrorToList(errors, getValueError(dataPoint));
		addErrorToList(errors, getLocationValueError(dataPoint));

		return errors;
	}

	String getRecordSizeError(DataPoint dataPoint) {
		String errorMsg = "";
		if (dataPoint.size() != dataFile.getHeaders().size()) {
			errorMsg = "row has " + dataPoint.size() + " columns. should have "
					+ dataFile.getHeaders().size() + " columns.";
		}
		return errorMsg;
	}

	private void addErrorToList(List<String> errorList, String err) {
		if (err != "")
			errorList.add(err);
	}

	private void addErrorToList(List<String> errors1, List<String> errors2) {
		if (!errors2.isEmpty())
			errors1.addAll(errors2);
	}

	String getLocationValueError(DataPoint dataPoint) {

		String errorMsg = "";
		String header;

		switch (dataFile.getFileFormat()) {
		case SeriesDataFile.ALS_ID_FORMAT:

			header = dataFile
					.stdHeaderToFileHeader(SeriesDataFile.ALS_ID_HEADER);

			if (!isNumber(dataPoint.get(header))) {
				errorMsg = header + ": " + dataPoint.get(header)
						+ " is not valid.";
			} else if (!existInAls(dataPoint.get(header))) {
				errorMsg = header + ": " + dataPoint.get(header)
						+ " does not exist in ALS.";
			}
			break;

		case SeriesDataFile.COORDINATE_FORMAT:

			header = dataFile
					.stdHeaderToFileHeader(SeriesDataFile.LATITUDE_HEADER);

			if (!isNumber(dataPoint.get(header))) {
				errorMsg = header + ": " + dataPoint.get(header)
						+ " is not valid. ";
			}
			header = dataFile
					.stdHeaderToFileHeader(SeriesDataFile.LONGITUDE_HEADER);
			if (!isNumber(dataPoint.get(header))) {
				errorMsg += header + ": " + dataPoint.get(header)
						+ " is not valid.";
			}

			break;
		}
		return errorMsg;
	}

	private boolean existInAls(String alsId) {
		try {
			locationRule.getLocationByAlsId(Long.parseLong(alsId));
		} catch (RuntimeException e) {
			return false;
		}
		return true;
	}

	private boolean isNumber(String val) {
		try {
			Double.parseDouble(val);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	String getValueError(DataPoint dataPoint) {
		String errorMsg = "";
		String header = dataFile
				.stdHeaderToFileHeader(SeriesDataFile.VALUE_HEADER);
		if (!isNumber(dataPoint.get(header))) {
			errorMsg = header + ": " + dataPoint.get(header) + " is not valid.";
		}
		return errorMsg;
	}

	String getDateTimeError(DataPoint dataPoint) {
		String errorMsg = "";
		String header = dataFile
				.stdHeaderToFileHeader(SeriesDataFile.TIME_HEADER);
		try {
			DateTime.parse(dataPoint.get(header));
		} catch (IllegalArgumentException e) {
			errorMsg = header + ": " + e.getMessage();
		}
		return errorMsg;
	}

	public void setLocationRule(LocationRule locationRule) {
		this.locationRule = locationRule;
	}

	public void setParser(Parser parser) {
		this.parser = parser;
	}

	public void setDataFile(SeriesDataFile dataFile) {
		this.dataFile = dataFile;

	}

}