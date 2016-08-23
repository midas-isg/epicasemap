package gateways.webservice;

import static org.fest.assertions.Assertions.assertThat;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import integrations.server.Server;
import models.entities.Location;

public class TestAlsDAO {
	
	private final String response =  "{"
			+ "\"type\": \"FeatureCollection\","
			+ "\"features\": ["
			+ "{"
			+ "\"type\": \"Feature\","
			+ "\"geometry\":{},"
			+ "\"properties\": {"
			+ "\"locationTypeName\": \"Sub-Divisions\","
			+ "\"startDate\": \"0001-01-01\","
			+ "\"parentGid\": \"14\","
			+ "\"lineage\": ["
			+ "{"
			+ "\"locationTypeName\": \"Country\","
			+ "\"startDate\": \"0001-01-01\","
			+ "\"name\": \"Sierra Leone\","
			+ "\"gid\": \"11\""
			+ "},"
			+ "{"
			+ "\"locationTypeName\": \"Province\","
			+ "\"startDate\": \"0001-01-01\","
			+ "\"parentGid\": \"11\","
			+ "\"name\": \"Southern\","
			+ "\"gid\": \"14\""
			+ "}"
			+ "],"
			+ "\"name\": \"Moyamba\","
			+ "\"gid\": \"1\","
			+ "\"codes\": ["
			+ "{"
			+ "\"codeTypeName\": \"ISG\","
			+ "\"code\": \"Moyamba\""
			+ "}"
			+ "],"
			+ "\"children\": [ ],"
			+ "\"related\": [ ]"
			+ "},"
			+ "\"id\": \"1\","
			+ "\"bbox\": [-13.002361297607308,"
			+ "7.653533935546875,"
			+ "-11.78927135467518,"
			+ "8.388533592224064]"
			+ "}"
			+ "],"
			+ "\"bbox\": ["
			+ "-13.002361297607308,"
			+ "7.653533935546875,"
			+ "-11.78927135467518,"
			+ "8.388533592224064]"
			+ "}";
	
	@Test
	public void test(){
		Runnable[] tests = { 
				testGetLocation(),
				testJsonToLocation()
		};
		Server.run(tests);
	}
	
	private void testGetAlsLocation() {
		Long id = 1L;
		AlsDao alsDao = new AlsDao();
		Location loc = alsDao.getLocationFromAls(id);
		assertThat(loc).isNotNull();
		assertThat(loc.getLatitude()).isNotNull();
		assertThat(loc.getLongitude()).isNotNull();
	}
	
	private void jsonToLocation() {
				
		JsonNode node = null;
		try {
			node = new ObjectMapper().readTree(response);
		} catch (IOException e) {
			e.printStackTrace();
		}
		AlsDao alsDao = new AlsDao();
		Location loc = alsDao.toLocation(node);
		assertThat(loc).isNotNull();
	}
	
	private static Runnable testGetLocation() {
		return () -> newInstance().testGetAlsLocation();
	}
	
	private static Runnable testJsonToLocation() {
		return () -> newInstance().jsonToLocation();
	}

	private static TestAlsDAO newInstance() {
		return new TestAlsDAO();
	}

}
