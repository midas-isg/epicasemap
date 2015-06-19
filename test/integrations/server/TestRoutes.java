package integrations.server;

import org.junit.Test;

public class TestRoutes {
    @Test
    public void allRoutes() {
    	Runnable[] tests = {
    		TestEndpointSeries.defaultParameters(),	
    		
    		TestEndpointTimeCoordinate.dateRange(),
    		TestEndpointTimeCoordinate.pagination(),
    	};
		Server.run(tests);
    }
}