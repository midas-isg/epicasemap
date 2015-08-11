"use strict";

app.controller('Vizs', function($scope, $rootScope, api) {
	var urlPath = 'vizs';

	$scope.loadModelHavingGivenId = loadModelHavingGivenId;
	loadModelHavingGivenId();
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
	$scope.go = function(viz) {
		window.open(CONTEXT + '?id=' + viz.id, '_top');
	};
	function loadVizs(){
		api.find(urlPath).then(function(rsp) {
			$scope.models = rsp.data.results;
			$scope.vizOrder = $scope.vizOrder || 'id';
		});
	}
	
	function loadModelHavingGivenId(){
	    var urlQuery = api.getUrlQuery();
		var id = urlQuery && urlQuery.id;
		if (id){
			api.read(urlPath, id).then(function(rsp){
				var model = rsp.data.result;
				if (model)
					$scope.edit(model);
				else
					alert('Viz with ID=' + id + " was not found!");
			});
		}
	}
});