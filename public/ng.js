var app = angular.module('app', []);

app.service("api", [ '$http', '$q', function ($http, $q) {
	var apiUrl = 'http://localhost:9000/epidemap/api/';
	
	this.list = function (path)	{
		return this.get(apiUrl + path);
	}
	
	this.get = function (url) {
		var deferred = $q.defer();
		$http.get(url).then(function (data) {
			deferred.resolve(data);
		});
		return deferred.promise;
	}
	
	this.save = function (body)	{
		var url = apiUrl + 'vizs';
		var deferred = $q.defer();
		$http.post(url, body).then(function (data) {
			deferred.resolve(data.headers().location);
		});
		return deferred.promise;
	}
	
}]);

app.controller('viz', [ '$scope', 'api', function($scope, api) {
	var allSeries;
	var view = {};
	$scope.view = view;

	api.list('series').then(function (rsp)	{
		allSeries = rsp.data.results;
		view.allSeries = allSeries;
	});
    
    $scope.getTotal = function () {
        return allSeries && allSeries.length || 0;
    };
    
    $scope.submit = function () {
    	var body = {name: 'test NG'};
    	
    	view.series1Objects = _.filter(allSeries, function(it){
            return it.s1;
        });
    	body.seriesIds = _.map(view.series1Objects, function(it){ return it.id; });
    	view.series1Ids = body.seriesIds;

    	view.series2Objects = _.filter(allSeries, function(it){
            return it.s2;
        });
    	body.series2Ids = _.map(view.series2Objects, function(it){ return it.id; });
    	view.series2Ids = body.series2Ids;
    	$scope.save(body);
    };
    
    $scope.save = function(data) {
    	api.save(data).then(function (location)	{
    		$scope.view.location = location;
        	api.get(location).then(function (rsp)	{
        		$scope.view.viz = rsp.data.result;
        	});
    	});
    }

} ]);