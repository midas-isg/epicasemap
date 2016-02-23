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
			else {
				$scope.showSummary = false;
				$scope.showSubmitButton = false;
				$scope.showResolverForm = true;
			}
			
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
			
			if(!ambiguitiesList[ambiguitiesListKeys[currentLocationIndex]].requery) {
				$scope.showRequeryText = false;
				$scope.showQueryInput = false;
			}
			else {
				$scope.showRequeryText = true;
				$scope.showQueryInput = true;
			}
			
			$scope.currentInputLabel = ambiguitiesListKeys[currentLocationIndex];
			
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
											$scope.requeryResults = {matches: [{label: "No results found"}], selectedLocationID: null};
											ambiguitiesList[$scope.currentInputLabel].requeryResults = {matches: null, selectedLocationID: null};
											ambiguitiesList[$scope.currentInputLabel].requeryInput = null;
										}
										
									break;
									
									default:
										$scope.requeryResults = {matches: [{label: "Error"}], selectedLocationID: null};
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
						$scope.requeryResults = {matches: [{label: "No results found"}], selectedLocationID: null};
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
		
		//TODO: move this so that it isn't recreated each time submitSelections is invoked!
		function validatesSubmission(selectedMappings, submissionMeta) {
			var i,
				submitUnmapped = false;
			
			for(i = 0; i < ambiguitiesListKeys.length; i++) {
				if(!ambiguitiesList[ambiguitiesListKeys[i]].selectedLocationID &&
					(!(ambiguitiesList[ambiguitiesListKeys[i]].requeryResults && ambiguitiesList[ambiguitiesListKeys[i]].requeryResults.selectedLocationID))) {
					if(!submitUnmapped) {
						if(!confirm("There are unmapped entries. Press OK to ignore those entries and submit.\nOtherwise press Cancel and finish selecting mappings.")) {
							return false;
						}
						
						submitUnmapped = true;
						submissionMeta.description += " \n[Unmapped location(s): "
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
			var selectedMappings = {},
				submissionMeta = {
					creator: seriesMeta.creator,
					description: seriesMeta.description,
					id: seriesMeta.id,
					isVersionOf: seriesMeta.isVersionOf,
					license: seriesMeta.license,
					lock: seriesMeta.lock,
					ownerId: seriesMeta.owner.id,
					publisher: seriesMeta.publisher,
					seriesDataUrl: seriesMeta.seriesDataUrl,
					title: seriesMeta.title,
					version: seriesMeta.version
				};
			
			if(validatesSubmission(selectedMappings, submissionMeta)) {
				$.ajax({
							url: CONTEXT + "/api/series/" + $scope.seriesID + "/save-tycho",
							type: "PUT",
							contentType: "application/json",
							dataType: "json",
							data: JSON.stringify({selectedMappings: selectedMappings, url: $scope.url, seriesID: $scope.seriesID}),
							beforeSend: function(xhr) {
								$scope.isWorking = true;
								$scope.$emit('modalBusyDialog');
								$scope.closeDialog();
								
								return;
							},
							success: function(result, status, xhr) {
								console.log(result);
								alert("Saved data series");
								//refresh Series Editor data
								location.reload();
								
								return;
							},
							error: function(xhr, status, error) {
								if(xhr.status === 201) { //this happens because 201 is only a success when datatype = text; since we are PUT-ing json, however...
									console.log(xhr.statusText);
									alert("Saved data series");
									//refresh Series Editor data
									location.reload();
								}
								else {
									console.log(xhr);
									console.log(status);
									console.log(error);
									alert("Failed to save data series");
									
									$rootScope.$emit('ambiguityResolver',
										{
											url: $scope.url,
											seriesID: $scope.seriesID,
											data: ambiguitiesList,
											seriesMeta: seriesMeta
										}
									);
								}
								
								return;
							},
							complete: function(xhr, status) {
								$scope.isWorking = false;
								$scope.$emit('hideBusyDialog');
								
								return;
							}
				});
				
				$.ajax({
							url: CONTEXT + "/api/series/" + $scope.seriesID,
							type: "PUT",
							contentType: "application/json",
							dataType: "json",
							data: JSON.stringify(submissionMeta),
							beforeSend: function(xhr) {
								//$scope.isWorking = true;
								//$scope.$emit('modalBusyDialog');
								
								return;
							},
							success: function(result, status, xhr) {
								console.log(result);
								//alert("Saved series description");
								
								return;
							},
							error: function(xhr, status, error) {
								if(xhr.status === 204) { //this happens because 201 is only a success when datatype = text; since we are PUT-ing json, however...
									console.log(xhr.statusText);
								}
								else {
									console.log(xhr);
									console.log(status);
									console.log(error);
									alert("Failed to save series description");
								}
								
								return;
							},
							complete: function(xhr, status) {
								//$scope.isWorking = false;
								//$scope.$emit('hideBusyDialog');
								
								return;
							}
				});
			}
			else {
				alert("Please finish mapping location entries before submitting");
			}
			
			return;
		}
		
		$scope.review = function(locationIndex) {
			reviewing = !reviewing;
			switchIndex(locationIndex);
			
			return;
		}
		
		return;
	}
});
