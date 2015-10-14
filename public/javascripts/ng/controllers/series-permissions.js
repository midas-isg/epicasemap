
app.controller('SeriesPermissions', function($scope, $rootScope, api) {
	"use strict"
	$scope.view = {};
	$scope.dialog = $('#seriesPermissionsModal');
	$scope.alertParent = $scope.dialog.find('.modal-body');
    $scope.dialog.on('shown.bs.modal', function (e) {
    	$scope.dialog.find('form').find(':input:enabled:visible:first').focus();
    });
    $scope.dialog.on('hide.bs.modal', function (e) {
    	var isOK = true;
		if ($scope.form.$dirty)
			isOK = confirm("Selected accounts have not been added. \nOK = Close without adding");
		if ( ! isOK) {
			e.preventDefault();
        	e.stopImmediatePropagation();
        }
    });
	$scope.$watch('permissions', refreshACL);
    $rootScope.$on('editSeriesPermissions', function(event, series) {
    	editPermissions(series);
	});
    $rootScope.$on('loadPermissions', function(event, seriesId) {
    	seriesId = seriesId || ($scope.model && $scope.model.id)
    	loadPermissions(seriesId);
	});
    $scope.edit = function(permissionId) {
    	$rootScope.$emit('editSeriesPermission', permissionId);
	}
    $scope.addPermissions = function(){
    	$scope.form.$setPristine();
    	var selectedAccounts = _.filter($scope.accounts, isSelected);
    	if (selectedAccounts.length < 1){
    		error("Please select at least one account!");
    		return;
    	}
    	var selectedAccountIds = _.map(selectedAccounts, id);
    	grant(selectedAccountIds, api.option2mode($scope.option), $scope.model.id);
    	_.each($scope.accounts, function(it){ delete it.isSelected; });
    	
    	function isSelected(it){ return it.isSelected; }
    	function id(it){ return it.id; }
    }
	$scope.close = function() {
		$scope.dialog.modal('hide');
	};
	$scope.isHidden = function(it){
		return it.hide;
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
	
	function editPermissions(series) {
		$scope.model = series;
		resetView();
		loadPermissions($scope.model.id);
		loadAccounts();
		$scope.dialog.modal();
		
		function resetView(){
			api.removeAllAlerts($scope.alertParent);
			$scope.form.$setPristine();
			$scope.permissions = [];
			$scope.accounts = [];
		}
	};
	
	function grant(accountIds, mode, seriesId){
		emitBusy();
		var p = _.extend(mode, {accountIds: accountIds, seriesId: seriesId});
		var path = 'series/' + seriesId + '/permissions';
		api.posting(path, p).then(ok, fail);
		
		function ok(rsp) {
			emitDone();
			success('Added the access to the accounts');
			$scope.permissions = rsp.data.results;
			loadPermissions($scope.model.id);
		}
		
		function fail(err){
			emitDone();
			if (err.data)
				error(err.data.userMessage);
			else
				error('Failed to load the permissions!');
		}
	}
	
	function loadPermissions(seriesId){
		if (! seriesId)
			return;
		var path = 'series/' + seriesId + '/permissions';
		api.finding(path).then(success, fail);
		
		function success(rsp) {
			$scope.permissions = rsp.data.results;
			$scope.viewPermissions = _.map(rsp.data.results, omit);
			
			function omit(e) { 
				var e1 = _.omit(e, 'series'); 
				e1.account = _.pick(e1.account, 'name');
				return e1;
			}
		}
		
		function fail(err){
			if (err.data)
				error(err.data.userMessage);
			else
				error('Failed to load the permissions!');
		}
	}
	
	function loadAccounts(){
		var path = 'accounts';
		api.finding(path).then(success, fail);
		
		function success(rsp) {
			$scope.accounts = rsp.data.results;
			refreshACL();
		}
		
		function fail(err){
			if (err.data)
				error(err.data.userMessage);
			else
				error('Failed to load accounts!');
		}
	}

	function success(message){
		alert('Success: ' + message, 'alert-success');
	}
	
	function error(message){
		alert('Error: ' + message, 'alert-danger');
	}
	
	function alert(message, classes){
		api.alert($scope.alertParent, message, classes);
	}
	
	function emitBusy(){
		$scope.isWorking = true;
		$rootScope.$emit('modalBusyDialog');
	}
	
	function emitDone(){
		$scope.isWorking = false;
		$rootScope.$emit('hideBusyDialog');
	}
});