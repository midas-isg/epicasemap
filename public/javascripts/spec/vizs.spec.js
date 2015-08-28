'use strict'; `comment: use ES6`;

app.test.workaroundForRealHttpCallsUsingNgMockAndNgMockE2E();

describe('Controller: Vizs', function() {
	var asynCallAfterTruthy = app.test.asynCallAfterTruthy;
	var $scope;
	app.test.init('Vizs');
	beforeEach(function(){
		$scope = app.test.scope.vizs;
	});
	
	describe('when loaded ', function() {
		it('should load all Vizs', assertLoadAllVizs);
	});
	
	let loadVizs = 'loadVizs';
	describe(`when ${loadVizs} was receipted`, function() {
		beforeEach(function(done){
			asynCallAfterTruthy($scope, 'models', done, function() {
				$scope.models = null;
				$scope.$emit(loadVizs);
			});
		});
		it('should load all Vizs', assertLoadAllVizs);
	});
	
	function assertLoadAllVizs(done){
		var models = 'models';
		asynCallAfterTruthy($scope, models, done, function() {
			expect($scope[models]).toBeNonEmptyArray();
			app.test.setValidViz($scope[models][0]);
		});
	}

	describe('when loaded with an ID', function() {
		beforeEach(app.test.injectController('Viz'));
		beforeEach(function(){
			spyOn(app.test.getApi(), 'getUrlQuery').and.returnValue({id:app.test.getValidViz().id});	
			$scope.loadModelHavingGivenId();
		});
		it('should load the Viz', function(done) {
    		var $scope = app.test.scope.viz;
    		var model = 'model';
			asynCallAfterTruthy($scope, model, done, function() {
				var viz = $scope[model]; 
				expect(viz).toBeObject();
				expect(viz.id).toBe(app.test.getValidViz().id);
			});
		});
	});

	let addNew = 'addNew';
	describe(`when ${addNew} was invoked`, function() {
		var viz = 'Viz';
    	beforeEach(app.test.injectController(viz));
    	beforeEach(function(){
    		$scope[addNew]();
    	});
    	
    	it('should pass a new Viz model to Viz controller', function() {
    		var $scope = app.test.scope.viz;
    		assertNewViz($scope.model);
    		expect($scope.showAll).toBe(true);
    		
    		function assertNewViz(model){
    			expect(model.id).toBeUndefined();
    			assertAllSeries(model.allSeries);
    			
    			function assertAllSeries(all){
    				expect(all.length).toBe(0);
    			}
    		}
        });
    	
    	let submit = 'submit';
        describe(`when ${submit} of Viz was invoked and form was dirty`, function() {
        	var $scope;
        	beforeEach(function(){
        		$scope = app.test.scope.viz;
        		$scope.form.$dirty = true;
        		$scope[submit]();
        	});
        	
        	it('should persist the new Viz model via Viz controller', function(done) {
        		$scope.$watch('model', function() {
        			var id = $scope.model.id;
        			if (id){
        				expect(id).toBeGreaterThan(0);
        				console.log("The Viz was created: id=" + id);
        				app.test.deleteViz(id, done);
        			}
        		});
        	});
        });
    });
	
	let edit = 'edit';
	describe(`when ${edit} was invoked`, function() {
		var viz = 'Viz';
		var passedModel;
		var receivedModel;
		let editViz = 'editViz';
    	beforeEach(function(){
    		app.test.scope.root.$on(editViz, function(event, model) {
    			receivedModel = model;
    		});
    		
    		receivedModel = null;
    		passedModel = {};
    		$scope[edit](passedModel);
    	});
    	
    	it(`should emit ${editViz} with the Viz model`, function() {
    		expect(receivedModel).toBe(passedModel);
        });
	});

	
	let go = 'go';
	describe(`when ${go} was invoked`, function() {
		var sut;
		
		beforeEach(function(){
			sut = $scope[go];
			spyOn(window, 'open');
		});

		it('should call window.open when an object with an id was passed', function(){
			sut({id:0});
			expect(window.open).toHaveBeenCalled();
		});

		it('should throw an exception if null was passed', function(){
			expect(function(){sut(null);}).toThrow();
		});
	});
	
	let count = 'count'; 
	describe(`when ${count} was invoked`, function() {
		var sut;
		
		beforeEach(function(){
			sut = $scope[count];
		}); 

		it('should return 0 if falsy was passed', function(){
			expect(sut(undefined)).toBe(0);
			expect(sut(null)).toBe(0);
		});

		it('should return 0 if [] was passed', function(){
			expect(sut([])).toBe(0);
		});

		it('should return 1 if [0] was passed', function(){
			expect(sut([0])).toBe(1);
		});
	});
});
