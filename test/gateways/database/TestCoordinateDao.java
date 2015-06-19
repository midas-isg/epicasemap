package gateways.database;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import gateways.database.jpa.JpaAdaptor;
import models.entities.Coordinate;

import org.junit.Before;
import org.junit.Test;

public class TestCoordinateDao {
	private CoordinateDao sut;
	private JpaAdaptor mock;
	
	@Before
	public void initDaoWithMocks() {
		mock = mock(JpaAdaptor.class);
		sut = new CoordinateDao(mock);
	}

	@Test
	public void findAll() throws Exception {
		sut.findAll();
		verify(mock).query(Coordinate.class, null);
	}
	
	@Test
	public void find() throws Exception {
		long id = 1L;
		sut.find(id);
		verify(mock).find(Coordinate.class, id);
	}
}