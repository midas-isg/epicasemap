/*
timeline.js
*/

(function() {
	function MagicMap() {
		var i;
		
		L.mapbox.accessToken = 'pk.eyJ1IjoidHBzMjMiLCJhIjoiVHEzc0tVWSJ9.0oYZqcggp29zNZlCcb2esA';
		this.map = L.mapbox.map('map', /*'mapbox.streets'*//**/'mapbox.dark'/**/, { worldCopyJump: true, bounceAtZoomLimits: false, zoom: 2, minZoom: 1})
			.setView([37.8, -96], 4);

		//this.popup = new L.Popup({ autoPan: false }),
		this.heat = []; //null;
		this.paused = false;
		this.frame;
		this.frameCount;
		this.chart = null;
		this.dataset = [];
		this.earliestDate = new Date();
		this.earliestDate.setUTCHours(0);
		this.earliestDate.setUTCMinutes(0);
		this.earliestDate.setUTCSeconds(0);
		
		this.latestDate = new Date(0);
		this.latestDate.setUTCHours(0);
		this.latestDate.setUTCMinutes(0);
		this.latestDate.setUTCSeconds(0);
		
		this.playBack = false;
		this.seriesToLoad = [1, 2, 3];
		this.set = [];
		for(i = 0; i < this.seriesToLoad.length; i++) {
			this.set.push({visiblePoints: []});
		}
		
		this.setGradient = [];
			
		this.setGradient.push({
			0.0: '#800000',
			1.0: '#ff0000'
		});
		
		this.setGradient.push({
			0.0: '#008000',
			1.0: '#00ff00'
		});
		
		this.setGradient.push({
			0.0: '#000080',
			1.0: '#0000ff'
		});
		
		return this;
	}

	MagicMap.prototype.start = function() {
		var i;
		
		for(i = 0; i < this.seriesToLoad.length; i++) {
			this.load(this.seriesToLoad[i]);
		}
		
		this.packHeat();
		
		this.loadBuffer();
		console.log("Finished generating. Unpause to begin.");
		this.paused = true;
		
		
		//legend stuff
		this.closeTooltip;
		//this.map.legendControl.addLegend(this.getLegendHTML());
		
		document.getElementById('body').onkeyup = this.handleInput;
		setInterval(this.loop, 0);
		
		return;
	}

	MagicMap.prototype.load = function(seriesID) {
		var URL = CONTEXT + "/api/series/" + seriesID + "/time-coordinate";
		thisMap = this;
		
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
				deltaTime,
				datasetID = thisMap.dataset.length;
				
				lastDate.setUTCHours(0);
				lastDate.setUTCMinutes(0);
				lastDate.setUTCSeconds(0);
				
				thisMap.dataset.push({seriesID: result.filter.equalities.seriesId, buffer: [{coordinates: [], date: null, value: 0}], maxValue: 0, frameAggregate: [0], frameOffset: 0});
				
				if(thisMap.earliestDate > lastDate) {
					thisMap.earliestDate = new Date(lastDate);
				}
				else {
					lastDate = new Date(thisMap.earliestDate);
				}
				
				for(i = 0; i < result.results.length; i++) {
					if(result.results[i]) {
						inputDate = new Date(result.results[i].timestamp);
						inputDate.setUTCHours(0);
						inputDate.setUTCMinutes(0);
						inputDate.setUTCSeconds(0);
						
						//Update frame if new point is outside of time threshold
						emptyDate = lastDate;
						deltaTime = inputDate.valueOf() - lastDate.valueOf();
						
						while(deltaTime >= threshold) {
							thisMap.dataset[datasetID].buffer.push({coordinates: [], date: null});
							frame++;
							filler++;
							emptyDate = new Date(emptyDate.valueOf() + threshold);
							thisMap.dataset[datasetID].buffer[frame].date = emptyDate;
							thisMap.dataset[datasetID].frameAggregate[frame] = 0;
							
							deltaTime -= threshold;
						}
						
						thisMap.dataset[datasetID].buffer[frame].coordinates.push({latitude: result.results[i].latitude, longitude: result.results[i].longitude});
						thisMap.dataset[datasetID].buffer[frame].date = inputDate;
						thisMap.dataset[datasetID].buffer[frame].value = result.results[i].value;
						
						if(thisMap.dataset[datasetID].maxValue < result.results[i].value) {
							thisMap.dataset[datasetID].maxValue = result.results[i].value;
						}
						
						thisMap.dataset[datasetID].frameAggregate[frame] += result.results[i].value;
						
						lastDate = thisMap.dataset[datasetID].buffer[frame].date;
					}
					else {
						skipped++;
					}
				}
				console.log("Loaded " + (result.results.length - skipped) + " entries");
				console.log("Skipped " + skipped + " malformed entries");
				console.log(filler + " days occurred without reports in this timespan");
				console.log("Total Frames: " + frame + 1);
				console.log("Buffer length: " + thisMap.dataset[datasetID].buffer.length);
				
				if(thisMap.latestDate < thisMap.dataset[datasetID].buffer[frame].date) {
					thisMap.latestDate = thisMap.dataset[datasetID].buffer[frame].date;
					thisMap.frameCount = frame + 1;
				}
				
				thisMap.seriesToLoad.shift();
				if(thisMap.seriesToLoad.length == 0) {
					for(i = 0; i < thisMap.dataset.length; i++) {
						deltaTime = thisMap.dataset[i].buffer[0].date.valueOf() - thisMap.earliestDate.valueOf();
						
						if(deltaTime != 0) {
							thisMap.dataset[i].frameOffset = Math.floor(deltaTime / threshold);
						}
					}
					
					thisMap.createChart(); //call this after loading all datasets
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
						name: "Series " + MAGIC_MAP.dataset[i].seriesID, //'Ebola References per Day',
						pointStart: detailStart[i],
						pointInterval: 86400000,//24 * 3600 * 1000,
						data: detailSeries[i].detailData
					}
				);
			}

			// create a detail chart referenced by a global variable
			detailChart = $('#detail-container').highcharts({
				chart: {
					marginBottom: 110,//120,
					reflow: false,
					marginLeft: 50,
					//marginRight: 20,
					backgroundColor: null,
					style: {
						//position: 'absolute'
					}
				},
				credits: {
					enabled: false
				},
				title: {
					text: 'Data Comparison',
					style: {
						color: 'rgba(255, 255, 255, 255)'
					}
				},
				subtitle: {
					text: 'Select an area by dragging across the lower chart',
					style: {
						color: 'rgba(255, 255, 255, 255)'
					}
				},
				xAxis: {
					type: 'datetime'
				},
				yAxis: {
					title: {
						text: null
					},
					maxZoom: 0.1
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
		}

		// create the master chart
		function createMaster() {
			var i,
				dataSeries = [];
			
			for(i = 0; i < MAGIC_MAP.dataset.length; i++) {
				dataSeries.push({
						type: 'area',
						name: MAGIC_MAP.dataset[i].seriesID, //'References per Day'
						pointInterval: 86400000, //24 * 3600 * 1000,
						pointStart: Date.UTC(MAGIC_MAP.dataset[i].buffer[0].date.getUTCFullYear(),
									MAGIC_MAP.dataset[i].buffer[0].date.getUTCMonth(),
									MAGIC_MAP.dataset[i].buffer[0].date.getUTCDate()),
						data: MAGIC_MAP.dataset[i].frameAggregate //y-value array
					}
				);
			}
			
			$('#master-container').highcharts({
				chart: {
					reflow: false,
					backgroundColor: null,
					marginLeft: 50,
					//marginRight: 20,
					zoomType: 'x',
					events: {
						// listen to the selection event on the master chart to update the
						// extremes of the detail chart
						selection: function (event) {
							var extremesObject = event.xAxis[0],
								min = extremesObject.min,
								max = extremesObject.max,
								detailSeries = [],
								xAxis = this.xAxis[0],
								startFrame,
								endFrame,
								i;
							
							for(i = 0; i < this.series.length; i++) {
								detailSeries.push({detailData: []});
								
								// reverse engineer the last part of the data
								$.each(this.series[i].data, function() {
									if((this.x >= min) && (this.x < max)) {
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
							
							for(i = 0; i < detailChart.series.length; i++) {
								detailChart.series[i].setData(detailSeries[i].detailData);
							}
							
							//startFrame = Math.floor((new Date(min) - MAGIC_MAP.dataset[0].buffer[0].date) / 86400000);
							startFrame = Math.floor((new Date(min) - MAGIC_MAP.earliestDate) / 86400000);
							endFrame = Math.floor((new Date(max) - MAGIC_MAP.earliestDate) / 86400000);
							
							if(startFrame < 0) {
								startFrame = 0;
							}
							
							//console.log(startFrame + "->" + (startFrame + detailSeries[0].detailData.length));
							//MAGIC_MAP.playSection(startFrame, startFrame + detailSeries[0].detailData.length);
							console.log(startFrame + "->" + endFrame);
							MAGIC_MAP.playSection(startFrame, endFrame);

							return false;
						}
					}
				},
				title: {
					text: null
				},
				xAxis: {
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
					enabled: false
				},
				credits: {
					enabled: false
				},
				plotOptions: {
					series: {
						fillColor: {
							linearGradient: [0, 0, 0, 70],
							stops: [
								[0, Highcharts.getOptions().colors[0]],
								[1, 'rgba(255,255,255,0)']
							]
						},
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
			.css({ position: 'relative', bottom: 100, height: 100 })
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
		this.playBack = true;
		
		for(i = 0; i < this.set.length; i++) {
			//empty visiblePoints array
			//while(this.set[i].visiblePoints.length > 0) { this.set[i].visiblePoints.pop(); }
			this.set[i].visiblePoints.length = 0; //hopefully the old data is garbage collected!
		}
		
		return;
	}

	MagicMap.prototype.playBuffer = function() {
		var i,
		setID,
		setFrame;
		
		for(setID = 0; setID < this.dataset.length; setID++){
			setFrame = this.frame - this.dataset[setID].frameOffset;
			
			if(this.dataset[setID].buffer[setFrame]) {
				for(i = 0; i < this.dataset[setID].buffer[setFrame].coordinates.length; i++) {
					this.set[setID].visiblePoints.push([this.dataset[setID].buffer[setFrame].coordinates[i].latitude,
						this.dataset[setID].buffer[setFrame].coordinates[i].longitude,
						this.dataset[setID].buffer[setFrame].value / this.dataset[setID].maxValue]);
				}
				
				if(this.playBack) {
					for(i = 0; i < this.set[setID].visiblePoints.length; i++) {
						if(this.set[setID].visiblePoints[i][2] > 0.00) {
							this.set[setID].visiblePoints[i][2] -= 0.01;
						}
						else {
							this.set[setID].visiblePoints.splice(i, 1);
							i--;
						}
					}
				}
			}
		}
		
		if(this.frame < this.frameCount) {
			this.frame++;
		}
		else if(this.playBack){
			this.playBack = false;
			this.paused = true;
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
		this.frame = startFrame;
		
		for(i = 0; i < this.set.length; i++) {
			//empty visiblePoints array
			this.set[i].visiblePoints.length = 0; //hopefully the old data is garbage collected!
		}
		
		for(i = startFrame; i <= endFrame; i++) {
			this.playBuffer();
		}
		
		this.packHeat();
		
		return;
	}

	MagicMap.prototype.packHeat = function() {
		var setID;
		
		for(setID = 0; setID < this.dataset.length; setID++) {
			if(!this.heat[setID]) {
				this.heat.push(L.heatLayer(this.set[setID].visiblePoints,
					{
						minOpacity: 0.0, maxZoom: 0, max: 1.0, blur: 0.1, radius: 5,
						gradient: this.setGradient[setID]
					}
				).addTo(this.map));
			}
			
			this.heat[setID].setLatLngs(this.set[setID].visiblePoints);
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
					thisMap.playBuffer();//TEST.timeMethod(thisMap.playBuffer);
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
				MAGIC_MAP.paused = !MAGIC_MAP.paused;
				console.log(new Date());
			break;
			
			case 96:
				MAGIC_MAP.playBack = false;
			break;
			
			case 82:
console.log("Buffer:");
//console.log(MAGIC_MAP.dataset[].buffer);
				MAGIC_MAP.loadBuffer();
			break;
			
			default:
				console.log("event.which code: " + event.which);
			break;
		}
		
		return;
	}

	MagicMap.prototype.getStyle = function(feature) {
		thisMap = this;
		
		return {
			weight: 2,
			opacity: 0.1,
			color: 'black',
			fillOpacity: 0.7,
			fillColor: thisMap.getColor(feature.properties.density)
		};
	}

	// get color depending on population density value
	MagicMap.prototype.getColor = function(d) {
		return d > 42 ? '#000000' :
			d > 35  ? '#c000c0' :
			d > 28  ? '#0000ff' :
			d > 21  ? '#00ffff' :
			d > 14   ? '#00ff00' :
			d > 7   ? '#ffff00' :
			'#ff0000';
	}

	/*
	MagicMap.prototype.onEachFeature = function(feature, layer) {
		layer.on({
			mousemove: mousemove,
			mouseout: mouseout,
			click: zoomToFeature
		});
	}

	MagicMap.prototype.mousemove = function(e) {
		var layer = e.target;

		this.popup.setLatLng(e.latlng);
		this.popup.setContent('<div class="marker-title">' + layer.feature.properties.name + '</div>' +
			layer.feature.properties.density + ' people per square mile');

		if (!this.popup._map) this.popup.openOn(this.map);
		window.clearTimeout(this.closeTooltip);

		// highlight feature
		layer.setStyle({
			weight: 3,
			opacity: 0.3,
			fillOpacity: 0.9
		});

		if (!L.Browser.ie && !L.Browser.opera) {
			layer.bringToFront();
		}
	}

	MagicMap.prototype.mouseout = function(e) {
		statesLayer.resetStyle(e.target);
		this.closeTooltip = window.setTimeout(function() {
			this.map.closePopup();
		}, 100);
	}

	MagicMap.prototype.zoomToFeature = function(e) {
		this.map.fitBounds(e.target.getBounds());
	}
	*/

	MagicMap.prototype.getLegendHTML = function() {
		var grades = [0, 7, 14, 21, 28, 35, 42],
		labels = [],
		from, to;

		for (var i = 0; i < grades.length; i++) {
		from = grades[i];
		to = grades[i + 1];

		labels.push(
			'<li><span class="swatch" style="background:' + this.getColor(from + 1) + '"></span> ' +
			from + (to ? '&ndash;' + to : '+')) + '</li>';
		}

		return '<span>Age of reports (days)</span><ul>' + labels.join('') + '</ul>';
	}
	
	$(document).ready(function() {
		window.MAGIC_MAP = new MagicMap();
		MAGIC_MAP.start();
	});
})();
