app.makePermissionsController = function($scope, $rootScope, api, modelName, modelRoot, modelTitle){
	'use strict';
	var prefix = modelName.toLowerCase();
	var modelKey = modelTitle.toLowerCase();
	// init controller /////////////////////////////////////////////////////////
	var my = app.initCommonControllerFeatures($scope, $rootScope, api);
	my.dom = cacheDom();
	populateScope();
	//loadModels();
	bindEvents();
	// init functions //////////////////////////////////////////////////////////
	function cacheDom(){
		var $dialog = $('#' + prefix + 'PermissionsModal');
		
		return {
			$dialog: $dialog,
			$form: $dialog.find('form'),
			$alertParent: $dialog.find('.modal-body')
		};
	}

	function populateScope(){
		$scope.view = {};

		$scope.edit = function(permission) {
			$scope.$emit('edit' + modelName + 'Permission', makeModel(permission));
		};
		$scope.addPermissions = grantPermissions;
		$scope.close = function() { my.dom.$dialog.modal('hide'); };
		$scope.isHidden = function(it){ return it.hide; };
	}

	function bindEvents(){
	    $rootScope.$on('edit' + modelName + 'Permissions', editPermissions);
	    $rootScope.$on('loadPermissions', function(event, modelId) {
	    	loadPermissions(modelId || ($scope.model && $scope.model.id));
		});
	    $scope.$watch('permissions', refreshACL);
		my.dom.$dialog.on('shown.bs.modal', function (e) {
	    	my.dom.$form.find(':input:enabled:visible:first').focus();
	    });
	    my.dom.$dialog.on('hide.bs.modal', function (e) {
	    	my.confirmNoSaving(event, $scope.form.$dirty);
		});
	}
	// helper functions ////////////////////////////////////////////////////////
	function editPermissions(event, model) {
		$scope.model = model;
		resetView();
		loadPermissions($scope.model.id);
		loadAccounts();
		my.dom.$dialog.modal();
		
		function resetView(){
			api.removeAllAlerts(my.dom.$alertParent);
			$scope.form.$setPristine();
			$scope.permissions = [];
			$scope.accounts = [];
		}
		
		function loadAccounts(callback){
			var doing = api.finding('accounts')
			doThen(doing, success, 'load accounts');
			
			function success(rsp){
				$scope.accounts = rsp.data.results;
				refreshACL();
			}
		}
	}
	
	function loadPermissions(modelId){
		if (! modelId)
			return;
		var path = modelRoot + '/' + modelId + '/permissions';
		var doing = api.finding(path);
		doThen(doing, success, 'load the permissions');
		
		function success(rsp) {
			$scope.permissions = rsp.data.results;
			$scope.viewPermissions = _.map(rsp.data.results, omit);
			
			function omit(e) { 
				var e1 = _.omit(e, modelKey); 
				e1.account = _.pick(e1.account, 'name');
				return e1;
			}
		}
	}
	
	function doThen(doing, callback, action){
		my.emitBusy();
		doing.then(function(rsp) {
			my.emitDone();
			if (callback){
				callback(rsp);
			}
		}, function(err){
			my.emitDone();
			my.alertError(err, "Failed to " + action);
		});
	}

	function refreshACL(){
		var permissions = $scope.permissions;
		var accounts = $scope.accounts;
		if (permissions){
			if (accounts){
				var accountIds = _.map(permissions, accountId);
				accountIds.push($scope.model.owner.id);
				_.each(accounts, function(a){
					a.hide = (accountIds.indexOf(a.id) >= 0)
				});
			}
		}
		
		function accountId(p){return p.account.id}
	}
	
	function makeModel(permission){
		var model = _.clone(permission);
		model[modelKey] = $scope.model;
		return model;
	}
	
	function grantPermissions(){
		$scope.form.$setPristine();
		var selectedAccounts = _.filter($scope.accounts, isSelected);
		if (selectedAccounts.length < 1){
			my.alertError("Please select at least one account!");
			return;
		}
		var selectedAccountIds = _.map(selectedAccounts, getId);
		grant(selectedAccountIds, api.option2mode($scope.option), $scope.model.id);
		resetSetlected();
		
		function resetSetlected(){
			_.each($scope.accounts, function(it){ delete it.isSelected; });
		}
		
		function isSelected(it){ return it.isSelected; }
		function getId(it){ return it.id; }
	}

	function grant(accountIds, mode, modelId){
		var idKey = prefix + 'Id';
		var p = _.extend(mode, {accountIds: accountIds, idKey: modelId});
		var path = modelRoot + '/' + modelId + '/permissions';
		var doing = api.posting(path, p);
		doThen(doing, success, 'load the permissions');
		
		function success(rsp) {
			my.alertSuccess('Added the access to the accounts');
			$scope.permissions = rsp.data.results;
			loadPermissions($scope.model.id);
		}
	}
};