package models.entities;

import static org.fest.assertions.Assertions.assertThat;

import java.util.Date;

import org.junit.Test;

public class TestCoordinate {
	private Coordinate sut = new Coordinate();
	
	@Test
	public void testAccessors() throws Exception {
		testId();
		testLatitude();
		testLongitude();
		testTimestamp();
	}

	private void testId() {
		long id = 2;
		sut.setId(id);
		assertThat(sut.getId()).isEqualTo(id);
	}
	
	private void testLatitude() {
		double latitude = 2.0;
		sut.setLatitude(latitude);
		assertThat(sut.getLatitude()).isEqualTo(latitude);
	}
	
	private void testLongitude() {
		double longtitude = 3.0;
		sut.setLongitude(longtitude);
		assertThat(sut.getLongitude()).isEqualTo(longtitude);
	}
	
	private void testTimestamp() {
		Date now = new Date();
		sut.setTimestamp(now);
		assertThat(sut.getTimestamp()).isEqualTo(now);
	}
	
	@Test
	public void ignoreToString() throws Exception {
		sut.toString();
	}
}
