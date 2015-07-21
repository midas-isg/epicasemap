package controllers;

import interactors.CSVFile;
import interactors.CSVFilePersister;
import interactors.CSVFileValidator;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Result;

public class UploadSeries extends Controller {

	public static Result upload(long seriesId) {

		CSVFile dataFile = getFileObject(request().body().asMultipartFormData());
		String errors = validate(dataFile);
		if (errors.equals("")) {
			create(dataFile, seriesId);
			return created();
		} else {
			return badRequest(errors);
		}
	}

	private static boolean create(CSVFile dataFile, long seriesId) {
		CSVFilePersister persister = new CSVFilePersister();
		return persister.persistCSVFile(dataFile, seriesId);
	}

	private static String validate(CSVFile dataFile) {
		CSVFileValidator validator = new CSVFileValidator();
		Map<Long, List<String>> errors = validator.getFileErrors(dataFile);
		return joinErrorsAsString(errors);
	}

	private static String joinErrorsAsString(Map<Long, List<String>> errors) {
		StringJoiner sj;
		String stringErrors = "";
		for (Long line : errors.keySet()) {
			sj = new StringJoiner("\n");
			for (String str : errors.get(line)) {
				sj.add(line + ": " + str);
			}
			stringErrors += sj.toString() + "\n";
		}
		return stringErrors;
	}

	private static CSVFile getFileObject(MultipartFormData body) {

		return new CSVFile(body.getFile("csvFile").getFile(),
				body.asFormUrlEncoded());
	}
}
