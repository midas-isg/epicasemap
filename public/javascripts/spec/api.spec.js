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
	
	let putting = 'putting';
	let path2 = 'series/2/data-url/http%3A%2F%2Flocalhost%3A9000%2Fepidemap%2Fassets%2Finput%2Fseries-data%2Fexamples%2Ftest_alsId_format.txt';
	let body2 = '5 existing item(s) deleted.\n5 new item(s) created.';
	describe(`when ${putting} to ${path2} with [${body2}]`, function(){
		var response;
		beforeEach(function(done){
			api[putting](path2).then(function(rsp){
				response = rsp;
				done();
			}, function(error){
				response = error; 
				done();
			});
		});
		
		it(`should return truthy for all response.data.result[${body2}]`, function(){
			expect(response.status).toBe(201);
			assertData(response.data);
			
			function assertData(data){
				expect(data).toEqual(body2);
			}
		});
	});
});
