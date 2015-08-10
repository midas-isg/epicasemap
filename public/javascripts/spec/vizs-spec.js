'use strict';

describe('Controller: Vizs', function () {
    var scope = {};
    var addNew = 'addNew';

    beforeEach(module('app'));
    beforeEach(initController('Vizs'));
    
    describe('when ' + addNew + ' is invoked', function () {
    	var sut;

    	//beforeEach(initController('Viz'));
    	beforeEach(function(){
    		spyOn(scope.vizs.$root, '$emit');//.andCallThrough();
    		sut = scope.vizs[addNew];
    		sut();
    	});
    	
    	it('should do something', function () {
    		expect(sut).not.toBeUndefined();
    		expect(scope.vizs.$root.$emit).toHaveBeenCalled();
    		
    		//expect(scope.viz.model).not.toBeUndefined();
    		//$scope.showAll = isNew;
        });
    });
    
    function initController(name, key){
    	key = key || name.toLowerCase();
    	return inject(function ($controller, $rootScope, api) {
    		scope[key] = $rootScope.$new();
            $controller(name, {
                $scope: scope[key],
                $rootScope: $rootScope,
                api: api
            });
        })
    }
});