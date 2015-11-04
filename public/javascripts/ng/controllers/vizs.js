app.controller('Vizs', function($scope, $rootScope, api) {
	'use strict';
	// init controller /////////////////////////////////////////////////////////
	var my = app.initCommonControllerFeatures($scope, $rootScope, api);
	my.dom = cacheDom();
	my.playVizUrl = CONTEXT;
	populateScope();
	loadModels();
	bindEvents();
	// init functions //////////////////////////////////////////////////////////
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
		$scope.count = my.length;
		$scope.addNew = function() {
	    	$scope.edit({allSeries:[]});
		};
		$scope.go = function(viz) {
			window.open(my.playVizUrl + '?id=' + viz.id, '_top');
		};
		$scope.editPermissions = function(viz) {
			$scope.$emit('editVizPermissions', viz);
		};
		$scope.test = {
			loadModels: loadModels,
			dom: my.dom
		};
	}
	
	function bindEvents(){
		$rootScope.$on('loadVizs', function(event) {
			loadVizs();
		});
	}
	// helper functions ////////////////////////////////////////////////////////
	function loadVizs(){
		my.loadVizsAsModels();
		my.loadPermissions();
	}
});