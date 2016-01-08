"use strict";

app.controller('AmbiguityResolver', function($scope, $rootScope, api) {
	var dom = cacheDom(),
		ambiguitiesList,
		currentLocationIndex,
		keys;
	
	populateScope();
	bindEvents();
	
	function cacheDom() {
		var dom = {$dialog: $('#ambiguity-resolver')};
		dom.$form = dom.$dialog.find('form');
		dom.$alertParent = dom.$dialog.find('.modal-body');
		
		return dom;
	}
	
	function bindEvents() {
		$rootScope.$on('ambiguityResolver', showDialog);
		dom.$dialog.on('shown.bs.modal', focusFirstFormInput);
		
		function showDialog(event, resultData) {
console.log(resultData);
			ambiguitiesList = resultData;
window.ambiguitiesList = ambiguitiesList;
			keys = Object.keys(ambiguitiesList);
			currentLocationIndex = -1;
			
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
		$scope.closeDialog = function() { 
			dom.$dialog.modal('hide');
			
			return;
		};
		
		function switchIndex() {
console.log("currentLocationIndex: " + currentLocationIndex);
			var i,
				requeryInput;
			
			if(currentLocationIndex === 0) {
				$("#edit-previous-location").hide();
			}
			else {
				$("#edit-previous-location").show();
			}
			
			if(currentLocationIndex === (keys.length - 1)) {
				$("#edit-next-location").hide();
			}
			else {
				$("#edit-next-location").show();
			}
			
			if(!ambiguitiesList[keys[currentLocationIndex]].requery) {
				$("#requery-text").hide();
				$("#query-input").hide();
			}
			else {
				$("#requery-text").show();
				$("#query-input").show();
			}
			
			$scope.currentInputLabel = keys[currentLocationIndex];
			
			requeryInput = ambiguitiesList[$scope.currentInputLabel].requeryInput ||
				{
					label: ambiguitiesList[$scope.currentInputLabel][0].alsidqueryInput.details.currentInputLabel,
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
			
			return;
		}
		
		$scope.editLocationQuery = function() {
			$("#query-input").toggle();
			return;
		}
		
		$scope.flagForRequery = function() {
			ambiguitiesList[keys[currentLocationIndex]].requery = !ambiguitiesList[keys[currentLocationIndex]].requery;
			
			if(ambiguitiesList[keys[currentLocationIndex]].requery) {
				$("#requery-text").show();
				$("#query-input").show();
				
				ambiguitiesList[$scope.currentInputLabel].requeryInput = $scope.requeryInput;
			}
			else {
				$("#requery-text").hide();
				$("#query-input").hide();
			}
			
			return;
		}
		
		$scope.editPreviousLocation = function() {
			if(currentLocationIndex > 0) {
				currentLocationIndex--;
				switchIndex();
			}
			
			return;
		}
		
		$scope.editNextLocation = function() {
			if(currentLocationIndex < (Object.keys(ambiguitiesList).length - 1)) {
				currentLocationIndex++;
				switchIndex();
			}
			
			return;
		}
		
		$scope.locationChange = function() {
			ambiguitiesList[keys[currentLocationIndex]].selectedLocationID = $scope.suggestionList.selectedLocationID;
			
			return;
		}
		
		return;
	}
});
