/*
common.js
useful helper functions
*/

function getURLParameterByName(name) {
	name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
	var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
		results = regex.exec(location.search);
	
	return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

function findParameterByName(searchName, inputString) {
	searchName = searchName.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
	var regex = new RegExp("[\\?&]" + searchName + "=([^&#]*)"),
		results = regex.exec(inputString);
	
	return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

function setTextValue(selector, input) {
	$(selector).text(input);
	$(selector).val(input);
	
	return;
}

function getValueText(selector) {
	var value = $(selector).val();
	
	if(value == "") {
		return $(selector).text();
	}
	
	return value;
}
