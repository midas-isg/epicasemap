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

		//return parser.iterator().next();
		return parser;

	}

	public SeriesDataFile createTestSeriesDataFileWithAlsIdFormat() {
		File csvFile = new File(
				"test/resources/input-files/test_alsId_format.txt");
		String fileFormat = SeriesDataFile.ALS_ID_FORMAT;
		String delimiter = ",";
		SeriesDataFile dataFile = creatSeriesDataFile(csvFile, fileFormat,
				delimiter);
		return dataFile;
	}

	private SeriesDataFile creatSeriesDataFile(File csvFile, String fileFormat,
			String delimiter) {
		SeriesDataFile dataFile = new SeriesDataFile();
		dataFile.setFile(csvFile);
		dataFile.setDelimiter(delimiter);
		dataFile.setFileFormat(fileFormat);
		return dataFile;
	}

	public SeriesDataFile createTestSeriesDataFileWithCoordianteFormat() {

		File csvFile = new File(
				"test/resources/input-files/test_coordinate_format.txt");
		String fileFormat = SeriesDataFile.COORDINATE_FORMAT;
		String delimiter = ",";
		SeriesDataFile dataFile = creatSeriesDataFile(csvFile, fileFormat,
				delimiter);
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
		File csvFile = new File(
				"test/resources/input-files/test_alsId_format_with_errors.txt");
		String fileFormat = SeriesDataFile.ALS_ID_FORMAT;
		String delimiter = ",";
		SeriesDataFile dataFile = creatSeriesDataFile(csvFile, fileFormat,
				delimiter);
		return dataFile;
	}

	public SeriesDataFile createTestSeriesDataFileWithCoordinateFormatWithErrors() {
		File csvFile = new File(
				"test/resources/input-files/test_coordinate_format_with_errors.txt");
		String fileFormat = SeriesDataFile.COORDINATE_FORMAT;
		String delimiter = ",";
		SeriesDataFile dataFile = creatSeriesDataFile(csvFile, fileFormat,
				delimiter);
		return dataFile;
	}
}
