describe('Module: app: Security', () => {
	'use strict'; `comment: use ES6`;
	var newPublicVizId = null;
	var newPublicSeriesId = null;
	
	afterAll(() =>{
		let vPath = toVizPath(newPublicVizId);
		login(()=>{
			put(vPath, {}, ()=>{
				remove(vPath, ()=>console.log(`${vPath} deleted`));
				
				let path = toSeriesPath(newPublicSeriesId);
				remove(path, ()=>console.log(`${path} deleted`));
			});
		});

	});
	
	describe(`when logging in`, () => {
		beforeEach(done => {
			login(done, done);
		});
		
		testSeriesAfterLoggedIn();
		testVizAfterLoggedIn();
	});
	describe(`when logging out`, () => {
		beforeEach(done => {
			get('logout', done, () => {
	        	done.fail(`logging out returned an error!`);
			});
		});
		
		testVizsAfterLoggedOut();
	});
	
	function login(success, done){
		let body = {email:'public@test.com',password:'public'};
		post('login', body, success, done);
	}
	
	function testSeriesAfterLoggedIn(){
		describe(`then creating new Series`, () => {
			beforeEach(done => {
				let path = toSeriesPath();
				newPublicSeriesId = null;
				post(path, {}, (data, textStatus, rsp)=>{
					newPublicSeriesId = toId(rsp, path); 
					done();
				}, done);
			});
			
			it('should create a new Series with a positive ID',()=>{
				expect(newPublicSeriesId).toBeGreaterThan(0);
			});
		});
	}
	
	function testVizAfterLoggedIn(){
		describe(`then creating new Viz`, () => {
			beforeEach(done => {
				let path = toVizPath();
				newPublicVizId = null;
				post(path, {seriesIds:[newPublicSeriesId]}, (data, textStatus, rsp)=>{
					newPublicVizId = toId(rsp, path); 
					done();
				}, done);
			});
			
			it('should create a new Viz with a positive ID',()=>{
				expect(newPublicVizId).toBeGreaterThan(0);
			});
		});
	}

	function toId(rsp, path){
		let location = rsp.getResponseHeader('Location');
		return Number(location.split(`${path}/`)[1]);
	}

	function toVizPath(vId){
		return toPath('api/vizs', vId);
	}

	function toSeriesPath(sId){
		return toPath('api/series', sId);
	}

	function toPath(path, id){
		if (id)
			path += `/${id}`;
		return path;
	}

	function get(path, success, error){
		let url = toUrl(path);
		$.ajax({type: 'GET', url, success, error});
	}
	
	function remove(path, success, error){
		let url = toUrl(path);
		$.ajax({type: 'DELETE', url, success, error});
	}

	function toUrl(path){
		return `${CONTEXT}/${path}`;
	}

	function post(path, data, success, done){
		ajaxWithBody('POST', path, data, success, done);
	}
	
	function put(path, data, success, done){
		ajaxWithBody('PUT', path, data, success, done);
	}
	
	function ajaxWithBody(type, path, data, success, done){
		let url = toUrl(path);
		$.ajax({type, url, data, success, error});
		
		function error(){
			let msg = `${type} ${url} with ${JSON.stringify(data)} returned an error!`;
			if (! done){
				fail(msg);
			} else {
				done.fail(msg);
			}
		}
	}

	function testVizsAfterLoggedOut(){
		let unauthorizedVizId = 12;

		describe(`then getting Series data via a permitted Viz`, () => {
			var response;
			beforeEach(done => {
				let path = toSeriesDataPath(newPublicVizId, newPublicSeriesId);
				get(path, data => {
					response = data;
					done();
				}, () => {
					done.fail(`getting Series Data returned an error!`);
				});
			});
			it('should return some response.results', () => {
				expect(response.results).toBeDefined();
			});
		});
		describe(`then getting Series data via an unauthorized Viz`, () => {
			var error = {};
			beforeEach(done => {
				let path = toSeriesDataPath(unauthorizedVizId, newPublicSeriesId);
				error.result = getExpectingError(path, done);
			});
			assertErrorTypeWithUserMessage(error, 'Unauthorized');
		});
		describe(`then getting not-in Series data via an permitted Viz`, () => {
			var error = {};
			beforeEach(done =>{
				let notInVizSeriesId = 19;
				let path = toSeriesDataPath(newPublicVizId, notInVizSeriesId);
				error.result = getExpectingError(path, done);
			});
			assertErrorTypeWithUserMessage(error, 'Not Found');
		});
		describe(`then getting a permitted Viz`, () => {
			var response;
			var id;
			beforeEach(done => {
				id = newPublicVizId;
				let path = toVizPath(id);
				get(path, data => {
					response = data;
					done();
				}, () => {
					done.fail(`getting Viz returned an error!`);
				});
			});
			it('should return a valid response.result', () => {
				expect(response.result.id).toBe(id);
			});
		});
		describe(`then getting Series data via an unauthorized Viz`, () => {
			var response;
			var id;
			beforeEach(done => {
				id = unauthorizedVizId;
				let path = toVizPath(id);
				get(path, data => {
					response = data;
					done();
				}, () => {
					done.fail(`getting Viz returned an error!`);
				});
			});
			it('should return a valid response.result', () => {
				expect(response.result.id).toBe(id);
			});
		});
		
		function assertErrorTypeWithUserMessage(error, type){
			it(`should return error ${type} with a user message`, () => {
				let response = error.result.response;
				expect(response.statusText).toBe(type);
				expect(response.responseJSON.userMessage).toBeDefined();
			});
		}
		
		function toSeriesDataPath(vId, sId){
			return `api/vizs/${vId}/series/${sId}/time-coordinate`
		}
		
		function getExpectingError(path, done){
			var result = {};
			get(path, expectNoDataReturned(done), (error) => {
				result.response = error;
				done();
			});
			return result;
		}
		
		function expectNoDataReturned(done){
			return (() => {
				done.fail(`expecting no data returned!`);
			});
		}
	}
});
