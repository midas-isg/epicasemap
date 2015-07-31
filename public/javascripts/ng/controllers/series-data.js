"use strict"

app.controller('SeriesData', function($scope, $rootScope, api) {
	$scope.view = {};
	$scope.dialog = $('#dataModal');
    $scope.dialog.on('shown.bs.modal', function (e) {
    	$scope.dialog.find('form').find(':input:enabled:visible:first').focus();
    });

	$scope.closeDialog = function() {
		$scope.dialog.modal('hide');
	};
    $rootScope.$on('uploadNewSeriesData', function(event, seriesId) {
    	showDialog(seriesId);
	});
    $scope.uploadThenClose = function() {
    	uploadThenClose();
	};
	
	function showDialog(seriesId){
		$scope.seriesId = seriesId;
		$scope.dialog.modal();
	}
	
	function uploadThenClose(){
		var seriesId = $scope.seriesId;
		var path = "fileUpload/" + seriesId + "/" + encodeURIComponent($("#delimiter").val()) + "/" + encodeURIComponent($("#format").val());
        var file = $scope.dataFile;
        api.uploadFile(path, file).then(function(rsp) {
			$scope.closeDialog();
			loadCoordinates($scope.seriesId);

			$("#data-file").replaceWith($("#data-file").clone(true));
			delete($scope.dataFile);
		});
	}
	
	function loadCoordinates(seriesId) {
    	$rootScope.$emit('loadCoordinates', seriesId);
	};
});