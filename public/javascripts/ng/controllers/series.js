"use strict"

app.controller('Series', function($scope, $rootScope, api) {
	$scope.view = {};
	$scope.coordinates = [];
	$scope.dataLimit = 100;
	
	$scope.dialog = $('#seriesModal');
	$scope.alertParent = $scope.dialog.find('.modal-body');
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
			emitBusy();
			api.deleting('series', $scope.model.id).then(function(rsp){
				emitDone();
				close();
			}, function (err){
				emitDone();
				error(err.data && err.data.userMessage);
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
	$scope.isHiddenButtonSaveThenClose = isNoData;

	function edit(series) {
		$scope.model = series;
		resetView();
		loadCoordinates(series.id);
		$scope.dialog.modal();
		
		function resetView(){
			api.removeAllAlerts($scope.alertParent);
			$scope.form.$setPristine();
			$scope.coordinates = [];
			$scope.locationIds = new Set();
		}
	};
	
	function loadCoordinates(seriesId){
		if (! seriesId)
			return;
		var path = 'series/' + seriesId + '/time-coordinate';
		api.finding(path).then(success, fail);
		
		function success(rsp) {
			$scope.coordinates = rsp.data.results;
			populateAdditionInfo(rsp.data.results);
		}
		
		function fail(reason){
			error('Failed to load the time-coordinate data!');
		}
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
	
	function isNoData(){
		return $scope.coordinates.length <= 0;
	}
	function save(callback){
		emitBusy();
		var toUpload = isNoData();
		var body = buildBody($scope.model);
		api.saving('series', body).then(function(location) {
			emitDone();
			success('The series was saved.');
			if (callback){
				callback();
			} else {
				api.gettingFromUrl(location).then(function(rsp) {
					$scope.model = rsp.data.result;
					$scope.form.$setPristine();
					if(toUpload)
						$scope.uploadNewData($scope.model.id);
				}, function(err){
					error('Failed to read the series!');
				});
			}
		}, function(err){
			emitDone();
			error('Failed to save the series!');
		});
	}
	
	function success(message){
		alert('Success: ' + message, 'alert-success');
	}
	
	function error(message){
		alert('Error: ' + message, 'alert-danger');
	}
	
	function alert(message, classes){
		api.alert($scope.alertParent, message, classes);
	}
	
	function emitBusy(){
		$scope.isWorking = true;
		$rootScope.$emit('modalBusyDialog');
	}
	
	function emitDone(){
		$scope.isWorking = false;
		$rootScope.$emit('hideBusyDialog');
	}
	
	function populateAdditionInfo(data){
		var path = 'locations/bulk-lables';
		$scope.locationIds = [];
		data.forEach(function (it) {
			it.date = api.toDateText(it.timestamp);
			$scope.locationIds.push(it.locationId);
		});

		$scope.locationIds = _.uniq($scope.locationIds);
		var array = _.toArray($scope.locationIds);
		api.posting(path, JSON.stringify($scope.locationIds)).then(function(rsp){
			var id2label = rsp.data.result;
			data.forEach(function (datum) {
				datum.location = id2label[datum.locationId];
			});
		});
	}
});