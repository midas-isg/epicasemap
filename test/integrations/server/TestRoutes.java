package integrations.server;

import org.junit.Ignore;
import org.junit.Test;

public class TestRoutes {
    @Ignore("TODO(fix broken test case)") @Test // @Ignore("to run test faster")
    public void allRoutes() {
    	Runnable[] tests = {
    		VizEndpointTester.test(),

			LocationEndpointTester.read(),

            SeriesEndpointTester.read(),
            SeriesEndpointTester.defaultParameters(),

            TimeCoordinateEndpointTester.dateRange(),
            TimeCoordinateEndpointTester.pagination(),
            TimeCoordinateEndpointTester.defaultParameters(),
    		
    		LandingPageTester.containsConextForJavaScript(),/**/
    		
    		//UploadSeriesEndpointTester.test()
    	};
		Server.run(tests);
    }
}
