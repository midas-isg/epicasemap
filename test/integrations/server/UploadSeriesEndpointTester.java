package integrations.server;

import static play.mvc.Http.Status.BAD_REQUEST;
import static play.mvc.Http.Status.CONFLICT;
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

import javax.persistence.EntityManager;

import com.ning.http.client.FluentCaseInsensitiveStringsMap;
import com.ning.http.multipart.FilePart;
import com.ning.http.multipart.MultipartRequestEntity;
import com.ning.http.multipart.Part;

import controllers.Factory;
import gateways.database.AccountDao;
import gateways.database.SeriesDao;
import interactors.SeriesRule;
import models.entities.Series;
import play.db.jpa.JPA;
import play.libs.ws.WS;
import play.libs.ws.WSRequestHolder;
import play.libs.ws.WSResponse;

public class UploadSeriesEndpointTester {

	private static final int timeout = 100_000;
	private final String basePath = "/api/series/";
	private long seriesId;
	private String alsIdFormatURL = "https://pitt.box.com/shared/static/y6e8o6gg2a9s1q5bpd2dnznfa83qk5bm.txt";

	public static Runnable test() {
		return () -> newInstance().testUpload();
	}

	public void testUpload() {

		seriesId = createSeries();
		try {
			testUploadViaUrl();
			testUploadViaMultiPartForm();
			testSeriesLockOnUpload();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			deleteSeries(seriesId);
		}
	}

	private long createSeries() {
		long id;
		Series data = new Series();
		data.setTitle("test series");
		try {
			id = JPA.withTransaction(() -> {
				EntityManager em = JPA.em();
				AccountDao accountDao = new AccountDao(em);
				data.setOwner(accountDao.read(1));
				SeriesDao dao = new SeriesDao(em);
				return new SeriesRule(dao).create(data);
			});
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		return id;
	}

	private void deleteSeries(long id) {
		try {
			JPA.withTransaction(() -> {
				EntityManager em = JPA.em();
				SeriesRule rule = Factory.makeSeriesRule(em);
				rule.delete(id);
			});
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	private void testSeriesLockOnUpload() {
		setSeriesLock(seriesId, true);
		uploadDataWithAlsIdFormat(seriesId);
		Series series = readSeries(seriesId);
		assertAreEqual(series.getLock(), false);

		setSeriesLock(seriesId, true);
		uploadAlsIdFormatWithURL(seriesId, true);
		series = readSeries(seriesId);
		assertAreEqual(series.getLock(), false);
	}

	private Series readSeries(long id) {
		Series series = null;
		try {
			series = JPA.withTransaction(() -> {
				SeriesRule rule = suites.SeriesDataFileHelper.makeSeriesRule();
				return rule.read(id);
			});
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		return series;
	}

	private void setSeriesLock(long id, boolean lock) {
		JPA.withTransaction(() -> {
			SeriesRule rule = suites.SeriesDataFileHelper.makeSeriesRule();
			Series series = rule.read(id);
			series.setLock(lock);
			rule.update(id, series);
		});
	}

	private void testUploadViaMultiPartForm() throws RuntimeException {
		WSResponse resp = uploadMultiPartFormData();
		assertStatus(resp, CREATED);

		WSResponse response = uploadDataWithAlsIdFormat(seriesId);
		assertStatus(response, CREATED);

		response = uploadDataWithAlsIdFormat(seriesId);
		assertBody(response, "5 existing item(s) deleted.\n" + "5 new item(s) created.");

		response = uploadDataWithCoordinateFormat(seriesId);
		assertStatus(response, CREATED);

		response = uploadDataWithCoordinateFormat(seriesId);
		assertBody(response, "5 existing item(s) deleted.\n" + "5 new item(s) created.");

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

	private void testUploadViaUrl() {
		WSResponse resp = uploadAlsIdFormatWithURL(seriesId, true);
		assertStatus(resp, CREATED);

		resp = uploadAlsIdFormatWithURL(seriesId, false);
		assertStatus(resp, CONFLICT);

		resp = uploadAlsIdFormatWithURL(seriesId, true);
		assertBody(resp, "5 existing item(s) deleted.\n" + "5 new item(s) created.");
	}

	private WSResponse uploadAlsIdFormatWithURL(long seriesId, boolean overWrite) {

		String body = "url=" + alsIdFormatURL;
		String url = buildUrl(seriesId, overWrite);
		WSRequestHolder req = WS.url(url).setContentType("application/x-www-form-urlencoded");
		WSResponse response = req.put(body).get(timeout);
		return response;
	}

	private String buildUrl(long seriesId, boolean overWrite) {
		String url = Server.makeTestUrl(basePath) + seriesId + "/data-url?overWrite=" + overWrite;
		return url;
	}

	private WSResponse uploadMultiPartFormData() {
		String url = buildUrl(seriesId);
		String boundary = "--xyz123--";

		String body = boundary + "\r\n" + "Content-Disposition: form-data; name=\"csv_file\"; filename=\"a.txt\""
				+ "\r\n" + "Content-Type: text/plain" + "\r\n" + "\r\n" +

				"time,als_id,VALUE" + "\r\n" + "2015-01-01,1,1234567" + "\r\n" + "--" + boundary + "--";

		WSRequestHolder requestHolder = WS.url(url);
		requestHolder.setHeader("content-type", "multipart/form-data; boundary=" + boundary);
		requestHolder.setHeader("content-length", String.valueOf(body.length()));
		WSResponse resp = requestHolder.put(body).get(timeout);
		return resp;
	}

	private WSResponse uploadDataWithAlsIdFormatWithTab(long seriesId) {
		File file = new File("public/input/series-data/test/test_alsId_format_tab.txt");
		String url = buildUrl(seriesId);
		WSResponse response = sendMultiPartRequest(file, url);
		return response;
	}

	private WSResponse uploadDataWithErrorWithCoordinateFormat(long seriesId) throws RuntimeException {
		WSResponse response;

		File file = new File("public/input/series-data/test/test_coordinate_format_with_errors.txt");
		String url = buildUrl(seriesId);
		response = sendMultiPartRequest(file, url);
		return response;
	}

	private WSResponse uploadDataWithErrorWithAlsIdFormat(long seriesId) throws RuntimeException {
		WSResponse response;
		File file = new File("public/input/series-data/test/test_alsId_format_with_errors.txt");
		String url = buildUrl(seriesId);
		response = sendMultiPartRequest(file, url);
		return response;
	}

	private WSResponse uploadDataWithCoordinateFormat(Long seriesId) throws RuntimeException {
		WSResponse response;

		File file = new File("public/input/series-data/examples/test_coordinate_format.txt");
		String url = buildUrl(seriesId);
		response = sendMultiPartRequest(file, url);
		return response;
	}

	private WSResponse uploadDataWithAlsIdFormat(Long seriesId) throws RuntimeException {

		File file = new File("public/input/series-data/examples/test_alsId_format.txt");
		// File file = new
		// File("public/input/series-data/examples/PERTUSSIS_Cases_1938-2011.ssv");
		String url = buildUrl(seriesId);
		WSResponse response = sendMultiPartRequest(file, url);
		return response;
	}

	private String buildUrl(long id) {
		String url = Server.makeTestUrl(basePath) + id + "/data";
		return url;
	}

	private WSResponse sendMultiPartRequest(File file, String url) throws RuntimeException {
		MultipartRequestEntity multiPartReqE = buildMultiPartReqEntity(file);

		InputStream reqIS = multiPartToInputStream(multiPartReqE);
		WSRequestHolder req = WS.url(url).setContentType(multiPartReqE.getContentType());
		WSResponse response = req.put(reqIS).get(timeout);
		return response;
	}

	private InputStream multiPartToInputStream(MultipartRequestEntity multiPartReqE) throws RuntimeException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		try {
			multiPartReqE.writeRequest(bos);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		InputStream reqIS = new ByteArrayInputStream(bos.toByteArray());
		return reqIS;
	}

	private MultipartRequestEntity buildMultiPartReqEntity(File file) throws RuntimeException {
		List<Part> parts = new ArrayList<Part>();
		try {
			parts.add(new FilePart("file", file));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}

		Part[] partsA = parts.toArray(new Part[parts.size()]);
		FluentCaseInsensitiveStringsMap requestHeaders = new FluentCaseInsensitiveStringsMap();
		requestHeaders.add("Content-Type", "");
		MultipartRequestEntity reqE = new MultipartRequestEntity(partsA, requestHeaders);
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
