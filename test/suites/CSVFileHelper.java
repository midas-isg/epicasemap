package suites;

import interactors.CSVFile;
import interactors.CSVFileParser;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class CSVFileHelper {

	public CSVRecord getCSVRecord(CSVFile dataFile) throws Exception {
		CSVFileHelper helper = new CSVFileHelper();
		helper.setStdToFileHeaderMap(dataFile);
		CSVFileParser fileParser = new CSVFileParser();
		CSVParser parser = null;
		parser = fileParser.parse(dataFile);

		return parser.iterator().next();
	}

	public CSVFile createTestDataFileWithAlsIdFormat() {
		File csvFile = new File("test/resources/test_alsId_format.txt");
		String fileFormat = CSVFile.ALS_ID_FORMAT;
		String delimiter = ",";
		CSVFile dataFile = creatDataFile(csvFile, fileFormat, delimiter);
		return dataFile;
	}

	private CSVFile creatDataFile(File csvFile, String fileFormat,
			String delimiter) {
		CSVFile dataFile = new CSVFile();
		dataFile.setFile(csvFile);
		dataFile.setDelimiter(delimiter);
		dataFile.setFileFormat(fileFormat);
		return dataFile;
	}

	public CSVFile createTestDataFileWithCoordianteFormat() {

		File csvFile = new File("test/resources/test_coordinate_format.txt");
		String fileFormat = CSVFile.COORDINATE_FORMAT;
		String delimiter = ",";
		CSVFile dataFile = creatDataFile(csvFile, fileFormat, delimiter);
		return dataFile;
	}

	public void setStdToFileHeaderMap(CSVFile dataFile) throws Exception {
		Map<String, String> result = new HashMap<String, String>();
		CSVFileParser csvParser = new CSVFileParser();
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

	public CSVFile createTestDataFileWithAlsIdFormatWithErrors() {
		File csvFile = new File(
				"test/resources/test_alsId_format_with_errors.txt");
		String fileFormat = CSVFile.ALS_ID_FORMAT;
		String delimiter = ",";
		CSVFile dataFile = creatDataFile(csvFile, fileFormat, delimiter);
		return dataFile;
	}

	public CSVFile createTestDataFileWithCoordinateFormatWithErrors() {
		File csvFile = new File(
				"test/resources/test_coordinate_format_with_errors.txt");
		String fileFormat = CSVFile.COORDINATE_FORMAT;
		String delimiter = ",";
		CSVFile dataFile = creatDataFile(csvFile, fileFormat, delimiter);
		return dataFile;
	}
}
