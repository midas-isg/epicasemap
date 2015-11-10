app.controller('Busy', function($rootScope){
	'use strict';
	// init controller /////////////////////////////////////////////////////////
	var my = {}; 
	my.dom = cacheDom();
	my.dom.$dialog.find('#busy-modal-close').hide();
	bindEvents();
	// init functions //////////////////////////////////////////////////////////
	function cacheDom(){
		return { $dialog: $('#busy-modal')};
	}
	
	function bindEvents(){
		$rootScope.$on('modalBusyDialog', function(event) {
			my.isDone = false;
			setTimeout(function() {my.dom.$dialog.modal()}, 1000);
		});
		my.dom.$dialog.on('show.bs.modal', function (event) {
			if (my.isDone) 
				cancelEvent(event);
	    });
	    $rootScope.$on('hideBusyDialog', function(event) {
			my.isDone = true;
			my.dom.$dialog.modal('hide');
		});
	    my.dom.$dialog.on('hide.bs.modal', function (event) {
			if (!my.isDone) 
				cancelEvent(event);
	    });
	    
	    function cancelEvent(event){
	    	event.preventDefault();
	    	event.stopImmediatePropagation();
	    }
	}
	// helper functions ////////////////////////////////////////////////////////
});