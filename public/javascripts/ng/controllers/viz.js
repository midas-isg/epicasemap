"use strict"

$(document).ready(function() {
	$("#filterPlaceholderNotToCountAsPartOfForm").replaceWith($("#seriesFilter"));
});

app.controller('Viz', function($scope, $rootScope, api) {
	$scope.view = {};
	$scope.dialog = $('#modal');
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
		if (confirm("About to delete this Viz. \nOK = Delete"))
			api.remove('vizs', $scope.model.id).then(close);
	};
	$scope.close = function() {
		$scope.dialog.modal('hide');
	};
	$scope.isShown = function(series) {
		return $scope.showAll || series.s1 || series.s2;
	};
	$scope.toggle = function() { $scope.showAll = ! $scope.showAll;	};
	$scope.lable = function() {
		return $scope.showAll ? 'Hide series not selected' :'Show all series';
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
			  return series[key] = ! series[key];
		});
	}
	
	function close(){
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
			$scope.seriesOrder = $scope.seriesOrder || 'id';
		});
	}
	
	function updateAllSeries(viz){
		if (viz && $scope.allSeries){
			check(viz.allSeries.map(byId), 's1');
			check(viz.allSeries2.map(byId), 's2');
		}
		if ($scope.form.isNew)
			$scope.form.$setDirty();
		else
			$scope.form.$setPristine();

		function byId(series) { return series.id; }
		
		function check(ids, key){
			$scope.allSeries.forEach(function (series) {
				  return series[key] = _.contains(ids, series.id);
			});
		}
	}

	function edit(viz) {
		var isNew = viz.id ? false : true;
		$scope.model = viz;
		$scope.showAll = isNew;
		$scope.form.isNew = isNew;
		$scope.dialog.modal();
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
		var body = buildBody($scope.model);
		$scope.working = true;
		api.save('vizs', body).then(function(location) {
			$scope.working = false;
			$scope.form.isNew = false;
			if (callback){
				callback();
			} else {
				api.get(location).then(function(rsp) {
					$scope.model = rsp.data.result;
				});
			}
		});
	}
});
