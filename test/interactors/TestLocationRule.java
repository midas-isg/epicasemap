package interactors;

import static org.fest.assertions.Assertions.assertThat;
import static suites.Helper.assertAreEqual;
import integrations.app.App;
import models.entities.Location;

import org.junit.Test;

import play.db.jpa.JPA;
import play.libs.F.Callback0;
import suites.SeriesDataFileHelper;
import controllers.Factory;

public class TestLocationRule {
	
	@Test
	public void testCreateLocation() {
		runWithTransaction(() -> createLocation());
	}

	private void createLocation() throws Exception{
		LocationRule rule = Factory.makeLocationRule(JPA.em());
		Location loc = SeriesDataFileHelper.makeLocation();
		loc.setAlsId(100L);
		Long exsitingLocId = rule.create(loc);
		Long locId = rule.getLocation(loc.getAlsId()).getId();
		assertAreEqual(locId, exsitingLocId);
		
		String expected = "";
		try{
			final long invalidAlsId = 0L;
			locId = rule.getLocation(invalidAlsId).getId();
		}
		catch(Exception e){
			expected = e.getMessage();
		}
		assertThat(expected).isEqualTo("Not Found");
		
		loc = SeriesDataFileHelper.makeLocation();
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
