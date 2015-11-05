app.test = (function(){ 'use strict'; `comment: use ES6`;
	var the = {};
	var scope = {};

	return { // variables
		scope,
		// methods
		workaroundForRealHttpCallsUsingNgMockAndNgMockE2E,
		injectController,
		init,
		asynCallAfterTruthy,
		deleteViz,
		setValidViz: function(viz) {the.validViz = viz},
		getValidViz,
		loginAsPublic,
		getApi: function() {return the.api},
		defer: function() {return the.q.defer()}
	};
	
	function loginAsPublic(done){
		let body = {email:'public@test.com',password:'public'};
		post('login', body, done);
		
		function post(path, data, success){
			let url = `${CONTEXT}/${path}`;
			$.ajax({type: 'POST', url, data, success, error});
			
			function error(){
        		done.fail(`logging in via ${url} returned an error!`);
			}
		}
	}
	

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
	
	function deleteViz(id, done){
		the.api.deleting('vizs', id).then(function(){
			console.log("The Viz was deleted: id =", id);
			done && done();
		});
	}
	
	function asynCallAfterTruthy($scope, key, done, callback){
		var unwatch = $scope.$watch(key, function() {
			if ($scope[key]){
				callback();
				done();
				unwatch();
			}
		});
	}
	
	function init(controllerName) {
		var originalTimeout;
		var suiName = controllerName || 'api';
		
		beforeAll(function(){
			originalTimeout = jasmine.DEFAULT_TIMEOUT_INTERVAL;
			jasmine.DEFAULT_TIMEOUT_INTERVAL = 2000;
			console.log(suiName + ': started with', timeoutText());
		});
	    beforeEach(module('app'));
	    beforeEach(angular.mock.http.init);
	    afterEach(angular.mock.http.reset);
	    if (controllerName)
	    	beforeEach(injectController(controllerName));
	    else
	    	beforeEach(inject(initApi));
	    afterAll(function() {
	    	jasmine.DEFAULT_TIMEOUT_INTERVAL = originalTimeout;
	    	console.log(suiName + ': stoped and restored', timeoutText());
	    	originalTimeout = null;
	    }); 
	    
	    function timeoutText(){
	    	return 'jasmine.DEFAULT_TIMEOUT_INTERVAL =' +
	    	jasmine.DEFAULT_TIMEOUT_INTERVAL;
	    }
	}
	
	function injectController(name, key){
		key = key || name.toLowerCase();
		return inject(function($rootScope, api, _$controller_, _$httpBackend_, $q){
			initApi(api, _$httpBackend_);
			the.q = $q;
			scope.root = $rootScope;
			scope[key] = $rootScope.$new();
			_$controller_(name, {
	            $scope: scope[key],
	            $rootScope: $rootScope,
	            api: api
	        });
			scope[key].form = {$setPristine: angular.noop};
	    })
	}
	
    function initApi(api, _$httpBackend_){
		the.api = api;
		initHttpBackend(_$httpBackend_);

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
