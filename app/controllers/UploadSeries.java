package controllers;

import interactors.SeriesRule;
import interactors.series_data_file.Persister;
import interactors.series_data_file.SeriesDataFile;
import interactors.series_data_file.Validator;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import models.entities.SeriesDataUrl;
import play.db.jpa.JPA;
import play.mvc.Controller;
import play.mvc.Http.Request;
import play.mvc.Result;

class UploadSeries extends Controller {
	static Result upload(long seriesId) {
		SeriesDataFile dataFile = getSeriesDataFile(request());
		return uploadSeriesData(seriesId, dataFile);
	}
	
	static Result uploadViaUrl(long seriesId, String url, boolean overWrite) {
		SeriesDataFile dataFile = getSeriesDataFile(url);
		if (overWrite || !checksumMatches(seriesId, url, dataFile.getChecksum()))
			return uploadSeriesData(seriesId, dataFile);
		else
			return status(CONFLICT, "url content seems unchanged. Use overWrite parameter to re-write data.");
	}

	private static Result uploadSeriesData(long seriesId,
			SeriesDataFile dataFile) {
		String errors = validate(dataFile);
		Result result;
		if (errors.equals("")) {
			int deletedDataSize = deleteExisitingSeriesData(seriesId);
			int createdDataSize = create(dataFile, seriesId);
			result = created(makeMsg(deletedDataSize, createdDataSize));
		} else {
			result = badRequest(errors);
		}
		deleteTempFile(dataFile);
		return result;
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

	private static SeriesDataFile getSeriesDataFile(Request request) {
		
		SeriesDataFile dataFile = Factory.makeSeriesDataFile(request);
		return dataFile;
	}
	
	private static SeriesDataFile getSeriesDataFile(String url) {
		return Factory.makeSeriesDataFile(url);
	}
	
	private static void deleteTempFile(SeriesDataFile dataFile) {
		dataFile.deleteFile();	
	}

	private static boolean checksumMatches(long seriesId, String url, String checksum) {
		List<SeriesDataUrl> result = Factory.makeSeriesDataUrlRule(JPA.em())
				.query(seriesId, url, checksum);
		return result.size() > 0;
	}
}
