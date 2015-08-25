package suites;

import interactors.series_data_file.Parser;
import interactors.series_data_file.Persister;
import interactors.series_data_file.Validator;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import models.SeriesDataFile;
import models.entities.Location;

import org.apache.commons.csv.CSVParser;

import play.db.jpa.JPA;
import controllers.Factory;

public class SeriesDataFileHelper {

	public static CSVParser getCSVParser(SeriesDataFile dataFile) {
		setStdToFileHeaderMap(dataFile);
		Parser fileParser = new Parser();
		return fileParser.parse(dataFile);
	}

	public static SeriesDataFile createTestSeriesDataFileWithAlsIdFormat() {
		File file = new File(
				"test/resources/input-files/test_alsId_format.txt");
		return new SeriesDataFile(file);

	}

	public static SeriesDataFile createTestSeriesDataFileWithCoordianteFormat() {

		File file = new File(
				"test/resources/input-files/test_coordinate_format.txt");
		return new SeriesDataFile(file);
	}

	public static void setStdToFileHeaderMap(SeriesDataFile dataFile) {
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

	public static SeriesDataFile createTestSeriesDataFileWithAlsIdFormatWithErrors() {
		File file = new File(
				"test/resources/input-files/test_alsId_format_with_errors.txt");
		return new SeriesDataFile(file);
	}

	public static SeriesDataFile createTestSeriesDataFileWithCoordinateFormatWithErrors() {
		File file = new File(
				"test/resources/input-files/test_coordinate_format_with_errors.txt");
		return new SeriesDataFile(file);
	}

	public static SeriesDataFile creatDataSeriesFileWithDelimiterError() {
		File file = new File(
				"test/resources/input-files/test_alsId_format_unix_with_delim_error.txt");
		return new SeriesDataFile(file);
	}

	public static SeriesDataFile creatDataSeriesFileWithHeaderError() {
		File file = new File(
				"test/resources/input-files/test_with_header_error.txt");
		return new SeriesDataFile(file);
	}

	public static Persister makePersister() {
		return Factory.makePersister(JPA.em());
	}

	public static Validator makeValidator() {
		return Factory.makeValidator(JPA.em());
	}

	public static Location makeLocation() {
		return new Location();
	}
}
