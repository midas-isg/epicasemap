package models.entities;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;

public class TestCoordinate {
	private Coordinate coordinate = new Coordinate();
	
	@Test
	public void testAccessors() throws Exception {
		testId();
		testLatitude();
		testLongitude();
		testType();
	}

	private void testId() {
		long id = 2;
		coordinate.setId(id);
		assertThat(coordinate.getId()).isEqualTo(id);
	}
	
	private void testLatitude() {
		double latitude = 2.0;
		coordinate.setLatitude(latitude);
		assertThat(coordinate.getLatitude()).isEqualTo(latitude);
	}
	
	private void testLongitude() {
		double longtitude = 3.0;
		coordinate.setLongitude(longtitude);
		assertThat(coordinate.getLongitude()).isEqualTo(longtitude);
	}
	
	private void testType() {
		String type = "test type";
		coordinate.setType(type);
		assertThat(coordinate.getType()).isEqualTo(type);
	}
}
