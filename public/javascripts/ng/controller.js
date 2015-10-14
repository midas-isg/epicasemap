(function(){ "use strict";
app.initCommonControllerFeatures = function($scope, $rootScope, api){
	var my = {};
	
	my.alertError = alertError;
	my.alertWarning = alertWarning;
	
	my.loadVizsAsModels = function(){
		loadAsModels('vizs', 'Visualizations');
	}
	my.editViz = function(viz) {
		$scope.$emit('editViz', viz);
	};
	my.editVizById = function(id){
		return editModel('vizs', id, 'Visualization', my.editViz);
	}
	
	my.loadSeriesAsModels = function(){
		loadAsModels('series', 'Series');
	}
	my.editSeries = function(series) {
		$scope.$emit('editSeries', series);
	};
	my.editSeriesById = function(id){
		return editModel('series', id, 'Series', my.editSeries);
	}
	
	my.editModelHavingGivenId = function(callback){
	    var urlQuery = api.getUrlQuery();
		var id = urlQuery && urlQuery.id;
		callback(id);
	}

	my.loadPermissions = function(){
		api.finding('accounts/my-permissions').then(function(rsp) {
			$scope.permissions = rsp.data.results;
		}, function(err){
			alertWarning(err, textFailToLoad('your permissions'));
		});
	}
	my.canAccessSeries = function(access, series){
		return canAccessModel(access, series, api.isSeriesPermitted);
	};
	my.canAccessViz = function(access, viz){
		return canAccessModel(access, viz, api.isVizPermitted);
	};
	
	function loadAsModels(path, plural){
		api.finding(path).then(function(rsp) {
			$scope.models = rsp.data.results;
		}, function(err){
			alertError(err, textFailToLoad('your ' + plural));
		});
	}

	function editModel(path, id, name, callback){
		if (! id)
			return;

		api.reading(path, id).then(function(rsp){
			var model = rsp.data.result;
			callback(model);
		}, function(err){
			alertWarning(err, textFailToLoad('the ' + name, id));
		});
	}

	function canAccessModel(access, model, callback){
		var id = model && model.id;
		return api.isMy(model) || isPermitted();
		
		function isPermitted(){
			return callback($scope.permissions, access, id);
		}
	}
	
	function textFailToLoad(name, id){
		var text = 'Failed to ' + name;
		if (id)
			text += ' with ID = ' + id;
		return text;
	}
	
	function toUserMessageOrAsTextOrElse(error, defaultText){
		var msg = error && error.data && error.data.userMessage;
		if (typeof error === 'string')
			msg = error;
		return msg || defaultText;
	}
	
	function alertError(error, defaultText){
		var message = toUserMessageOrAsTextOrElse(error, defaultText);
		$scope.lastError = message;
		alert('Error: ' + message, 'alert-danger');
	}

	function alertWarning(error, defaultText){
		var message = toUserMessageOrAsTextOrElse(error, defaultText);
		$scope.lastWarning = message;
		alert(message);
	}

	function alert(message, classes){
		$scope.lastAlert = message;
		api.alert(my.dom.$alertParent, message, classes);
	}
	
	return my;
}
})();