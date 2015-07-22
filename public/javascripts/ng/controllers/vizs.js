"use strict"

app.controller('Vizs', function($scope, $rootScope, api) {
    loadVizs();
    $rootScope.$on('loadVizs', function(event) {
    	loadVizs();
	});

    $scope.addNew = function() {
    	$scope.edit({allSeries:[], allSeries2:[]});
	};
	$scope.edit = function(viz) {
		$rootScope.$emit('editViz', viz);
	};
	$scope.count = function(array) { return array && array.length || 0;	};
	
	function loadVizs(){
		api.find('vizs').then(function(rsp) {
			$scope.vizs = rsp.data.results;
			$scope.vizOrder = $scope.vizOrder || 'id';
		});
	}
});