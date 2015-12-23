"use strict";

app.controller('AmbiguityResolver', function($scope, $rootScope, api) {
	var dom = cacheDom();
	//populateScope();
	bindEvents();
	
	function cacheDom(){
		var dom = {$dialog: $('#ambiguity-resolver')};
		dom.$form = dom.$dialog.find('form');
		dom.$alertParent = dom.$dialog.find('.modal-body');
		
		return dom;
	}
	
	function bindEvents(){
		$rootScope.$on('ambiguityResolver', showDialog);
		dom.$dialog.on('shown.bs.modal', focusFirstFormInput);

		function showDialog(event, ambiguitiesList) {
			console.log(ambiguitiesList);
			var keys = Object.keys(ambiguitiesList),
				i;
			
			$scope.currentInputLabel = keys[0];
			$scope.suggestionList = [];
			
			for(i = 0; i < ambiguitiesList[keys[0]].length; i++) {
				$scope.suggestionList.push({});
				$scope.suggestionList[i].index = i;
				$scope.suggestionList[i].label = ambiguitiesList[keys[0]][i].label;
				$scope.suggestionList[i].alsID = ambiguitiesList[keys[0]][i].id;
			}
			$scope.locationSelection = $scope.suggestionList[0].label;
			
			/*
			$scope.series = series;
			$scope.seriesId = $scope.series.id;
			if($scope.series.seriesDataUrl != null){
				$scope.url = $scope.series.seriesDataUrl.url;
				$scope.radioIn = "url";
			}
			$scope.isWorking = $scope.series.lock;
			api.removeAllAlerts(dom.$alertParent);
			*/
			dom.$dialog.modal();
		}
		
		function focusFirstFormInput(event) {
			dom.$form.find(':input:enabled:visible:first').focus();
		}
	}
});
