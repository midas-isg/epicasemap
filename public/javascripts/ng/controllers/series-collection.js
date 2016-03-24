app.controller('SeriesCollection', function($scope, $rootScope, api) {
	'use strict';
	// init controller /////////////////////////////////////////////////////////
	var my = app.initCommonControllerFeatures($scope, $rootScope, api);

	my.dom = cacheDom();
	populateScope();
	loadModels();
	bindEvents();
	// init functions //////////////////////////////////////////////////////////
	function cacheDom(){
		return {$alertParent: $('#series-body')};
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

		$scope.isHidden = function(it) {
			if($scope.seriesShowAllSeries) {
				return false;
			}
			return !($scope.can('use', it));
		};
	}
	
	function loadModels(){
		my.editModelHavingGivenId(my.editSeriesById);
		loadSeries();
	}
	
	function bindEvents(){
	    $rootScope.$on('loadSeries', function(event) {
	    	loadSeries();
		});
	}
	// helper functions ////////////////////////////////////////////////////////
	function loadSeries(){
		my.loadSeriesAsModels();
		my.loadPermissions();
	}
});