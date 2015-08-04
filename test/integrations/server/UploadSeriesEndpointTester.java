package integrations.server;

import static play.mvc.Http.Status.BAD_REQUEST;
import static play.mvc.Http.Status.CREATED;
import static suites.Helper.assertAreEqual;
import interactors.CSVFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import play.libs.ws.WS;
import play.libs.ws.WSRequestHolder;
import play.libs.ws.WSResponse;

import com.ning.http.client.FluentCaseInsensitiveStringsMap;
import com.ning.http.multipart.FilePart;
import com.ning.http.multipart.MultipartRequestEntity;
import com.ning.http.multipart.Part;

public class UploadSeriesEndpointTester {

	private static final int timeout = 100_000;
	private final String basePath = "/api/fileUpload/";
	private final long seriesId = 123456789;

	public static Runnable test() {
		return () -> newInstance().testUpload();
	}

	public void testUpload() {

		WSResponse resp = postMultiPartFormData();
		assertStatus(resp, CREATED);

		WSResponse response = postDataWithApolloIdFormat();
		assertStatus(response, CREATED);

		response = postDataWithCoordinateFormat();
		assertStatus(response, CREATED);

		response = postDataWithAlsIdFormatWithTab();
		assertStatus(response, CREATED);

		response = postDataWithErrorWithApolloIdFormat();
		assertStatus(response, BAD_REQUEST);

		response = postDataWithErrorWithCoordinateFormat();
		assertStatus(response, BAD_REQUEST);

	}

	private WSResponse postMultiPartFormData() {
		String url = buildUrl(seriesId, "%2C", CSVFile.ALS_ID_FORMAT);
		String boundary = "--xyz123--";
		
		String body = boundary + "\r\n" + 
		"Content-Disposition: form-data; name=\"csv_file\"; filename=\"a.txt\"" + "\r\n" +
		"Content-Type: text/plain" + "\r\n" + "\r\n" + 
		
		"time,als_id,VALUE" + "\r\n" +
		"2015-01-01,1,1234567" + "\r\n" + 
		"--" + boundary + "--";
		
		WSRequestHolder requestHolder = WS.url(url);
		requestHolder.setHeader("content-type",
				"multipart/form-data; boundary=" + boundary);
		requestHolder.setHeader("content-length",
				String.valueOf(body.length()));
		WSResponse resp = requestHolder.post(body).get(timeout);
		return resp;
	}

	private WSResponse postDataWithAlsIdFormatWithTab() {
		File file = new File("test/resources/test_alsId_format_tab.txt");
		String url = buildUrl(seriesId, "%09", CSVFile.ALS_ID_FORMAT);
		WSResponse response = postMultiPartRequest(file, url);
		return response;
	}

	private WSResponse postDataWithErrorWithCoordinateFormat()
			throws RuntimeException {
		WSResponse response;

		File file = new File(
				"test/resources/test_coordinate_format_with_errors.txt");
		String url = buildUrl(seriesId, "%2C", CSVFile.COORDINATE_FORMAT);
		response = postMultiPartRequest(file, url);
		return response;
	}

	private WSResponse postDataWithErrorWithApolloIdFormat()
			throws RuntimeException {
		WSResponse response;
		File file = new File(
				"test/resources/test_alsId_format_with_errors.txt");
		String url = buildUrl(seriesId, "%2C", CSVFile.ALS_ID_FORMAT);
		response = postMultiPartRequest(file, url);
		return response;
	}

	private WSResponse postDataWithCoordinateFormat() throws RuntimeException {
		WSResponse response;

		File file = new File("test/resources/test_coordinate_format.txt");
		String url = buildUrl(seriesId, "%2C", CSVFile.COORDINATE_FORMAT);
		response = postMultiPartRequest(file, url);
		return response;
	}

	private WSResponse postDataWithApolloIdFormat() throws RuntimeException {
		File file = new File("test/resources/test_alsId_format.txt");
		String url = buildUrl(seriesId, "%2C", CSVFile.ALS_ID_FORMAT);
		WSResponse response = postMultiPartRequest(file, url);
		return response;
	}

	private String buildUrl(long id, String delimiter, String format) {
		String url = Server.makeTestUrl(basePath) + id + "/" + delimiter + "/"
				+ format;
		return url;
	}

	private WSResponse postMultiPartRequest(File file, String url)
			throws RuntimeException {
		MultipartRequestEntity multiPartReqE = buildMultiPartReqEntity(file);

		InputStream reqIS = multiPartToInputStream(multiPartReqE);
		WSRequestHolder req = WS.url(url).setContentType(
				multiPartReqE.getContentType());
		// req.post(reqIS).map(...);
		WSResponse response = req.post(reqIS).get(timeout);
		return response;
	}

	private InputStream multiPartToInputStream(
			MultipartRequestEntity multiPartReqE) throws RuntimeException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		try {
			multiPartReqE.writeRequest(bos);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		InputStream reqIS = new ByteArrayInputStream(bos.toByteArray());
		return reqIS;
	}

	private MultipartRequestEntity buildMultiPartReqEntity(File file)
			throws RuntimeException {
		List<Part> parts = new ArrayList<Part>();
		try {
			parts.add(new FilePart("file", file));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}

		Part[] partsA = parts.toArray(new Part[parts.size()]);
		FluentCaseInsensitiveStringsMap requestHeaders = new FluentCaseInsensitiveStringsMap();
		requestHeaders.add("Content-Type", "");
		MultipartRequestEntity reqE = new MultipartRequestEntity(partsA,
				requestHeaders);
		return reqE;
	}

	private void assertStatus(WSResponse wsResponse, int expected) {
		assertAreEqual(wsResponse.getStatus(), expected);
	}

	private static UploadSeriesEndpointTester newInstance() {
		return new UploadSeriesEndpointTester();
	}
}
