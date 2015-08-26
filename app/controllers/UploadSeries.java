package controllers;

import interactors.SeriesRule;
import interactors.series_data_file.Persister;
import interactors.series_data_file.Validator;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import models.SeriesDataFile;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Http.Request;
import play.mvc.Result;

class UploadSeries extends Controller {

	@Transactional
	public static Result upload(long seriesId) {

		SeriesDataFile dataFile = getFileObject(request());
		String errors = validate(dataFile);

		if (errors.equals("")) {
			int deletedDataSize = deleteExisitingSeriesData(seriesId);
			int createdDataSize = create(dataFile, seriesId);
			return created(makeMsg(deletedDataSize, createdDataSize));
		} else {
			return badRequest(errors);
		}
	}

	private static String makeMsg(int deletedDataSize, int createdDataSize) {
		String msg = deletedDataSize + " existing item(s) deleted." + "\n"
				+ createdDataSize + " new item(s) created.";
		return msg;
	}

	private static int deleteExisitingSeriesData(Long seriesId) {
		return makeSeriesRule().deleteAllSeriesData(seriesId);
	}

	private static SeriesRule makeSeriesRule() {
		return Factory.makeSeriesRule(JPA.em());
	}

	private static int create(SeriesDataFile dataFile, Long seriesId) {
		Persister persister = Factory.makePersister(dataFile);
		return persister.persistSeriesDataFile(seriesId);
	}

	private static String validate(SeriesDataFile dataFile) {
		Validator validator = Factory.makeValidator(dataFile);
		Map<Long, List<String>> errors = validator.validateDataFile();
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

	private static SeriesDataFile getFileObject(Request request) {

		SeriesDataFile dataFile = new SeriesDataFile(request.body()
				.asMultipartFormData().getFiles().get(0).getFile());
		dataFile.setFile(request.body().asMultipartFormData().getFiles().get(0)
				.getFile());

		return dataFile;
	}
}
