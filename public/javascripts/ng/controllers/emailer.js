"use strict";

app.controller('Emailer', function($scope, $rootScope, api) {
	var LS_URL = "http://betaweb.rods.pitt.edu/ls/browser?id=",
		dom = cacheDom(),
		sender = "Anonymous",
		senderID,
		senderEmail,
		recipient,
		requestID,
		requestTitle;
	
	populateScope();
	bindEvents();
	
	function cacheDom() {
		var dom = {$dialog: $('#emailer')};
		dom.$form = dom.$dialog.find('emailer-form');
		dom.$alertParent = dom.$dialog.find('.modal-body');
		
		return dom;
	}
	
	function bindEvents() {
		$rootScope.$on('emailer', showDialog);
		dom.$dialog.on('shown.bs.modal', focusFirstFormInput);
		
		function showDialog(event, resultData, my) {
			console.log(resultData);
			console.log(my);
			sender = USER.name;
			senderID = USER.id;
			senderEmail = USER.email;
			recipient = resultData.owner.email;
			requestID = resultData.id;
			requestTitle = resultData.title;

			$scope.emailSubject = sender + " is requesting permission to use '" + requestTitle + "'";
			$scope.emailBody = $scope.emailSubject;

			//api.removeAllAlerts(dom.$alertParent);
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

		$scope.emailRequest = function() {
			var input = {
				sender: sender,
				senderEmail: senderEmail,
				recipient: recipient,
				subject: $scope.emailSubject,
				body: $scope.emailBody
			};

			$.ajax({
				url: CONTEXT + "/api/vizs/" + requestID + "/permissions/request",
				type: "POST",
				contentType: "application/json",
				data: JSON.stringify(input),
				success: function(result, status, xhr) {
					console.log(result);
					console.log(status);
					console.log(xhr);

					return;
				},
				error: function(xhr, status, error) {
					console.error(xhr);
					console.error(status);
					console.error(error);

					return;
				}
			});

			return;
		}

		return;
	}
});
