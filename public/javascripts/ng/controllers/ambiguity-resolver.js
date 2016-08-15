"use strict";

app.controller('AmbiguityResolver', function($scope, $rootScope, api) {
	var LS_URL = "http://betaweb.rods.pitt.edu/ls/browser?id=",
		dom = cacheDom(),
		ambiguitiesList,
		currentLocationIndex,
		ambiguitiesListKeys,
		reviewing,
		requeryCallbackID,
		seriesMeta;
	
	populateScope();
	bindEvents();
	
	function cacheDom() {
		var dom = {$dialog: $('#ambiguity-resolver')};
		dom.$form = dom.$dialog.find('resolver-form');
		dom.$alertParent = dom.$dialog.find('.modal-body');
		
		return dom;
	}
	
	function bindEvents() {
		$rootScope.$on('ambiguityResolver', showDialog);
		dom.$dialog.on('shown.bs.modal', focusFirstFormInput);
		
		function showDialog(event, resultData) {
			var i,
				locationEntry;
			
			$scope.url = resultData.url;
			$scope.seriesID = resultData.seriesID;
			ambiguitiesList = resultData.data;
			ambiguitiesListKeys = Object.keys(ambiguitiesList);
			seriesMeta = resultData.seriesMeta;
			currentLocationIndex = -1;
			reviewing = true;
			
			$("#resolution-space").css("height", (window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight) >> 1);
			
			for(i = 0; i < ambiguitiesListKeys.length; i++) {
				$scope.locationEntries[i] = {};
				$scope.locationEntries[i].key = ambiguitiesListKeys[i];
				
				if(ambiguitiesList[ambiguitiesListKeys[i]].possibleMappings.length === 1) {
					ambiguitiesList[ambiguitiesListKeys[i]].selectedLocationID = ambiguitiesList[ambiguitiesListKeys[i]].possibleMappings[0].alsId;
					ambiguitiesList[ambiguitiesListKeys[i]].selectedLocationLabel = ambiguitiesList[ambiguitiesListKeys[i]].possibleMappings[0].label;
				}
				
				$scope.locationEntries[i].id = "location-" + i;
				$scope.locationEntries[i].value = ambiguitiesList[ambiguitiesListKeys[i]].selectedLocationLabel || "[No mapping selected!]";
				$scope.locationEntries[i].index = i;
				$scope.locationEntries[i].class = "";
				$scope.locationEntries[i].onClick = function() {
					return $scope.review(this.index)
				};
			}
			
			$("#view-ls").click(function() {
				var url = LS_URL + $scope.suggestionList.selectedLocationID;
				window.open(url);
				
				return;
			});
			
			$("#view-requery-ls").click(function() {
				var url = LS_URL + $scope.requeryResults.selectedLocationID;
				window.open(url);
				
				return;
			});
			
			$scope.editNextLocation();
			
			/*
			api.removeAllAlerts(dom.$alertParent);
			*/
			dom.$dialog.modal();
			
			return;
		}
		
		function focusFirstFormInput(event) {
			dom.$form.find(':input:enabled:visible:first').focus();
			
			return;
		}
		
		return;
	}
	
	function populateScope() {
		$scope.locationEntries = [];
		$scope.showEditPreviousLocation = false;
		$scope.showEditNextLocation = true;
		$scope.showResolverForm = true;
		$scope.showSummary = false;
		$scope.showSubmitButton = false;
		$scope.showRequeryText = false;
		$scope.showQueryInput = false;
		$scope.reviewButtonText = "Edit Mapping";
		
		$scope.closeDialog = function() { 
			dom.$dialog.modal('hide');
			
			return;
		};
		
		function displayReview() {
			var i,
				locationEntry;
			
			$scope.showEditPreviousLocation = false;
			$scope.showEditNextLocation = false;
			$scope.showResolverForm = false;
			$scope.showSummary = true;
			$scope.showSubmitButton = true;
			
			for(i = 0; i < ambiguitiesListKeys.length; i++) {
				if(ambiguitiesList[ambiguitiesListKeys[i]].requery && ambiguitiesList[ambiguitiesListKeys[i]].requeryInput) {
					$scope.locationEntries[i].class = "btn-warning";
					$scope.locationEntries[i].value = ambiguitiesList[ambiguitiesListKeys[i]].requeryInput.label + ": " +
						ambiguitiesList[ambiguitiesListKeys[i]].requeryResults.selectedLocationLabel;
				}
				else if(ambiguitiesList[ambiguitiesListKeys[i]].selectedLocationLabel) {
					$scope.locationEntries[i].value = ambiguitiesList[ambiguitiesListKeys[i]].selectedLocationLabel;
					$scope.locationEntries[i].class = "btn-primary";
				}
				else {
					$scope.locationEntries[i].value = "[No mapping selected!]";
					$scope.locationEntries[i].class = "";
				}
			}
			
			return;
		}
		
		function switchIndex(inputIndex) {
			var i,
				requeryInput;
			
			if(inputIndex >= 0) {
				currentLocationIndex = inputIndex;
			}
			
			if(reviewing) {
				return displayReview();
			}

			$scope.showSummary = false;
			$scope.showSubmitButton = false;
			$scope.showResolverForm = true;
			
			if(currentLocationIndex === 0) {
				$scope.showEditPreviousLocation = false;
			}
			else {
				$scope.showEditPreviousLocation = true;
			}
			
			if(currentLocationIndex === (ambiguitiesListKeys.length - 1)) {
				$scope.showEditNextLocation = false;
			}
			else {
				$scope.showEditNextLocation = true;
			}
			
			$scope.currentInputLabel = ambiguitiesListKeys[currentLocationIndex];
			
			if(!ambiguitiesList[$scope.currentInputLabel].requery) {
				$scope.showRequeryText = false;
				$scope.showQueryInput = false;
			}
			else {
				$scope.showRequeryText = true;
				$scope.showQueryInput = true;
			}
			
			$scope.reloadResultTypes();
			
			requeryInput = ambiguitiesList[$scope.currentInputLabel].requeryInput ||
				{
					label: ambiguitiesList[$scope.currentInputLabel].alsIDQueryInput.details.currentInputLabel,
					date: ambiguitiesList[$scope.currentInputLabel].alsIDQueryInput.details.date,
					state: ambiguitiesList[$scope.currentInputLabel].alsIDQueryInput.details.state,
					city: ambiguitiesList[$scope.currentInputLabel].alsIDQueryInput.details.city,
					locationType: ambiguitiesList[$scope.currentInputLabel].alsIDQueryInput.details.locationType
				};
			
			$scope.requeryInput = requeryInput;
			
			$scope.suggestionList = {
				locations: [{label: "No results found"}]
			};
			
			if(ambiguitiesList[$scope.currentInputLabel].possibleMappings.length > 0) {
				$scope.suggestionList.locations.pop();
				$scope.suggestionList.locations.push({
					label: ambiguitiesList[$scope.currentInputLabel].possibleMappings[0].label,
					alsID: ambiguitiesList[$scope.currentInputLabel].possibleMappings[0].alsId
				});
				
				for(i = 1; i < ambiguitiesList[$scope.currentInputLabel].possibleMappings.length; i++) {
					$scope.suggestionList.locations.push({
						label: ambiguitiesList[$scope.currentInputLabel].possibleMappings[i].label,
						alsID: ambiguitiesList[$scope.currentInputLabel].possibleMappings[i].alsId
					});
				}
				
				if(ambiguitiesList[$scope.currentInputLabel].selectedLocationID) {
					$scope.suggestionList.selectedLocationID = ambiguitiesList[$scope.currentInputLabel].selectedLocationID;
				}
				else {
					$scope.suggestionList.selectedLocationID = ambiguitiesList[$scope.currentInputLabel].possibleMappings[0].alsId;
					ambiguitiesList[$scope.currentInputLabel].selectedLocationID = $scope.suggestionList.selectedLocationID;
					ambiguitiesList[$scope.currentInputLabel].selectedLocationLabel = ambiguitiesList[$scope.currentInputLabel].possibleMappings[0].label;
				}
			}
			else {
				$scope.showQueryInput = false;
				$scope.editLocationQuery();
				$scope.requeryInput.label = $scope.currentInputLabel;
				$scope.requeryInputEvent();
			}
			
			$scope.requeryResults = ambiguitiesList[$scope.currentInputLabel].requeryResults;
			
			return;
		}
		
		$scope.editLocationQuery = function() {
			$scope.showQueryInput = !$scope.showQueryInput;
			ambiguitiesList[ambiguitiesListKeys[currentLocationIndex]].requery = $scope.showQueryInput;
			$scope.showRequeryText = $scope.showQueryInput;
			
			return;
		}
		
		$scope.requeryInputEvent = function() {
			clearTimeout(requeryCallbackID);
			requeryCallbackID = setTimeout(function(){ return $scope.flagForRequery(); }, 750);
			
			return;
		}
		
		$scope.flagForRequery = function() {
			if(ambiguitiesList[ambiguitiesListKeys[currentLocationIndex]].requery) {
				function requeryInstance($scope) {
					var input = $scope.requeryInput;
					
					if(input.label.length > 0) {
						$.ajax({
							url: CONTEXT + "/api/locations/data-location",
							type: "POST",
							contentType: "application/json",
							dataType: "json",
							data: JSON.stringify(input),
							beforeSend: function(xhr) {
								$scope.requeryResults = {matches: [{label: "Searching...", alsId: 0}], selectedLocationID: 0};
								$scope.$apply();

								return;
							},
							success: function(result, status, xhr) {
								console.log(result);

								return;
							},
							error: function(xhr, status, error) {
								switch(xhr.status) {
									case 300:
										//tie data to scope variable
										if(xhr.responseJSON[input.label] && xhr.responseJSON[input.label].length > 0) {
											ambiguitiesList[$scope.currentInputLabel].requeryResults = {
												matches: xhr.responseJSON[input.label],
												selectedLocationID: xhr.responseJSON[input.label][0].alsId
											};
											
											$scope.requeryResults = ambiguitiesList[$scope.currentInputLabel].requeryResults;
											ambiguitiesList[$scope.currentInputLabel].requeryResults.selectedLocationLabel = $scope.requeryResults.matches[0].label;
											ambiguitiesList[$scope.currentInputLabel].requeryInput = $scope.requeryInput;
										}
										else {
											$scope.requeryResults = {matches: [{label: "No results found", alsId: 0}], selectedLocationID: 0};
											ambiguitiesList[$scope.currentInputLabel].requeryResults = {matches: null, selectedLocationID: null};
											ambiguitiesList[$scope.currentInputLabel].requeryInput = null;
										}

										$scope.reloadResultTypes();

									break;
									
									default:
										$scope.requeryResults = {matches: [{label: "Error", alsId: 0}], selectedLocationID: 0};
										console.log(xhr);
										console.log(status);
										console.log(error);
									break;
								}
								
								$scope.$apply();
								
								return;
							}
						});
					}
					else {
						$scope.requeryResults = {matches: [{label: "No results found", alsId: 0}], selectedLocationID: 0};
						ambiguitiesList[$scope.currentInputLabel].requeryResults = {matches: null, selectedLocationID: null};
						ambiguitiesList[$scope.currentInputLabel].requeryInput = null;
						
						$scope.$apply();
					}
					
					return;
				}
				
				requeryInstance($scope);
			}
			else {
				$scope.showRequeryText = false;
				$scope.showQueryInput = false;
				$scope.requeryResults = {matches: [{label: "Please enter a location name to query"}], selectedLocationID: null};
			}
			
			return;
		}
		
		$scope.reloadResultTypes = function() {
			var i;
			
			$scope.requeryResultTypes = {};
			$scope.requeryResultTypes["[ALL]"] = "[ALL]";
			$scope.selectedRequeryType = $scope.requeryResultTypes["[ALL]"];
			
			if(ambiguitiesList[$scope.currentInputLabel].requeryResults) {
				$scope.requeryResultList = ambiguitiesList[$scope.currentInputLabel].requeryResults.matches;
				
				if($scope.requeryResultList) {
					for(i = 0; i < $scope.requeryResultList.length; i++) {
						$scope.requeryResultTypes[$scope.requeryResultList[i].locationTypeName] = $scope.requeryResultList[i].locationTypeName;
					}
				}
			}
			
			return;
		}
		
		$scope.changeRequeryType = function() {
			var i,
				requeryDisplayList = [];
			
			if(!$scope.selectedRequeryType) {
				return;
			}
			else if($scope.selectedRequeryType !== "[ALL]") {
				for(i = 0; i < $scope.requeryResultList.length; i++) {
					if($scope.requeryResultList[i].locationTypeName === $scope.selectedRequeryType) {
						requeryDisplayList.push($scope.requeryResultList[i]);
					}
				}
			}
			else {
				requeryDisplayList = $scope.requeryResultList;
			}
			
			$scope.requeryResults = {matches: requeryDisplayList, selectedLocationID: requeryDisplayList[0].alsId};
			ambiguitiesList[$scope.currentInputLabel].requeryResults.selectedLocationID = requeryDisplayList[0].alsId;
			ambiguitiesList[$scope.currentInputLabel].requeryResults.selectedLocationLabel = $scope.requeryResults.matches[0].label;
			
			return;
		}
		
		$scope.editPreviousLocation = function() {
			if(currentLocationIndex > 0) {
				switchIndex(currentLocationIndex - 1);
			}
			
			return;
		}
		
		$scope.editNextLocation = function() {
			if(currentLocationIndex < (Object.keys(ambiguitiesList).length - 1)) {
				switchIndex(currentLocationIndex + 1);
			}
			
			return;
		}
		
		$scope.locationChange = function() {
			if(ambiguitiesList[ambiguitiesListKeys[currentLocationIndex]].requeryResults) {
				if($scope.requeryResults.selectedLocationID) {
					ambiguitiesList[ambiguitiesListKeys[currentLocationIndex]].requeryResults.selectedLocationID = $scope.requeryResults.selectedLocationID;
					ambiguitiesList[ambiguitiesListKeys[currentLocationIndex]].requeryResults.selectedLocationLabel = $("#selected-requery :selected").first().text();
				}
			}
			else {
				ambiguitiesList[ambiguitiesListKeys[currentLocationIndex]].selectedLocationID = $scope.suggestionList.selectedLocationID;
				ambiguitiesList[ambiguitiesListKeys[currentLocationIndex]].selectedLocationLabel = $("#selected-location :selected").first().text();
			}
			
			return;
		}
		
		function validatesSubmission(selectedMappings, submissionMeta) {
			var i,
				replaceIndex,
				submitUnmapped = false;
			
			for(i = 0; i < ambiguitiesListKeys.length; i++) {
				if(!ambiguitiesList[ambiguitiesListKeys[i]].selectedLocationID &&
					(!(ambiguitiesList[ambiguitiesListKeys[i]].requeryResults && ambiguitiesList[ambiguitiesListKeys[i]].requeryResults.selectedLocationID))) {
					if(!submitUnmapped) {
						if(!confirm("There are unmapped entries. Press OK to ignore those entries and submit.\nOtherwise press Cancel and finish selecting mappings.")) {
							return false;
						}
						
						submitUnmapped = true;
						replaceIndex = submissionMeta.description.search(/ \x5BUnmapped location/);
							if(replaceIndex >= 0) {
								submissionMeta.description = submissionMeta.description.slice(0, replaceIndex);
							}
						submissionMeta.description += " \n[Unmapped location(s): ";
					}
					
					submissionMeta.description += (ambiguitiesList[ambiguitiesListKeys[i]].alsIDQueryInput.locationName + "; ");
				}
				
				selectedMappings[ambiguitiesListKeys[i]] = ambiguitiesList[ambiguitiesListKeys[i]];
			}
			
			if(submitUnmapped) {
				submissionMeta.description += "]";
			}
			
			return true;
		}
		
		$scope.submitSelections = function() {
			function buildBody(model) {
				var body = _.omit(model, 'owner');
				body.ownerId = model.owner && model.owner.id 
				return body;
			}
			
			var selectedMappings = {},
				submissionMeta = buildBody(seriesMeta);
			
			if(validatesSubmission(selectedMappings, submissionMeta)) {
				$scope.saveAs(submissionMeta, selectedMappings);
			}
			else {
				alert("Please finish mapping location entries before submitting");
			}
			
			return;
		}
		
		$scope.saveAs = function(series, selectedMappings) {
			api.saving('series', series).then(function(location) {
					//save series data here
					$scope.saveSeriesData(selectedMappings);
				},
				function(err) {
					console.log(err);
					//api.alert($scope.alertParent, 'Failed to save the series.', 'alert-danger');
					alert('Failed to save the series.');
					
					return;
				}
			);
			
			return;
		}
		
		$scope.saveSeriesData = function(selectedMappings) {
			$.ajax({
				url: CONTEXT + "/api/series/" + $scope.seriesID + "/save-tycho",
				type: "PUT",
				contentType: "application/json",
				dataType: "json",
				data: JSON.stringify({selectedMappings: selectedMappings, url: $scope.url, seriesID: $scope.seriesID}),
				beforeSend: function(xhr) {
					$scope.isWorking = true;
					$scope.$emit('modalBusyDialog');
					
					return;
				},
				success: function(result, status, xhr) {
					console.log(result);
					$rootScope.$emit('refreshSeriesEditor', $scope.seriesID);
					$rootScope.$emit('loadCoordinates', $scope.seriesID);
					$scope.closeDialog();
					
					return;
				},
				error: function(xhr, status, error) {
					if(xhr.status === 201) { //this happens because 201 is only a success when datatype = text; since we are PUT-ing json, however...
						console.log(xhr.statusText);
						$scope.$emit('refreshSeriesEditor', $scope.seriesID);
						$scope.$emit('loadCoordinates', $scope.seriesID);
						$scope.closeDialog();
					}
					else {
						console.log(xhr);
						console.log(status);
						console.log(error);
						api.alert(dom.$alertParent, "Failed to save data series", 'alert-danger');
					}
					
					return;
				},
				complete: function(xhr, status) {
					console.log("Creating TopoJSON using seriesID:" + seriesID);
					api.createTopoJSON($scope.seriesID);

					$scope.isWorking = false;
					$scope.$emit('hideBusyDialog');

					return;
				}
			});
		}
		
		$scope.review = function(locationIndex) {
			reviewing = !reviewing;

			if(reviewing) {
				$scope.reviewButtonText = "Edit Mapping";
			}
			else {
				$scope.reviewButtonText = "View Summary";
			}

			switchIndex(locationIndex);
			
			return;
		}
		
		return;
	}
});
