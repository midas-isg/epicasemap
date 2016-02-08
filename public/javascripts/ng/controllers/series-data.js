"use strict";

app.controller('SeriesData', function($scope, $rootScope, api) {
	var dom = cacheDom();
	populateScope();
	bindEvents();
	
	function cacheDom() {
		var dom = {$dialog: $('#dataModal')};
		dom.$form = dom.$dialog.find('form');
		dom.$alertParent = dom.$dialog.find('.modal-body');
		return dom;
	}
	
	function bindEvents() {
		$rootScope.$on('uploadNewSeriesData', showDialog);
		dom.$dialog.on('shown.bs.modal', focusFirstFormInput);

		function showDialog(event, series) {
			$scope.series = series;
			$scope.seriesId = $scope.series.id;
			if($scope.series.seriesDataUrl != null){
				$scope.url = $scope.series.seriesDataUrl.url;
				$scope.radioIn = "url";
			}
			$scope.isWorking = $scope.series.lock;
			api.removeAllAlerts(dom.$alertParent);
			dom.$dialog.modal();
		}
		
		function focusFirstFormInput(event) {
			dom.$form.find(':input:enabled:visible:first').focus();
		}
	}
	
	function populateScope() {
		$scope.radioIn= 'file';
		$scope.url;
		$scope.overWrite = false;
		
		$scope.urlContentTypes = [
			"CSV",
			"Tycho"
			//,{type: "Apollo"}
		];
		$scope.urlContentType = $scope.urlContentTypes[0];
		
		$scope.view = {};
		$scope.closeDialog = function() { 
			dom.$dialog.modal('hide'); 
		};
		$scope.uploadThenClose = uploadThenClose;
		$scope.uploadViaUrlThenClose = uploadViaUrlThenClose;
	}

	function uploadThenClose() {
		$scope.isWorking = true;
		$rootScope.$emit('modalBusyDialog');
		api.uploading(makePath(), $scope.dataFile).then(function(rsp) {
			emitDone();
			$scope.closeDialog();
			loadCoordinates($scope.seriesId);
		}, function (reason) {
			emitDone();
			api.alert(dom.$alertParent, reason.statusText + 
					': ' + reason.data && reason.data.userMessage, 'alert-danger');
		});
		
		function emitDone() {
			$scope.isWorking = false;
			$rootScope.$emit('hideBusyDialog');
		}

		function makePath() {
			return 'series/' + $scope.seriesId + '/data';
		}
		
		function loadCoordinates(seriesId) {
			$rootScope.$emit('loadCoordinates', seriesId);
		}
	}
	
	function uploadViaUrlThenClose() {
		$scope.isWorking = true;
		$rootScope.$emit('modalBusyDialog');
		
		api.uploadingViaUrl(makePath(), $scope.url).then(function(rsp) {
			emitDone();
				$scope.closeDialog();
				loadCoordinates($scope.seriesId);
			}, function (reason) {
				emitDone();
				var isOK = true,
					ambiguityResolverData;
				
				if(reason.status === 409) {
					isOK = confirm("It seems URL content already exists. \nOK = Overwrite existing series data");
					if (isOK) {
						$scope.overWrite=true;
						uploadViaUrlThenClose();
					}
				}
				else if(reason.status === 300) /*multiple choices*/ {
					//for each index of reason.data, summon modal with filtering options for selection, then process when finished
					console.log("Multiple Choices:");
console.log($scope.series);
					ambiguityResolverData = {data: reason.data, url: $scope.url, seriesID: $scope.seriesId};
					$rootScope.$emit('ambiguityResolver', ambiguityResolverData);
				}
				else if(reason.status === 404) /*not found*/ {
					//for each index of reason.data, summon modal to edit empty list entries
					console.log("Not Found:");
					console.log(reason.data);
					//$rootScope.$emit('');
				}
				else {
					api.alert(dom.$alertParent, reason.statusText + 
						': ' + reason.data && reason.data.userMessage, 'alert-danger');
				}
			}
		);
		
		function emitDone() {
			$scope.isWorking = false;
			$scope.overWrite = false;
			$rootScope.$emit('hideBusyDialog');
		 }
		
		function makePath() {
			if($scope.urlContentType === "Tycho") {
				return 'series/' + $scope.seriesId + '/data-tycho' + '?overWrite=' + $scope.overWrite;
			}
			 
			return 'series/' + $scope.seriesId + '/data-url' + '?overWrite=' + $scope.overWrite;
		}
		 
		function loadCoordinates(seriesId) {
		 	$rootScope.$emit('loadCoordinates', seriesId);
	 	}
	}
});