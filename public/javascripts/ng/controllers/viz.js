"use strict"

$(document).ready(function() {
	$("#filterPlaceholderNotToCountAsPartOfForm").replaceWith($("#seriesFilter"));
});

app.controller('Viz', function($scope, $rootScope, api) {
	$scope.view = {};
	$scope.dialog = $('#modal');
	$scope.alertParent = $scope.dialog.find('.modal-body');
    loadAllSeries();
    $scope.$watch('model', function() { updateAllSeries($scope.model); });
    $scope.dialog.on('hide.bs.modal', function (e) {
		var isOK = true;
		if ($scope.form.$dirty)
			isOK = confirm("Changes are not saved. \nOK = Close without save");
		if (isOK) {
			loadVizs();
		} else {
			e.preventDefault();
        	e.stopImmediatePropagation();
        }
    });
    $scope.dialog.on('shown.bs.modal', function (e) {
    	$scope.dialog.find('form').find(':input:enabled:visible:first').focus();
    });
    $rootScope.$on('editViz', function(event, viz) {
    	edit(viz);
	});
    $rootScope.$on('loadSeries', function(event) {
    	loadAllSeries();
	});
	$scope.countBy = function(key) {
		return _.countBy($scope.allSeries, function(s) {
			return s[key] ? key : 'others';
		})[key] || 0;
	};
	$scope.submit = function(callback) {
		if ($scope.form.$dirty) {
			save(callback);
		} else if (callback) {
			callback();
		}
	};
	$scope.submitThenClose = function() { $scope.submit(close);	};
	$scope.removeThenClose = function() {
		if (confirm("About to delete this Viz. \nOK = Delete")){
			emitBusy();
			api.remove('vizs', $scope.model.id).then(close, function(err){
				emitDone();
				error('Failed to delete the Viz!');
			});
		}
	};
	$scope.close = function() {
		$scope.dialog.modal('hide');
		$scope.model = null;
	};
	$scope.isShown = function(series) {
		return $scope.showAll || series.s1 || series.s2;
	};
	$scope.isHidden = function(series) {
		return ! $scope.isShown(series);
	};
	$scope.editSeries = function(series){
		$rootScope.$emit('editSeries', series);
	}
	$scope.invertA = function(){
		invert('s1');
	}
	$scope.invertB = function(){
		invert('s2');
	}
	
	function invert(key){
		$scope.allSeries.forEach(function (series) {
			  series[key] = ! series[key];
		});
		$scope.form.$setDirty();
	}
	
	function close(){
		emitDone();
		$scope.form.$setPristine();
		$scope.close();
	}
	
	function loadVizs(){
		$rootScope.$emit('loadVizs');
	}
	
	function loadAllSeries(){
		api.find('series').then(function(rsp) {
			$scope.allSeries = rsp.data.results;
			updateAllSeries($scope.model);
		}, function(err){
			error("Failed to read all Series!");
		});
	}
	
	function updateAllSeries(viz){
		if (viz && $scope.allSeries){
			check(viz.allSeries.map(byId), 's1');
			check(viz.allSeries2.map(byId), 's2');
		}
		$scope.form.$setPristine();

		function byId(series) { return series.id; }
		
		function check(ids, key){
			$scope.allSeries.forEach(function (series) {
				  return series[key] = _.contains(ids, series.id);
			});
		}
	}

	function edit(viz) {
		$scope.model = viz;
		resetView();
		$scope.dialog.modal();
		
		function resetView(){
			api.removeAllAlerts($scope.alertParent);
			$scope.form.$setPristine();
			if (viz.id) {
				$scope.showAll = false;
			} else {
				$scope.showAll = true;
			} 
		}

	}

	function buildBody(model) {
		var body = _.omit(model, 'allSeries', 'allSeries2');
		body.seriesIds = toSeriesIds('s1');
		body.series2Ids = toSeriesIds('s2');
		return body;
		
		function toSeriesIds(key){
			var seriesObjects = filterByKey();
			return mapById();

			function filterByKey() {
				return _.filter($scope.allSeries, function(it) { 
					return it[key]; 
				});
			}

			function mapById() {
				return _.map(seriesObjects, function(it) { return it.id; });
			}
		}
	}
	
	function save(callback){
		emitBusy();
		var body = buildBody($scope.model);
		api.save('vizs', body).then(function(location) {
			emitDone();
			success('The Viz was saved');
			if (callback){
				callback();
			} else {
				api.get(location).then(function(rsp) {
					$scope.model = rsp.data.result;
				}, function(err){
					error('Failed to read the Viz!');
				});
			}
		}, function(err){
			emitDone();
			error('Failed to save the Viz!');
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
});