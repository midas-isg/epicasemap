"use strict"

app.controller('Series', function($scope, $rootScope, api) {
	$scope.view = {};
	$scope.coordinates = [];
	
	$scope.dialog = $('#seriesModal');
	$scope.dialogBody = $scope.dialog.find('.modal-body');
	$scope.dialog.ready(function(){
		$('#Series-btn-save-close').hide();
	});
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
			api.remove('series', $scope.model.id).then(function(err){
				if (! err){
					close();
				} else{
					api.alert($scope.dialogBody, 'Error: The series could not be deleted!', 'alert-danger');
				}
			});
	};
	$scope.close = function() {
		$scope.dialog.modal('hide');
	};
	$scope.isShown = function(series){
		return true;
	};
    $scope.uploadNewData = function(seriesId) {
    	$rootScope.$emit('uploadNewSeriesData', seriesId);
	};

	function edit(series) {
		$scope.model = series;
		resetView();
		loadCoordinates(series.id);
		$scope.dialog.modal();
		
		function resetView(){
			if (series.id)
				$scope.form.$setPristine();
			else 
				$scope.form.$setDirty();
			$scope.coordinates = [];
			$scope.locationIds = new Set();
			api.removeAllAlerts($scope.dialogBody);
		}
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
		var toUpload = ! $scope.model.id;
		var body = buildBody($scope.model);
		$scope.working = true;
		api.save('series', body).then(function(location) {
			$scope.working = false;
			api.alert($scope.dialogBody, 'The series was saved.', 'alert-success');
			if (callback){
				callback();
			} else {
				api.get(location).then(function(rsp) {
					$scope.model = rsp.data.result;
					$scope.form.$setPristine();
					if(toUpload)
						$scope.uploadNewData($scope.model.id);
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
					data.forEach(function (it) {
						it.location = $scope.locations.get(it.locationId);
					});
				}
			});
		});
	}
});