app.controller('Viz', function($scope, $rootScope, api) {
	'use strict';
	// init controller /////////////////////////////////////////////////////////
	var my = app.initCommonControllerFeatures($scope, $rootScope, api);
	
	(function initializeShowAllCheckbox() {
		return $("#vizFilter").append($("#showAllCheckbox").detach());
	})();
	
	my.dom = cacheDom();
	my.apiPath = 'vizs';
	populateScope();
	loadModels();
	bindEvents();
	// init functions //////////////////////////////////////////////////////////
	function cacheDom(){
		var $dialog = $('#modal');
		return {
			$alertParent: $dialog.find('.modal-body'),
			$form: $dialog.find('form'),
			$dialog: $dialog
		};
	}
	
	function populateScope(){
		$scope.can = my.canAccessSeries;
		$scope.countSelected = countSelected;
		$scope.editSeries = my.editSeries;
		$scope.submit = submit;
		$scope.removeThenClose = removeThenClose;
		$scope.submitThenClose = function() { $scope.submit(close);	};
		$scope.close = function() {
			my.model = $scope.model;
			$scope.model = null;
			my.dom.$dialog.modal('hide');
		};
		$scope.isShown = function(series) {
			return $scope.showAll || series.isSelected;
		};
		$scope.isHidden = function(series) {
			return ! $scope.isShown(series);
		};
		$scope.invertSelection = function(){
			invert('isSelected');
		}
		$scope.isModelEditable = function(){
			return my.canAccessViz('change', $scope.model);
		};
		$scope.permit = function(series) {
			$scope.$emit('editSeriesPermissions', series);
		};
	}

	function loadModels(){
	    loadAllSeries();
	}
	
	function bindEvents(){
		$(document).ready(function() {
			$('#filterPlaceholderNotToCountAsPartOfForm').replaceWith($('#seriesFilter'));
		});
	    $scope.$watch('model', function() { updateAllSeries($scope.model); });
	    my.dom.$dialog.on('hide.bs.modal', function (e) {
	    	var isOK = my.confirmNoSaving(e, $scope.form.$dirty, loadVizs);
	    	if (!isOK)
	    		$scope.model = my.model;
	    });
	    my.dom.$dialog.on('shown.bs.modal', function (e) {
	    	my.dom.$form.find(':input:enabled:visible:first').focus();
	    });
	    $rootScope.$on('editViz', function(event, viz) {
	    	edit(viz);
		});
	    $rootScope.$on('loadSeries', function(event) {
	    	loadAllSeries();
		});
	}
	// helper functions ////////////////////////////////////////////////////////
	function countSelected() {
		var key = 'isSelected';
		return _.countBy($scope.allSeries, function(s) {
			return s[key] ? key : 'others';
		})[key] || 0;
	}
	
	function invert(key){
		$scope.allSeries.forEach(function (series) {
			  series[key] = ! series[key];
		});
		$scope.form.$setDirty();
	}
	
	function loadVizs(){
		$scope.$emit('loadVizs');
	}
	
	function loadAllSeries(){
		var doing = api.finding('series');
		var callback = function(rsp) {
			$scope.allSeries = rsp.data.results;
			updateAllSeries($scope.model);
		};
		doThen(doing, callback, "load your Series", false);
		
		loadPermissions();
		
		function loadPermissions(){
			api.finding('accounts/my-permissions').then(function(rsp) {
				$scope.permissions = rsp.data.results;
			}, function(err){
				$scope.error = 'Failed to load your permissions!';
				my.alertError(null, $scope.error);
			});
		}
	}
	
	function doThen(doing, callback, doText, shouldAlertSuccess){
		my.emitBusy();
		doing.then(function(rsp) {
			my.emitDone();
			if (shouldAlertSuccess)
				my.alertSuccess("Succeed to " + doText);
			$scope.form.$setPristine();
			if (callback){
				callback(rsp);
			}
		}, function(err){
			my.emitDone();
			my.alertError(err, "Failed to " + doText);
		});
	}


	function updateAllSeries(viz){
		var wasPristine = $scope.form.$pristine;
		if (viz && $scope.allSeries){
			check(viz.allSeries.map(byId), 'isSelected');
			$scope.allSeries = removeIrrelevant($scope.allSeries);
		}
		if (wasPristine)
			$scope.form.$setPristine();

		function byId(series) { return series.id; }
		
		function check(ids, key){
			$scope.allSeries.forEach(function (series) {
				  return series[key] = _.contains(ids, series.id);
			});
		}

		function removeIrrelevant(allSeries){
			return _.filter(allSeries, function(series){
				return series.isSelected || $scope.can('use', series);
			});
		}
	}

	function edit(viz) {
		$scope.model = viz;
		resetView();
		my.dom.$dialog.modal();
		
		function resetView(){
			api.removeAllAlerts(my.dom.$alertParent);
			$scope.form.$setPristine();
			if (viz.id) {
				$scope.showAll = false;
			} else {
				$scope.showAll = true;
			} 
		}
	}

	function buildBody(model) {
		var body = _.omit(model, 'allSeries', 'owner');
		body.seriesIds = toSeriesIds('isSelected');
		body.ownerId = model.owner && model.owner.id 
		return body;
		
		function toSeriesIds(key){
			var seriesObjects = filterByKey();
			return mapById();

			function filterByKey() {
				return _.filter($scope.allSeries, function(it) { 
					return it[key]; 
				});
			}

			function mapById() {
				return _.map(seriesObjects, function(it) { return it.id; });
			}
		}
	}
	
	function submit(callback) {
		if ($scope.form.$dirty) {
			save(callback);
		} else if (callback) {
			callback();
		}
	}
	
	function save(callback){
		var body = buildBody($scope.model);
		var id = $scope.model.id;
		var doing = api.saving(my.apiPath, body);
		if (! callback){
			callback = function(location){
				api.gettingFromUrl(location).then(function(rsp) {
					$scope.model = rsp.data.result;
				}, function(err){
					my.alertError(err, "Failed to read the Visualization!");
				});
			};
		}
		doThen(doing, callback, "save the Visualization");
	}
	
	function removeThenClose() {
		if (confirm("About to delete the Visualization. \nOK = Delete")){
			var doing = api.deleting(my.apiPath, $scope.model.id);
			doThen(doing, close, "delete the Visualization");
		}
	}

	function close(){
		my.emitDone();
		$scope.form.$setPristine();
		$scope.close();
	}
});