
app.service("api", function($http, $q, $location) {
	"use strict";
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
	this.putting = function(path, body, cfg) {
		return requesting('put', makeUrl(path), cfg, body);
	};
	this.deleting = function(path, id, cfg) {
		return requesting('delete', makeUrl(path, id), cfg);
	};
	this.finding = function(path, cfg) {
		return this.gettingFromUrl(makeUrl(path), cfg);
	};
	
    this.uploading = uploading;
    this.uploadingViaUrl = uploadingViaUrl;
    
	this.getUrlQuery = function() {
		return $location.search();
	};
	this.toDateText = toDateText;
	this.alert = function($parent, message, classes){
		this.removeAllAlerts($parent);
		alert($parent, message, classes);
	};
	this.removeAllAlerts = function($parent){
		$parent.find('.alert').alert("close");
	};
	this.option2mode = function(option){
		switch(option) {
	    case 'ur':
	    	return {use:true, read_data:true};
	    case 'urc':
	    	return {use:true, read_data:true, change:true};
	    case 'urcp':
	    	return {use:true, read_data:true, change:true, permit:true};
	    default:
	    	return {use:true};
		}
	};
	this.mode2option = function (mode){
		var option = '';
		if (mode.use)
			option += 'u'; 
		if (mode.read_data)
			option += 'r';
		if (mode.change)
			option += 'c';
		if (mode.permit)
			option += 'p';
		return option;
	};
	this.isMy = function(model){
		var ownerId = model && model.owner && model.owner.id;
		if (! ownerId)
			return true;
		return ownerId === MY_ID;
	};
	this.isSeriesPermitted = function(permissions, access, seriesId){
		return isPermitted(permissions, 'series',  access, seriesId);
	};
	this.isVizPermitted = function(permissions, access, vizId){
		return isPermitted(permissions, 'viz',  access, vizId);
	};

	function isPermitted(permissions, key, access, id){
		if (! permissions)
			return true;
		var matchedPermissions = _.filter(permissions, matchedId);
		return _.some(matchedPermissions, function(p) {return p[access]})
		
		function matchedId(permission) {
			var model = permission[key];
			return model && model.id === id
		}
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
	
	function uploadingViaUrl(path, url, cfg){
        var fd = new FormData();
        fd.append('url', url);
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

	this.createTopoJSON = function(visualizationID){
		var path;

		if(!visualizationID) {
			console.warn("No visualization ID");
			return;
		}

		path = 'vizs/' + visualizationID + '/data';
		this.finding(path).then(success, fail);
		
		function fail(err){
			$rootScope.$emit('Failed to load the time-coordinate data!', err);

			return;
		}

		return;
	}
});
