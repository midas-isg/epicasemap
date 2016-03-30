"use strict";

app.controller('Emailer', function($scope, $rootScope, api) {
	var dom = cacheDom(),
		sender = "Anonymous",
		senderID,
		senderEmail,
		recipient,
		requestID,
		requestTitle,
		requestType;
	
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
		
		function showDialog(event, resultData, my, type) {
			console.log(resultData);
			console.log(my);
			sender = USER.name;
			senderID = USER.id;
			senderEmail = USER.email;
			recipient = resultData.owner.email;
			requestID = resultData.id;
			requestTitle = resultData.title;
			requestType = type;
			
			$scope.emailSubject = sender + " is requesting permission to use the " + requestType + ", '" + requestTitle + "'";
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
					recipient: recipient,
					subject: $scope.emailSubject,
					body: $scope.emailBody
				},
				url = CONTEXT + "/api/vizs/" + requestID + "/permissions/request";

			if(requestType === "series") {
				url = CONTEXT + "/api/series/" + requestID + "/permissions/request";
			}

			invokeSender(url, input);

			return;
		};

		function invokeSender(url, input) {
			$.ajax({
				url: url,
				type: "POST",
				contentType: "application/json",
				data: JSON.stringify(input),
				success: function (result, status, xhr) {
					console.log(result);
					console.log(status);
					console.log(xhr);

					//TODO: open & close working dialog
					//TODO: close dialog

					return;
				},
				error: function (xhr, status, error) {
					console.error(xhr);
					console.error(status);
					console.error(error);

					return;
				}
			});
		}

		return;
	}
});
