"use strict";

app.controller('SeriesData', function($scope, $rootScope, api) {
	var dom = cacheDom();
	populateScope();
	bindEvents();
	
	function cacheDom(){
		var dom = {$dialog: $('#dataModal')};
		dom.$form = dom.$dialog.find('form');
		dom.$alertParent = dom.$dialog.find('.modal-body');
		return dom;
	}
	
	function bindEvents(){
	    $rootScope.$on('uploadNewSeriesData', showDialog);
	    dom.$dialog.on('shown.bs.modal', focusFirstFormInput);

		function showDialog(event, seriesId){
			$scope.seriesId = seriesId;
			api.removeAllAlerts(dom.$alertParent);
			dom.$dialog.modal();
		}
		
		function focusFirstFormInput(event) {
	    	dom.$form.find(':input:enabled:visible:first').focus();
	    }
	}
	
	function populateScope(){
		$scope.view = {};
		$scope.closeDialog = function() { 
			dom.$dialog.modal('hide'); 
		};
	    $scope.uploadThenClose = uploadThenClose;
	}

	function uploadThenClose(){
		$scope.isWorking = true;
		$rootScope.$emit('modalBusyDialog');
        api.uploading(makePath(), $scope.dataFile).then(function(rsp) {
        	emitDone();
       		$scope.closeDialog();
       		loadCoordinates($scope.seriesId);
		}, function (reason){
        	emitDone();
    		api.alert(dom.$alertParent, reason.statusText + ': ' + reason.data, 'alert-danger');
		});
        
        function emitDone(){
        	$scope.isWorking = false;
        	$rootScope.$emit('hideBusyDialog');
        }

        function makePath(){
        	return 'series/' + $scope.seriesId + '/data';
        }
        
        function loadCoordinates(seriesId) {
        	$rootScope.$emit('loadCoordinates', seriesId);
    	}
	}
});