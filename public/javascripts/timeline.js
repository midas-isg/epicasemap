/*
timeline.js
*/

(function() {
	function MagicMap() {
		L.mapbox.accessToken = 'pk.eyJ1IjoidHBzMjMiLCJhIjoiVHEzc0tVWSJ9.0oYZqcggp29zNZlCcb2esA';
		this.map = L.mapbox.map('map', /*'mapbox.streets'*//**/'mapbox.dark'/**/, { worldCopyJump: true, bounceAtZoomLimits: false, zoom: 1, minZoom: 1})
			.setView([37.8, -96], 4);

		//this.popup = new L.Popup({ autoPan: false }),
		this.points = [],
		//this.buffer = [],
		this.frameTotal = [],
		this.heat = null,
		this.paused = false,
		this.frame,
		this.chart = null,
		this.dataset = [],
		this.playBack = false;
		
		return this;
	}

	MagicMap.prototype.start = function() {
		this.load(1);
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
		var URL = "http://localhost:9000/epidemap/api/series/" + seriesID + "/time-coordinate",
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
				
				thisMap.dataset.push({buffer: [{coordinates: [], date: null}]});
				//thisMap.buffer.push({coordinates: [], date: null});
				thisMap.frameTotal[0] = 0;
				
				for(i = 0; i < result.results.length; i++) {
					if(result.results[i]) {
						inputDate = new Date(result.results[i].timestamp);
						inputDate.setHours(0);
						inputDate.setMinutes(0);
						inputDate.setSeconds(0);
						
						//Update frame if new point is outside of time threshold
						emptyDate = lastDate;
						deltaTime = inputDate.valueOf() - lastDate.valueOf();
						
						while(deltaTime >= threshold) {
							thisMap.dataset[datasetID].buffer.push({coordinates: [], date: null});
							frame++;
							filler++;
							emptyDate = new Date(emptyDate.valueOf() + threshold);
							thisMap.dataset[datasetID].buffer[frame].date = emptyDate;
							thisMap.frameTotal[frame] = 0;
							
							deltaTime -= threshold;
						}
						
						thisMap.dataset[datasetID].buffer[frame].coordinates.push({latitude: result.results[i].latitude, longitude: result.results[i].longitude});
						thisMap.dataset[datasetID].buffer[frame].date = inputDate;
						thisMap.frameTotal[frame]++;
						
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
				
				thisMap.createChart(); //TODO: call this after loading all datasets
				
				return;
			},
			error: function() {
				return;
			}
		});
		
		return;
	}
	
	MagicMap.prototype.createChart = function() {
		//TODO: replace dataset[0] with iteration such as dataset[id]
		
			// create the detail chart
		function createDetail(masterChart) {
			// prepare the detail chart
			var detailData = [],
				detailStart = Date.UTC(MAGIC_MAP.dataset[0].buffer[0].date.getUTCFullYear(),
						MAGIC_MAP.dataset[0].buffer[0].date.getUTCMonth(),
						MAGIC_MAP.dataset[0].buffer[0].date.getUTCDate()); //Date.UTC(2008, 7, 1);

			$.each(masterChart.series[0].data, function () {
				if (this.x >= detailStart) {
					detailData.push(this.y);
				}
			});

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
					text: 'Ebola References',
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
					formatter: function () {
						var point = this.points[0];
						return '<b>' + point.series.name + '</b><br/>' +
							Highcharts.dateFormat('%A %B %e %Y', this.x) + ':<br/>' +
							point.y;//'1 USD = ' + Highcharts.numberFormat(point.y, 2) + ' EUR';
					},
					shared: true
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
				series: [{
					name: 'Ebola References per Day',
					pointStart: detailStart,
					pointInterval: 24 * 3600 * 1000,
					data: detailData
				}],
				exporting: {
					enabled: false
				}

			}).highcharts(); // return chart
		}

		// create the master chart
		function createMaster() {
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
								detailData = [],
								xAxis = this.xAxis[0],
								startFrame;

							// reverse engineer the last part of the data
							$.each(this.series[0].data, function () {
								if((this.x > min) && (this.x < max)) {
									detailData.push([this.x, this.y]);
								}
							});

							// move the plot bands to reflect the new detail span
							xAxis.removePlotBand('mask-before');
							xAxis.addPlotBand({
								id: 'mask-before',
								from: Date.UTC(MAGIC_MAP.dataset[0].buffer[0].date.getUTCFullYear(),
									MAGIC_MAP.dataset[0].buffer[0].date.getUTCMonth(),
									MAGIC_MAP.dataset[0].buffer[0].date.getUTCDate()),
								to: min,
								color: 'rgba(128, 128, 128, 0.2)'
							});

							xAxis.removePlotBand('mask-after');
							xAxis.addPlotBand({
								id: 'mask-after',
								from: max,
								to: Date.UTC(MAGIC_MAP.dataset[0].buffer[MAGIC_MAP.dataset[0].buffer.length - 1].date.getUTCFullYear(),
									MAGIC_MAP.dataset[0].buffer[MAGIC_MAP.dataset[0].buffer.length - 1].date.getUTCMonth(),
									MAGIC_MAP.dataset[0].buffer[MAGIC_MAP.dataset[0].buffer.length - 1].date.getUTCDate()),
								color: 'rgba(128, 128, 128, 0.2)'
							});
							
							detailChart.series[0].setData(detailData);
							
							startFrame = Math.floor((new Date(min) - MAGIC_MAP.dataset[0].buffer[0].date) / 86400000);
							
							if(startFrame < 0) {
								startFrame = 0;
							}
							
							console.log(startFrame + "->" + (startFrame + detailData.length));
							
							MAGIC_MAP.playSection(startFrame, startFrame + detailData.length);

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
						from: Date.UTC(MAGIC_MAP.dataset[0].buffer[0].date.getUTCFullYear(),
								MAGIC_MAP.dataset[0].buffer[0].date.getUTCMonth(),
								MAGIC_MAP.dataset[0].buffer[0].date.getUTCDate()),//Date.UTC(2006, 0, 1),
						to: Date.UTC(MAGIC_MAP.dataset[0].buffer[MAGIC_MAP.dataset[0].buffer.length -1].date.getUTCFullYear(),
								MAGIC_MAP.dataset[0].buffer[MAGIC_MAP.dataset[0].buffer.length -1].date.getUTCMonth(),
								MAGIC_MAP.dataset[0].buffer[MAGIC_MAP.dataset[0].buffer.length -1].date.getUTCDate()), //Date.UTC(2008, 7, 1),
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
				series: [{
					type: 'area',
					name: 'References per Day',
					pointInterval: 86400000, //24 * 3600 * 1000,
					pointStart: Date.UTC(MAGIC_MAP.dataset[0].buffer[0].date.getUTCFullYear(),
								MAGIC_MAP.dataset[0].buffer[0].date.getUTCMonth(),
								MAGIC_MAP.dataset[0].buffer[0].date.getUTCDate()), //Date.UTC(2006, 0, 1),
					data: MAGIC_MAP.frameTotal//data
				}],
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
			//.css({ position: 'absolute', top: 300, height: 100, width: '100%' })
			.css({ position: 'relative', bottom: 100, height: 100 })
			.appendTo($container);

		// create master and in its callback, create the detail chart
		createMaster();
		
		return;
	}
	
	MagicMap.prototype.evolve = function() {
		return;
	}

	MagicMap.prototype.loadBuffer = function() {
		this.frame = 0;
		this.playBack = true;
		
		//empty points array
		//while(this.points.length > 0) { this.points.pop(); }
		this.points.length = 0; //hopefully the old data is garbage collected!
		
		return;
	}

	MagicMap.prototype.playBuffer = function() {
		var i;
		
		for(i = 0; i < this.dataset[0].buffer[this.frame].coordinates.length; i++) {
			this.points.push([this.dataset[0].buffer[this.frame].coordinates[i].latitude, this.dataset[0].buffer[this.frame].coordinates[i].longitude, 0.4/*1.0*/]);
		}
		
		if(this.playBack) {
			for(i = 0; i < this.points.length; i++) {
				if(this.points[i][2] > 0) {
					this.points[i][2] -= 0.01;
				}
				else {
					this.points.splice(i, 1);
				}
			}
		}
		
		if(this.frame < (this.dataset[0].buffer.length - 1)) {
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
		this.playBack = false;
		this.paused = true;
		this.frame = startFrame;
		
		//empty points array
		this.points.length = 0; //hopefully the old data is garbage collected!
		
		var i;
		for(i = startFrame; i <= endFrame; i++) {
			this.playBuffer();
		}
		
		this.packHeat();
		
		return;
	}

	MagicMap.prototype.packHeat = function() {
		if(!this.heat) {
			this.heat = L.heatLayer(this.points, {minOpacity: 0.0, maxZoom: 0, max: 1.0, blur: 1, radius: 5, /*gradient: {0.0: '#000000', 0.0: '#c000c0', 0.3: '#0000ff', 0.45: '#00ffff', 0.6: '#00ff00', 0.75: '#ffff00', 1.0: '#ff0000'}*/}).addTo(this.map);
		}
		
		this.heat.setLatLngs(this.points);
	//console.log(this.heat._latlngs);
		
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
				
				// update the chart each frame
				var x = (thisMap.dataset[0].buffer[thisMap.frame].date.getUTCMonth() + 1) + "/" +
					thisMap.dataset[0].buffer[thisMap.frame].date.getUTCDate() + "/" +
					thisMap.dataset[0].buffer[thisMap.frame].date.getUTCFullYear(); //thisMap.frame + 1;
				
				thisMap.packHeat();
			}
		}
		
		process(MAGIC_MAP);
		
		return;
	}

	MagicMap.prototype.handleInput = function(event) {
		switch(event.keyCode) {
			case 13:
				MAGIC_MAP.paused = !MAGIC_MAP.paused;
				console.log(new Date());
			break;
			
			case 32:
	console.log("Buffer:");
	console.log(MAGIC_MAP.dataset[0].buffer);
				MAGIC_MAP.loadBuffer();
			break;
			
			case 96:
				MAGIC_MAP.playBack = false;
			break;
			
			default:
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
