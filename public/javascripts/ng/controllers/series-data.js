"use strict"

app.controller('SeriesData', function($scope, $rootScope, api) {
	$scope.view = {};
	
	$scope.dialog = $('#dataModal');
    $scope.dialog.on('shown.bs.modal', function (e) {
    	$scope.dialog.find('form').find(':input:enabled:visible:first').focus();
    });

    /*$scope.submit = function(callback) {
		if ($scope.form.$dirty) {
			save(callback);
		} else if (callback) {
			callback();
		}
	};
	$scope.submitThenClose = function() { $scope.submit(close);	};
	$scope.removeThenClose = function() {
		if (confirm("About to delete this Series. \nOK = Delete"))
			api.remove('series', $scope.model.id).then(close);
	};*/
	$scope.closeDialog = function() {
		$scope.dialog.modal('hide');
	};
    $rootScope.$on('uploadNewSeriesData', function(event, seriesId) {
    	uploadNewData(seriesId);
	});

	
	function uploadNewData(seriesId){
		$scope.seriesId = seriesId;
		$scope.dialog.modal();
		$("#form").submit(function(e) {
			var formData = new FormData($(this)[0]);
			upload(formData);
			return false;
		});
	}
	
	function upload(formData){
		var seriesId = $scope.seriesId;
		var postURL = "/epidemap/api/fileUpload/" + seriesId + "/" + encodeURIComponent($("#delimiter").val()) + "/" + encodeURIComponent($("#format").val());
		
		$.ajax({
			url: postURL,
			data: formData,
			type: 'POST',
			async: false,
			mimeType:"multipart/form-data",
			contentType: false,
			cache: false,
			processData:false,
			success: function (data,status,xhr) {
				$scope.closeDialog();
				console.log("success in FileUpload()",status);
				loadCoordinates($scope.seriesId);
				$("#csv_file").replaceWith($("#csv_file").clone(true));
			},
			error: function(error, status) {
	  			console.log(error.responseText,status);
			}
		});
	}
	
	function loadCoordinates(seriesId) {
    	$rootScope.$emit('loadCoordinates', seriesId);
	};


});