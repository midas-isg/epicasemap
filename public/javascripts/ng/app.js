"use strict";

var app = angular.module('app', [])
.config(function($locationProvider) {
	  $locationProvider.html5Mode({
		  enabled: true,
		  requireBase: false
		}).hashPrefix('!');
});

app.run(function(){
	$( document ).ready(resizeResizableTable);
	$( window ).resize(resizeResizableTable);

	function resizeResizableTable() {
		var tc = $(".table-content-resizable");
		var th = $(".table-header");
		var height = $(window).height() - ($("body").height() - tc.height()) - 20;
		var minHeight = $( "tr:first" ).height() * 3;
		tc.css('max-height', Math.max(height, minHeight) + 'px');
	}
});

app.directive('appFileModel', function ($parse) {
    return {
        restrict: 'A',
        link: function(scope, element, attrs) {
            var model = $parse(attrs.appFileModel);
            
            element.bind('change', function(){
                scope.$apply(function(){
                    model.assign(scope, element[0].files[0]);
                });
            });
        }
    };
});
