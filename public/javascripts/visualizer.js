/*
visualizer.js
*/

(function() {
	var DEBUG = false;
	
	function MagicMap() {
		var i,
			j,
			temp,
			svgElement,
			svg,
			thisMap = this,
			mapID;
		
		this.mapTypes = [
			"financialtimes.map-w7l4lfi8",
			"mapbox.streets",
			"mapbox.dark"
		];
		
		mapID = getURLParameterByName("map");
		if(!mapID) {
			mapID = 0;
		}
		temp = this.mapTypes[mapID];
		
		L.mapbox.accessToken = 'pk.eyJ1IjoidHBzMjMiLCJhIjoiVHEzc0tVWSJ9.0oYZqcggp29zNZlCcb2esA';
		this.map = L.mapbox.map( 'map', temp,
			{ worldCopyJump: true, bounceAtZoomLimits: false, zoom: 2, minZoom: 2, center: [0, 0] });
		
		this.heat = []; //null;
		this.paused = false;
		this.frame;
		this.frameCount;
		this.startFrame;
		this.endFrame;
		this.reset = false;
		this.masterChart = null;
		this.detailChart = null;
		this.dataset = [];
		this.absoluteMaxValue = 0;
		this.earliestDate = null;
		this.latestDate = null;
		this.loopIntervalID;
		
		this.vizID = getURLParameterByName("id");
		
		this.suggestedDelay = 50;
		this.mostConcentratedFrame = 0;
		
		this.uiSettings = {
			series: [{index: -1, color: 0}],
			colorPalette: 0,
			bBox: [[], []],
			timeSelectionEvent: null,
			daysPerFrame: 1,
			renderDelay: null,
			pointDecay: 0.0231,
			dataGapMethod: "bridge" //zero, show, bridge
		};
		
		this.allContainedBox = [[], []];
		
		this.seriesList = [];
		this.seriesDescriptions = {};
		this.seriesToLoad = [];
		
		this.showControlPanel = false;
		this.showSecondary = false;
		this.showNumbers = false;
		
		this.playBack = false;
		this.displaySet = [];
		
		if(this.vizID) {
			this.loadVisualization(this.vizID);
		}
		else {
			location.assign(CONTEXT + "/manage/vizs");
		}
		
		for(i = 0; i < this.mapTypes.length; i++) {
			$("#map-selector").append("<option value='" + i + "'>" + this.mapTypes[i] + "</option>");
		}
		$("#map-selector").val(mapID);
		
		$("#map-selector").change(function() {
			sessionStorage.uiSettings = JSON.stringify(thisMap.uiSettings);
			
			return location.assign(CONTEXT + "/visualizer?id=" + thisMap.vizID + "&map=" + $(this).val());
		});
		
		
		this.setGradient = [];
		this.colorSet = [
			['#1b9e77', '#d95f02', '#7570b3', '#e7298a', '#66a61e'],
			['#a6cee3', '#1f78b4', '#b2df8a', "#33a02c", "#fb9a99"],
			['#66c2a5', '#fc8d62', '#8da0cb', "#e78ac3", "#a6d854"]
		];
		
		this.debugColor = ['#ff0000'];
		
		this.uiSettings.colorPalette = 0;
		this.colors = this.colorSet[this.uiSettings.colorPalette];
		
		for(i = 0; i < this.colorSet.length; i++) {
			svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
			svg.setAttribute("width", 15);
			svg.setAttribute("height", 75);
			
			$("#ramps").append("<div id='palette-" + i + "' class='ramp'></div>");
			$("#palette-" + i ).append(svg);
			
			temp = this.colorSet[i];
			for(j = 0; j < temp.length; j++) {
				svgElement = document.createElementNS("http://www.w3.org/2000/svg", "rect");
				svgElement.setAttributeNS(null, "width", 15);
				svgElement.setAttributeNS(null, "height", 15);
				svgElement.setAttributeNS(null, "y", 15 * j);
				svgElement.setAttributeNS(null, "fill", temp[j]);
				
				$(svg).append(svgElement);
			}
		}
		$("#palette-0").addClass("selected");
		
		return this;
	}
	
	MagicMap.prototype.saveVisualization = function() {
		if(DEBUG) { console.log("[DEBUG] called saveVisualization()"); }
		
		var URL = CONTEXT + "/api/vizs/" + this.vizID + "/ui-setting",
			bounds = this.map.getBounds();
		
		if(this.vizID) {
			this.uiSettings.bBox[0][0] = bounds.getSouth();
			this.uiSettings.bBox[0][1] = bounds.getWest();
			this.uiSettings.bBox[1][0] = bounds.getNorth();
			this.uiSettings.bBox[1][1] = bounds.getEast();

			$.ajax({
				url: URL,
				type: "PUT",
				data: JSON.stringify(this.uiSettings),
				contentType: "application/json; charset=UTF-8",
				success: function(result, status, xhr) {
					alert("Save successful");
					return;
				},
				error: function(xhr, status, error) {
					alert(xhr + "\n" + status + "\n" + error);
					return;
				}
			});
		}
		
		return;
	}
	
	MagicMap.prototype.loadVisualization = function(vizID) {
		if(DEBUG) { console.log("[DEBUG] called loadVisualization()"); }
		
		var URL = CONTEXT + "/api/vizs/" + vizID,
			thisMap = this;
		
		$.ajax({
			url: URL,
			success: function(result) {
				var h,
					i,
					svg,
					svgElement;
				
				$("#title").text(result.result.title);
				
				thisMap.seriesList = result.result.allSeries;
				
				for(h = 0; h < thisMap.seriesList.length; h++) {
					thisMap.seriesDescriptions[thisMap.seriesList[h].id] = {
						title: thisMap.seriesList[h].title,
						description: thisMap.seriesList[h].description
					};
				}
				
				if(result.result.uiSetting) {
					if(sessionStorage.uiSettings) {
						thisMap.uiSettings = JSON.parse(sessionStorage.uiSettings);
						sessionStorage.removeItem("uiSettings");
					}
					else {
						thisMap.uiSettings = JSON.parse(result.result.uiSetting);
						
						thisMap.uiSettings.daysPerFrame = (thisMap.uiSettings.daysPerFrame | 1);
						
						if(!thisMap.uiSettings.pointDecay) {
							thisMap.uiSettings.pointDecay = 0.0231;
						}
					}
					
					for(h = 0; h < thisMap.uiSettings.series.length; h++) {
						if(thisMap.seriesDescriptions[thisMap.uiSettings.series[h].index]) {
							thisMap.seriesToLoad.push(thisMap.uiSettings.series[h].index);
							
							thisMap.setGradient.push({
								0.0: thisMap.colors[thisMap.uiSettings.series[h].color]
							});
						}
						else {
							thisMap.uiSettings.series.splice(h, 1);
							h--;
						}
					}
					
					thisMap.map.fitBounds(thisMap.uiSettings.bBox);
					$("#palette-" + thisMap.uiSettings.colorPalette).click();
				}
				else {
					//no ui-settings defined so just load first five of the series using different colors
					for(h = 0; (h < thisMap.seriesList.length) && (h < 5); h++) {
						thisMap.uiSettings.series[h] = {index: thisMap.seriesList[h].id, color: h};
						thisMap.seriesToLoad.push(thisMap.uiSettings.series[h].index);
					}
				}
				
				for(i = 0; i < thisMap.seriesToLoad.length; i++) {
					thisMap.load(thisMap.seriesToLoad[i]);
				}
				
				for(h = 0; h < thisMap.uiSettings.series.length; h++) {
					thisMap.pushSeries();
					$("#series-" + h).val(thisMap.uiSettings.series[h].index);
					$("#color-selector-" + h + " #color-" + thisMap.uiSettings.series[h].color).addClass("selected");
				}
				
				$("#render-delay").val(thisMap.uiSettings.renderDelay);
				$("#render-delay").change();
				
				$("#days-per-frame").val(thisMap.uiSettings.daysPerFrame);
				$("#days-per-frame").change();
				
				$("#point-decay").val(thisMap.uiSettings.pointDecay);
				$("#point-decay").change();
				
				return;
			},
			error: function() {
				return;
			}
		});
		
		return;
	}
	
	MagicMap.prototype.initialize = function() {
		"use strict";
		if(DEBUG) { console.log("[DEBUG] called initialize()"); }
		
		var thisMap = this,
		i;
		
		document.getElementById('body').onkeyup = this.handleInput;
		this.loopIntervalID = setInterval(this.loop, 0);
		
		$("#reset-button").click(function() {
			var i;
			
			//console.log("Time Group:");
			//console.log(thisMap.dataset[].timeGroup);
			thisMap.loadBuffer();
			
			for(i = 0; i < thisMap.heat.length; i++) {
				thisMap.heat[i].redraw();
			}
			
			thisMap.masterChart.xAxis[0].removePlotLine('date-line');
			
			return;
		});
		
		$("#backstep-button").click(function() {
			var i,
				pastFrame = thisMap.frame;
			
			thisMap.frame = pastFrame - Math.ceil(1 / thisMap.uiSettings.pointDecay);
			
			if(thisMap.frame < 0) {
				thisMap.frame = 0;
			}
			
			thisMap.playBack = true;
			for(i = thisMap.frame; i < (pastFrame - 1); i++) {
				thisMap.playBuffer(thisMap.frame, thisMap.endFrame);
			}
			thisMap.packHeat();
			thisMap.playBack = false;
			
			thisMap.paused = true;
			thisMap.updatePlaybackInterface();
			
			return;
		});
		
		$("#playback-button").click(function() {
			thisMap.paused = !thisMap.paused;
			//console.log(new Date()); //real-time timestamp
			
			return thisMap.updatePlaybackInterface();
		});
		
		$("#toggle-details-button").click(function() {
			var i;
			
			$("#detail-container *").toggle();
			
			if($("#detail-container").css("pointer-events") === "auto") {
				$("#detail-container").css("pointer-events", "none");
			}
			else {
				$("#detail-container").css("pointer-events", "auto");
			}
			
			for(i = 0; i < thisMap.displaySet.length; i++) {
				thisMap.detailChart.series[i].update({color: thisMap.colors[thisMap.uiSettings.series[i].color]}, true);
			}
			
			return;
		});
		
		$("#toggle-secondary-button").click(function() {
			thisMap.showSecondary = !thisMap.showSecondary;
			thisMap.packHeat();
			
			return;
		});
		
		$("#toggle-numbers-button").click(function() {
			thisMap.showNumbers = !thisMap.showNumbers;
			thisMap.packHeat();
			
			return;
		});
		
		$("#step-forward-button").click(function() {
			thisMap.playBack = true;
			thisMap.playBuffer(thisMap.startFrame, thisMap.endFrame);
			thisMap.packHeat();
			thisMap.playBack = false;
			
			thisMap.paused = true;
			thisMap.updatePlaybackInterface();
			
			return;
		});
		
		$("#toggle-controls-button").click(function() {
			$("#control-panel").toggle();
			
			return;
		});
		
		$("#remove-series-button").click(function() {
			thisMap.popSeries();
			
			return;
		});
		
		$("#add-series-button").click(function() {
			var id = thisMap.pushSeries();
			
			$("#series-" + id).change();
			thisMap.setSeriesColor($("#color-selector-" + id + " #color-0")[0]);
			
			return;
		});
		
		for(i = 0; i < 3; i++) {
			setColorPaletteClickEvent(i);
		}
		
		function setColorPaletteClickEvent(index) {
			$("#palette-" + index).click(function() {
				if(!$("#palette-" + index).hasClass("selected")) {
					thisMap.uiSettings.colorPalette = index;
					thisMap.setColorPalette(index);
				}
				
				return;
			});
			
			return;
		}
		
		$("#render-delay").change(function() {
			if($(this).val() < 0) {
				$(this).val(0);
			}
			
			thisMap.uiSettings.renderDelay = $(this).val();
			
			return;
		});
		$("#render-delay").val(this.uiSettings.renderDelay);
		$("#render-delay").change();
		
		$("#days-per-frame").change(function() {
			if($(this).val() < 1) {
				$(this).val(1);
			}
			
			thisMap.uiSettings.daysPerFrame = $(this).val();
			
			return;
		});
		$("#days-per-frame").val(thisMap.uiSettings.daysPerFrame);
		$("#days-per-frame").change();
		
		$("#point-decay").change(function() {
			if($(this).val() < 0.0001) {
				$(this).val(0.0001);
			}
			
			thisMap.uiSettings.pointDecay = $(this).val();
			
			return;
		});
		$("#point-decay").val(thisMap.uiSettings.pointDecay);
		$("#point-decay").change();
		
		
		$("#data-gap-handler").change(function() {
			var zeroSeries = [],
				i,
				j;
			
			thisMap.uiSettings.dataGapMethod = $('input[name=data-gap-option]:checked', '#data-gap-handler').val();
			
			for(i = 0; i < thisMap.masterChart.series.length; i++) {
				zeroSeries[i] = [];
				for(j = 0; j < thisMap.dataset[i].frameAggregate.length; j++) {
					zeroSeries[i][j] = (thisMap.uiSettings.dataGapMethod === "zero") ? (thisMap.dataset[i].frameAggregate[j] + 0) : thisMap.dataset[i].frameAggregate[j];
				}
				
				thisMap.masterChart.series[i].update({ connectNulls: (thisMap.uiSettings.dataGapMethod === "bridge")});
				thisMap.detailChart.series[i].update({ connectNulls: (thisMap.uiSettings.dataGapMethod === "bridge")});
				thisMap.masterChart.series[i].setData(zeroSeries[i], true, false, false);
				thisMap.doSelection(thisMap.uiSettings.timeSelectionEvent);
			}
			
			return;
		});
		
		
		$("#save-button").click(function() {
			thisMap.saveVisualization();
			return;
		});
		
		$(window).resize(function() {
			thisMap.detailChart.reflow();
			thisMap.masterChart.reflow();
			
			return;
		});
		
		return;
	}
	
	MagicMap.prototype.pushSeries = function() {
		if(DEBUG) { console.log("[DEBUG] called pushSeries()"); }
		
		var optionID = $("#series-options").children().last().index() + 1,
			thisMap = this;
		
		function appendColorSelector(selectorID) {
			for(i = 0; i < thisMap.colors.length; i++) {
				$("#color-selector-" + selectorID).append("<div id='color-" + i + "' class='ramp'></div>");
				svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
				svg.setAttribute("width", 15);
				svg.setAttribute("height", 15);
				svgElement = document.createElementNS("http://www.w3.org/2000/svg", "rect");
				svgElement.setAttributeNS(null, "width", 15);
				svgElement.setAttributeNS(null, "height", 15);
				svgElement.setAttributeNS(null, "fill", thisMap.colors[i]);
				$(svg).append(svgElement);
				
				$("#color-selector-" + selectorID + " #color-" + i).append(svg);
				
				$("#color-selector-" + selectorID + " #color-" + i).click(function() {
					thisMap.setSeriesColor(this);
				});
			}
			
			return;
		}
		
		function appendSeriesSelector(selectorID) {
			if(selectorID >= thisMap.uiSettings.series.length) {
				selectorID = thisMap.uiSettings.series.length;
				thisMap.uiSettings.series.push({ color: 0, index: thisMap.seriesList[0].id });
			}
			
			thisMap.displaySet.push({visiblePoints: [], secondValues: [], hide: false});
			
			$("#series-options").append(
				"<div style='clear: both;'>" +
					"<h5 class='no-margin'>Select series " + String.fromCharCode(selectorID + 65) + "</h5>" +
					"<select id='series-" + selectorID + "' style='max-width: 100%;'>" + "</select>" +
					"<div id='color-selector-" + selectorID + "'></div>" +
				"</div>"
			);
			
			for(i = 0; i < thisMap.seriesList.length; i++) {
				$("#series-" + selectorID).append("<option value='" + thisMap.seriesList[i].id + "'>" + thisMap.seriesList[i].title +"</option>");
			}
			
			$("#series-" + selectorID).change(function() {
				var id = $(this).val(),
				l,
				k = $(this).attr("id").split("-")[1];
console.log("series " + k + ": " + id);
				
				thisMap.uiSettings.series[k].index = id;
				
				thisMap.seriesToLoad.push(id);
				thisMap.load(thisMap.seriesToLoad[0], k);
				
				return;
			});
		}
		
		appendSeriesSelector(optionID);
		appendColorSelector(optionID);
		
		return optionID;
	}
	
	MagicMap.prototype.popSeries = function() {
		if(DEBUG) { console.log("[DEBUG] called popSeries()"); }
		
		var selectorID = $("#series-options").children().last().index();
		
		$("#series-" + selectorID).parent().remove();
		
		this.detailChart.series[selectorID].remove();
		this.masterChart.series[selectorID].remove();
		this.dataset.splice(selectorID, 1);
		this.displaySet.pop();
		
		this.heat[(selectorID << 1)].setLatLngs([]);
		this.heat[(selectorID << 1) + 1].setLatLngs([]);
		
		this.uiSettings.series.pop();
		
		return;
	}
	
	MagicMap.prototype.setColorPalette = function(palette) {
		if(DEBUG) { console.log("[DEBUG] called setColorPalette()"); }
		
		var i;
		
		for(i = 0; i < 3; i++) {
			$("#palette-" + i).removeClass("selected");
		}
		
		$("#palette-" + palette).addClass("selected");
		
		this.colors = this.colorSet[palette];
		
		this.setGradient.length = 0;
		for(i = 0; i < this.uiSettings.series.length; i++) {
			this.setGradient.push({
				0.0: this.colors[this.uiSettings.series[i].color]
			});
		}
		
		for(i = 0; i < this.displaySet.length; i++) {
			this.detailChart.series[i].update({color: this.colors[this.uiSettings.series[i].color]}, true);
			this.masterChart.series[i].update({color: this.colors[this.uiSettings.series[i].color]}, true);
			
			this.heat[(i << 1)].setOptions({gradient: this.setGradient[i]});
			this.heat[(i << 1) + 1].setOptions({gradient: this.setGradient[i] /*this.debugColor*/});
		}
		
		for(i = 0; i < this.colors.length; i++) {
			$("#color-" + i + " svg rect").attr("fill", this.colors[i]);
		}
		
		this.packHeat();
		
		return;
	}
	
	MagicMap.prototype.setSeriesColor = function(colorSelector) {
		if(DEBUG) { console.log("[DEBUG] called setSeriesColor()"); }
		
		var colorID = colorSelector.id.split("-")[1],
			selectorID = $(colorSelector).parent().attr("id").split("-")[2],
			siblings = $(colorSelector).siblings().get(),
			i;
		
		if(!$(colorSelector).hasClass("selected")) {
			this.uiSettings.series[selectorID].color = colorID;
			
			for(i = 0; i < siblings.length; i++) {
				if($(siblings[i]).hasClass("selected")) {
					$(siblings[i]).removeClass("selected");
					break;
				}
			}
			$(colorSelector).addClass("selected");
			
			if(!this.heat[(selectorID << 1)]) {
				this.heat.push(L.heatLayer(this.displaySet[selectorID].visiblePoints,
					{
						minOpacity: 0.0, maxZoom: 0, max: 1.0, blur: 0.01, radius: 20,
						gradient: this.setGradient[selectorID]
					}
				).addTo(this.map));
				
				this.heat.push(L.heatLayer(this.displaySet[selectorID].secondValues,
					{
						minOpacity: 0.0, maxZoom: 0, max: 1.0, blur: 0.01, radius: 30,
						gradient: this.setGradient[selectorID] //this.debugColor
					}
				).addTo(this.map));
			}
			
			this.setGradient[selectorID] = {0.0: this.colors[colorID]};
			this.heat[(selectorID << 1)].setOptions({gradient: this.setGradient[selectorID]});
			this.heat[(selectorID << 1) + 1].setOptions({gradient: this.setGradient[selectorID] /*this.debugColor*/});
			
			if(this.detailChart.series[selectorID]) {
				this.detailChart.series[selectorID].update({color: this.colors[colorID]}, true);
				this.masterChart.series[selectorID].update({color: this.colors[colorID]}, true);
			}
		}
		
		this.packHeat();
		
		return;
	}
	
	MagicMap.prototype.updatePlaybackInterface = function() {
		if(DEBUG) { console.log("[DEBUG] called updatePlaybackInterface()"); }
		
		if(!MAGIC_MAP.paused) {
			$("#playback-button span").removeClass("glyphicon-play")
				.addClass("glyphicon-pause");
			MAGIC_MAP.playBack = true;
		}
		else {
			$("#playback-button span").addClass("glyphicon-play")
			.removeClass("glyphicon-pause");
		}
		
		return;
	}

	MagicMap.prototype.load = function(seriesID, index) {
		if(DEBUG) { console.log("[DEBUG] called load()"); }
		
		var URL = CONTEXT + "/api/series/" + seriesID + "/time-coordinate",
			currentDataset = {
				seriesID: seriesID,
				title: "title",
				timeGroup: [{point: [], date: null}],
				maxValue: 0,
				standardDeviation: 0,
				frameAggregate: [0],
				frameOffset: 0
			},
			thisMap = this;
		
		if(!index && (index !== 0)) {
			this.dataset.push(currentDataset);
		}
		else {
			this.dataset[index] = currentDataset;
		}
		
		$.ajax({
			url: URL,
			success: function(result) {
				var i,
					j,
					k,
					temp,
					frame = 0,
					skipped = 0,
					filler = 0,
					threshold = 86400000, //ms in a day
					lastDate = new Date(result.results[0].timestamp),
					emptyDate,
					inputDate,
					deltaTime,
					largestConcentration = 0,
					sumArray,
					pointLifeSpan = Math.ceil(1 / thisMap.uiSettings.pointDecay),
					datasetAverage = 0,
					datasetVariance = 0;
				
				thisMap.zeroTime(lastDate);
				
				currentDataset.title = thisMap.seriesDescriptions[result.results[0].seriesId].title;
				
				for(i = 0; i < result.results.length; i++) {
					if(result.results[i]) {
result.results[i].secondValue = ((i % 5) * 0.25) + 0.5;
						
						inputDate = new Date(result.results[i].timestamp);
						thisMap.zeroTime(inputDate);
						
						//Update frame if new point is outside of time threshold
						emptyDate = lastDate;
						deltaTime = inputDate.valueOf() - lastDate.valueOf();
						
						while(deltaTime >= threshold) {
							currentDataset.timeGroup.push({point: [], date: null});
							frame++;
							filler++;
							emptyDate = new Date(emptyDate.valueOf() + threshold);
							currentDataset.timeGroup[frame].date = emptyDate;
							currentDataset.frameAggregate[frame] = null;
							
							deltaTime -= threshold;
						}
						
						datasetAverage += result.results[i].value;
						
						currentDataset.timeGroup[frame].point.push({latitude: result.results[i].latitude, longitude: result.results[i].longitude, value: result.results[i].value, secondValue: result.results[i].secondValue});
						currentDataset.timeGroup[frame].date = inputDate;
						
						currentDataset.frameAggregate[frame] += result.results[i].value;
						
						if(currentDataset.maxValue < result.results[i].value) {
							currentDataset.maxValue = result.results[i].value;
							
							if(thisMap.absoluteMaxValue < currentDataset.maxValue) {
								thisMap.absoluteMaxValue = currentDataset.maxValue;
							}
						}
						
						lastDate = currentDataset.timeGroup[frame].date;
					}
					else {
						skipped++;
					}
				}
				
				console.log("Loaded " + (result.results.length - skipped) + " entries for " + currentDataset.title);
				
				if(skipped > 0) {
					console.warn("Skipped " + skipped + " malformed entries");
				}
				else {
					console.log("Skipped " + skipped + " malformed entries");
				}
				
				console.log(filler + " days occurred without incidents in this timespan");
				console.log("Total Frames: " + frame + 1);
				console.log("Time Groups: " + currentDataset.timeGroup.length);
				console.log("---");
				
				datasetAverage /= result.results.length;
				
				for(i = 0; i < result.results.length; i++) {
					temp = (result.results[i].value - datasetAverage);
					temp *= temp;
					datasetVariance += temp;
				}
				datasetVariance /= result.results.length;
				currentDataset.standardDeviation = Math.sqrt(datasetVariance);
				
				thisMap.seriesToLoad.shift();
				if(thisMap.seriesToLoad.length === 0) {
					thisMap.earliestDate = thisMap.dataset[0].timeGroup[0].date;
					thisMap.latestDate = thisMap.dataset[0].timeGroup[thisMap.dataset[0].timeGroup.length - 1].date;
					
					for(i = 0; i < thisMap.dataset.length; i++) {
						if(thisMap.earliestDate > thisMap.dataset[i].timeGroup[0].date) {
							thisMap.earliestDate = thisMap.dataset[i].timeGroup[0].date;
						}
						
						if(thisMap.latestDate < thisMap.dataset[i].timeGroup[thisMap.dataset[i].timeGroup.length - 1].date) {
							thisMap.latestDate = thisMap.dataset[i].timeGroup[thisMap.dataset[i].timeGroup.length - 1].date;
						}
						
						thisMap.allContainedBox[0][0] = 90.0;
						thisMap.allContainedBox[1][0] = -90.0;
						thisMap.allContainedBox[0][1] = 180.0;
						thisMap.allContainedBox[1][1] = -180.0;
						
						for(j = 0; j < thisMap.dataset[i].timeGroup.length; j++) {
							for(k = 0; k < thisMap.dataset[i].timeGroup[j].point.length; k++) {
								if(thisMap.dataset[i].timeGroup[j].point[k].latitude && thisMap.dataset[i].timeGroup[j].point[k].longitude) {
									if(thisMap.allContainedBox[0][0] > thisMap.dataset[i].timeGroup[j].point[k].latitude) {
										thisMap.allContainedBox[0][0] = thisMap.dataset[i].timeGroup[j].point[k].latitude;
									}
									
									if(thisMap.allContainedBox[1][0] < thisMap.dataset[i].timeGroup[j].point[k].latitude) {
										thisMap.allContainedBox[1][0] = thisMap.dataset[i].timeGroup[j].point[k].latitude;
									}
									
									if(thisMap.allContainedBox[0][1] > thisMap.dataset[i].timeGroup[j].point[k].longitude) {
										thisMap.allContainedBox[0][1] = thisMap.dataset[i].timeGroup[j].point[k].longitude;
									}
									
									if(thisMap.allContainedBox[1][1] < thisMap.dataset[i].timeGroup[j].point[k].longitude) {
										thisMap.allContainedBox[1][1] = thisMap.dataset[i].timeGroup[j].point[k].longitude;
									}
								}
							}
						}
					}
					console.log("Beginning date: " + thisMap.earliestDate);
					console.log("Ending date: " + thisMap.latestDate);
					
					thisMap.frameCount = Math.floor((thisMap.latestDate.valueOf() - thisMap.earliestDate.valueOf()) / threshold) + 1;
					
					for(i = 0; i < thisMap.dataset.length; i++) {
						deltaTime = thisMap.dataset[i].timeGroup[0].date.valueOf() - thisMap.earliestDate.valueOf();
						
						if(deltaTime !== 0) {
							thisMap.dataset[i].frameOffset = Math.floor(deltaTime / threshold);
						}
					}
					
					thisMap.createChart(); //call this after loading all datasets
					
					//total timespan of dataseries
					deltaTime = (thisMap.latestDate.valueOf() - thisMap.earliestDate.valueOf()) / threshold;
					
					//set the largestConcentration of points to be the sum of the frame of the most concentrated lifecycle to display
					sumArray = [];
					for(i = 0; i < deltaTime; i++) {
						temp = 0;
						
						for(j = 0; j < thisMap.dataset.length; j++) {
							if(thisMap.dataset[j].timeGroup[thisMap.dataset[j].frameOffset + i]) {
								temp += thisMap.dataset[j].timeGroup[thisMap.dataset[j].frameOffset + i].point.length;
							}
						}
						
						sumArray.push(temp);
						if(sumArray.length > pointLifeSpan) {
							sumArray.shift();
						}
						
						temp = 0;
						for(k = 0; k < sumArray.length; k++) {
							temp += sumArray[k];
						}
						
						if(largestConcentration < temp) {
							largestConcentration = temp
							thisMap.mostConcentratedFrame = i;
						}
					}
					
					thisMap.map.whenReady(function() {
							setTimeout(function() {
									thisMap.suggestDelay();
									if(!thisMap.uiSettings.renderDelay) {
										$("#render-delay").val(thisMap.suggestedDelay);
										$("#render-delay").change();
									}
									
									return;
								},
								1000
							);
							
							return;
						}
					);
					
					thisMap.loadBuffer();
					thisMap.paused = true;
					
					//use timeline selection event object as parameter and trigger master-container/chart selection event
					if(thisMap.uiSettings.timeSelectionEvent) {
						thisMap.doSelection(thisMap.uiSettings.timeSelectionEvent);
					}
					
					if(!thisMap.uiSettings.bBox[0][0]) {
						thisMap.map.fitBounds(thisMap.allContainedBox);
					}
					
					for(i = 0; i < 3; i++) {
						$("#palette-" + i).removeClass("selected");
					}
					
					$("#palette-" + thisMap.uiSettings.colorPalette).addClass("selected");
					
					thisMap.colors = thisMap.colorSet[thisMap.uiSettings.colorPalette];
					
					thisMap.setGradient.length = 0;
					for(i = 0; i < thisMap.uiSettings.series.length; i++) {
						thisMap.setGradient.push({
							0.0: thisMap.colors[thisMap.uiSettings.series[i].color]
						});
					}
					
					$('input[value=' + thisMap.uiSettings.dataGapMethod + ']', '#data-gap-handler').attr("checked", true);
					$("#data-gap-handler").change();
					
					console.log("Finished loading. Unpause to begin.");
					console.log("===");
				}
				
				return;
			},
			error: function() {
				return;
			}
		});
		
		return;
	}
	
	MagicMap.prototype.suggestDelay = function() {
		if(DEBUG) { console.log("[DEBUG] called suggestDelay()"); }
		
		var thisMap = this,
			startFrame,
			threshold = 86400000, //ms in a day
			originalFrame = this.frame,
			pointLifeSpan = Math.ceil(1 / this.uiSettings.pointDecay);
		
		startFrame = this.mostConcentratedFrame - pointLifeSpan;
		if(startFrame < 0) {
			startFrame = 0;
		}
		
		this.frame = startFrame;
		
		//calculate the time to render the frame along with preexisting decaying points
		this.suggestedDelay = this.timeMethod(function() {
			var i;
			
			for(i = startFrame; i < thisMap.mostConcentratedFrame; i++) {
				thisMap.playBuffer(startFrame, thisMap.endFrame);
			}
			
			thisMap.packHeat();
			
			return;
		});
		
		this.frame = originalFrame;
		this.playSection(this.startFrame, this.endFrame);
		
		return;
	}
	
	MagicMap.prototype.timeMethod = function(method) {
		var initialTime,
			renderTime;
		
		initialTime = new Date().valueOf();
		method();
		renderTime = new Date().valueOf();
		
		return renderTime - initialTime;
	}
	
	MagicMap.prototype.createChart = function() {
		if(DEBUG) { console.log("[DEBUG] called createChart()"); }
		
		var seriesColors = [],
			i;
		
		for(i = 0; i < this.uiSettings.series.length; i++) {
			seriesColors.push(this.colors[this.uiSettings.series[i].color]);
		}
		
			// create the detail chart
		function createDetail(masterChart) {
			// prepare the detail chart
			var detailSeries = [],
				detailStart =  [],
				series = [],
				i;
			
			for(i = 0; i < MAGIC_MAP.dataset.length; i++) {
				detailStart.push(Date.UTC(MAGIC_MAP.dataset[i].timeGroup[0].date.getUTCFullYear(),
					MAGIC_MAP.dataset[i].timeGroup[0].date.getUTCMonth(),
					MAGIC_MAP.dataset[i].timeGroup[0].date.getUTCDate()));
			}

			for(i = 0; i < masterChart.series.length; i++) {
				detailSeries.push({detailData: []});
				
				$.each(masterChart.series[i].data, function () {
					if(this.x >= detailStart[i]) {
						detailSeries[i].detailData.push(this.y);
					}
				});
			}
			
			for(i = 0; i < MAGIC_MAP.dataset.length; i++) {
				series.push({
						name: MAGIC_MAP.dataset[i].title, //"Series " + MAGIC_MAP.dataset[i].seriesID,
						pointStart: detailStart[i],
						pointInterval: 86400000,//24 * 3600 * 1000,
						connectNulls: true,
						data: detailSeries[i].detailData
					}
				);
			}

			// create a detail chart referenced by a variable
			MAGIC_MAP.detailChart = $('#detail-container').highcharts({
				chart: {
					marginBottom: 110,//120,
					reflow: false,
					marginLeft: 50,
					//marginRight: 20,
					backgroundColor: "rgba(128, 128, 128, 0.1)", //null,
					style: {
						//position: 'absolute'
					}
				},
				colors: seriesColors, //MAGIC_MAP.colors,
				credits: {
					enabled: false
				},
				title: {
					text: null, //'Data Comparison',
					style: {
						color: 'rgba(0, 128, 128, 255)'
					}
				},
				subtitle: {
					text: null, //'Select an area by dragging across the lower chart',
					style: {
						color: 'rgba(0, 128, 128, 255)'
					}
				},
				xAxis: {
					type: 'datetime',
					lineColor: '#008080'
				},
				yAxis: {
					title: {
						text: null
					},
					maxZoom: 0.1,
					gridLineColor: '#008080'
				},
				tooltip: {
					/*
					formatter: function () {
						var point = this.visiblePoints[0];
						return '<b>' + point.series.name + '</b><br/>' +
							Highcharts.dateFormat('%A %B %e %Y', this.x) + ':<br/>' +
							point.y;//'1 USD = ' + Highcharts.numberFormat(point.y, 2) + ' EUR';
					},
					shared: true
					*/
					pointFormat: '{series.name} <br /><b>{point.y:,.0f}</b> instances'
				},
				legend: {
					enabled: false
				},
				plotOptions: {
					series: {
						marker: {
							enabled: false,
							states: {
								hover: {
									enabled: true,
									radius: 3
								}
							}
						}
					}
				},
				series: series,
				exporting: {
					enabled: false
				}
			}).highcharts(); // return chart
			
			$("#toggle-details-button").click();
			
			return;
		}

		// create the master chart
		function createMaster() {
			var i,
				dataSeries = [];
			
			for(i = 0; i < MAGIC_MAP.dataset.length; i++) {
				dataSeries.push({
						type: 'area',
						name: String.fromCharCode(i + 65) + ": " + MAGIC_MAP.dataset[i].title,
						pointInterval: 86400000, //24 * 3600 * 1000,
						pointStart: Date.UTC(MAGIC_MAP.dataset[i].timeGroup[0].date.getUTCFullYear(),
									MAGIC_MAP.dataset[i].timeGroup[0].date.getUTCMonth(),
									MAGIC_MAP.dataset[i].timeGroup[0].date.getUTCDate()),
						connectNulls: true,
						data: MAGIC_MAP.dataset[i].frameAggregate //y-value array
					}
				);
			}
			
			//TODO: hide/show displaySets that are toggled off/on via legend
			MAGIC_MAP.masterChart = $('#master-container').highcharts({
				chart: {
					reflow: false,
					backgroundColor: "rgba(128, 128, 128, 0.1)", //null,
					marginLeft: 50,
					//marginRight: 20,
					spacingTop: 5,
					spacingBottom: 5,
					zoomType: 'x',
					events: {
						// listen to the selection event on the master chart to update the
						// extremes of the detail chart
						selection: function(event) {
							MAGIC_MAP.doSelection(event);
							
							return false;
						}
					}
				},
				colors: seriesColors, //MAGIC_MAP.colors,
				title: {
					text: null
				},
				xAxis: {
					crosshair: true,
					type: 'datetime',
					showLastTickLabel: true,
					maxZoom: 1209600000, //14 * 24 * 3600000, // fourteen days
					plotBands: [{
						id: 'mask-before',
						from: Date.UTC(MAGIC_MAP.earliestDate.getUTCFullYear(),
								MAGIC_MAP.earliestDate.getUTCMonth(),
								MAGIC_MAP.earliestDate.getUTCDate()),
						to: Date.UTC(MAGIC_MAP.latestDate.getUTCFullYear(),
								MAGIC_MAP.latestDate.getUTCMonth(),
								MAGIC_MAP.latestDate.getUTCDate()),
						color: 'rgba(0, 0, 0, 0.2)'
					}],
					title: {
						text: null
					}
				},
				yAxis: {
					gridLineWidth: 0,
					labels: {
						enabled: false
					},
					title: {
						text: null
					},
					min: 0.6,
					showFirstLabel: false
				},
				tooltip: {
					formatter: function () {
						return false;
					}
					//, crosshairs: true
				},
				legend: {
					layout: 'vertical',
					align: 'left',
					verticalAlign: 'top',
					floating: true,
					enabled: true,
					padding: 0,
					y: 25,
					itemStyle: {
						fontSize: "12px"
					},
					labelFormatter: function() {
						var thisLegendElement = this.legendGroup.element,
							thisLegendIndex = this.index;
						
						$(thisLegendElement).mouseenter(function() {
							$("#information-container #series-description").text(MAGIC_MAP.seriesDescriptions[MAGIC_MAP.dataset[thisLegendIndex].seriesID].description);
							$("#information-container").show();
							
							return;
						});
						
						$(thisLegendElement).mouseout(function() {
							$("#information-container #series-description").text("");
							$("#information-container").hide();
							
							return;
						});
						
						return this.name;
					}
				},
				credits: {
					enabled: false
				},
				plotOptions: {
					series: {
						/*
						fillColor: {
							linearGradient: [0, 0, 0, 70],
							stops: [
								[0, Highcharts.getOptions().colors[0]],
								[1, 'rgba(255,255,255,0)']
							]
						},
						*/
						lineWidth: 1,
						marker: {
							enabled: false
						},
						shadow: false,
						states: {
							hover: {
								lineWidth: 1
							}
						},
						enableMouseTracking: false,
						events: {
							legendItemClick: function(event) {
								MAGIC_MAP.displaySet[event.currentTarget.index].hide = !MAGIC_MAP.displaySet[event.currentTarget.index].hide;
								MAGIC_MAP.detailChart.series[this.index].setVisible(!this.visible);
								MAGIC_MAP.packHeat();
								
								return;
							}
						}
					}
				},
				series: dataSeries,
				exporting: {
					enabled: false
				}
			},
			function (masterChart) {
				createDetail(masterChart);
			}).highcharts(); // return chart instance
		}

		// make the container smaller and add a second container for the master chart
		var $container = $('#container');
		
		// create master and in its callback, create the detail chart
		createMaster();
		
		//BONUS: transfer mouse events from detail container to map for all browsers
		//$('#detail-container').mousedown(function(event) { event.stopImmediatePropagation(); return $('.leaflet-layer').mousedown(); });
		//$('#detail-container').mousemove(function(event) { event.stopImmediatePropagation(); return $('.leaflet-layer').mousemove(); });
		
		return;
	}
	
	MagicMap.prototype.doSelection = function(event) {
		if(DEBUG) { console.log("[DEBUG] called doSelection()"); }
		
		if(!event) {
			return;
		}
		
		var extremesObject = event.xAxis[0],
			min = extremesObject.min,
			max = extremesObject.max,
			detailStart = [],
			detailSeries = [],
			xAxis = this.masterChart.xAxis[0],
			minDate = new Date(extremesObject.min),
			maxDate = new Date(extremesObject.max),
			startFrame,
			endFrame,
			i;

		this.zeroTime(minDate);
		this.zeroTime(maxDate);
		
		console.log("***");
		console.log("min: " + min);
		console.log("min date: " + minDate);
		console.log("max: " + max);
		console.log("max date: " + maxDate);

		for(i = 0; i < this.masterChart.series.length; i++) {
			detailSeries.push({detailData: []});
			
			// reverse engineer the last part of the data
			$.each(this.masterChart.series[i].data, function() {
				if((this.x >= min) && (this.x <= max)) {
					detailSeries[i].detailData.push([this.x, this.y]);
				}
			});
		}
		
		// move the plot bands to reflect the new detail span
		xAxis.removePlotBand('mask-before');
		xAxis.addPlotBand({
			id: 'mask-before',
			from: Date.UTC(MAGIC_MAP.earliestDate.getUTCFullYear(),
				MAGIC_MAP.earliestDate.getUTCMonth(),
				MAGIC_MAP.earliestDate.getUTCDate()),
			to: minDate.valueOf(),//min,
			color: 'rgba(128, 128, 128, 0.2)'
		});

		xAxis.removePlotBand('mask-after');
		xAxis.addPlotBand({
			id: 'mask-after',
			from: maxDate.valueOf(),//max,
			to: Date.UTC(MAGIC_MAP.latestDate.getUTCFullYear(),
				MAGIC_MAP.latestDate.getUTCMonth(),
				MAGIC_MAP.latestDate.getUTCDate()),
			color: 'rgba(128, 128, 128, 0.2)'
		});
		
		for(i = 0; i < MAGIC_MAP.detailChart.series.length; i++) {
			MAGIC_MAP.detailChart.series[i].setData(detailSeries[i].detailData);
		}
		
		MAGIC_MAP.zeroTime(minDate);
		MAGIC_MAP.zeroTime(maxDate);
		
		startFrame = Math.floor((minDate - MAGIC_MAP.earliestDate) / 86400000);
		endFrame = Math.floor((maxDate - MAGIC_MAP.earliestDate) / 86400000);
		
		if(startFrame < 0) {
			startFrame = 0;
		}
		
		if(endFrame > (MAGIC_MAP.frameCount - 1)) {
			endFrame = (MAGIC_MAP.frameCount - 1);
		}
		
		console.log("Selection: " + startFrame + "->" + endFrame);
		console.log("***");
		MAGIC_MAP.playSection(startFrame, endFrame);
		
		MAGIC_MAP.uiSettings.timeSelectionEvent = {xAxis: []};
		MAGIC_MAP.uiSettings.timeSelectionEvent.xAxis.push({min: event.xAxis[0].min, max: event.xAxis[0].max});
		
		return;
	}
	
	MagicMap.prototype.loadBuffer = function() {
		if(DEBUG) { console.log("[DEBUG] called loadBuffer()"); }
		
		var i;
		
		this.frame = 0;
		this.startFrame = 0;
		this.endFrame = this.frameCount - 1;
		this.playBack = true;
		
		this.masterChart.xAxis[0].removePlotBand('mask-before');
		this.masterChart.xAxis[0].removePlotBand('mask-after');
		
		for(i = 0; i < this.displaySet.length; i++) {
			//empty visiblePoints array
			this.displaySet[i].visiblePoints.length = 0; //hopefully the old data is garbage collected!
			this.displaySet[i].secondValues.length = 0;
		}
		
		$("#playback-button").removeClass("disabled");
		
		return;
	}

	MagicMap.prototype.playBuffer = function(startFrame, endFrame) {
		if(DEBUG) { console.log("[DEBUG] called playBuffer()"); }
		
		var i,
			setID,
			setFrame,
			currentDate,
			dateString = null,
			adjustedStart,
			adjustedEnd;
		
		if(this.reset) {
			this.reset = false;
			
			for(i = 0; i < this.displaySet.length; i++) {
				//empty visiblePoints array
				this.displaySet[i].visiblePoints.length = 0; //hopefully the old data is garbage collected!
				this.displaySet[i].secondValues.length = 0;
			}
		}
		
		for(setID = 0; setID < this.displaySet.length; setID++) {
			setFrame = this.frame - this.dataset[setID].frameOffset;
			adjustedStart = startFrame - this.dataset[setID].frameOffset;
			adjustedEnd = endFrame - this.dataset[setID].frameOffset;
			
			if(this.dataset[setID].timeGroup[setFrame]) {
				for(i = 0; i < this.dataset[setID].timeGroup[setFrame].point.length; i++) {
					if((this.dataset[setID].timeGroup[setFrame].point[i].value > 0) &&
					(this.dataset[setID].timeGroup[setFrame].point[i].latitude && this.dataset[setID].timeGroup[setFrame].point[i].longitude)) {
						this.displaySet[setID].visiblePoints.push([this.dataset[setID].timeGroup[setFrame].point[i].latitude,
							this.dataset[setID].timeGroup[setFrame].point[i].longitude,
							0.7,
							//(this.dataset[setID].timeGroup[setFrame].point[i].value / this.dataset[setID].maxValue),
							(this.dataset[setID].timeGroup[setFrame].point[i].value / this.absoluteMaxValue),
							this.dataset[setID].timeGroup[setFrame].point[i].value]);
						
						this.displaySet[setID].secondValues.push([this.dataset[setID].timeGroup[setFrame].point[i].latitude,
							this.dataset[setID].timeGroup[setFrame].point[i].longitude,
							0.7,
							-this.dataset[setID].timeGroup[setFrame].point[i].secondValue,
							0]);
					}
				}
				
				if(!dateString && ((this.frame % this.uiSettings.daysPerFrame) === 0)) {
					this.masterChart.xAxis[0].removePlotLine('date-line');
					this.detailChart.xAxis[0].removePlotLine('date-line');

					if(this.playBack) {
						currentDate = this.dataset[setID].timeGroup[setFrame].date;
						dateString = (currentDate.getUTCMonth() + 1) + '/' + currentDate.getUTCDate() + '/' + currentDate.getUTCFullYear();
						$("#current-date").text(dateString);
						
					//console.log(currentDate);
					//console.log(dateString);
						
						this.masterChart.xAxis[0].addPlotLine({
							value: currentDate.valueOf(),
							color: 'red',
							width: 2,
							id: 'date-line'
						});
						this.detailChart.xAxis[0].addPlotLine({
							value: currentDate.valueOf(),
							color: 'red',
							width: 2,
							id: 'date-line'
						});
					}
					else if(this.dataset[setID].timeGroup[adjustedStart] && this.dataset[setID].timeGroup[adjustedEnd]) {
						currentDate = this.dataset[setID].timeGroup[adjustedStart].date;
						dateString = (currentDate.getUTCMonth() + 1) + '/' + currentDate.getUTCDate() + '/' + currentDate.getUTCFullYear() + " - ";
						currentDate = this.dataset[setID].timeGroup[adjustedEnd].date;
						dateString += (currentDate.getUTCMonth() + 1) + '/' + currentDate.getUTCDate() + '/' + currentDate.getUTCFullYear();
						$("#current-date").text(dateString);
					}
				}
			}
			
			if(this.playBack) {
				for(i = 0; i < this.displaySet[setID].visiblePoints.length; i++) {
					if(this.displaySet[setID].visiblePoints[i][2] > 0) {
						this.displaySet[setID].visiblePoints[i][2] -= this.uiSettings.pointDecay;
						this.displaySet[setID].secondValues[i][2] -= this.uiSettings.pointDecay;
					}
					else {
						this.displaySet[setID].visiblePoints.splice(i, 1);
						this.displaySet[setID].secondValues.splice(i, 1);
						i--;
					}
				}
			}
		}
		
		if(this.frame < endFrame) {
			this.frame++;
		}
		else if(this.playBack) {
			this.playBack = false;
			this.paused = true;
			this.frame = startFrame;
			this.reset = true;
			//$("#playback-button").addClass("disabled");
			
			$("#playback-button span").toggleClass("glyphicon-pause")
				.toggleClass("glyphicon-play");
			
			//console.log("Finished playback");
			//console.log(new Date()); //real-time timestamp
		}
		
		return;
	}
	
	MagicMap.prototype.playSection = function(startFrame, endFrame) {
		if(DEBUG) { console.log("[DEBUG] called playSection()"); }
		
		var i;
		
		this.playBack = false;
		this.paused = true;
		this.updatePlaybackInterface();
		$("#playback-button").removeClass("disabled");
		this.frame = startFrame;
		
		for(i = 0; i < this.displaySet.length; i++) {
			//empty visiblePoints array
			this.displaySet[i].visiblePoints.length = 0; //hopefully the old data is garbage collected!
			this.displaySet[i].secondValues.length = 0;
		}
		
console.log((endFrame - startFrame) + " frames");
		
		for(i = startFrame; i <= endFrame; i++) {
			this.playBuffer(startFrame, endFrame);
		}
		
		this.packHeat();
		this.frame = startFrame;
		this.startFrame = startFrame;
		this.endFrame = endFrame;
		this.reset = true;
		
		return;
	}

	MagicMap.prototype.packHeat = function() {
		if(DEBUG) { console.log("[DEBUG] called packHeat()"); }
		
		var setID;
		
		for(setID = 0; setID < this.displaySet.length; setID++) {
			if(!this.heat[setID << 1]) {
				if(this.displaySet[setID].hide) {
					this.heat.push(L.heatLayer([],
						{
							minOpacity: 0.0, maxZoom: 0, max: 1.0, blur: 0.01, radius: 20,
							gradient: this.setGradient[setID]
						}
					).addTo(this.map));
					
					this.heat.push(L.heatLayer([],
						{
							minOpacity: 0.0, maxZoom: 0, max: 1.0, blur: 0.01, radius: 30,
							gradient: this.setGradient[setID] //this.debugColor
						}
					).addTo(this.map));
				}
				else {
					this.heat.push(L.heatLayer(this.displaySet[setID].visiblePoints,
						{
							minOpacity: 0.0, maxZoom: 0, max: 1.0, blur: 0.01, radius: 20,
							gradient: this.setGradient[setID]
						}
					).addTo(this.map));
					
					if(this.showSecondary) {
						this.heat.push(L.heatLayer(this.displaySet[setID].secondValues,
							{
								minOpacity: 0.0, maxZoom: 0, max: 1.0, blur: 0.01, radius: 30,
								gradient: this.setGradient[setID] //this.debugColor
							}
						).addTo(this.map));
					}
					else {
							this.heat.push(L.heatLayer([],
							{
								minOpacity: 0.0, maxZoom: 0, max: 1.0, blur: 0.01, radius: 30,
								gradient: this.setGradient[setID] //this.debugColor
							}
						).addTo(this.map));
					}
				}
			}
			else {
				if(this.displaySet[setID].hide) {
					this.heat[(setID << 1)].setLatLngs([], this.showNumbers);
					this.heat[(setID << 1) + 1].setLatLngs([], this.showNumbers);
				}
				else {
					this.heat[(setID << 1)].setLatLngs(this.displaySet[setID].visiblePoints, this.showNumbers);
					
					if(this.showSecondary) {
						this.heat[(setID << 1) + 1].setLatLngs(this.displaySet[setID].secondValues, this.showNumbers);
					}
					else {
						this.heat[(setID << 1) + 1].setLatLngs([], this.showNumbers);
					}
				}
			}
//console.log(this.heat[setID]._latlngs);
		}
		
		return;
	}

	MagicMap.prototype.loop = function() {
		var thisMap = MAGIC_MAP,
			loopTime = thisMap.timeMethod(thisMap.processFrame),
			waitTime = thisMap.uiSettings.renderDelay - loopTime;
		
		if(loopTime > thisMap.suggestedDelay) {
			thisMap.suggestedDelay = loopTime;
			$("#suggested-delay").text(thisMap.suggestedDelay);
		}
		
		if(waitTime > 0) {
			clearInterval(thisMap.loopIntervalID);
			
			setTimeout(function() {
					thisMap.loopIntervalID = setInterval(thisMap.loop, 0);
					
					return;
				},
				waitTime
			);
		}
		
		return;
	}
	
	MagicMap.prototype.processFrame = function() {
		var thisMap = MAGIC_MAP;
		
		if(!thisMap.paused) {
			if(thisMap.playBack) {
				thisMap.playBuffer(thisMap.startFrame, thisMap.endFrame);
				
				if((thisMap.frame % thisMap.uiSettings.daysPerFrame) === 0) {
					thisMap.packHeat();
				}
			}
		}
		
		return;
	}
	
	MagicMap.prototype.handleInput = function(event) {
		switch(event.which) {
			case 13:
				$("#playback-button").click();
			break;
			
			case 96:
				MAGIC_MAP.playBack = false;
			break;
			
			case 82:
				$("#reset-button").click();
			break;
			
			default:
				//console.log("event.which code: " + event.which);
			break;
		}
		
		return;
	}

	/* helper to zero-out the time of a Date object (it's bad practice to modify standard JavaScript objects) */
	MagicMap.prototype.zeroTime = function(dateTime) {
		dateTime.setUTCHours(0);
		dateTime.setUTCMinutes(0);
		dateTime.setUTCSeconds(0);
		dateTime.setUTCMilliseconds(0);
		
		return dateTime;
	}
	
	$(document).ready(function() {
		window.MAGIC_MAP = new MagicMap();
		MAGIC_MAP.initialize();
		
		return;
	});
})();