"use strict";

var app = angular.module('app', [])
.config(function($locationProvider) {
	  $locationProvider.html5Mode({
		  enabled: true,
		  requireBase: false
		}).hashPrefix('!');
});

app.run(function(){
	$( document ).ready(resizeResizableTable);
	$( window ).resize(resizeResizableTable);

	function resizeResizableTable() {
		var tc = $(".table-content-resizable");
		var th = $(".table-header");
		var height = $(window).height() - ($("body").height() - tc.height()) - 20;
		var minHeight = $( "tr:first" ).height() * 3;
		tc.css('max-height', Math.max(height, minHeight) + 'px');
	}
})

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
	this.gettingFromUrl = function(url, cfg) {
		return requesting('get', url, cfg);
	};
	this.posting = function(path, body, cfg) {
		return requesting('post', makeUrl(path), cfg, body);
	};
	this.saving = saving;
	this.reading = function(path, id, cfg) {
		return this.gettingFromUrl(makeUrl(path, id), cfg);
	};
	this.deleting = function(path, id, cfg) {
		return requesting('delete', makeUrl(path, id), cfg);
	};
	this.finding = function(path, cfg) {
		return this.gettingFromUrl(makeUrl(path), cfg);
	};
    this.uploading = uploading;
	this.getUrlQuery = function() {
		return $location.search();
	}
	this.toDateText = toDateText;
	this.alert = function($parent, message, classes){
		this.removeAllAlerts($parent);
		alert($parent, message, classes);
	}
	this.removeAllAlerts = function($parent){
		$parent.find('.alert').alert("close");
	}
	
	function alert($parent, message, classes){
		if (! classes)
			classes = 'alert-warning';
		$parent.prepend(
			'<div class="alert ' + classes + ' fade in"><strong>' + message + 
			'</strong><small> @ ' + new Date()  +
			'</small> <button class="close" data-dismiss="alert">&times;</button>' +
		    '</div>'
		);
	}
	
	function requesting(method, url, cfg, body){
		var deferred = $q.defer();
		$http[method](url, body, cfg).then(success, fail);
		return deferred.promise;
		
		function success(response){
			deferred.resolve(response);
		}
		
		function fail(reason){
			deferred.reject(reason);
		}
	}
	
	function saving(path, body, cfg) {
		var url = makeUrl(path, body.id),
			method = body.id ? 'put' : 'post',
			deferred = $q.defer();
		$http[method](url, body, cfg).then(success, fail);
		return deferred.promise;
		
		function success(response){
			deferred.resolve(response.headers().location);
		}
		
		function fail(reason){
			deferred.reject(reason);
		}
	}

	function uploading(path, file, cfg){
        var fd = new FormData();
        fd.append('file', file);
        return requesting('put', makeUrl(path), modify(cfg), fd);
        
        function modify(cfg){
        	cfg = cfg || {};
        	cfg.transformRequest = angular.identity;
        	cfg.headers = cfg.headers || {};
        	cfg.headers['Content-Type'] = undefined;
        	return cfg;
        }
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
	
	function toDateText(timestamp){
		var d = new Date(timestamp),
			MM = to2digits(d.getUTCMonth() + 1),
			dd = to2digits(d.getUTCDate());
		return d.getUTCFullYear() + '-' + MM + '-' + dd;

		function to2digits(number){
			var text = number.toString();
			return number > 9 ? text : '0' + text;
		}
	}
});