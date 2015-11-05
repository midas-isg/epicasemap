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
		var doing = api.putting(my.apiPath + '/' + id + '/mode', body);
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