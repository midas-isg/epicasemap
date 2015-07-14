"use strict"

var app = angular.module('app', [])
.config(function($locationProvider) {
	  $locationProvider.html5Mode(true).hashPrefix('!');
})
;

app.service("api", function($http, $q, $location) {
	var apiUrl = makeApiUrl();

	this.remove = function(path, id) {
		var url = makeUrl(path, id);
		var deferred = $q.defer();
		$http['delete'](url).then(function(data) {
			deferred.resolve(data);
		});
		return deferred.promise;
	}

	this.read = function(path, id) {
		return this.get(makeUrl(path, id));
	}

	this.find = function(path) {
		return this.get(makeUrl(path));
	}

	this.get = function(url) {
		var deferred = $q.defer();
		$http.get(url).then(function(data) {
			deferred.resolve(data);
		});
		return deferred.promise;
	}

	this.save = function(path, body) {
		var url = makeUrl(path, body.id);
		var deferred = $q.defer();
		if (body.id) {
			$http.put(url, body).then(function(data) {
				deferred.resolve(data.headers().location);
			});
		} else {
			$http.post(url, body).then(function(data) {
				deferred.resolve(data.headers().location);
			});
		}
		return deferred.promise;
	}
	
	function makeUrl(path, id){
		var url = apiUrl + path;
		if (id)
			url += '/' + id;
		return url;
	}
	
	function makeApiUrl(){
		var path = CONTEXT + '/api/';
		return $location.absUrl().split(CONTEXT)[0] + path.replace('//', '/');
	}

});

app.controller('viz', function($scope, api) {
	var vizs;
	var allSeries;
    var dialog = document.getElementById('viz');

    updateVizs();
	
    $scope.addNew = function() {
    	$scope.edit({allSeries:[], allSeries2:[]}, true);
	}
    
	$scope.edit = function(viz, showAll) {
		$scope.model = viz;
		allSeries || api.find('series').then(function(rsp) {
			allSeries = rsp.data.results;
			$scope.allSeries = allSeries;
			sync($scope.model, allSeries);
			setShowAll(showAll);
		});
		sync($scope.model, allSeries);
		setShowAll(showAll);
		dialog.showModal();
	};

	function setShowAll(showAll){
		showAll && $scope.form.$setDirty();
		$scope.showAll = showAll || false;
	}
	
	$scope.count = function(array) {
		return array && array.length || 0;
	};

	$scope.countBy = function(key) {
		return _.countBy(allSeries, function(s) {
			return s[key] ? key : 'others';
		})[key] || 0;
	};

	$scope.submit = function(callBack) {
		var body = initBody($scope.model);
		body.seriesIds = toSeriesIds('s1');
		body.series2Ids = toSeriesIds('s2');
		save(body, callBack);

		function initBody(model) {
			return _.omit(model, 'allSeries', 'allSeries2');
		}

		function toSeriesIds(key){
			var seriesObjects = filterByKey(allSeries, key);
			return mapById(seriesObjects);

			function filterByKey(array, key) {
				return _.filter(array, function(it) { return it[key]; });
			}

			function mapById(array) {
				return _.map(array, function(it) { return it.id; });
			}
		}
		
		function save(body, callBack) {
			if ($scope.form.$pristine) 
				return;
			callBack = callBack || updateModel;
			api.save('vizs', body).then(callBack);
			
			function updateModel(location) {
				$scope.location = location;
				api.get(location).then(function(rsp) {
					$scope.model = rsp.data.result;
					sync($scope.model, allSeries);
				});
			}
		}
	};
	
	$scope.submitThenClose = function() {
		$scope.submit(close);
	}
		
	$scope.removeThenClose = function() {
		api.remove('vizs', $scope.model.id).then(function(rsp){
			close();
		});
	}
	
	$scope.close = function() {
		var yes = true;
		if ($scope.form.$dirty)
			yes = confirm("Changes are not saved. \nOK = Close without save");
		if (yes) {
			close();
		}
	}
	
	$scope.isShown = function(series) {
		return $scope.showAll || series.s1 || series.s2;
	};
	
	$scope.toggle = function(series) {
		$scope.showAll = ! $scope.showAll;
	};
	
	$scope.lable = function() {
		return $scope.showAll ? 'Hide series not selected' :'Show all series';
	}
	
	function close(){
		updateVizs();
        dialog.close();
	}
	
	function updateVizs(){
		api.find('vizs').then(function(rsp) {
			vizs = rsp.data.results;
			$scope.vizs = vizs;
		});
	}
	
	function sync(viz, allSeries){
		if (!viz || !allSeries)
			return;
		check(viz.allSeries.map(byId), 's1');
		check(viz.allSeries2.map(byId), 's2');
		$scope.form.$setPristine();

		function byId(series) {
			  return series.id;
		}
		
		function check(ids, key){
			allSeries.forEach(function (series) {
				  return series[key] = _.contains(ids, series.id);
			});
		}
	}

})
;