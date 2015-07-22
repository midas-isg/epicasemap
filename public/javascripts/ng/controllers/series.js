"use strict"

app.controller('Series', function($scope, $rootScope, api) {
	$scope.dialog = $('#seriesModal');
    $scope.dialog.on('hide.bs.modal', function (e) {
		var isOK = true;
		if ($scope.form.$dirty)
			isOK = confirm("Changes are not saved. \nOK = Close without save");
		if (isOK) {
			loadSeries();
		} else {
			e.preventDefault();
        	e.stopImmediatePropagation();
        }
    });
    $scope.dialog.on('shown.bs.modal', function (e) {
    	$scope.dialog.find('form').find(':input:enabled:visible:first').focus();
    });
    $rootScope.$on('editSeries', function(event, series) {
    	edit(series);
	});

	$scope.submit = function(callback) {
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
	};
	$scope.close = function() {
		$scope.dialog.modal('hide');
	};
	
	function edit(series) {
		var isNew = series.id ? false : true;
		$scope.model = series;
		$scope.form.isNew = isNew;
		$scope.dialog.modal();
	};
	function close(){
		$scope.form.$setPristine();
		$scope.close();
	}
	
	function loadSeries(){
		$rootScope.$emit('loadSeries');
	}
	
	function buildBody(model) {
		var body = _.omit(model);
		return body;
	}
	
	function save(callback){
		var body = buildBody($scope.model);
		$scope.working = true;
		api.save('series', body).then(function(location) {
			$scope.working = false;
			$scope.form.isNew = false;
			if (callback){
				callback();
			} else {
				api.get(location).then(function(rsp) {
					$scope.model = rsp.data.result;
				});
			}
		});
	}
});