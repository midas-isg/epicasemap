package controllers;

import interactors.SeriesDataFile;
import interactors.SeriesDataFilePersister;
import interactors.SeriesDataFileValidator;
import interactors.CoordinateRule;
import interactors.SeriesDataRule;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import models.entities.Coordinate;
import models.entities.CoordinateFilter;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Http.Request;
import play.mvc.Result;

public class UploadSeries extends Controller {

	@Transactional
	public static Result upload(long seriesId, String delimiter,
			String fileFormat) throws Exception {

		Long deletedDataSize = deleteExisitingSeriesData(seriesId);
		SeriesDataFile dataFile = getFileObject(request(), delimiter, fileFormat);
		String errors = validate(dataFile);

		if (errors.equals("")) {
			Long createdDataSize = create(dataFile, seriesId);
			return created(makeMsg(deletedDataSize, createdDataSize));
		} else {
			return badRequest(errors);
		}
	}

	private static String makeMsg(Long deletedDataSize, Long createdDataSize) {
		String msg = deletedDataSize + " existing item(s) deleted." + "\n"
				+ createdDataSize + " new item(s) created.";
		return msg;
	}

	private static Long deleteExisitingSeriesData(Long seriesId) {
		CoordinateFilter filter = buildSeriesDataFilter(seriesId);
		List<Coordinate> seriesData = makeCoordinateRule().query(filter);
		for (Coordinate data : seriesData) {
			makeSeriesDataRule().delete(data.getId());
		}
		return (long) seriesData.size();
	}

	private static SeriesDataRule makeSeriesDataRule() {
		return Factory.makeSeriesDataRule(JPA.em());
	}

	private static CoordinateFilter buildSeriesDataFilter(Long seriesId) {

		CoordinateFilter filter = new CoordinateFilter();
		filter.setSeriesId(seriesId);
		filter.setOffset(0);
		return filter;

	}

	private static CoordinateRule makeCoordinateRule() {
		return Factory.makeCoordinateRule(JPA.em());
	}

	private static Long create(SeriesDataFile dataFile, Long seriesId)
			throws Exception {
		SeriesDataFilePersister persister = new SeriesDataFilePersister();
		return persister.persistCSVFile(dataFile, seriesId);
	}

	private static String validate(SeriesDataFile dataFile) {
		SeriesDataFileValidator validator = new SeriesDataFileValidator();
		Map<Long, List<String>> errors = validator.validate(dataFile);
		return joinErrorsAsString(errors);
	}

	private static String joinErrorsAsString(Map<Long, List<String>> errors) {
		StringJoiner sj;
		String stringErrors = "";
		for (Long line : errors.keySet()) {
			sj = new StringJoiner("\n");
			for (String str : errors.get(line)) {
				sj.add("Line " + line + ": " + str);
			}
			stringErrors += sj.toString() + "\n";
		}
		return stringErrors;
	}

	private static SeriesDataFile getFileObject(Request request, String delimiter,
			String fileFormat) {

		SeriesDataFile csvFile = new SeriesDataFile();
		csvFile.setFile(request.body().asMultipartFormData().getFiles().get(0)
				.getFile());
		csvFile.setDelimiter(delimiter);
		csvFile.setFileFormat(fileFormat);
		return csvFile;

	}
}
