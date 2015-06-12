package gateways.database;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import gateways.database.CoordinateTimeDao;
import gateways.database.jpa.JpaAdaptor;
import models.entities.CoordinateTime;

import org.junit.Before;
import org.junit.Test;

public class TestCoordinateTimeDao {
	private CoordinateTimeDao sut;
	private JpaAdaptor mock;
	
	@Before
	public void initDaoWithMocks() {
		mock = mock(JpaAdaptor.class);
		sut = new CoordinateTimeDao(mock);
	}

	@Test
	public void findAll() throws Exception {
		sut.findAll();
		verify(mock).query(CoordinateTime.class, null, 0);
	}
	
	@Test
	public void find() throws Exception {
		long id = 1L;
		sut.find(id);
		verify(mock).find(CoordinateTime.class, id);
	}
}