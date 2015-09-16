/*
timeline.js
*/

(function() {
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
		this.map = L.mapbox.map( 'map',
				temp,
				{ worldCopyJump: true, bounceAtZoomLimits: false, zoom: 2, minZoom: 2})
			.setView([30, 0], 2);

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
		this.earliestDate = null;
		this.latestDate = null;
		this.loopIntervalID;
		
		this.vizID = getURLParameterByName("id");
		
		this.suggestedDelay = 50;
		
		this.uiSettings = {
			series: [{index: -1, color: 0}],
			colorPalette: 0,
			bBox: [[], []],
			timeSelectionEvent: null,
			daysPerFrame: 1,
			renderDelay: null,
			pointDecay: 0.0231
		};
		
		this.seriesList = [];
		this.seriesDescriptions = {};
		this.seriesToLoad = [];
		
		this.showControlPanel = false;
		
		this.playBack = false;
		this.displaySet = [];
		
		if(this.vizID) {
			this.loadVisualization(this.vizID);
		}
		else {
			$("#save-button").hide();
			
			function getDescriptions(seriesToLoad) {
				var URL = CONTEXT + "/api/series/",
				i,
				j;
				
				thisMap.seriesToLoad = seriesToLoad.slice();
				
				for(j = 0; j < thisMap.seriesToLoad.length; j++) {
					$.ajax({
						url: URL + thisMap.seriesToLoad[j],
						success: function(result) {
							thisMap.seriesDescriptions[result.result.id] = {
								title: result.result.title,
								description: result.result.description
							};
							
							seriesToLoad.shift();
							if(seriesToLoad.length === 0) {
								for(i = 0; i < thisMap.seriesToLoad.length; i++) {
									thisMap.load(thisMap.seriesToLoad[i]);
								}
							}
							
							return;
						},
						error: function() {
							return;
						}
					});
				}
				
				return;
			}
			
			getDescriptions([1, 259]);
		}
		
		for(i = 0; i < this.mapTypes.length; i++) {
			$("#map-selector").append("<option value='" + i + "'>" + this.mapTypes[i] + "</option>");
		}
		$("#map-selector").val(mapID);
		
		$("#map-selector").change(function() {
			sessionStorage.uiSettings = JSON.stringify(thisMap.uiSettings);
			
			return location.assign(CONTEXT + "?id=" + thisMap.vizID + "&map=" + $(this).val());
		});
		
		
		this.setGradient = [];
		this.colorSet = [
			['#1b9e77', '#d95f02', '#7570b3', '#e7298a', '#66a61e'],
			['#a6cee3', '#1f78b4', '#b2df8a', "#33a02c", "#fb9a99"],
			['#66c2a5', '#fc8d62', '#8da0cb', "#e78ac3", "#a6d854"]
		];
		
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
				
				//console.log(thisMap.seriesDescriptions);
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
					//TODO: FIX default page (no ID associated)
					thisMap.uiSettings.series[0] = {index: thisMap.seriesList[0].id, color: 0};
					thisMap.seriesToLoad.push(thisMap.uiSettings.series[0].index);
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
		var thisMap = this;
		
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
				thisMap.detailChart.series[i].update({color: thisMap.colors[i]}, true);
			}
			
			return;
		});
		
		$("#step-forward-button").click(function() {
			thisMap.playBack = true;
			thisMap.playBuffer(thisMap.startFrame, thisMap.endFrame);
			thisMap.packHeat();
			thisMap.playBack = false;
			
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
		
		//TODO: refactor palette click code without causing closure issues
		$("#palette-0").click(function() {
			if(!$("#palette-0").hasClass("selected")) {
				thisMap.uiSettings.colorPalette = 0;
				thisMap.setColorPalette(0);
			}
			
			return;
		});
		
		$("#palette-1").click(function() {
			if(!$("#palette-1").hasClass("selected")) {
				thisMap.uiSettings.colorPalette = 1;
				thisMap.setColorPalette(1);
			}
			
			return;
		});
		
		$("#palette-2").click(function() {
			if(!$("#palette-2").hasClass("selected")) {
				thisMap.uiSettings.colorPalette = 2;
				thisMap.setColorPalette(2);
			}
			
			return;
		});
		
		$("#render-delay").change(function() {
			if($(this).val() < 0) {
				$(this).val(0);
			}
			
			thisMap.uiSettings.renderDelay = $(this).val();
			
			return;
		});
		$("#render-delay").val(this.uiSettings.renderDelay);
		$("#render-delay").change();
		
		$("#calibrate-button").click(function() {
			$("#render-delay").val(thisMap.suggestedDelay);
			$("#render-delay").change();
			
			return;
		});
		
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
			
			thisMap.displaySet.push({visiblePoints: []});
			
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
		var selectorID = $("#series-options").children().last().index();
		
		$("#series-" + selectorID).parent().remove();
		
		this.detailChart.series[selectorID].remove();
		this.masterChart.series[selectorID].remove();
		this.dataset.splice(selectorID, 1);
		this.displaySet.pop();
		this.heat[selectorID].setLatLngs([]);
		this.uiSettings.series.pop();
		
		return;
	}
	
	MagicMap.prototype.setColorPalette = function(palette) {
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
			
			this.heat[i].setOptions({gradient: this.setGradient[i]});
		}
		
		for(i = 0; i < this.colors.length; i++) {
			$("#color-" + i + " svg rect").attr("fill", this.colors[i]);
		}
		
		return;
	}
	
	MagicMap.prototype.setSeriesColor = function(colorSelector) {
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
			
			if(!this.heat[selectorID]) {
				this.heat.push(L.heatLayer(this.displaySet[selectorID].visiblePoints,
					{
						minOpacity: 0.0, maxZoom: 0, max: 1.0, blur: 0.1, //radius: 5,
						gradient: this.setGradient[selectorID]
					}
				).addTo(this.map));
			}
			
			this.setGradient[selectorID] = {0.0: this.colors[colorID]};
			this.heat[selectorID].setOptions({gradient: this.setGradient[selectorID]});
			
			if(this.detailChart.series[selectorID]) {
				this.detailChart.series[selectorID].update({color: this.colors[colorID]}, true);
				this.masterChart.series[selectorID].update({color: this.colors[colorID]}, true);
			}
		}
		
		return;
	}
	
	MagicMap.prototype.updatePlaybackInterface = function() {
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
		var URL = CONTEXT + "/api/series/" + seriesID + "/time-coordinate",
		currentDataset = {
			seriesID: seriesID,
			title: "title",
			timeGroup: [{point: [], date: null}],
			maxValue: 0,
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
				temp,
				largestConcentration = 0,
				mostConcentratedFrame = 0,
				frame = 0,
				skipped = 0,
				filler = 0,
				threshold = 86400000, //ms in a day
				lastDate = new Date(result.results[0].timestamp),
				emptyDate,
				inputDate,
				deltaTime;
				
				thisMap.zeroTime(lastDate);
				
				currentDataset.title = thisMap.seriesDescriptions[result.results[0].seriesId].title;
				
				for(i = 0; i < result.results.length; i++) {
					if(result.results[i]) {
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
							currentDataset.frameAggregate[frame] = 0;
							
							deltaTime -= threshold;
						}
						
						currentDataset.timeGroup[frame].point.push({latitude: result.results[i].latitude, longitude: result.results[i].longitude, value: result.results[i].value});
						currentDataset.timeGroup[frame].date = inputDate;
						
						currentDataset.frameAggregate[frame] += result.results[i].value;
						
						if(currentDataset.maxValue < result.results[i].value) {
							currentDataset.maxValue = result.results[i].value;
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
				
				thisMap.seriesToLoad.shift();
				if(thisMap.seriesToLoad.length === 0) {
					thisMap.earliestDate = thisMap.dataset[0].timeGroup[0].date;
					thisMap.latestDate = thisMap.dataset[0].timeGroup[thisMap.dataset[0].timeGroup.length - 1].date;
					
					for(i = 1; i < thisMap.dataset.length; i++) {
						if(thisMap.earliestDate > thisMap.dataset[i].timeGroup[0].date) {
							thisMap.earliestDate = thisMap.dataset[i].timeGroup[0].date;
						}
						
						if(thisMap.latestDate < thisMap.dataset[i].timeGroup[thisMap.dataset[i].timeGroup.length - 1].date) {
							thisMap.latestDate = thisMap.dataset[i].timeGroup[thisMap.dataset[i].timeGroup.length - 1].date;
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
					
					for(i = 0; i < deltaTime; i++) {
						temp = 0;
						
						for(j = 0; j < thisMap.dataset.length; j++) {
							if(thisMap.dataset[j].timeGroup[thisMap.dataset[j].frameOffset + i]) {
								temp += thisMap.dataset[j].timeGroup[thisMap.dataset[j].frameOffset + i].length;
								
								if(largestConcentration < temp) {
									largestConcentration = temp
									mostConcentratedFrame = i;
								}
							}
						}
					}
					
					//calculate the time to render it
					deltaTime = new Date();
					thisMap.playSection(mostConcentratedFrame, mostConcentratedFrame + 1);
					deltaTime = new Date().valueOf() - deltaTime.valueOf();
					
					//then use that as the default render delay if it doesn't already exist
					thisMap.suggestedDelay = deltaTime;
					
					if(!thisMap.uiSettings.renderDelay) {
						$("#render-delay").val(deltaTime);
						$("#render-delay").change();
					}
					
					thisMap.loadBuffer();
					thisMap.paused = true;
					
					//use timeline selection event object as parameter and trigger master-container/chart selection event
					if(thisMap.uiSettings.timeSelectionEvent) {
						thisMap.doSelection(thisMap.uiSettings.timeSelectionEvent);
					}
					
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
	
	MagicMap.prototype.createChart = function() {
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
						data: MAGIC_MAP.dataset[i].frameAggregate //y-value array
					}
				);
			}
			
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
						enableMouseTracking: false
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
		var extremesObject = event.xAxis[0],
			min = extremesObject.min,
			max = extremesObject.max,
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
					detailSeries[i].detailData.push([this.x, this.y])
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
	
	MagicMap.prototype.evolve = function() {
		return;
	}

	MagicMap.prototype.loadBuffer = function() {
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
		}
		
		$("#playback-button").removeClass("disabled");
		
		return;
	}

	MagicMap.prototype.playBuffer = function(startFrame, endFrame) {
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
			}
		}
		
		for(setID = 0; setID < this.displaySet.length; setID++) {
			setFrame = this.frame - this.dataset[setID].frameOffset;
			adjustedStart = startFrame - this.dataset[setID].frameOffset;
			adjustedEnd = endFrame - this.dataset[setID].frameOffset;
			
			if(this.dataset[setID].timeGroup[setFrame]) {
				for(i = 0; i < this.dataset[setID].timeGroup[setFrame].point.length; i++) {
					if(this.dataset[setID].timeGroup[setFrame].point[i].value > 0) {
						this.displaySet[setID].visiblePoints.push([this.dataset[setID].timeGroup[setFrame].point[i].latitude,
							this.dataset[setID].timeGroup[setFrame].point[i].longitude,
							0.7,
							(this.dataset[setID].timeGroup[setFrame].point[i].value / this.dataset[setID].maxValue)]);
					}
				}
				
				if(!dateString && ((this.frame % this.uiSettings.daysPerFrame) === 0)) {
					this.masterChart.xAxis[0].removePlotLine('date-line');
					
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
					if(this.displaySet[setID].visiblePoints[i][2] > 0.00) {
						this.displaySet[setID].visiblePoints[i][2] -= this.uiSettings.pointDecay;
					}
					else {
						this.displaySet[setID].visiblePoints.splice(i, 1);
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
		var i;
		
		this.playBack = false;
		this.paused = true;
		this.updatePlaybackInterface();
		$("#playback-button").removeClass("disabled");
		this.frame = startFrame;
		
		for(i = 0; i < this.displaySet.length; i++) {
			//empty visiblePoints array
			this.displaySet[i].visiblePoints.length = 0; //hopefully the old data is garbage collected!
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
		var setID;
		
		for(setID = 0; setID < this.displaySet.length; setID++) {
			if(!this.heat[setID]) {
				this.heat.push(L.heatLayer(this.displaySet[setID].visiblePoints,
					{
						minOpacity: 0.0, maxZoom: 0, max: 1.0, blur: 0.1, //radius: 5,
						gradient: this.setGradient[setID]
					}
				).addTo(this.map));
			}
			else {
				this.heat[setID].setLatLngs(this.displaySet[setID].visiblePoints);
			}
//console.log(this.heat[setID]._latlngs);
		}
		
		return;
	}

	MagicMap.prototype.loop = function() {
		function process(thisMap) {
			var renderTime;
			
			if(!thisMap.paused) {
				if(!thisMap.playBack) {
					thisMap.evolve();//TEST.timeMethod(thisMap.evolve);
				}
				else {
					thisMap.playBuffer(thisMap.startFrame, thisMap.endFrame);//TEST.timeMethod(thisMap.playBuffer);
				}
				
				if((thisMap.frame % thisMap.uiSettings.daysPerFrame) === 0) {
					renderTime = new Date();
					thisMap.packHeat();
					renderTime = new Date() - renderTime;
					
					if((thisMap.uiSettings.renderDelay - renderTime) > 0) {
						clearInterval(thisMap.loopIntervalID);
						
						setTimeout(function() {
								return thisMap.loopIntervalID = setInterval(thisMap.loop, 0);
							},
							(thisMap.uiSettings.renderDelay - renderTime)
						);
					}
				}
			}
			
			return;
		}
		
		process(MAGIC_MAP);
		
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
