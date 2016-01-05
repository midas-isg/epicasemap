"use strict";

app.controller('AmbiguityResolver', function($scope, $rootScope, api) {
	var dom = cacheDom(),
		ambiguitiesList,
		currentLocationIndex;
	
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
			var keys = Object.keys(ambiguitiesList),
				i,
				inputQuery;
			
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
			
			//if(!flaggedForRequery)
			{
				$("#requery-text").hide();
				$("#query-input").hide();
			}
			/*
			else {
				$("#requery-text").show();
				$("#query-input").show();
			}
			*/
			
			$scope.currentInputLabel = keys[currentLocationIndex];
			$scope.suggestionList = [];
			
			inputQuery = ambiguitiesList[$scope.currentInputLabel][0].alsidqueryInput.details;
			$scope.aliasLabel = $scope.currentInputLabel;
			$scope.state = inputQuery.state;
			$scope.city = inputQuery.city;
			$scope.locationType = inputQuery.locationType;
			
			for(i = 0; i < ambiguitiesList[$scope.currentInputLabel].length; i++) {
				$scope.suggestionList.push({});
				$scope.suggestionList[i].index = i;
				$scope.suggestionList[i].label = ambiguitiesList[$scope.currentInputLabel][i].label;
				$scope.suggestionList[i].alsID = ambiguitiesList[$scope.currentInputLabel][i].id;
			}
			$scope.locationSelection = $scope.suggestionList[0];
			
			return;
		}
		
		$scope.editLocationQuery = function() {
			$("#query-input").toggle();
			return;
		}
		
		$scope.flagForRequery = function() {
			$("#requery-text").toggle();
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
		
		return;
	}
});
