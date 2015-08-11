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

public class UploadSeries extends Controller {

	@Transactional
	public static Result upload(long seriesId, String delimiter,
			String fileFormat) throws Exception {
		
		SeriesDataFile dataFile = getFileObject(request(), delimiter, fileFormat);
		String errors = validate(dataFile);

		if (errors.equals("")) {
			int deletedDataSize = deleteExisitingSeriesData(seriesId);
			long createdDataSize = create(dataFile, seriesId);
			return created(makeMsg(deletedDataSize, createdDataSize));
		} else {
			return badRequest(errors);
		}
	}

	private static String makeMsg(int deletedDataSize, long createdDataSize) {
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

	private static Long create(SeriesDataFile dataFile, Long seriesId)
			throws Exception {
		Persister persister = new Persister();
		return persister.persistCSVFile(dataFile, seriesId);
	}

	private static String validate(SeriesDataFile dataFile) {
		Validator validator = new Validator();
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
