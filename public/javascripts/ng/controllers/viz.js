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
	$scope.countSelected = function() {
		var key = 'isSelected';
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
		if (confirm("About to delete the Visualization. \nOK = Delete")){
			emitBusy();
			api.deleting('vizs', $scope.model.id).then(close, function(err){
				emitDone();
				error('Failed to delete the Visualization!', err);
			});
		}
	};
	$scope.close = function() {
		$scope.dialog.modal('hide');
		$scope.model = null;
	};
	$scope.isShown = function(series) {
		return $scope.showAll || series.isSelected;
	};
	$scope.isHidden = function(series) {
		return ! $scope.isShown(series);
	};
	$scope.editSeries = function(series){
		$rootScope.$emit('editSeries', series);
	}
	$scope.invertSelection = function(){
		invert('isSelected');
	}
	$scope.can = can
	$scope.isModelEditable = function(){
		return api.isMy($scope.model) || canChangeModel();
		
		function canChangeModel(){
			var modelId = $scope.model && $scope.model.id
			return api.isVizPermitted($scope.permissions, 'change', modelId)
		}
	};
	$scope.permit = function(series) {
		$rootScope.$emit('editSeriesPermissions', series);
	};

	function can(access, series){
		var seriesId = series && series.id;
		return api.isMy(series) || isSeriesPermitted();
		
		function isSeriesPermitted(){
			return api.isSeriesPermitted($scope.permissions, access, seriesId);
		}
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
		api.finding('series').then(function(rsp) {
			$scope.allSeries = rsp.data.results;
			updateAllSeries($scope.model);
		}, function(err){
			error("Failed to load your Series!", err);
		});
		
		loadPermissions();
		
		function loadPermissions(){
			api.finding('accounts/my-permissions').then(function(rsp) {
				$scope.permissions = rsp.data.results;
			}, function(err){
				$scope.error = 'Failed to load your permissions!';
				alert($scope.error);
			});
		}
	}
	
	function updateAllSeries(viz){
		var wasPristine = $scope.form.$pristine;
		if (viz && $scope.allSeries){
			check(viz.allSeries.map(byId), 'isSelected');
		}
		if (wasPristine)
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
		var body = _.omit(model, 'allSeries', 'owner');
		body.seriesIds = toSeriesIds('isSelected');
		body.ownerId = model.owner && model.owner.id 
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
		api.saving('vizs', body).then(function(location) {
			emitDone();
			success('The Visualization was saved');
			if (callback){
				callback();
			} else {
				api.gettingFromUrl(location).then(function(rsp) {
					$scope.model = rsp.data.result;
				}, function(err){
					error('Failed to read the Visualization!', err);
				});
			}
		}, function(err){
			emitDone();
			error('Failed to save the Visualization!', err);
		});
	}
	
	function success(message){
		alert('Success: ' + message, 'alert-success');
	}
	
	function error(defaultMessage, err){
		var message = err.data.userMessage || defaultMessage;
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