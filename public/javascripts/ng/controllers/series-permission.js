
app.controller('SeriesPermission', function($scope, $rootScope, api) {
	"use strict"
	$scope.view = {};
	
	$scope.dialog = $('#seriesPermissionModal');
	$scope.alertParent = $scope.dialog.find('.modal-body');
    $scope.dialog.on('hide.bs.modal', function (e) {
    	var isOK = true;
		if ($scope.form.$dirty)
			isOK = confirm("Changes are not saved. \nOK = Close without save");
		if (isOK) {
			loadPermissions();
		} else {
			e.preventDefault();
        	e.stopImmediatePropagation();
        }
    });
    $scope.dialog.on('shown.bs.modal', function (e) {
    	$scope.dialog.find('form').find(':input:enabled:visible:first').focus();
    });
    $rootScope.$on('editSeriesPermission', function(event, permission) {
    	edit(permission);
	});
	$scope.submit = function(callback) {
		if ($scope.form.$dirty) {
			save(callback);
		} else if (callback) {
			callback();
		}
	};
	$scope.submitThenClose = function() { $scope.submit(close);	};
	$scope.removeThenClose = function() {
		if (confirm("About to delete this Permission. \nOK = Delete"))
			emitBusy();
			api.deleting(path($scope.model), $scope.model.id).then(function(rsp){
				emitDone();
				close();
			}, function (err){
				emitDone();
				error(err.data && err.data.userMessage);
			});
	};
	$scope.close = function() {
		$scope.dialog.modal('hide');
	};
	$scope.isModelEditable = function(){ return true; };
	
	function edit(permission) {
		$scope.model = permission;
		$scope.option = api.mode2option(permission)
		resetView();
		$scope.dialog.modal();
		
		function resetView(){
			api.removeAllAlerts($scope.alertParent);
			$scope.form.$setPristine();
		}
	};
	
	function close(){
		$scope.form.$setPristine();
		$scope.close();
	}

	function loadPermissions(){
		$rootScope.$emit('loadPermissions');
	}
	
	function buildBody() {
		return api.option2mode($scope.option);
	}
	
	function save(callback){
		emitBusy();
		var body = buildBody();
		api.putting(path() + '/' + $scope.model.id + '/mode', body).then(function(location) {
			emitDone();
			success('The permission was saved.');
			if (callback){
				callback();
			} else {
				api.gettingFromUrl(location).then(function(rsp) {
					$scope.model = rsp.data.result;
					$scope.form.$setPristine();
				}, function(err){
					error('Failed to read the permission!');
				});
			}
		}, function(err){
			emitDone();
			error('Failed to save the permission!');
		});
	}
	
	function path(permission){
		return 'series/permissions'
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