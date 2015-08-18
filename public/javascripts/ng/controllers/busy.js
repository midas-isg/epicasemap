"use strict";

app.controller("Busy", function($rootScope){
	var dialog = $('#busy-modal');
	dialog.find('#busy-modal-close').hide();
	
	$rootScope.$on('modalBusyDialog', function(event) {
		dialog.isDone = false;
		setTimeout(function() {dialog.modal()}, 1000);
	});

	dialog.on('show.bs.modal', function (e) {
		if (dialog.isDone) {
			e.preventDefault();
        	e.stopImmediatePropagation();
        }
    });
    
    $rootScope.$on('hideBusyDialog', function(event) {
		dialog.isDone = true;
		dialog.modal('hide');
	});

    dialog.on('hide.bs.modal', function (e) {
		if ( ! dialog.isDone) {
			e.preventDefault();
        	e.stopImmediatePropagation();
        }
    });
});
