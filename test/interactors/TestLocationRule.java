package interactors;

import static org.fest.assertions.Assertions.assertThat;
import static suites.Helper.assertAreEqual;
import gateways.database.LocationDao;
import integrations.app.App;
import models.entities.Location;

import org.junit.Test;

import play.db.jpa.JPA;
import play.libs.F.Callback0;

public class TestLocationRule {
	
	@Test
	public void testCreateLocation() {
		runWithTransaction(() -> createLocation());
	}

	private void createLocation() throws Exception{
		LocationRule rule = new LocationRule(new LocationDao(JPA.em()));
		Location loc = new Location();
		loc.setAlsId(100L);
		Long exsitingLocId = rule.create(loc);
		Long locId = rule.getLocation(loc.getAlsId()).getId();
		assertAreEqual(locId, exsitingLocId);
		
		String expected = "";
		try{
		locId = rule.getLocation(987654321L).getId();
		}
		catch(Exception e){
			expected = e.getMessage();
		}
		assertThat(expected).isEqualTo("Internal Server Error");
		
		loc = new Location();
		double lat = 10.1;
		double lon = -10.1;
		exsitingLocId = rule.createNew(lat, lon).getId();		
		locId = rule.getLocation(lat, lon).getId();
		assertAreEqual(locId,exsitingLocId);
		
		locId = rule.getLocation(987654321.0,-987654321.0).getId();
		assertThat(locId).isNotEqualTo(exsitingLocId);
	}
	
	private static void runWithTransaction(Callback0 callback) {
		App.newWithInMemoryDbWithDbOpen().runWithTransaction(callback);
	}
}
