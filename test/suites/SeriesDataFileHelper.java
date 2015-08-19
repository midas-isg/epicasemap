package suites;

import interactors.series_data_file.Parser;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import models.SeriesDataFile;

import org.apache.commons.csv.CSVParser;

public class SeriesDataFileHelper {

	public CSVParser getCSVParser(SeriesDataFile dataFile) throws Exception {
		SeriesDataFileHelper helper = new SeriesDataFileHelper();
		helper.setStdToFileHeaderMap(dataFile);
		Parser fileParser = new Parser();
		CSVParser parser = null;
		parser = fileParser.parse(dataFile);
		return parser;

	}

	public SeriesDataFile createTestSeriesDataFileWithAlsIdFormat() {
		File file = new File(
				"test/resources/input-files/test_alsId_format.txt");
		SeriesDataFile dataFile = creatSeriesDataFile(file);
		return dataFile;
	}

	private SeriesDataFile creatSeriesDataFile(File file) {
		SeriesDataFile dataFile = new SeriesDataFile(file);
		return dataFile;
	}

	public SeriesDataFile createTestSeriesDataFileWithCoordianteFormat() {

		File file = new File(
				"test/resources/input-files/test_coordinate_format.txt");
		SeriesDataFile dataFile = creatSeriesDataFile(file);
		return dataFile;
	}

	public void setStdToFileHeaderMap(SeriesDataFile dataFile) throws Exception {
		Map<String, String> result = new HashMap<String, String>();
		Parser csvParser = new Parser();
		CSVParser parser = null;
		parser = csvParser.parse(dataFile);

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

	public SeriesDataFile createTestSeriesDataFileWithAlsIdFormatWithErrors() {
		File file = new File(
				"test/resources/input-files/test_alsId_format_with_errors.txt");
		SeriesDataFile dataFile = creatSeriesDataFile(file);
		return dataFile;
	}

	public SeriesDataFile createTestSeriesDataFileWithCoordinateFormatWithErrors() {
		File file = new File(
				"test/resources/input-files/test_coordinate_format_with_errors.txt");
		SeriesDataFile dataFile = creatSeriesDataFile(file);
		return dataFile;
	}

	public SeriesDataFile creatDataSeriesFileWithDelimiterError() {
		File file = new File(
				"test/resources/input-files/test_alsId_format_unix_with_delim_error.txt");
		SeriesDataFile dataFile = creatSeriesDataFile(file);
		return dataFile;
	}

	public SeriesDataFile creatDataSeriesFileWithHeaderError() {
		File file = new File(
				"test/resources/input-files/test_with_header_error.txt");
		SeriesDataFile dataFile = creatSeriesDataFile(file);
		return dataFile;
	}
}
