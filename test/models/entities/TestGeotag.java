package models.entities;

import static org.fest.assertions.Assertions.assertThat;

import java.util.Date;

import org.junit.Test;

public class TestGeotag {
	private Geotag geotag = new Geotag();
	
	@Test
	public void testAccessors() throws Exception {
		testId();
		testLatitude();
		testLongitude();
		testTimestamp();
	}

	private void testId() {
		long id = 2;
		geotag.setId(id);
		assertThat(geotag.getId()).isEqualTo(id);
	}
	
	private void testLatitude() {
		double latitude = 2.0;
		geotag.setLatitude(latitude);
		assertThat(geotag.getLatitude()).isEqualTo(latitude);
	}
	
	private void testLongitude() {
		double longtitude = 3.0;
		geotag.setLongitude(longtitude);
		assertThat(geotag.getLongitude()).isEqualTo(longtitude);
	}
	
	private void testTimestamp() {
		Date now = new Date();
		geotag.setTimestamp(now);
		assertThat(geotag.getTimestamp()).isEqualTo(now);
	}
}
