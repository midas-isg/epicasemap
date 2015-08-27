"use strict"

app.controller('SeriesCollection', function($scope, $rootScope, api) {
	var urlPath = 'series';

	$scope.alertParent = $("#series-body");
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
		api.finding(urlPath).then(function(rsp) {
			$scope.models = rsp.data.results;
		}, function(err){
			error('Failed to load all Series');
		});
	}
	
	function loadModelHavingGivenId(){
	    var urlQuery = api.getUrlQuery();
		var id = urlQuery && urlQuery.id;
		if (id){
			api.reading(urlPath, id).then(function(rsp){
				var model = rsp.data.result;
				if (model)
					$scope.edit(model);
				else
					alert('Series with ID = ' + id + " was not found!");
			}, function(err){
				error('Failed to read Series with ID = ' + id);
			});
		}
	}
	
	function error(message){
		alert('Error: ' + message, 'alert-danger');
	}
	
	function alert(message, classes){
		api.alert($scope.alertParent, message, classes);
	}
});