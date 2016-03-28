app.makePermissionController = function($scope, $rootScope, api, 
		modelName, modelRoot){
	'use strict';
	var prefix = modelName.toLowerCase();
	// init controller /////////////////////////////////////////////////////////
	var my = app.initCommonControllerFeatures($scope, $rootScope, api);
	my.dom = cacheDom();
	my.apiPath = modelRoot + '/permissions';
	populateScope();
	//loadModels();
	bindEvents();
	// init functions //////////////////////////////////////////////////////////
	function cacheDom(){
		var $dialog = $('#' + prefix + 'PermissionModal');
		
		return {
			$dialog: $dialog,
			$form: $dialog.find('form'),
			$alertParent: $dialog.find('.modal-body')
		};
	}
	
	function populateScope(){
		$scope.close = function() { my.dom.$dialog.modal('hide'); };
		$scope.submit = submitThen;
		$scope.submitThenClose = function() { submitThen($scope.close); };
		$scope.removeThenClose = removeThenClose;
		$scope.isModelEditable = function(){ return true; };
	}

	function bindEvents(){
		$rootScope.$on('edit'+ modelName +'Permission', edit);
		my.dom.$dialog.on('hide.bs.modal', function(event){
			my.confirmNoSaving(event, $scope.form.$dirty, loadPermissions);
		});
		my.dom.$dialog.on('shown.bs.modal', function (event) {
			my.dom.$form.find(':input:enabled:visible:first').focus();
		});
	}
	// helper functions ////////////////////////////////////////////////////////
	function edit(event, permission) {
		resetView();
		$scope.model = permission;
		$scope.option = api.mode2option(permission);
		my.dom.$dialog.modal();
		
		function resetView(){
			api.removeAllAlerts(my.dom.$alertParent);
			$scope.form.$setPristine();
			$scope.model = null;
			$scope.option = null;
		}
	};
	
	function loadPermissions(){
		$scope.$emit('loadPermissions');
	}
	
	function submitThen(callback) {
		if ($scope.form.$dirty) {
			saveThen(callback);
		} else if (callback) {
			callback();
		}
	};

	function saveThen(callback){
		var body = buildBody();
		var id = $scope.model.id;
		var path = my.apiPath + '/' + id + '/mode';
		var email = parseInt(getURLParameterByName("email"));
		var visualizationID = parseInt(getURLParameterByName("visualizationID"));
		var seriesID = parseInt(getURLParameterByName("seriesID"));

		if(email === $scope.model.account.id) {
			if (($scope.model.visualization && ($scope.model.visualization.id === visualizationID)) ||
				($scope.model.series && ($scope.model.series.id === seriesID))) {
				path += '?email=1';
			}
		}
		
		var doing = api.putting(path, body);
		doThen(doing, callback, 'save');
	}

	function buildBody() {
		return api.option2mode($scope.option);
	}
	
	function doThen(doing, callback, verb){
		my.emitBusy();
		doing.then(function() {
			my.emitDone();
			my.alertSuccess("The permission was " + verb + "d.");
			$scope.form.$setPristine();
			if (callback){
				callback();
			}
		}, function(err){
			my.emitDone();
			my.alertError(err, "Failed to " + verb + " the permission!");
		});
	}

	function removeThenClose() {
		if (confirm("About to delete this Permission. \nOK = Delete")){
			var doing = api.deleting(my.apiPath, $scope.model.id)
			doThen(doing, $scope.close, "delete");
		}
	}
};