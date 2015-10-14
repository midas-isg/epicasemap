app.controller('Vizs', function($scope, $rootScope, api) {
	"use strict";
	var my = app.initCommonControllerFeatures($scope, $rootScope, api);
	my.dom = cacheDom();
	populateScope();
	loadModels();
	bindEvents();

	function cacheDom(){
		return {$alertParent: $('#vizs-body')};
	}
	
	function loadModels(){
		my.editModelHavingGivenId(my.editVizById);
		loadVizs();
	}
	
	function populateScope(){
		$scope.can = my.canAccessViz;
		$scope.edit = my.editViz;

		$scope.addNew = function() {
	    	$scope.edit({allSeries:[]});
		};
		$scope.count = function(array) { 
			return array && array.length || 0;	
		};
		$scope.go = function(viz) {
			window.open(CONTEXT + '?id=' + viz.id, '_top');
		};
		$scope.test = {
			loadModels: loadModels,
			dom: my.dom
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
		my.loadVizsAsModels();
		my.loadPermissions();
	}
});