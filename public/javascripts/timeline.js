/*
timeline.js
*/

(function() {
	function MagicMap() {
		var i,
		j,
		temp,
		vizID = getURLParameterByName("id"),
		thisMap = this;
		
		L.mapbox.accessToken = 'pk.eyJ1IjoidHBzMjMiLCJhIjoiVHEzc0tVWSJ9.0oYZqcggp29zNZlCcb2esA';
		this.map = L.mapbox.map('map', 'mapbox.streets'/*'mapbox.dark'*/, { worldCopyJump: true, bounceAtZoomLimits: false, zoom: 2, minZoom: 2})
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
		this.earliestDate = new Date();
		this.latestDate = new Date(0);
		this.zeroTime(this.latestDate);
		
		this.seriesList0;
		this.seriesList1;
		this.seriesDescriptions = {};
		
		this.showControlPanel = false;
		
		this.playBack = false;
		this.set = [];
		
		if(vizID) {
			this.loadVisualization(vizID);
		}
		else {
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
								name: result.result.name,
								description: result.result.description
							};
							
							seriesToLoad.shift();
							if(seriesToLoad.length === 0) {
								for(i = 0; i < thisMap.seriesToLoad.length; i++) {
									thisMap.set.push({visiblePoints: []});
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
		
		this.setGradient = [];
		/*
		this.colorSet = [
			['#1b9e77', '#d95f02', '#7570b3', '#e7298a', '#66a61e'],
			['#a6cee3', '#1f78b4', '#b2df8a', "#33a02c", "#fb9a99"],
			['#66c2a5', '#fc8d62', '#8da0cb', "#e78ac3", "#a6d854"]
		];
		*/
		this.colorSet = [
			['#1b9e77', '#d95f02', '#7570b3', '#e7298a', '#66a61e'],
			["#33a02c", '#1f78b4', '#b2df8a', '#a6cee3', "#fb9a99"],
			['#66c2a5', '#fc8d62', '#8da0cb', "#e78ac3", "#a6d854"]
		];
		
		this.paletteSelection = 0;
		this.colors = this.colorSet[this.paletteSelection];
		
		for(i = 0; i < this.colors.length; i++) {
			this.setGradient.push({
				0.0: this.colors[i]
			});
		}
		
		/*
		for(i = 0; i < this.colorSet.length; i++) {
			$("#ramps").append("<div id='palette-" + i + "' class='ramp'><svg width='15' height='75'></svg></div>");
			
			temp = this.colorSet[i];
			for(j = 0; j < temp.length; j++) {
				$("#palette-" + i + " svg").append("<rect style='fill: " + temp[j] + ";' width='15' height='15' y='" + (15 * j) + "'></rect>");
			}
		}
		$("#palette-0").addClass("selected");
		*/
		
		return this;
	}
	
	MagicMap.prototype.loadVisualization = function(vizID) {
		var URL = CONTEXT + "/api/vizs/" + vizID,
		thisMap = this;
		
		$.ajax({
			url: URL,
			success: function(result) {
				var h,
				i,
				seriesDisplayCount = 2;
				
				thisMap.seriesList0 = result.result.allSeries;
				thisMap.seriesList1 = result.result.allSeries2;
				
				$("#title").text(result.result.name);
				
				for(h = 0; h < seriesDisplayCount; h++) {
					for(i = 0; i < thisMap["seriesList" + h].length; i++) {
						thisMap.seriesDescriptions[thisMap["seriesList" + h][i].id] = {
							name: thisMap["seriesList" + h][i].name,
							description: thisMap["seriesList" + h][i].description
						};
					}
				}
				
				//console.log(thisMap.seriesDescriptions);
				
				thisMap.seriesToLoad = [thisMap.seriesList0[0].id, thisMap.seriesList1[0].id];
				for(i = 0; i < thisMap.seriesToLoad.length; i++) {
					thisMap.set.push({visiblePoints: []});
					thisMap.load(thisMap.seriesToLoad[i]);
				}
				
				/**/
				for(h = 0; h < seriesDisplayCount; h++) {
					$("#series-options").append(
						"<div>" +
							"<h5>Select series " + String.fromCharCode(h + 65) + "</h5>" +
							"<select id='series-" + h + "' style='max-width: 100%;'>" +
							"</select>" +
						"</div>"
					);
					
					for(i = 0; i < thisMap["seriesList" + h].length; i++) {
						$("#series-" + h).append("<option value='" + thisMap["seriesList" + h][i].id + "'>" + thisMap["seriesList" + h][i].name +"</option>");
					}
					
					$("#series-" + h).change(function() {
						var id = $(this).val(),
						l,
						k = $(this).attr("id").split("-")[1];
console.log("series " + k + ": " + id);
						
						//TODO: recalculate frameOffset, earliest & latest dates after loading is finished
						//(refactor block to external call -calculate from 0 and length-1 indexes)
						thisMap.latestDate = new Date(0);
						thisMap.earliestDate = new Date();
						
						thisMap.seriesToLoad = [];
						for(l = 0; l < seriesDisplayCount; l++) {
							thisMap.seriesToLoad.push($("#series-" + l).val());
						}
						
						for(l = 0; l < thisMap.seriesToLoad.length; l++) {
							thisMap.set[k].visiblePoints.length = 0;
							thisMap.load(thisMap.seriesToLoad[l], l);
						}
						
						return;
					});
				}
				/**/
				
				return;
			},
			error: function() {
				return;
			}
		});
		
		return;
	}
	
	MagicMap.prototype.start = function() {
		thisMap = this;
		
		document.getElementById('body').onkeyup = this.handleInput;
		setInterval(this.loop, 0);
		
		$("#reset-button").click(function() {
			var i;
			
			//console.log("Buffer:");
			//console.log(MAGIC_MAP.dataset[].buffer);
			MAGIC_MAP.loadBuffer();
			
			for(i = 0; i < MAGIC_MAP.heat.length; i++) {
				MAGIC_MAP.heat[i].redraw();
			}
			
			MAGIC_MAP.masterChart.xAxis[0].removePlotLine('date-line');
			
			return;
		});
		
		$("#playback-button").click(function() {
			MAGIC_MAP.paused = !MAGIC_MAP.paused;
			console.log(new Date());
			
			return MAGIC_MAP.updatePlaybackInterface();
		});
		
		$("#toggle-details-button").click(function() {
			var i;
			
			$("#detail-container *").toggle();
			
			for(i = 0; i < thisMap.set.length; i++) {
				thisMap.detailChart.series[i].update({color: thisMap.colors[i]}, true);
			}
			
			return;
		});
		
		$("#toggle-controls-button").click(function() {
			$("#control-panel").toggle();
			
			return;
		});
		
		//TODO: refactor palette click code without causing closure issues
		$("#palette-0").click(function() {
			if(!$("#palette-0").hasClass("selected")) {
				MAGIC_MAP.paletteSelection = 0;
				MAGIC_MAP.setColorPalette(0);
			}
		});
		
		$("#palette-1").click(function() {
			if(!$("#palette-1").hasClass("selected")) {
				MAGIC_MAP.paletteSelection = 1;
				MAGIC_MAP.setColorPalette(1);
			}
		});
		
		$("#palette-2").click(function() {
			if(!$("#palette-2").hasClass("selected")) {
				MAGIC_MAP.paletteSelection = 2;
				MAGIC_MAP.setColorPalette(2);
			}
		});
		
		$(window).resize(function() {
			thisMap.detailChart.reflow();
			thisMap.masterChart.reflow();
			
			return;
		});
		
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
		for(i = 0; i < this.colors.length; i++) {
			this.setGradient.push({
				0.0: this.colors[i]
			});
		}
		
		for(i = 0; i < this.set.length; i++) {
			this.detailChart.series[i].update({color: this.colors[i]}, true);
			this.masterChart.series[i].update({color: this.colors[i]}, true);
			
			this.heat[i].setOptions({gradient: this.setGradient[i]});
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
			name: "name",
			buffer: [{point: [], date: null}],
			maxValue: 0,
			frameAggregate: [0],
			frameOffset: 0
		},
		thisMap = this;
		
		if(!index && (index !== 0)) {
			this.dataset.push(currentDataset);
		}
		else {
			this.dataset[index] = {
				seriesID: seriesID,
				name: "name",
				buffer: [{point: [], date: null}],
				maxValue: 0,
				frameAggregate: [0],
				frameOffset: 0
			}
			
			currentDataset = this.dataset[index];
		}
		
		$.ajax({
			url: URL,
			success: function(result) {
				var i,
				frame = 0,
				skipped = 0,
				filler = 0,
				threshold = 86400000, //ms in a day
				lastDate = new Date(result.results[0].timestamp),
				emptyDate,
				inputDate,
				deltaTime;
				
				thisMap.zeroTime(lastDate);
				
				currentDataset.name = thisMap.seriesDescriptions[seriesID].name;
				
				if(thisMap.earliestDate > lastDate) {
					thisMap.earliestDate = new Date(lastDate);
				}
				else {
					//lastDate = new Date(thisMap.earliestDate);
				}
				
				for(i = 0; i < result.results.length; i++) {
					if(result.results[i]) {
						inputDate = new Date(result.results[i].timestamp);
						thisMap.zeroTime(inputDate);
						
						//Update frame if new point is outside of time threshold
						emptyDate = lastDate;
						deltaTime = inputDate.valueOf() - lastDate.valueOf();
						
						while(deltaTime >= threshold) {
							currentDataset.buffer.push({point: [], date: null});
							frame++;
							filler++;
							emptyDate = new Date(emptyDate.valueOf() + threshold);
							currentDataset.buffer[frame].date = emptyDate;
							currentDataset.frameAggregate[frame] = 0;
							
							deltaTime -= threshold;
						}
						
						currentDataset.buffer[frame].point.push({latitude: result.results[i].latitude, longitude: result.results[i].longitude, value: result.results[i].value});
						currentDataset.buffer[frame].date = inputDate;
						
						currentDataset.frameAggregate[frame] += result.results[i].value;
						
						if(currentDataset.maxValue < result.results[i].value) {
							currentDataset.maxValue = result.results[i].value;
						}
						
						lastDate = currentDataset.buffer[frame].date;
					}
					else {
						skipped++;
					}
				}
				console.log("Loaded " + (result.results.length - skipped) + " entries");
				console.log("Skipped " + skipped + " malformed entries");
				console.log(filler + " days occurred without reports in this timespan");
				console.log("Total Frames: " + frame + 1);
				console.log("Buffer length: " + currentDataset.buffer.length);
				
				if(thisMap.latestDate < currentDataset.buffer[frame].date) {
					thisMap.latestDate = currentDataset.buffer[frame].date;
				}
				
				thisMap.seriesToLoad.shift();
				if(thisMap.seriesToLoad.length === 0) {
					thisMap.frameCount = Math.floor((thisMap.latestDate.valueOf() - thisMap.earliestDate.valueOf()) / threshold) + 1;
					
					for(i = 0; i < thisMap.dataset.length; i++) {
						deltaTime = thisMap.dataset[i].buffer[0].date.valueOf() - thisMap.earliestDate.valueOf();
						
						if(deltaTime !== 0) {
							thisMap.dataset[i].frameOffset = Math.floor(deltaTime / threshold);
						}
					}
					
					thisMap.createChart(); //call this after loading all datasets
					thisMap.packHeat();
					thisMap.loadBuffer();
					thisMap.paused = true;
					console.log("Finished loading. Unpause to begin.");
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
			// create the detail chart
		function createDetail(masterChart) {
			// prepare the detail chart
			var detailSeries = [],
				detailStart =  [],
				series = [],
				i;
			
			for(i = 0; i < MAGIC_MAP.dataset.length; i++) {
				detailStart.push(Date.UTC(MAGIC_MAP.dataset[i].buffer[0].date.getUTCFullYear(),
					MAGIC_MAP.dataset[i].buffer[0].date.getUTCMonth(),
					MAGIC_MAP.dataset[i].buffer[0].date.getUTCDate()));
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
						name: MAGIC_MAP.dataset[i].name, //"Series " + MAGIC_MAP.dataset[i].seriesID,
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
				colors: MAGIC_MAP.colors,
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
						name: String.fromCharCode(i + 65) + ": " + MAGIC_MAP.dataset[i].name,
						pointInterval: 86400000, //24 * 3600 * 1000,
						pointStart: Date.UTC(MAGIC_MAP.dataset[i].buffer[0].date.getUTCFullYear(),
									MAGIC_MAP.dataset[i].buffer[0].date.getUTCMonth(),
									MAGIC_MAP.dataset[i].buffer[0].date.getUTCDate()),
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
							var extremesObject = event.xAxis[0],
								min = extremesObject.min,
								max = extremesObject.max,
								detailSeries = [],
								xAxis = this.xAxis[0],
								minDate = new Date(extremesObject.min),
								maxDate = new Date(extremesObject.max),
								startFrame,
								endFrame,
								i;
							
							for(i = 0; i < this.series.length; i++) {
								detailSeries.push({detailData: []});
								
								// reverse engineer the last part of the data
								$.each(this.series[i].data, function() {
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
								to: min,
								color: 'rgba(128, 128, 128, 0.2)'
							});

							xAxis.removePlotBand('mask-after');
							xAxis.addPlotBand({
								id: 'mask-after',
								from: max,
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
							
							console.log(startFrame + "->" + endFrame);
							MAGIC_MAP.playSection(startFrame, endFrame);

							return false;
						}
					}
				},
				colors: MAGIC_MAP.colors,
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
		
		//$('<div id="detail-container">').appendTo($container);
		$('<div id="master-container">')
			.css({ position: 'relative', bottom: 115, height: 125, 'background-color': 'rgba(255, 255, 255, 0.1)' })
			.appendTo($container);
			
		// create master and in its callback, create the detail chart
		createMaster();
		
		//TODO: transfer mouse events from detail container to map
		//$('#detail-container').mousedown(function(event) { event.stopImmediatePropagation(); return $('.leaflet-layer').mousedown(); });
		//$('#detail-container').mousemove(function(event) { event.stopImmediatePropagation(); return $('.leaflet-layer').mousemove(); });
		
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
		
		for(i = 0; i < this.set.length; i++) {
			//empty visiblePoints array
			//while(this.set[i].visiblePoints.length > 0) { this.set[i].visiblePoints.pop(); }
			this.set[i].visiblePoints.length = 0; //hopefully the old data is garbage collected!
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
			
			for(i = 0; i < this.set.length; i++) {
				//empty visiblePoints array
				this.set[i].visiblePoints.length = 0; //hopefully the old data is garbage collected!
			}
		}
		
		for(setID = 0; setID < this.dataset.length; setID++) {
			setFrame = this.frame - this.dataset[setID].frameOffset;
			adjustedStart = startFrame - this.dataset[setID].frameOffset;
			adjustedEnd = endFrame - this.dataset[setID].frameOffset;
			
			if(this.dataset[setID].buffer[setFrame]) {
				for(i = 0; i < this.dataset[setID].buffer[setFrame].point.length; i++) {
					if(this.dataset[setID].buffer[setFrame].point[i].value > 0) {
						this.set[setID].visiblePoints.push([this.dataset[setID].buffer[setFrame].point[i].latitude,
							this.dataset[setID].buffer[setFrame].point[i].longitude,
							0.7,
							(this.dataset[setID].buffer[setFrame].point[i].value / this.dataset[setID].maxValue)]);
					}
				}
				
				if(!dateString) {
					this.masterChart.xAxis[0].removePlotLine('date-line');
					
					if(this.playBack) {
						currentDate = this.dataset[setID].buffer[setFrame].date;
						dateString = (currentDate.getUTCMonth() + 1) + '/' + currentDate.getUTCDate() + '/' + currentDate.getUTCFullYear();
						$("#current-date").text(dateString);
						
						this.masterChart.xAxis[0].addPlotLine({
							value: currentDate.valueOf(),
							color: 'red',
							width: 2,
							id: 'date-line'
						});
					}
					else if(this.dataset[setID].buffer[adjustedStart] && this.dataset[setID].buffer[adjustedEnd]) {
						currentDate = this.dataset[setID].buffer[adjustedStart].date;
						dateString = (currentDate.getUTCMonth() + 1) + '/' + currentDate.getUTCDate() + '/' + currentDate.getUTCFullYear() + " - ";
						currentDate = this.dataset[setID].buffer[adjustedEnd].date;
						dateString += (currentDate.getUTCMonth() + 1) + '/' + currentDate.getUTCDate() + '/' + currentDate.getUTCFullYear();
						$("#current-date").text(dateString);
					}
				}
			}
			
			if(this.playBack) {
				for(i = 0; i < this.set[setID].visiblePoints.length; i++) {
					if(this.set[setID].visiblePoints[i][2] > 0.00) {
						this.set[setID].visiblePoints[i][2] -= 0.0231;
					}
					else {
						this.set[setID].visiblePoints.splice(i, 1);
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
			
			console.log("Finished playback");
			console.log(new Date());
		}
		
		return;
	}
	
	MagicMap.prototype.playSection = function(startFrame, endFrame) {
		var i;
		
		//TODO: ensure startFrame and EndFrame hit all series throughout during visualization
		this.playBack = false;
		this.paused = true;
		this.updatePlaybackInterface();
		$("#playback-button").removeClass("disabled");
		this.frame = startFrame;
		
		for(i = 0; i < this.set.length; i++) {
			//empty visiblePoints array
			this.set[i].visiblePoints.length = 0; //hopefully the old data is garbage collected!
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
		
		for(setID = 0; setID < this.dataset.length; setID++) {
			if(!this.heat[setID]) {
				this.heat.push(L.heatLayer(this.set[setID].visiblePoints,
					{
						minOpacity: 0.0, maxZoom: 0, max: 1.0, blur: 0.1, //radius: 5,
						gradient: this.setGradient[setID]
					}
				).addTo(this.map));
			}
			else {
				this.heat[setID].setLatLngs(this.set[setID].visiblePoints);
			}
//console.log(this.heat[setID]._latlngs);
		}
		
		return;
	}

	MagicMap.prototype.loop = function() {
		function process(thisMap) {
			if(!thisMap.paused) {
				if(!thisMap.playBack) {
					thisMap.evolve();//TEST.timeMethod(thisMap.evolve);
				}
				else {
					thisMap.playBuffer(thisMap.startFrame, thisMap.endFrame);//TEST.timeMethod(thisMap.playBuffer);
				}
				
				thisMap.packHeat();
			}
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
				console.log("event.which code: " + event.which);
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
		MAGIC_MAP.start();
	});
})();
