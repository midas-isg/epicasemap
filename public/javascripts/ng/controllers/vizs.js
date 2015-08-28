"use strict";

app.controller('Vizs', function($scope, $rootScope, api) {
	var urlPath = 'vizs';
	var dom = cacheDom();
	populateScope();
	bindEvents();

	function cacheDom(){
		return {$alertParent: $("#vizs-body")};
	}
	
	function populateScope(){
		$scope.loadModelHavingGivenId = loadModelHavingGivenId;
	    $scope.addNew = function() {
	    	$scope.edit({allSeries:[]});
		};
		$scope.edit = function(viz) {
			$rootScope.$emit('editViz', viz);
		};
		$scope.count = function(array) { 
			return array && array.length || 0;	
		};
		$scope.go = function(viz) {
			window.open(CONTEXT + '?id=' + viz.id, '_top');
		};
	}
	
	function bindEvents(){
		$(document).ready(function(){
			loadModelHavingGivenId();
			loadVizs();
		});
		$rootScope.$on('loadVizs', function(event) {
			loadVizs();
		});

		function loadVizs(){
			api.finding(urlPath).then(function(rsp) {
				$scope.models = rsp.data.results;
			}, function(err){
				error('Failed to load all Visualizations!');
			});
		}
	}

	function loadModelHavingGivenId(){
	    var urlQuery = api.getUrlQuery();
		var id = urlQuery && urlQuery.id;
		if (id){
			api.reading(urlPath, id).then(function(rsp){
				var model = rsp.data.result;
				$scope.edit(model);
			}, function(err){
				alert('Visualization with ID = ' + id + " was not found!");
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