package controllers;

import interactors.DelimitedFile;
import static interactors.FileHandler.parsFile;
import static interactors.FileHandler.saveFile;

import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Result;

public class DataSerieUpload extends Controller {

	public static Result upload() {

		if (persist(getFileObject(request().body().asMultipartFormData()))) {
			return ok("File uploaded");
		} else {

			// TODO: return error
			return badRequest();
		}

	}

	private static DelimitedFile getFileObject(MultipartFormData body) {

		return new DelimitedFile(body.getFile("csvFile").getFile(),
				body.asFormUrlEncoded());
	}

	private static boolean persist(DelimitedFile datafile) {
		return saveFile(parsFile(datafile));

	}
}
