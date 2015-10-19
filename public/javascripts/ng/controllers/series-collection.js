app.controller('SeriesCollection', function($scope, $rootScope, api) {
	"use strict"
	var my = app.initCommonControllerFeatures($scope, $rootScope, api);
	my.dom = cacheDom();
	populateScope();
	loadModels();
	bindEvents();

	function cacheDom(){
		return {$alertParent: $('#series-body')};
	}
	
	function loadModels(){
		my.editModelHavingGivenId(my.editSeriesById);
		loadSeries();
	}
	
	function populateScope(){
		$scope.can = my.canAccessSeries;
		$scope.count = my.length;

		$scope.addNew = function() {
	    	$scope.edit({});
		};
		$scope.edit = function(series) {
			$rootScope.$emit('editSeries', series);
		};
		$scope.permit = function(series) {
			$rootScope.$emit('editSeriesPermissions', series);
		};
	}
	
	function bindEvents(){
	    $rootScope.$on('loadSeries', function(event) {
	    	loadSeries();
		});
	}
	
	function loadSeries(){
		my.loadSeriesAsModels();
		my.loadPermissions();
	}
});