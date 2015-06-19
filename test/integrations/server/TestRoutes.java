package integrations.server;

import org.junit.Test;

public class TestRoutes {
    @Test // @Ignore("to run test faster")
    public void allRoutes() {
    	Runnable[] tests = {
   			SeriesEndpointTester.read(),	
    		SeriesEndpointTester.defaultParameters(),	
    		
    		TimeCoordinateEndpointTester.dateRange(),
    		TimeCoordinateEndpointTester.pagination(),
    		TimeCoordinateEndpointTester.defaultParameters(),
    		
    		LandingPageTester.containsConextForJavaScript()
    	};
		Server.run(tests);
    }
}