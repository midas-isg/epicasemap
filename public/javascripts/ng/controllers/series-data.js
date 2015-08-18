"use strict";

app.controller('SeriesData', function($scope, $rootScope, api) {
	var dom = {};
	cacheDOM();
	bindEvents();
	populateScope();
	
	
	function cacheDOM(){
		dom.$dialog = $('#dataModal');
		dom.$form = dom.$dialog.find('form');
		dom.$alertParent = dom.$dialog.find('.modal-body');
	}
	
	function bindEvents(){
	    $rootScope.$on('uploadNewSeriesData', showDialog);
	    dom.$dialog.on('shown.bs.modal', focusFirstFormInput);

		function showDialog(event, seriesId){
			$scope.seriesId = seriesId;
			resetView();
			dom.$dialog.modal();
			
			function resetView(){
				api.removeAllAlerts(dom.$alertParent);
			}
		}
		
		function focusFirstFormInput(event) {
	    	dom.$form.find(':input:enabled:visible:first').focus();
	    }
	}
	
	function uploadThenClose(){
		$scope.isWorking = true;
		$rootScope.$emit('modalBusyDialog');
        api.uploadFile(makePath(), $scope.dataFile).then(function(rsp) {
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
        	return 'series/' + $scope.seriesId + '/data?' +
        	'delimiter=' + encodeURIComponent($scope.delimiter) + 
        	'&format=' + encodeURIComponent($scope.format);
        }
        
        function makePath_old(){
        	return "fileUpload/" + $scope.seriesId + 
        	"/" + encodeURIComponent($scope.delimiter) + 
        	"/" + encodeURIComponent($scope.format);
        }

        function loadCoordinates(seriesId) {
        	$rootScope.$emit('loadCoordinates', seriesId);
    	}
	}
	
	function populateScope(){
		$scope.view = {};
		$scope.closeDialog = function() { 
			dom.$dialog.modal('hide'); 
		};
	    $scope.uploadThenClose = uploadThenClose;
	}
});