describe('Module: app: Security', () => {
	'use strict'; `comment: use ES6`;
	var my = {publicAccountId:1};
	var seriesId2permissionId = {}
	let testEmail = 'test@test.com';
	
	afterAll(() =>{
		let vPath = toVizPath(my.publicVizId);
		login(()=>{
			put(vPath, {ownerId:my.publicAccountId}, ()=>{
				del(vPath);
				del(toSeriesPath(my.seriesIdSharingNone));
				delTree(my.seriesIdSharingToPublic);
				delTree(my.seriesIdSharingToTest);
				
				function delTree(id){
					remove(toPath(id), ()=>{
						del(toSeriesPath(id));
					}, nop);
				}
				function del(path){
					remove(path, ()=>console.log(`${path} deleted`), nop);
				}
				function toPath(id){
					return `api/series/permissions/${seriesId2permissionId[id]}`;
				}
				function nop(){}
			});
		});

	});
	
	describe(`when logging in as Public`, () => {
		beforeEach(done => {
			login(()=>{
				if (! my.testAccountId){
					getTestAccountId();
				} else {
					done();
				}
			}, done);
		
			function getTestAccountId(){
				get('api/accounts', (body)=>{
					let testAccount = _.find(body.results, (account) =>{
						return account.email === testEmail;
					});
					if (testAccount){
						my.testAccountId = testAccount.id;
						done();
					} else {
						done.fail(`Cannot find the account with email = ${testEmail}. Please create the account with password='test' and rerun the tests`)
					}
				}, () => {
		        	done.fail(`get accounts returned an error!`);
				});
			}
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
	describe(`when logging in as Test`, () => {
		beforeEach(done => {
			loginAsTest(done, done);
		});
		
		//testSeriesAfterLoggedInAsTest();
		testVizAfterLoggedInAsTest();
	});

	function testVizAfterLoggedInAsTest(){
		var vizId = null;
		describe(`then creating new Viz`, () => {
			beforeEach(done => {
				let path = toVizPath();
				let seriesIds = [my.seriesIdSharingToPublic, my.seriesIdSharingToTest];
				post(path, {seriesIds}, (data, textStatus, rsp)=>{
					vizId = toId(rsp, path); 
					done();
				}, done);
			});
			
			it('should create a new Viz with a positive ID',()=>{
				expect(vizId).toBeGreaterThan(0);
			});
		});
		
		describe(`then creating new Viz with unauthorized Series`, () => {
			var statusMessage = null;
			beforeEach(done => {
				let path = toVizPath();
				post(path, {seriesIds:[my.seriesIdSharingNone]}, ()=>{
					done.fail(`shouldn't permit to create new Viz`);
				}, (body, text, rsp)=>{
					statusMessage = rsp;
					done();
				});
			});
			let errorMessage = 'Unauthorized'
			it(`should return error ${errorMessage}`,()=>{
				expect(statusMessage).toBe(errorMessage);
			});
		});

		describe('then delete the new Viz', ()=>{
			var vPath = null;
			beforeEach(done => {
				vPath = toVizPath(vizId);
				put(vPath, {ownerId:my.testAccountId}, ()=>{
					remove(vPath, done, done);
				});
			});
			
			it('should delete the new Viz', done=>{
				get(vPath, ()=>{
					done.fail(`shouldn't get deleted Viz id=${vizId}`);
				}, (body, text, rsp) => {
		        	expect(rsp).toBe('Not Found');
		        	done();
				});
			});
		});
	}
	
	function login(success, done){
		let body = {email:'public@test.com',password:'public'};
		post('login', body, success, done);
	}
	
	function loginAsTest(success, done){
		let body = {email:'test@test.com',password:'test'};
		post('login', body, success, done);
	}
	function testSeriesAfterLoggedIn(){
		describe(`then creating new Series`, () => {
			beforeEach(done => {
				let path = toSeriesPath();
				my.seriesIdSharingNone = null;
				post(path, {}, (data, textStatus, rsp)=>{
					my.seriesIdSharingNone = toId(rsp, path); 
					done();
				}, done);
			});
			
			it('should create a new Series with a positive ID',()=>{
				expect(my.seriesIdSharingNone).toBeGreaterThan(0);
			});
		});
		
		testShareNewSeriesToId('testAccountId', 'seriesIdSharingToTest');
		testShareNewSeriesToId('publicAccountId', 'seriesIdSharingToPublic');
		
		
		function testShareNewSeriesToId(aKey, sKey){
		describe(`then creating new Series sharing to Test`, () => {
			var id = null;
			var permission = null;
			var seriesPermissionsPath = null;
			beforeEach(done => {
				let path = toSeriesPath();
				id = my[aKey];
				post(path, {}, (data, textStatus, rsp)=>{
					let idKey = toId(rsp, path);
					my[sKey] = idKey; 
					let accountIds = [id];
					seriesPermissionsPath = toSeriesPath(my[sKey]) + '/permissions';
					post(seriesPermissionsPath, {use: true, accountIds, idKey}, done, done);
				}, done);
			});
			
			it('should create a new Series with a positive ID with permission', (done)=>{
				expect(my[sKey]).toBeGreaterThan(0);
				get(seriesPermissionsPath, (body)=>{
					expect(body.results.length).toBeGreaterThan(0);
					let p = body.results[0];
					let pId = p.id;
					expect(pId).toBeGreaterThan(0);
					expect(p.use).toBe(true);
					expect(p.account.id).toBe(id);
					let seriesId = p.series.id;
					expect(seriesId).toBeGreaterThan(0);
					seriesId2permissionId[seriesId] = pId;
					done();
				},() => {
		        	done.fail(`logging out returned an error!`);
				} )
			});
		});
		}
	}
	
	function testVizAfterLoggedIn(){
		describe(`then creating new Viz`, () => {
			beforeEach(done => {
				let path = toVizPath();
				my.publicVizId = null;
				post(path, {seriesIds:[my.seriesIdSharingNone, my.seriesIdSharingToTest]}, (data, textStatus, rsp)=>{
					my.publicVizId = toId(rsp, path); 
					done();
				}, done);
			});
			
			it('should create a new Viz with a positive ID',()=>{
				expect(my.publicVizId).toBeGreaterThan(0);
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
		
		function error(body, text, rsp){
			let msg = `${type} ${url} with ${JSON.stringify(data)} returned an error!`;
			if (! done){
				fail(msg);
			} else {
				if(done.fail)
					done.fail(msg);
				else
					done(body, text, rsp);
			}
		}
	}

	function testVizsAfterLoggedOut(){
		let unauthorizedVizId = 12;

		describe(`then getting Series data via a permitted Viz`, () => {
			var response;
			beforeEach(done => {
				let path = toSeriesDataPath(my.publicVizId, my.seriesIdSharingNone);
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
				let path = toSeriesDataPath(unauthorizedVizId, my.seriesIdSharingNone);
				error.result = getExpectingError(path, done);
			});
			assertErrorTypeWithUserMessage(error, 'Unauthorized');
		});
		describe(`then getting not-in Series data via an permitted Viz`, () => {
			var error = {};
			beforeEach(done =>{
				let notInVizSeriesId = 19;
				let path = toSeriesDataPath(my.publicVizId, notInVizSeriesId);
				error.result = getExpectingError(path, done);
			});
			assertErrorTypeWithUserMessage(error, 'Not Found');
		});
		describe(`then getting a permitted Viz`, () => {
			var response;
			var id;
			beforeEach(done => {
				id = my.publicVizId;
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
