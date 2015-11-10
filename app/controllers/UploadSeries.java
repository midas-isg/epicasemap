package controllers;

import interactors.SeriesRule;
import interactors.series_data_file.Persister;
import interactors.series_data_file.SeriesDataFile;
import interactors.series_data_file.Validator;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import javax.persistence.EntityManager;

import models.entities.Series;
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
	
	static Result uploadFile(long seriesId, File file){
		SeriesDataFile dataFile = Factory.makeSeriesDataFile(file);
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
		Result result = null;
		EntityManager emFromTransactionalAnnoation = JPA.em();
		try {
			lockSeries(seriesId, true);
			result = save(seriesId, dataFile);
		} finally {
			lockSeries(seriesId, false);
			JPA.bindForCurrentThread(emFromTransactionalAnnoation);
		}
		return result;
	}

	private static Result save(long seriesId, SeriesDataFile dataFile) {
		Result result = null;
		try {
			result = JPA.withTransaction(() -> {
				Result innerResult = null;

				EntityManager em = JPA.em();
				Validator validator = Factory.makeValidator(dataFile);
				Persister persister = Factory.makePersister(dataFile);
				String errors = validate(validator, dataFile);
				if (errors.equals("")) {
					int deletedDataSize = deleteExisitingSeriesData(em,
							seriesId);
					int createdDataSize = create(persister, seriesId);
					innerResult = created(makeMsg(deletedDataSize,
							createdDataSize));
				} else {
					innerResult = badRequest(errors);
				}
				deleteTempFile(dataFile);
				return innerResult;
			});
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		
		return result;
	}

	private static void lockSeries(long seriesId, boolean lock) {
		JPA.withTransaction(() -> {
			EntityManager em = JPA.em();
			SeriesRule rule = Factory.makeSeriesRule(em);
			Series series = rule.read(seriesId);
			series.setLock(lock);
			rule.update(seriesId, series);
		});
	}

	private static String makeMsg(int deletedDataSize, int createdDataSize) {
		String msg = deletedDataSize + " existing item(s) deleted." + "\n"
				+ createdDataSize + " new item(s) created.";
		return msg;
	}

	private static int deleteExisitingSeriesData(EntityManager em, Long seriesId) {
		return makeSeriesRule(em).deleteAllSeriesData(seriesId);
	}

	private static SeriesRule makeSeriesRule(EntityManager em) {
		return Factory.makeSeriesRule(em);
	}

	private static int create(Persister persister, Long seriesId) {

		return persister.persistSeriesDataFile(seriesId);
	}

	private static String validate(Validator validator, SeriesDataFile dataFile) {
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
