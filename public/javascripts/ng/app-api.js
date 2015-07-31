"use strict";

var app = angular.module('app', [])
.config(function($locationProvider) {
	  $locationProvider.html5Mode(true).hashPrefix('!');
});

app.directive('appFileModel', function ($parse) {
    return {
        restrict: 'A',
        link: function(scope, element, attrs) {
            var model = $parse(attrs.appFileModel);
            
            element.bind('change', function(){
                scope.$apply(function(){
                    model.assign(scope, element[0].files[0]);
                });
            });
        }
    };
});

app.service("api", function($http, $q, $location) {
	$( document ).ready(resizeResizableTable);
	$( window ).resize(resizeResizableTable);

	function resizeResizableTable() {
		var tc = $(".table-content-resizable");
		var th = $(".table-header");
		var height = $(window).height() - ($("body").height() - tc.height()) - 20;
		var minHeight = $( "tr:first" ).height() * 3;
		tc.css('max-height', Math.max(height, minHeight) + 'px');
	}
	
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
	this.getUrlQuery = function() {
		return $location.search();
	}
	this.toDateText = function(timestamp){
		var d = new Date(timestamp),
			MM = to2digits(d.getUTCMonth() + 1),
			dd = to2digits(d.getUTCDate());
		
		return d.getUTCFullYear() + '-' + MM + '-' + dd;
	}
    this.uploadFile = function(path, file){
        var fd = new FormData(),
        	deferred = $q.defer();
        fd.append('file', file);
        $http.post(makeUrl(path), fd, {
            transformRequest: angular.identity,
            headers: {'Content-Type': undefined}
        }).then(function(data){
        	deferred.resolve(data);
        });
        return deferred.promise;
    }
	
	function makeUrl(path, id){
		var url = makeApiUrl() + path;
		if (id)
			url += '/' + id;
		return url;
	}
	
	function makeApiUrl(){
		var path = CONTEXT + '/api/';
		return path.replace('//', '/');
	}
	
	function to2digits(number){
		var text = number.toString();
		return number > 9 ? text : '0' + text;
	}
});