"use strict";

app.controller('SeriesData', function($scope, $rootScope, api) {
	var dom = {};
	cacheDOM();
	bindEvents();
	$scope.view = {};
	$scope.closeDialog = function() { 
		dom.$dialog.modal('hide'); 
	};
    $scope.uploadThenClose = uploadThenClose;
	
	function cacheDOM(){
		dom.$dialog = $('#dataModal');
		dom.$form = dom.$dialog.find('form');
	}
	
	function bindEvents(){
	    $rootScope.$on('uploadNewSeriesData', showDialog);
	    dom.$dialog.on('shown.bs.modal', focusFirstFormInput);

		function showDialog(event, seriesId){
			$scope.seriesId = seriesId;
			dom.$dialog.modal();
		}
		
		function focusFirstFormInput(event) {
	    	dom.$form.find(':input:enabled:visible:first').focus();
	    }
	}
	
	function uploadThenClose(){
        api.uploadFile(makePath(), $scope.dataFile).then(function(rsp) {
			$scope.closeDialog();
			loadCoordinates($scope.seriesId);
		});

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
});