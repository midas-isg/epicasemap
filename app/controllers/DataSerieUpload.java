package controllers;

import interactors.CSVFilePersister;
import interactors.CSVFileValidator;
import interactors.CSVFile;

import java.util.ArrayList;

import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Result;

public class DataSerieUpload extends Controller {

	public static Result upload() {

		CSVFile dataFile = getFileObject(request().body()
				.asMultipartFormData());
		ArrayList<String> errorMsgList = CSVFileValidator.getFileErrors(dataFile);
		if (errorMsgList.size() == 0) {
			if (CSVFilePersister.persistCSVFile(dataFile)) { // TODO:
																	// should
																	// return
																	// msg
				return ok("File uploaded");
			} else {
				// TODO: error msg
				return badRequest();
			}

		} else {

			// TODO: return error
			return badRequest();
		}

	}

	private static CSVFile getFileObject(MultipartFormData body) {

		return new CSVFile(body.getFile("csvFile").getFile(),
				body.asFormUrlEncoded());
	}
}
