"use strict"

app.controller('SeriesCollection', function($scope, $rootScope, api) {
	var urlPath = 'series';
	loadModelHavingGivenId();

	loadSeries();
    $rootScope.$on('loadSeries', function(event) {
    	loadSeries();
	});

    $scope.addNew = function() {
    	$scope.edit({allSeries:[], allSeries2:[]});
	};
	$scope.edit = function(series) {
		$rootScope.$emit('editSeries', series);
	};
	$scope.count = function(array) { return array && array.length || 0;	};
	
	function loadSeries(){
		api.find(urlPath).then(function(rsp) {
			$scope.models = rsp.data.results;
			$scope.seriesOrder = $scope.seriesOrder || 'id';
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
					alert('Series with ID=' + id + " was not found!");
			});
		}
	}
});