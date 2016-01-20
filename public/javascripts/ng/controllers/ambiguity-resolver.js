"use strict";

app.controller('AmbiguityResolver', function($scope, $rootScope, api) {
	var dom = cacheDom(),
		ambiguitiesList,
		currentLocationIndex,
		ambiguitiesListKeys,
		reviewing;
	
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
console.log(resultData);
			ambiguitiesList = resultData;
window.ambiguitiesList = ambiguitiesList;
			ambiguitiesListKeys = Object.keys(ambiguitiesList);
			currentLocationIndex = -1;
			reviewing = false;
			
			$("#resolution-space").css("height", (window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight) >> 1);
			
			for(i = 0; i < ambiguitiesListKeys.length; i++) {
				$scope.locationEntries[i] = {};
				$scope.locationEntries[i].key = ambiguitiesListKeys[i];
				
				$scope.locationEntries[i].id = "location-" + i;
				$scope.locationEntries[i].value = ambiguitiesList[ambiguitiesListKeys[i]].selectedLocationLabel || "[No mapping selected!]";
				$scope.locationEntries[i].index = i;
				$scope.locationEntries[i].class = "";
				$scope.locationEntries[i].onClick = function() {
					return $scope.review(this.index)
				};
			}
			
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
				$scope.locationEntries[i].value = ambiguitiesList[ambiguitiesListKeys[i]].selectedLocationLabel || "[No mapping selected!]";
				
				if(ambiguitiesList[ambiguitiesListKeys[i]].requery) {
					$scope.locationEntries[i].class = "btn-warning";
					$scope.locationEntries[i].value = ambiguitiesList[ambiguitiesListKeys[i]].requeryInput.label + ": " + ambiguitiesList[ambiguitiesListKeys[i]].requeryResults.selectedLabel;
				}
				else if(ambiguitiesList[ambiguitiesListKeys[i]].selectedLocationLabel) {
					$scope.locationEntries[i].class = "btn-primary";
				}
				else {
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
					label: ambiguitiesList[$scope.currentInputLabel][0].alsidqueryInput.details.currentInputLabel,
					date: ambiguitiesList[$scope.currentInputLabel][0].alsidqueryInput.details.date,
					state: ambiguitiesList[$scope.currentInputLabel][0].alsidqueryInput.details.state,
					city: ambiguitiesList[$scope.currentInputLabel][0].alsidqueryInput.details.city,
					locationType: ambiguitiesList[$scope.currentInputLabel][0].alsidqueryInput.details.locationType
				};
			
			$scope.requeryInput = requeryInput;
			
			$scope.suggestionList = {
				locations: []
			};
			
			for(i = 0; i < ambiguitiesList[$scope.currentInputLabel].length; i++) {
				$scope.suggestionList.locations.push({
					label: ambiguitiesList[$scope.currentInputLabel][i].label,
					alsID: ambiguitiesList[$scope.currentInputLabel][i].alsId
				});
			}
			
			$scope.suggestionList.selectedLocationID = ambiguitiesList[$scope.currentInputLabel].selectedLocationID || ambiguitiesList[$scope.currentInputLabel][0].alsId;
			$scope.requeryResults = ambiguitiesList[$scope.currentInputLabel].requeryResults;
			
			return;
		}
		
		$scope.editLocationQuery = function() {
			$scope.showQueryInput = !$scope.showQueryInput;
			return;
		}
		
		$scope.flagForRequery = function() {
			ambiguitiesList[ambiguitiesListKeys[currentLocationIndex]].requery = !ambiguitiesList[ambiguitiesListKeys[currentLocationIndex]].requery;
			
			if(ambiguitiesList[ambiguitiesListKeys[currentLocationIndex]].requery) {
				$scope.showRequeryText = true;
				$scope.showQueryInput = true;
				
				function requeryInstance(input) {
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
									ambiguitiesList[$scope.currentInputLabel].requeryResults = {
										matches: xhr.responseJSON[input.label],
										selectedLocationID: xhr.responseJSON[input.label][0].alsId
									};
									
									$scope.requeryResults = ambiguitiesList[$scope.currentInputLabel].requeryResults;
								break;
								
								default:
									console.log(xhr);
									console.log(status);
									console.log(error);
								break;
							}
							
							return;
						}
					});
					
					return;
				}
				
				ambiguitiesList[$scope.currentInputLabel].requeryInput = $scope.requeryInput;
				requeryInstance($scope.requeryInput);
			}
			else {
				$scope.showRequeryText = false;
				$scope.showQueryInput = false;
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
				ambiguitiesList[ambiguitiesListKeys[currentLocationIndex]].requeryResults.selectedLocationID = $scope.requeryResults.selectedLocationID;
				ambiguitiesList[ambiguitiesListKeys[currentLocationIndex]].requeryResults.selectedLabel = $("#selected-requery :selected").first().text();
			}
			
			ambiguitiesList[ambiguitiesListKeys[currentLocationIndex]].selectedLocationID = $scope.suggestionList.selectedLocationID;
			ambiguitiesList[ambiguitiesListKeys[currentLocationIndex]].selectedLocationLabel = $("#selected-location :selected").first().text();
			
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
