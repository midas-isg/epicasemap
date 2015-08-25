package suites;

import gateways.database.LocationDao;
import gateways.database.SeriesDao;
import gateways.database.SeriesDataDao;
import interactors.LocationRule;
import interactors.SeriesDataRule;
import interactors.SeriesRule;
import interactors.series_data_file.Parser;
import interactors.series_data_file.Persister;
import interactors.series_data_file.Validator;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import models.SeriesDataFile;

import org.apache.commons.csv.CSVParser;

import play.db.jpa.JPA;

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
		Persister persister = new Persister();
		persister.setLocationRule(makeLocationRule());
		persister.setSeriesRule(makeSeriesRule());
		persister.setSeriesDataRule(makeSeriesDataRule());
		persister.setParser(new Parser());
		return persister;
	}

	private static SeriesDataRule makeSeriesDataRule() {
		SeriesDataDao dao = new SeriesDataDao(JPA.em());
		return new SeriesDataRule(dao);
	}

	private static SeriesRule makeSeriesRule() {
		SeriesDao dao = new SeriesDao(JPA.em());
		return new SeriesRule(dao);
	}

	private static LocationRule makeLocationRule() {
		
		LocationDao dao = new LocationDao(JPA.em());
		return new LocationRule(dao );
	}

	public static Validator makeValidator() {
		Validator validator = new Validator();
		validator.setLocationRule(makeLocationRule());
		validator.setParser(new Parser());
		return validator;
	}
}
