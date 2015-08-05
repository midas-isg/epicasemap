"use strict"

app.controller('Series', function($scope, $rootScope, api) {
	$scope.view = {};
	$scope.coordinates = [];
	
	$scope.dialog = $('#seriesModal');
    $scope.dialog.on('hide.bs.modal', function (e) {
		var isOK = true;
		if ($scope.form.$dirty)
			isOK = confirm("Changes are not saved. \nOK = Close without save");
		if (isOK) {
			loadSeries();
		} else {
			e.preventDefault();
        	e.stopImmediatePropagation();
        }
    });
    $scope.dialog.on('shown.bs.modal', function (e) {
    	$scope.dialog.find('form').find(':input:enabled:visible:first').focus();
    });
    $rootScope.$on('editSeries', function(event, series) {
    	edit(series);
	});
    $rootScope.$on('loadCoordinates', function(event, seriesId) {
    	loadCoordinates(seriesId);
	});

	$scope.submit = function(callback) {
		if ($scope.form.$dirty) {
			save(callback);
		} else if (callback) {
			callback();
		}
	};
	$scope.submitThenClose = function() { $scope.submit(close);	};
	$scope.removeThenClose = function() {
		if (confirm("About to delete this Series. \nOK = Delete"))
			api.remove('series', $scope.model.id).then(close);
	};
	$scope.close = function() {
		$scope.dialog.modal('hide');
		resetView();
		
		function resetView(){
			$scope.form.$setPristine();
			$scope.coordinates = [];
			$scope.locationIds = new Set();
		}
	};
	$scope.isShown = function(series){
		return true;
	};
    $scope.uploadNewData = function(seriesId) {
    	$rootScope.$emit('uploadNewSeriesData', seriesId);
	};

	function edit(series) {
		$scope.model = series;
		loadCoordinates(series.id);
		$scope.dialog.modal();
	};
	
	function loadCoordinates(seriesId){
		if (! seriesId)
			return;
		var path = 'series/' + seriesId + '/time-coordinate';
		api.find(path).then(function(rsp) {
			$scope.coordinates =  rsp.data.results;
			populateAdditionInfo($scope.coordinates);
			$scope.dataOrder = $scope.dataOrder || 'date';
		});
	}

	function close(){
		$scope.form.$setPristine();
		$scope.close();
	}

	function loadSeries(){
		$rootScope.$emit('loadSeries');
	}
	
	function buildBody(model) {
		return model;
	}
	
	function save(callback){
		var body = buildBody($scope.model);
		$scope.working = true;
		api.save('series', body).then(function(location) {
			$scope.working = false;
			if (callback){
				callback();
			} else {
				api.get(location).then(function(rsp) {
					$scope.model = rsp.data.result;
				});
			}
		});
	}
	
	function populateAdditionInfo(data){
		var path = 'locations/',
			size = 0;
		$scope.locationIds = new Set();
		$scope.locations = new Map();
		data.forEach(function (it) {
			it.date = api.toDateText(it.timestamp);
			$scope.locationIds.add(it.locationId);
		});

		size = $scope.locationIds.size;
		if (size > 50)
			return;
		$scope.locationIds.forEach(function (it) {
			api.find(path + it).then(function(rsp) {
				$scope.locations.set(it, rsp.data.result.label);
				if ($scope.locations.size >= size){
					console.log($scope.locations);
					data.forEach(function (it) {
						it.location = $scope.locations.get(it.locationId);
					});
				}
			});
		});
	}
});