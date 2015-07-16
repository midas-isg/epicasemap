"use strict"

var app = angular.module('app', [])
.config(function($locationProvider) {
	  $locationProvider.html5Mode(true).hashPrefix('!');
});

app.service("api", function($http, $q, $location) {
	this.remove = function(path, id) {
		var url = makeUrl(path, id), 
			deferred = $q.defer();
		$http['delete'](url).then(function(data) {
			deferred.resolve(data);
		});
		return deferred.promise;
	};
	this.read = function(path, id) {
		return this.get(makeUrl(path, id));
	};
	this.find = function(path) {
		return this.get(makeUrl(path));
	};
	this.get = function(url) {
		var deferred = $q.defer();
		$http.get(url).then(function(data) {
			deferred.resolve(data);
		});
		return deferred.promise;
	};
	this.save = function(path, body) {
		var url = makeUrl(path, body.id),
			method = body.id ? 'put' : 'post',
			deferred = $q.defer();
		$http[method](url, body).then(success);
		return deferred.promise;
		
		function success(data){
			deferred.resolve(data.headers().location);
		}
	};
	
	function makeUrl(path, id){
		var url = makeApiUrl() + path;
		if (id)
			url += '/' + id;
		return url;
	}
	
	function makeApiUrl(){
		var path = CONTEXT + '/api/',
			host = $location.absUrl().split(CONTEXT)[0];
		return host + path.replace('//', '/');
	}
});

app.controller('viz', function($scope, api) {
	$scope.dialog = document.getElementById('viz');
    loadVizs();
    loadAllSeries();
    $scope.$watch('model', function() { updateAllSeries($scope.model); });
	
    $scope.addNew = function() {
    	$scope.edit({allSeries:[], allSeries2:[]}, true);
	};
	$scope.edit = function(viz, isNew) {
		$scope.model = viz;
		$scope.showAll = isNew || false;
		$scope.form.isNew = isNew;
		$scope.dialog.showModal();
	};
	$scope.count = function(array) { return array && array.length || 0;	};
	$scope.countBy = function(key) {
		return _.countBy($scope.allSeries, function(s) {
			return s[key] ? key : 'others';
		})[key] || 0;
	};
	$scope.submit = function(callBack) {
		var body = buildBody($scope.model);
		save(body, callBack);

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
		
		function save(body, callBack) {
			callBack = callBack || loadModel;
			if ($scope.form.$pristine){
				callBack();
			} else {
				api.save('vizs', body).then(callBack);
			}
		
			function loadModel(location) {
				api.get(location).then(function(rsp) {
					$scope.model = rsp.data.result;
				});
			}
		}
	};
	$scope.submitThenClose = function() { $scope.submit(close);	};
	$scope.removeThenClose = function() {
		if (confirm("About to delete this Viz. \nOK = Delete")) 
			api.remove('vizs', $scope.model.id).then(close);
	};
	$scope.close = function() {
		var isOK = true;
		if ($scope.form.$dirty)
			isOK = confirm("Changes are not saved. \nOK = Close without save");
		if (isOK) 
			close();
	};
	$scope.isShown = function(series) {
		return $scope.showAll || series.s1 || series.s2;
	};
	$scope.toggle = function() { $scope.showAll = ! $scope.showAll;	};
	$scope.lable = function() {
		return $scope.showAll ? 'Hide series not selected' :'Show all series';
	};
	
	function close(){
		loadVizs();
		$scope.dialog.close();
	}
	
	function loadVizs(){
		api.find('vizs').then(function(rsp) {
			$scope.vizs = rsp.data.results;
			$scope.vizOrder = $scope.vizOrder || 'id';
		});
	}
	
	function loadAllSeries(){
		api.find('series').then(function(rsp) {
			$scope.allSeries = rsp.data.results;
			updateAllSeries($scope.model);
			$scope.seriesOrder = $scope.seriesOrder || 'id';
		});
	}
	
	function updateAllSeries(viz){
		if (!viz || !$scope.allSeries)
			return;
		check(viz.allSeries.map(byId), 's1');
		check(viz.allSeries2.map(byId), 's2');
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
});