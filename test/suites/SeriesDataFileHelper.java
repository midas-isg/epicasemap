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
import controllers.Factory;

public class SeriesDataFileHelper {

	public static Parser getParser(SeriesDataFile dataFile) {
		setStdToFileHeaderMap(dataFile);
		return new Parser(dataFile);
		
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
		Parser parser = new Parser(dataFile);

		Set<String> fileHeaderSet = parser.getFileHeaders();
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

	public static Persister makePersister(SeriesDataFile dataFile) {
		return Factory.makePersister(dataFile);
	}

	public static Validator makeValidator(SeriesDataFile dataFile) {
		return Factory.makeValidator(dataFile);
	}

	public static Location makeLocation() {
		return new Location();
	}
}
