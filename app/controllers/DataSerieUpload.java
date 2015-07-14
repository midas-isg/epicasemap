package controllers;

import interactors.CSVFilePersister;
import interactors.CSVFileValidator;
import interactors.DelimitedFile;

import java.util.ArrayList;

import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Result;

public class DataSerieUpload extends Controller {

	public static Result upload() {

		DelimitedFile dataFile = getFileObject(request().body()
				.asMultipartFormData());
		ArrayList<String> errorMsgList = CSVFileValidator.getFileErrors(dataFile);
		if (errorMsgList.size() == 0) {
			if (CSVFilePersister.persistDelimitedFile(dataFile)) { // TODO:
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

	private static DelimitedFile getFileObject(MultipartFormData body) {

		return new DelimitedFile(body.getFile("csvFile").getFile(),
				body.asFormUrlEncoded());
	}
}
