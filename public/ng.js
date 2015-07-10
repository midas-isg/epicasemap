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

app.controller('PromiseCtrl', [ '$scope', 'api', function($scope, api) {
	var data;
	
	api.list('series').then(function (rsp)	{
		data = rsp.data;
		$scope.example1 = data;
	});
    
    $scope.getTotal = function () {
        return data && data.results && data.results.length || 0;
    };
    
    $scope.submit = function () {
    	var body = {name: 'test NG'};
    	
    	$scope.example2 = _.filter(data.results, function(it){
            return it.s1;
        });
    	$scope.example3 = _.map($scope.example2, function(it){ return it.id; });
    	body.seriesIds = $scope.example3;

    	$scope.example4 = _.filter(data.results, function(it){
            return it.s2;
        });
    	$scope.example5 = _.map($scope.example4, function(it){ return it.id; });
    	body.series2Ids = $scope.example5;
    	$scope.save(body);
    };
    
    $scope.save = function(data) {
    	api.save(data).then(function (location)	{
    		$scope.example5 = location;
        	api.get(location).then(function (rsp)	{
        		$scope.example2 = rsp.data;
        	});
    	});
    }

} ]);