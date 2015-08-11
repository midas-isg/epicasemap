(function(){ 'use strict';
var theApi;
var scope = {};
var validViz;

workaroundForRealHttpCallsUsingNgMockAndNgMockE2E();

describe('Controller: Vizs', function() {
	var $scope, validId;
	init('Vizs');
	beforeEach(function(){
		$scope = scope.vizs;
	});
	
	describe('when loaded ', function() {
		it('should load all Vizs', assertLoadAllVizs);
	});
	
	describe('when loadVizs was receipted', function() {
		var loadVizs = 'loadVizs';

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
			validViz = $scope[models][0];
			validId = validViz.id;
		});
	}

	describe('when loaded with an ID', function() {
		beforeEach(initController('Viz'));
		beforeEach(function(){
			spyOn(theApi, 'getUrlQuery').and.returnValue({id:validId});	
			$scope.loadModelHavingGivenId();
		});
		it('should load the Viz', function(done) {
    		var $scope = scope.viz;
    		var model = 'model';
			asynCallAfterTruthy($scope, model, done, function() {
				var viz = $scope[model]; 
				expect(viz).toBeObject();
				expect(viz.id).toBe(validId);
			});
		});
	});

	describe('when addNew was invoked', function() {
		var viz = 'Viz';
    	beforeEach(initController(viz));
    	beforeEach(function(){
    		$scope['addNew']();
    	});
    	
    	it('should pass a new Viz model to Viz controller', function() {
    		var $scope = scope.viz;
    		assertNewViz($scope.model);
    		expect($scope.showAll).toBe(true);
    		
    		function assertNewViz(model){
    			expect(model.id).toBeUndefined();
    			assertAllSeries(model.allSeries);
    			assertAllSeries(model.allSeries2);
    			
    			function assertAllSeries(all){
    				expect(all.length).toBe(0);
    			}
    		}
        });
    	
        describe('when submit of Viz was invoked and form was dirty', function() {
        	var $scope;
        	beforeEach(function(){
        		$scope = scope.viz;
        		$scope.form.$dirty = true;
        		$scope['submit']();
        	});
        	
        	it('should persist the new Viz model via Viz controller', function(done) {
        		$scope.$watch('model', function() {
        			var id = $scope.model.id;
        			if (id){
        				expect(id).toBeGreaterThan(0);
        				console.log("The Viz was created: id=" + id);
        				removeViz(id, done);
        			}
        		});
        	});
        });
    });
	
	describe('when go was invoked', function() {
		var sut;
		
		beforeEach(function(){
			sut = $scope['go'];
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
	
	describe('when count was invoked', function() {
		var sut;
		
		beforeEach(function(){
			sut = $scope['count'];
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

describe('Controller: Viz', function() {
	var $scope;
	init('Viz');
	beforeEach(function(){
		$scope = scope.viz;
	});
	
	describe('when loaded', shouldLoadAllSeries);
	
	describe('when loadSeries was receipted', function() {
		beforeEach(function(done){
			asynCallAfterTruthy($scope, 'allSeries', done, function() {
				$scope.allSeries = null;
				$scope.$emit('loadSeries');
			});
		});
		
		shouldLoadAllSeries();
	});
	
	describe('when editViz was receipted', function() {
		var event = 'editViz';

		beforeEach(function(done){
			$scope.model = null;
			$scope.$emit(event, validViz);
			asynCallAfterTruthy($scope, 'model', done, function() {
				done();
			});
		});
		
		it('should load the Viz as the model', function(){
			var model = $scope.model;
			expect(model.id).toBe(validViz.id);
		});

		it('should close without change if close was invoked', function(done){
			spyOn(theApi, 'save');
			spyOn(theApi, 'remove');
			$scope.close();
			var unregister =$scope.$watch('model', function(){
				var model = $scope.model;
				if (model === null){
					done();
					expect(theApi.save).not.toHaveBeenCalled();
					expect(theApi.remove).not.toHaveBeenCalled();
					unregister();
				}
			});
		});
	});
	
    describe('when a new Viz model was persisted', function() {
    	var viz;
    	beforeEach(function(done){
        	viz = createValidViz();
    		persistNewViz(viz, done);
    	});
    	
    	afterEach(function(){
    		if (viz.id)
    			removeViz(viz.id);
    	});
    	
    	describe('when removeThenClose was invoked', function(){ 
    		it('should delete the model', function(done) {
				$scope.model = viz;
				spyOn(window, 'confirm').and.returnValue(true);
				$scope['removeThenClose']();
				var unregister =$scope.$watch('model', function(){
					if ($scope.model === null){
						console.log("The Viz was deleted: id=" + viz.id);
						readViz(viz.id, function(it){
							expect(it).toBeNull();
							done();
						});
						unregister();
						delete viz.id;
					}
				});
    		});
    	});
    	
        describe('when submit was invoked', function() {
    		it('should persist the model', function(done) {
    			var desc = 'description added by Acceptance test';
    			$scope.model = viz;
    			$scope.model.description = desc;
        		$scope.form.$dirty = true;
        		$scope['submit']();
        		delete $scope.model.description;
				var unregister = $scope.$watch('model', function(){
					if ($scope.model.description){
						readViz(viz.id, function(model){
							expect(model.id).toBe(viz.id);
							expect(model.description).toBe(desc);
							done();
						});
						unregister();
					}
				});
    		});
        });

        describe('when submitThenClose was invoked', function() {
    		it('should persist the model', function(done) {
    			var desc = 'description added by Acceptance test';
    			$scope.model = viz;
    			$scope.model.description = desc;
        		$scope.form.$dirty = true;
        		$scope['submitThenClose']();
				var unregister = $scope.$watch('model', function(){
					if ($scope.model === null){
						readViz(viz.id, function(it){
							expect(it.id).toBe(viz.id);
							expect(it.description).toBe(desc);
							done();
						});
						unregister();
					}
				});
    		});
        });
    });
    
    function readViz(id, callback){
    	theApi.read('vizs', id).then(function(rsp){
    		callback && callback(rsp.data.result);
		}, function(){
			callback && callback(null);
		});
    }
    
    function persistNewViz(viz, done){
    	theApi.save('vizs', viz).then(function(location){
    		var id = toId(location);
			console.log("The Viz was persisted: id="+id);
			viz.id = id;
			done && done();
		});
    	
    	function toId(location){
    		var tokens = location.split('/');
    		return tokens[tokens.length - 1]  - 0;
    	}
    }
    
	function createValidViz(id){
		return {id:id, allSeries:[], allSeries2:[]};
	}

	function shouldLoadAllSeries(){
		var allSeries = 'allSeries';
		it('should load ' + allSeries, function(done) {
			asynCallAfterTruthy($scope, allSeries, done, function() {
				expect($scope[allSeries]).toBeNonEmptyArray();
			});
		});
	}
});

function removeViz(id, done){
	theApi.remove('vizs', id).then(function(){
		console.log("The Viz was deleted: id="+id);
		done && done();
	});
}

function asynCallAfterTruthy($scope, key, done, callback){
	var unregister = $scope.$watch(key, function() {
		if ($scope[key]){
			callback();
			done();
			unregister();
		}
	});
}

function init(controllerName) {
	var original;
	
	beforeAll(function(){
		original = jasmine.DEFAULT_TIMEOUT_INTERVAL;
		jasmine.DEFAULT_TIMEOUT_INTERVAL = 1000;
		console.log(controllerName + ': started with jasmine.DEFAULT_TIMEOUT_INTERVAL = ' + jasmine.DEFAULT_TIMEOUT_INTERVAL);
	});
    beforeEach(module('app'));
    beforeEach(angular.mock.http.init);
    afterEach(angular.mock.http.reset);
    beforeEach(initController(controllerName));
    afterAll(function() {
    	jasmine.DEFAULT_TIMEOUT_INTERVAL = original;
    	console.log(controllerName + ': stoped and restored jasmine.DEFAULT_TIMEOUT_INTERVAL = ' + jasmine.DEFAULT_TIMEOUT_INTERVAL);
    	original = null;
    }); 
}

function initController(name, key){
	key = key || name.toLowerCase();
	return inject(function ($rootScope, api, _$controller_, _$httpBackend_) {
		theApi = api;
		initHttpBackend(_$httpBackend_);
		scope[key] = $rootScope.$new();
		_$controller_(name, {
            $scope: scope[key],
            $rootScope: $rootScope,
            api: api
        });

		scope[key].form = {$setPristine: angular.noop};
    })
    
    function initHttpBackend(httpBackend){
		var any = /.+\//;
		httpBackend.whenPOST(any).passThrough();
		httpBackend.whenGET(any).passThrough();
		httpBackend.whenPUT(any).passThrough();
		httpBackend.whenDELETE(any).passThrough();
	}
}

function workaroundForRealHttpCallsUsingNgMockAndNgMockE2E(){
	angular.mock.http = {};
	
	angular.mock.http.init = function() {
	  angular.module('ngMock', ['ng', 'ngMockE2E']).provider({
	    $exceptionHandler: angular.mock.$ExceptionHandlerProvider,
	    $log: angular.mock.$LogProvider,
	    $interval: angular.mock.$IntervalProvider,
	    $rootElement: angular.mock.$RootElementProvider
	  }).config(['$provide', function($provide) {
	    $provide.decorator('$timeout', angular.mock.$TimeoutDecorator);
	    $provide.decorator('$$rAF', angular.mock.$RAFDecorator);
	    //$provide.decorator('$$asyncCallback', angular.mock.$AsyncCallbackDecorator);
	    $provide.decorator('$rootScope', angular.mock.$RootScopeDecorator);
	    $provide.decorator('$controller', angular.mock.$ControllerDecorator);
	  }]);
	};
	
	angular.mock.http.reset = function() {
	  angular.module('ngMock', ['ng']).provider({
	    $browser: angular.mock.$BrowserProvider,
	    $exceptionHandler: angular.mock.$ExceptionHandlerProvider,
	    $log: angular.mock.$LogProvider,
	    $interval: angular.mock.$IntervalProvider,
	    $httpBackend: angular.mock.$HttpBackendProvider,
	    $rootElement: angular.mock.$RootElementProvider
	  }).config(['$provide', function($provide) {
	    $provide.decorator('$timeout', angular.mock.$TimeoutDecorator);
	    $provide.decorator('$$rAF', angular.mock.$RAFDecorator);
	    //$provide.decorator('$$asyncCallback', angular.mock.$AsyncCallbackDecorator);
	    $provide.decorator('$rootScope', angular.mock.$RootScopeDecorator);
	    $provide.decorator('$controller', angular.mock.$ControllerDecorator);
	  }]);
	};
}

})();
