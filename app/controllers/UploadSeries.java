package controllers;

import interactors.CSVFile;
import interactors.CSVFilePersister;
import interactors.CSVFileValidator;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Http.Request;
import play.mvc.Result;
import views.html.uploadForm;

public class UploadSeries extends Controller {

	public static Result uploadForm() {
		return ok(uploadForm.render());
	}

	@Transactional
	public static Result upload(long seriesId, String delimiter,
			String fileFormat) {

		CSVFile dataFile = getFileObject(request(), delimiter, fileFormat);
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
		Map<Long, List<String>> errors = validator.validate(dataFile);
		return joinErrorsAsString(errors);
	}

	private static String joinErrorsAsString(Map<Long, List<String>> errors) {
		StringJoiner sj;
		String stringErrors = "";
		for (Long line : errors.keySet()) {
			sj = new StringJoiner("\n");
			for (String str : errors.get(line)) {
				sj.add("Line "+line + ": " + str);
			}
			stringErrors += sj.toString() + "\n";
		}
		return stringErrors;
	}

	private static CSVFile getFileObject(Request request, String delimiter,
			String fileFormat) {

		CSVFile csvFile = new CSVFile();
		csvFile.setFile(request.body().asMultipartFormData().getFiles().get(0)
				.getFile());
		csvFile.setDelimiter(delimiter);
		csvFile.setFileFormat(fileFormat);
		return csvFile;

	}
}