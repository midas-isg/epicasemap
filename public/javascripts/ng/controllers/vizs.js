app.controller('Vizs', function($scope, $rootScope, api) {
	"use strict";
	var urlPath = 'vizs';
	var dom = cacheDom();
	populateScope();
	loadModels();
	bindEvents();

	function cacheDom(){
		return {$alertParent: $("#vizs-body")};
	}
	
	function loadModels(){
		loadModelHavingGivenId();
		loadVizs();
	}
	
	function populateScope(){
	    $scope.addNew = function() {
	    	$scope.edit({allSeries:[]});
		};
		$scope.edit = function(viz) {
			$scope.$emit('editViz', viz);
		};
		$scope.count = function(array) { 
			return array && array.length || 0;	
		};
		$scope.go = function(viz) {
			window.open(CONTEXT + '?id=' + viz.id, '_top');
		};
		$scope.test = {
			loadModels: loadModels,
			dom: dom
		};
		$scope.permit = function(viz) {
			$rootScope.$emit('editVizPermissions', viz);
		};
	}
	
	function bindEvents(){
		$rootScope.$on('loadVizs', function(event) {
			loadVizs();
		});
	}

	function loadVizs(){
		api.finding(urlPath).then(function(rsp) {
			$scope.models = rsp.data.results;
		}, function(err){
			$scope.error = 'Failed to load your Visualizations!';
			error($scope.error);
		});
	}
 
	function loadModelHavingGivenId(){
	    var urlQuery = api.getUrlQuery();
		var id = urlQuery && urlQuery.id;
		if (id){
			api.reading(urlPath, id).then(function(rsp){
				var model = rsp.data.result;
				$scope.edit(model);
			}, function(err){
				$scope.error = err.data && err.data.userMessage;
				alert($scope.error);
			});
		}
	}
	
	function error(message){
		alert('Error: ' + message, 'alert-danger');
	}
	
	function alert(message, classes){
		api.alert(dom.$alertParent, message, classes);
	}
});