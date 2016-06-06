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
			$scope.seriesId = series.id;

			(function initialize() {
				var currentYear = new Date().getFullYear(),
				fetchServiceBaseURL = CONTEXT + "/api/series/tycho/json?type=";

				$("#start-date").attr("max", currentYear);
				$("#end-date").attr("max", currentYear);

				api.gettingFromUrl(fetchServiceBaseURL + "cities").then(function success(response) {
						$scope.tychoLocations = {locations: response.data};
						$scope.tychoLocations.locations.push("[All]");
						$scope.tychoQueryLoc = "[All]";

						return;
					},
					function error() {
						alert("Failed to connect to Tycho Server");

						return;
					}
				);

				api.gettingFromUrl(fetchServiceBaseURL + "states").then(function success(response){//result, status, xhr) {
						$scope.tychoStates = {abbreviations: response.data};
						$scope.tychoStates.abbreviations.push("[All]");
						$scope.tychoQueryState = "[All]";

						return;
					},
					function error() {
						alert("Failed to connect to Tycho Server");

						return;
					}
				);

				api.gettingFromUrl(fetchServiceBaseURL + "diseases").then(function success(response) {
						$scope.tychoDiseases = {names: response.data};
						$scope.tychoDiseases.names.push("[All]");
						$scope.tychoQueryDisease = "[All]";

						return;
					},
					function error() {
						alert("Failed to connect to Tycho Server");

						return;
					}
				);

				return;
			})();

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

		$scope.toggleTychoQueryWizard = function() {
			$scope.showTychoQueryWizard = !$scope.showTychoQueryWizard;
			$("#tycho-query-wizard-button").toggleClass("active");
			
			return;
		}
		
		$scope.buildTychoQuery = function() {
			var optionString;
			$scope.url = "http://www.tycho.pitt.edu/api/query?";
			
			optionString = "loc_type=" + $scope.tychoQueryLocType;
			$scope.url += optionString;
			
			if($scope.tychoQueryEvent !== "[All]") {
				optionString = "&event=" + $scope.tychoQueryEvent;
				$scope.url += optionString;
			}
			
			if($scope.tychoQueryState !== "[All]") {
				optionString = "&state=" + encodeURIComponent($scope.tychoQueryState);
				$scope.url += optionString;
			}
			
			if($scope.tychoQueryLoc !== "[All]") {
				optionString = "&loc=" + encodeURIComponent($scope.tychoQueryLoc);
				$scope.url += optionString;
			}
			
			if($scope.tychoQueryDisease !== "[All]") {
				optionString = "&disease=" + encodeURIComponent($scope.tychoQueryDisease);
				$scope.url += optionString;
			}
			
			if($scope.tychoQueryStart != null) {
				optionString = "&start=" + $scope.tychoQueryStart;
				$scope.url += optionString;
			}
			
			if($scope.tychoQueryEnd != null) {
				optionString = "&end=" + $scope.tychoQueryEnd;
				$scope.url += optionString;
			}
			
			$scope.urlContentType = "Tycho";
			$scope.radioIn = 'url';
			$scope.overrideValidation = true;
			
			return;
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
		
		$scope.decideUpload = function() {
			if(!$scope.isWorking) {
				if($scope.radioIn === "file" && $scope.dataFile) {
					$scope.uploadThenClose();
				}
				else if(!$scope.form.url.$invalid || $scope.overrideValidation) {
					$scope.uploadViaUrlThenClose();
				}
			}
			
			return;
		}
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
		
		if(($scope.urlContentType === "Tycho") && (!findParameterByName("apikey", $scope.url))) {
			$scope.url += "&apikey=9a4c75183895f07e7776";
		}
		
		api.uploadingViaUrl(makePath(), $scope.url).then(function(rsp) {
				emitDone();
				
				if(rsp.status === 204) /* no content */ {
					console.log(rsp);
					api.alert(dom.$alertParent, rsp.statusText + ': 0 results found for this query', 'alert-warning');
					
					return;
				}
				
				$scope.closeDialog();
				loadCoordinates($scope.seriesId);
				
				return;
			},
			function (reason) {
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
				else if(reason.status === 300) /* multiple choices */ {
					ambiguityResolverData = {data: reason.data, url: $scope.url, seriesMeta: $scope.series, seriesID: $scope.seriesId};
					$rootScope.$emit('ambiguityResolver', ambiguityResolverData);
					$scope.closeDialog();
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