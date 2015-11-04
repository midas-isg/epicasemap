'use strict'; `comment: use ES6`;

app.test.workaroundForRealHttpCallsUsingNgMockAndNgMockE2E();

describe('Controller: Viz', function() {
	var asynCallAfterTruthy = app.test.asynCallAfterTruthy;
	var $scope;
	
	app.test.init('Viz');
	beforeEach(done => {
		$scope = app.test.scope.viz;
		app.test.loginAsPublic(done);
	});
	
	describe('when loaded', shouldLoadAllSeries);
	
	let loadSeries = 'loadSeries';
	describe(`when ${loadSeries} was receipted`, function() {
		beforeEach(function(done){
			asynCallAfterTruthy($scope, 'allSeries', done, function() {
				$scope.allSeries = null;
				$scope.$emit(loadSeries);
			});
		});
		
		shouldLoadAllSeries();
	});
	
	let editViz = 'editViz';
	describe(`when ${editViz} was receipted`, function() {
		beforeEach(function(done){
			$scope.model = null;
			$scope.$emit(editViz, app.test.getValidViz());
			asynCallAfterTruthy($scope, 'model', done, function() {
				done();
			});
		});
		
		it('should load the Viz as the model', function(){
			var model = $scope.model;
			expect(model.id).toBe(app.test.getValidViz().id);
		});

		it('should close without change if close was invoked', function(done){
			spyOn(app.test.getApi(), 'saving');
			spyOn(app.test.getApi(), 'deleting');
			$scope.close();
			var unregister =$scope.$watch('model', function(){
				var model = $scope.model;
				if (model === null){
					expect(app.test.getApi().saving).not.toHaveBeenCalled();
					expect(app.test.getApi().deleting).not.toHaveBeenCalled();
					unregister();
					done();
				}
			});
		});
	});
	
    describe('when a new Viz model was persisted', function() {
    	var viz;
    	beforeEach(function(done){
        	viz = createValidViz();
    		persistNewViz(viz, done);
    	});
    	
    	afterEach(function(){
    		if (viz.id)
    			app.test.deleteViz(viz.id);
    	});
    	
    	let removeThenClose = 'removeThenClose';
    	describe(`when ${removeThenClose} was invoked`, function(){ 
    		it('should delete the model', function(done) {
				$scope.model = viz;
				spyOn(window, 'confirm').and.returnValue(true);
				$scope[removeThenClose]();
				var unregister =$scope.$watch('model', function(){
					if ($scope.model === null){
						console.log("The Viz was deleted: id=" + viz.id);
						readViz(viz.id, function(it){
							expect(it).toBeNull();
							done();
						});
						unregister();
						delete viz.id;
					}
				});
    		});
    	});
    	
    	let submit = 'submit';
        describe(`when ${submit} was invoked`, function() {
    		it('should persist the model', function(done) {
    			var desc = 'description added by Acceptance test';
    			$scope.model = viz;
    			$scope.model.description = desc;
        		$scope.form.$dirty = true;
        		$scope[submit]();
        		delete $scope.model.description;
				var unregister = $scope.$watch('model', function(){
					if ($scope.model.description){
						readViz(viz.id, function(model){
							expect(model.id).toBe(viz.id);
							expect(model.description).toBe(desc);
							done();
						});
						unregister();
					}
				});
    		});
        });

        let submitThenClose = 'submitThenClose';
        describe(`when ${submitThenClose} was invoked`, function() {
    		it('should persist the model', function(done) {
    			var desc = 'description added by Acceptance test';
    			$scope.model = viz;
    			$scope.model.description = desc;
        		$scope.form.$dirty = true;
        		$scope['submitThenClose']();
				var unregister = $scope.$watch('model', function(){
					if ($scope.model === null){
						readViz(viz.id, function(it){
							expect(it.id).toBe(viz.id);
							expect(it.description).toBe(desc);
							done();
						});
						unregister();
					}
				});
    		});
        });
    });
    
    function readViz(id, callback){
    	app.test.getApi().reading('vizs', id).then(function(rsp){
    		callback && callback(rsp.data.result);
		}, function(){
			callback && callback(null);
		});
    }
    
    function persistNewViz(viz, done){
    	app.test.getApi().saving('vizs', viz).then(function(location){
    		var id = toId(location);
			console.log("The Viz was persisted: id="+id);
			viz.id = id;
			done && done();
		});
    	
    	function toId(location){
    		var tokens = location.split('/');
    		return tokens[tokens.length - 1]  - 0;
    	}
    }
    
	function createValidViz(id){
		return {id:id, allSeries:[]};
	}

	function shouldLoadAllSeries(){
		var allSeries = 'allSeries';
		it('should load ' + allSeries, function(done) {
			asynCallAfterTruthy($scope, allSeries, done, function() {
				expect($scope[allSeries]).toBeNonEmptyArray();
			});
		});
	}
});