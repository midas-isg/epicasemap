app.test = (function(){ 'use strict'; `comment: use ES6`;
	var the = {};
	var scope = {};

	return { // variables
		scope,
		// methods
		workaroundForRealHttpCallsUsingNgMockAndNgMockE2E,
		initController,
		init,
		asynCallAfterTruthy,
		removeViz,
		setValidViz: function(viz) {the.validViz = viz},
		getValidViz,
		getApi: function() {return the.api},
	};
	
	function getValidViz(){
		if (! the.validViz){
			$.ajax({
		        type: "GET",
		        url: `${CONTEXT}/api/vizs`,
		        async: false,
		        success : function(data) {
		        	the.validViz = data.results[0];
		        }
		    });
		}
		
		return the.validViz;
	}
	
	function removeViz(id, done){
		the.api.remove('vizs', id).then(function(){
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
			the.api = api;
			scope.root = $rootScope;
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
