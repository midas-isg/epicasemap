package integrations.server;

import static play.mvc.Http.Status.BAD_REQUEST;
import static play.mvc.Http.Status.CREATED;
import static suites.Helper.assertAreEqual;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import models.SeriesDataFile;
import play.libs.ws.WS;
import play.libs.ws.WSRequestHolder;
import play.libs.ws.WSResponse;

import com.ning.http.client.FluentCaseInsensitiveStringsMap;
import com.ning.http.multipart.FilePart;
import com.ning.http.multipart.MultipartRequestEntity;
import com.ning.http.multipart.Part;

public class UploadSeriesEndpointTester {

	private static final int timeout = 100_000;
	private final String basePath = "/api/series/";
	private final long seriesId = 1000_000L;

	public static Runnable test() {
		return () -> newInstance().testUpload();
	}

	public void testUpload() {

		WSResponse resp = uploadMultiPartFormData();
		assertStatus(resp, CREATED);

		WSResponse response = uploadDataWithAlsIdFormat(seriesId);
		assertStatus(response, CREATED);

		response = uploadDataWithAlsIdFormat(seriesId);
		assertBody(response, "5 existing item(s) deleted.\n"
				+ "5 new item(s) created.");

		response = uploadDataWithCoordinateFormat(seriesId);
		assertStatus(response, CREATED);

		response = uploadDataWithCoordinateFormat(seriesId);
		assertBody(response, "5 existing item(s) deleted.\n"
				+ "5 new item(s) created.");

		response = uploadDataWithAlsIdFormatWithTab(seriesId);
		assertStatus(response, CREATED);

		response = uploadDataWithErrorWithAlsIdFormat(seriesId);
		assertStatus(response, BAD_REQUEST);
		assertBody(response, "Line 1: number of columns is 4. should be 3.\n"
				+ "Line 1: \"lat\" column name is not allowed in alsIdFormat format.\n");
		
		response = uploadDataWithErrorWithCoordinateFormat(seriesId);
		assertStatus(response, BAD_REQUEST);
		assertBody(response, "Line 1: number of columns is 5. should be 4.\n"
				+ "Line 1: \"error\" column name is not allowed in coordinateFormat format.\n");

	}

	private WSResponse uploadMultiPartFormData() {
		String url = buildUrl(seriesId, "%2C", SeriesDataFile.ALS_ID_FORMAT);
		String boundary = "--xyz123--";

		String body = boundary
				+ "\r\n"
				+ "Content-Disposition: form-data; name=\"csv_file\"; filename=\"a.txt\""
				+ "\r\n" + "Content-Type: text/plain" + "\r\n" + "\r\n" +

				"time,als_id,VALUE" + "\r\n" + "2015-01-01,1,1234567" + "\r\n"
				+ "--" + boundary + "--";

		WSRequestHolder requestHolder = WS.url(url);
		requestHolder.setHeader("content-type",
				"multipart/form-data; boundary=" + boundary);
		requestHolder
				.setHeader("content-length", String.valueOf(body.length()));
		WSResponse resp = requestHolder.put(body).get(timeout);
		return resp;
	}

	private WSResponse uploadDataWithAlsIdFormatWithTab(long seriesId) {
		File file = new File(
				"test/resources/input-files/test_alsId_format_tab.txt");
		String url = buildUrl(seriesId, "%09", SeriesDataFile.ALS_ID_FORMAT);
		WSResponse response = sendMultiPartRequest(file, url);
		return response;
	}

	private WSResponse uploadDataWithErrorWithCoordinateFormat(long seriesId)
			throws RuntimeException {
		WSResponse response;

		File file = new File(
				"test/resources/input-files/test_coordinate_format_with_errors.txt");
		String url = buildUrl(seriesId, "%2C", SeriesDataFile.COORDINATE_FORMAT);
		response = sendMultiPartRequest(file, url);
		return response;
	}

	private WSResponse uploadDataWithErrorWithAlsIdFormat(long seriesId)
			throws RuntimeException {
		WSResponse response;
		File file = new File(
				"test/resources/input-files/test_alsId_format_with_errors.txt");
		String url = buildUrl(seriesId, "%2C", SeriesDataFile.ALS_ID_FORMAT);
		response = sendMultiPartRequest(file, url);
		return response;
	}

	private WSResponse uploadDataWithCoordinateFormat(Long seriesId)
			throws RuntimeException {
		WSResponse response;

		File file = new File(
				"test/resources/input-files/test_coordinate_format.txt");
		String url = buildUrl(seriesId, "%2C", SeriesDataFile.COORDINATE_FORMAT);
		response = sendMultiPartRequest(file, url);
		return response;
	}

	private WSResponse uploadDataWithAlsIdFormat(Long seriesId)
			throws RuntimeException {
		File file = new File("test/resources/input-files/test_alsId_format.txt");
		String url = buildUrl(seriesId, "%2C", SeriesDataFile.ALS_ID_FORMAT);
		WSResponse response = sendMultiPartRequest(file, url);
		return response;
	}

	private String buildUrl(long id, String delimiter, String format) {
		String url = Server.makeTestUrl(basePath) + id + "/data?" ;
		url += "delimiter=" + delimiter;
		url += "&format=" + format;
		return url;
	}

	private WSResponse sendMultiPartRequest(File file, String url)
			throws RuntimeException {
		MultipartRequestEntity multiPartReqE = buildMultiPartReqEntity(file);

		InputStream reqIS = multiPartToInputStream(multiPartReqE);
		WSRequestHolder req = WS.url(url).setContentType(
				multiPartReqE.getContentType());
		WSResponse response = req.put(reqIS).get(timeout);
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

	private void assertBody(WSResponse wsResponse, String expected) {
		assertAreEqual(wsResponse.getBody(), expected);
	}

	private static UploadSeriesEndpointTester newInstance() {
		return new UploadSeriesEndpointTester();
	}
}
