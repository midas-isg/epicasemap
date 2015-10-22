'use strict'; `comment: use ES6`;

app.test.workaroundForRealHttpCallsUsingNgMockAndNgMockE2E();

describe('Service: api', function() {
	var api;
	app.test.init();
	beforeEach(function(){
		api = app.test.getApi();
		expect(app.test.getApi()).toBeDefined();
	});

	let posting = 'posting';
	let path = 'locations/bulk-lables';
	let body = [1,2];
	describe(`when ${posting} to ${path} with [${body}]`, function(){
		var response;
		beforeEach(function(done){
			api[posting](path, body).then(function(rsp){
				response = rsp;
				done();
			});
		});
		
		it(`should return truthy for all response.data.result[${body}]`, function(){
			expect(response.status).toBe(200);
			assertData(response.data);
			
			function assertData(data){
				var result = data.result;
				for (let i = 0; i < body.length; i++)
					expect(result[body[i]]).toBeTruthy();
			}
		});
	});
});
