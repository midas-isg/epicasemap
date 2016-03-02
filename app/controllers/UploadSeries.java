package controllers;

import static gateways.configuration.AppKey.APP_NAME_IN_DB;
import interactors.ConfRule;
import interactors.SeriesRule;
import interactors.series_data_file.Persister;
import interactors.series_data_file.SeriesDataFile;
import interactors.series_data_file.Validator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.xml.bind.JAXBException;

import com.fasterxml.jackson.databind.JsonNode;

import lsparser.xmlparser.TychoParser;
import models.entities.NamedLocation;
import models.entities.Series;
import models.entities.SeriesDataUrl;
import models.exceptions.NoConnectionAvailable;
import play.Logger;
import play.db.jpa.JPA;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http.Request;
import play.mvc.Result;
import play.mvc.Results;
import scala.util.parsing.json.JSON;

class UploadSeries extends Controller {

	private static int minRequiredConnections = 2;

	static Result upload(long seriesId) {
		SeriesDataFile dataFile = getSeriesDataFile(request());
		return uploadSeriesData(seriesId, dataFile);
	}

	static Result uploadFile(long seriesId, File file) {
		SeriesDataFile dataFile = Factory.makeSeriesDataFile(file);
		return uploadSeriesData(seriesId, dataFile);
	}

	static Result uploadViaUrl(long seriesId, String url, boolean overWrite) {
		SeriesDataFile dataFile = getSeriesDataFile(url);
		if (overWrite
				|| !checksumMatches(seriesId, url, dataFile.getChecksum()))
			return uploadSeriesData(seriesId, dataFile);
		else
			return status(CONFLICT,
					"url content seems unchanged. Use overWrite parameter to re-write data.");
	}
	
	static Result uploadTychoViaUrl(long seriesId, String url, boolean overWrite) {
		SeriesDataFile dataFile = getSeriesDataFile(url);
		//parse as tycho file
		TychoParser tychoParser = new TychoParser();
		Map<String, TychoParser.BulkLocationResult> ambiguities;
		
		try {
			tychoParser.unmarshal(lsparser.tycho.Result.class, new FileInputStream(dataFile.getFile()));
			
			//ambiguities = tychoParser.asynchronizedGetALSIDs();
			ambiguities = tychoParser.bulkGetALSIDs();
			
			if(ambiguities.size() > 0) {
				overWrite = false;
				
				//convert to JSON
				return Results.status(MULTIPLE_CHOICES, Json.toJson(ambiguities));
			}
			
			return Results.noContent();
			//else if(){ return notFound(Json.toJson(ambiguities)); }
			/*else {
				overWrite = true;
				//convert to csv
			}
			*/
		}
		catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		//if new or overwrite, return ambiguities list to user
		/*
		if(overWrite
				|| !checksumMatches(seriesId, url, dataFile.getChecksum()))
			return uploadSeriesData(seriesId, dataFile);
		else
			return status(CONFLICT,
					"url content seems unchanged. Use overWrite parameter to re-write data.");
		*/
	}
	
	static Result uploadTychoJSON(long seriesId, JsonNode jsonMappings, boolean overWrite) {
		String url = null;
		Path tempDataFile;
		BufferedWriter bufferedWriter;
		SeriesDataFile seriesDataFile = null;

		try {
			//convert to csv
Logger.debug("\n=== Json Mapping Data ===");
Logger.debug(jsonMappings.get("url").asText());
Logger.debug(String.valueOf(seriesId));
			tempDataFile = Files.createTempFile(jsonMappings.get("seriesID").asText(), ".txt");
			bufferedWriter = Files.newBufferedWriter(tempDataFile);
			
			bufferedWriter.write("time,als_id,VALUE\n");
			
			JsonNode selectedMappings = jsonMappings.get("selectedMappings");
			JsonNode currentNode;
			Iterator<String> fieldNamesIterator = selectedMappings.fieldNames();
			Iterator<JsonNode> occurrencesIterator;
			JsonNode occurrenceNode;
			String fieldName;
			String currentLine;
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			
			while(fieldNamesIterator.hasNext()) {
				fieldName = fieldNamesIterator.next();
				currentNode = selectedMappings.get(fieldName);
				
				occurrencesIterator = currentNode.get("occurrences").elements();
				
				while(occurrencesIterator.hasNext()) {
					occurrenceNode = occurrencesIterator.next();
					currentLine = formatter.format(new Date(occurrenceNode.get("date").asLong()));
					currentLine += ",";
					
					if((currentNode.get("requeryResults") != null) &&
						(!currentNode.get("requeryResults").get("selectedLocationID").isNull())) {
						currentLine += currentNode.get("requeryResults").get("selectedLocationID").asText();
					}
					else {
						if(currentNode.has("selectedLocationID")) {
							currentLine += currentNode.get("selectedLocationID").asText();
						}
					}
					
					currentLine += "," + occurrenceNode.get("number") + "\n";
					bufferedWriter.write(currentLine);
				}
			}
			
			bufferedWriter.close();
			seriesDataFile = new SeriesDataFile(tempDataFile.toFile());
//overWrite = false;
		}
		catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
		if(overWrite || !checksumMatches(seriesId, url, seriesDataFile.getChecksum())) {
Logger.debug("File created: time to save");
			return uploadSeriesData(seriesId, seriesDataFile);
		}
		
		return status(CONFLICT,
			"url content seems unchanged. Use overWrite parameter to re-write data.");
	}
	
	private static Result uploadSeriesData(long seriesId, SeriesDataFile dataFile) {
		Result result = null;
		EntityManager emFromTransactionalAnnoation = JPA.em();
Logger.debug("checking for connection...");
		checkConnectionAvailability(emFromTransactionalAnnoation);
Logger.debug("connection available");
		
		try {
			lockSeries(seriesId, true);
			result = save(seriesId, dataFile);
		} finally {
			lockSeries(seriesId, false);
			JPA.bindForCurrentThread(emFromTransactionalAnnoation);
			deleteTempFile(dataFile);
		}
Logger.debug(result.toString());
		
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

	private static boolean checksumMatches(long seriesId, String url,
			String checksum) {
		List<SeriesDataUrl> result = Factory.makeSeriesDataUrlRule(JPA.em())
				.query(seriesId, url, checksum);
		return result.size() > 0;
	}

	private static void checkConnectionAvailability(EntityManager em)
			throws NoConnectionAvailable {
		if (availableConnections(em) < minRequiredConnections) {
			throw new NoConnectionAvailable(
					"Server is busy. Please try again later");
		}
	}

	private static int availableConnections(EntityManager em) {
		Query query = createQuery(em);
		int n = ((BigInteger) query.getSingleResult()).intValue();
		return n;

	}

	private static Query createQuery(EntityManager em) {
		Query query = em
				.createNativeQuery("SELECT COUNT(*) FROM pg_stat_activity "
						+ "WHERE application_name = :appNameDb and state = 'idle'");
		query.setParameter("appNameDb", getAppNameDb());
		return query;
	}

	private static ConfRule makeConfRule() {
		return Factory.makeConfRule();
	}

	private static String getAppNameDb() {
		return makeConfRule().readString(APP_NAME_IN_DB.key());
	}
}
