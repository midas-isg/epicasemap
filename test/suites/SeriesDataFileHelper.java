package suites;

import interactors.SeriesRule;
import interactors.series_data_file.Parser;
import interactors.series_data_file.Persister;
import interactors.series_data_file.SeriesDataFile;
import interactors.series_data_file.Validator;

import java.io.File;

import play.db.jpa.JPA;
import models.entities.Location;
import controllers.Factory;

public class SeriesDataFileHelper {

	public static Parser getParser(SeriesDataFile dataFile) {
		return new Parser(dataFile);
		
	}

	public static SeriesDataFile createTestSeriesDataFileWithAlsIdFormat() {
		File file = new File(
				"public/input/series-data/examples/test_alsId_format.txt");
		return new SeriesDataFile(file);

	}

	public static SeriesDataFile createTestSeriesDataFileWithCoordianteFormat() {

		File file = new File(
				"public/input/series-data/examples/test_coordinate_format.txt");
		return new SeriesDataFile(file);
	}

	public static SeriesDataFile createTestSeriesDataFileWithAlsIdFormatWithErrors() {
		File file = new File(
				"public/input/series-data/test/test_alsId_format_with_errors.txt");
		return new SeriesDataFile(file);
	}

	public static SeriesDataFile createTestSeriesDataFileWithCoordinateFormatWithErrors() {
		File file = new File(
				"public/input/series-data/test/test_coordinate_format_with_errors.txt");
		return new SeriesDataFile(file);
	}

	public static SeriesDataFile creatDataSeriesFileWithDelimiterError() {
		File file = new File(
				"public/input/series-data/test/test_alsId_format_unix_with_delim_error.txt");
		return new SeriesDataFile(file);
	}

	public static SeriesDataFile creatDataSeriesFileWithHeaderError() {
		File file = new File(
				"public/input/series-data/test/test_with_header_error.txt");
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

	public static SeriesDataFile createTestSeriesDataFileWithTab() {
		File file = new File("public/input/series-data/test/test_alsId_format_tab.txt");
		return new SeriesDataFile(file);
	}

	public static SeriesDataFile createTestSeriesDataFileWithUnixLineEnding() {
		File file = new File("public/input/series-data/test/test_alsId_format_unix_line_ending.txt");
		return new SeriesDataFile(file);
	}
	
	public static SeriesRule makeSeriesRule(){
		return Factory.makeSeriesRule(JPA.em());
	}
}
