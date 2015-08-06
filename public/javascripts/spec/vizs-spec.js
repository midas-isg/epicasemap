'use strict';

workaroundForRealHttpCallsUsingNgMockAndNgMockE2E();

describe('Controller: Vizs', function() {
    var theApi;
	var scope = {};
    var addNew = 'addNew';

    beforeEach(module('app'));
    beforeEach(angular.mock.http.init);
    afterEach(angular.mock.http.reset);
    beforeEach(initController('Vizs'));
    
    describe('when ' + addNew + ' is invoked', function() {
    	beforeEach(initController('Viz'));
    	beforeEach(function(){
    		expect(0).toBe(0);
    		scope.viz.form = {$setPristine: angular.noop};
    		scope.vizs[addNew]();
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
    	
        describe('when form is dirty and submit is invoked', function() {
        	var $scope;
        	beforeEach(function(){
        		$scope = scope.viz;
        		$scope.form.$dirty = true;
        		$scope['submit']();
        	});
        	
        	it('should request Viz controller to persist the new Viz model', function(done) {
        		$scope.$watch('model', function() {
        			var id = $scope.model.id;
        			if (id){
        				expect(id).toBeGreaterThan(0);
        				console.log("The Viz was created: id=" + id);
        				theApi.remove('vizs', id).then(function(){
        					console.log("The Viz was deleted: id="+id);
        				});
        				done(); 
        			}
        		});
        	});
        });
    });
    
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
        })
        
        function initHttpBackend(httpBackend){
    		var any = /.+\//;
    		httpBackend.whenPOST(any).passThrough();
    		httpBackend.whenGET(any).passThrough();
    		httpBackend.whenPUT(any).passThrough();
    		httpBackend.whenDELETE(any).passThrough();
    	}
    }
});

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