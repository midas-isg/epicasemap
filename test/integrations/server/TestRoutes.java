package integrations.server;

import org.junit.Test;

public class TestRoutes {
	@Test
	public void allRoutes() {
		Runnable[] tests = {
				VizEndpointTester.test(),

				LocationEndpointTester.read(),

				SeriesEndpointTester.read(),
				SeriesEndpointTester.defaultParameters(),

				TimeCoordinateEndpointTester.dateRange(),
				TimeCoordinateEndpointTester.pagination(),
				TimeCoordinateEndpointTester.defaultParameters(),
				
				LandingPageTester.containsConextForJavaScript(),

				UploadSeriesEndpointTester.test()
		};
		Server.run(tests);
	}
}
