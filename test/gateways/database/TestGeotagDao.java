package gateways.database;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import gateways.database.GeotagDao;
import gateways.database.jpa.JpaAdaptor;
import models.entities.Geotag;

import org.junit.Before;
import org.junit.Test;

public class TestGeotagDao {
	private GeotagDao sut;
	private JpaAdaptor mock;
	
	@Before
	public void initDaoWithMocks() {
		mock = mock(JpaAdaptor.class);
		sut = new GeotagDao(mock);
	}

	@Test
	public void findAll() throws Exception {
		sut.findAll();
		verify(mock).findAll(Geotag.class);
	}
	
	@Test
	public void find() throws Exception {
		long id = 1L;
		sut.find(id);
		verify(mock).find(Geotag.class, id);
	}
}