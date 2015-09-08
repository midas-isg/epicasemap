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
			error('Failed to load your Series');
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
				alert(err.data && err.data.userMessage);
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