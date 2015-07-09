package controllers;

import static interactors.FileHandler.persist;
import interactors.DelimitedFile;
import interactors.FileHandler;

import java.util.ArrayList;

import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Result;

public class DataSerieUpload extends Controller {

	public static Result upload() {

		DelimitedFile dataFile = getFileObject(request().body()
				.asMultipartFormData());
		ArrayList<String> errorMsgList = FileHandler.getFileErrors(dataFile);
		if (errorMsgList.size() == 0) {
			if (persist(dataFile)) { // TODO: should return msg
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
