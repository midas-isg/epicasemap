/*
 (c) 2014, Vladimir Agafonkin
 Leaflet.heat, a tiny and fast heatmap plugin for Leaflet.
 https://github.com/Leaflet/Leaflet.heat
*/

L.HeatLayer = (L.Layer ? L.Layer : L.Class).extend({
    // options: {
    //     minOpacity: 0.05,
    //     maxZoom: 18,
    //     radius: 25,
    //     blur: 15,
    //     max: 1.0
    // },

    initialize: function (latlngs, options) {
        this._latlngs = latlngs;
        L.setOptions(this, options);
    },

    setLatLngs: function (latlngs, showNumbers) {
        this._latlngs = latlngs;
		this._heat._showNumbers = showNumbers;
		
        return this.redraw();
    },

    addLatLng: function (latlng) {
        this._latlngs.push(latlng);
        return this.redraw();
    },

    setOptions: function (options) {
        L.setOptions(this, options);
		
        if(this._heat) {
            this._updateOptions();
        }
		
        return this.redraw();
    },

    redraw: function () {
        if (this._heat && !this._frame && !this._map._animating) {
            this._frame = L.Util.requestAnimFrame(this._redraw, this);
        }
        return this;
    },

    onAdd: function (map) {
        this._map = map;

        if (!this._canvas) {
            this._initCanvas();
        }

        /*BEGIN MOD*/
        var heatLayerGroup = document.getElementById("heat-layer-group");
        if(!heatLayerGroup) {
            heatLayerGroup = document.createElement("div");
            heatLayerGroup.setAttribute("id", "heat-layer-group");
            map._panes.overlayPane.insertBefore(heatLayerGroup, map._panes.overlayPane.firstElementChild);
        }

        heatLayerGroup.appendChild(this._canvas);
        //map._panes.overlayPane.appendChild(this._canvas);
        /*END MOD*/

        map.on('moveend', this._reset, this);

        if(map.options.zoomAnimation && L.Browser.any3d) {
            map.on('zoomanim', this._animateZoom, this);
        }

        this._reset();
    },

    onRemove: function (map) {
		//console.log(this._canvas);
		document.getElementById("heat-layer-group").removeChild(this._canvas);

        map.off('moveend', this._reset, this);

        if (map.options.zoomAnimation) {
            map.off('zoomanim', this._animateZoom, this);
        }
    },

    addTo: function (map) {
        map.addLayer(this);
        return this;
    },

    _initCanvas: function () {
		var canvas,
			size,
			animated,
			radiusFactor = this.options.radius || this.defaultRadius;
			optionsBlur = this.options.blur || 0;
		
		canvas = this._canvas = L.DomUtil.create('canvas', 'leaflet-heatmap-layer leaflet-layer');

        size = this._map.getSize();
        canvas.width  = size.x;
        canvas.height = size.y;

        animated = this._map.options.zoomAnimation && L.Browser.any3d;
        L.DomUtil.addClass(canvas, 'leaflet-zoom-' + (animated ? 'animated' : 'hide'));

		this._heat = simpleheat(canvas);
		this._heat._showNumbers = false;
		
		/* _heat Object Extension */
		this._heat.drawValue = function(ctx, value, x, y) {
			ctx.fillStyle = 'white';
			ctx.font = "14px Arial";
			ctx.textAlign = "center";
			ctx.fillText(value, x, y);
			
			return this;
		}
		
		this._heat.radius2 = function(r, blur) {
			blur = blur === undefined ? 15 : blur;

			// create a grayscale blurred circle image that we'll use for drawing points
			var circle = this._circle = document.createElement('canvas'),
				ctx = circle.getContext('2d'),
				r2 = this._r = (-r) + blur;

			circle.width = circle.height = r2 * 2;

			ctx.shadowOffsetX = ctx.shadowOffsetY = 200;
			ctx.shadowBlur = blur;
			ctx.shadowColor = 'black';

			ctx.beginPath();
			ctx.arc(r2 - 200, r2 - 200, -r, 0, Math.PI * 2, true);
			ctx.closePath();
			ctx.stroke(); //ctx.fill();

			return this;
		}
		
		this._heat.draw2 = function(minOpacity) {
			var ctx = this._ctx,
				i,
				colored,
				p,
				radius;
			
			if(!this._circle) {
				this.radius(radiusFactor, optionsBlur); //this.radius(this.defaultRadius);
			}
			
			if(!this._grad) {
				this.gradient(this.defaultGradient);
			}
			
			ctx.clearRect(0, 0, this._width, this._height);
			// draw a grayscale heatmap by putting a blurred circle at each data point
			for(i = 0; i < this._data.length; i++) {
				p = this._data[i];
				
				radius = p[3] * radiusFactor;
				//data[i][2] cannot be NaN so make work-around
				if(p[2] && (p[2] > 0)) {
					if(radius > 0) {
						this.radius(radius >= 1 ? radius : 1, optionsBlur);
						
						ctx.globalAlpha = Math.max(p[2], minOpacity === undefined ? 0 : minOpacity);
						ctx.drawImage(this._circle, p[0] - this._r, p[1] - this._r);
						
						if(p[4] && this._showNumbers) {
							this.drawValue(ctx, p[4], p[0], p[1] - (this._r + 1));
						}
					}
					else {
						//negatives are treated as hollow circles
						this.radius2(radius <= -1 ? radius : -1, optionsBlur);
						
						ctx.globalAlpha = Math.max(p[2], minOpacity === undefined ? 0 : minOpacity);
						ctx.drawImage(this._circle, p[0] - this._r, p[1] - this._r);
					}
				}
			}
			
			// colorize the heatmap, using opacity value of each pixel to get the right color from our gradient
			colored = ctx.getImageData(0, 0, this._width, this._height);
			this._colorize(colored.data, this._grad);
			ctx.putImageData(colored, 0, 0);
			
			return this;
		}
		/* End _heat Object Extension */
		
		
        this._updateOptions();
    },

    _updateOptions: function () {
        this._heat.radius(this.options.radius || this._heat.defaultRadius, this.options.blur);

        if (this.options.gradient) {
            this._heat.gradient(this.options.gradient);
        }
        if (this.options.max) {
            this._heat.max(this.options.max);
        }
    },

    _reset: function () {
        var topLeft = this._map.containerPointToLayerPoint([0, 0]);
        L.DomUtil.setPosition(this._canvas, topLeft);

        var size = this._map.getSize();

        if (this._heat._width !== size.x) {
            this._canvas.width = this._heat._width  = size.x;
        }
        if (this._heat._height !== size.y) {
            this._canvas.height = this._heat._height = size.y;
        }

        this._redraw();
    },

    _redraw: function () {
        var data = [],
            r = this._heat._r,
            size = this._map.getSize(),
            bounds = new L.LatLngBounds(
                this._map.containerPointToLatLng(L.point([-r, -r])),
                this._map.containerPointToLatLng(size.add([r, r]))),

            max = this.options.max === undefined ? 1 : this.options.max,
            maxZoom = this.options.maxZoom === undefined ? this._map.getMaxZoom() : this.options.maxZoom,
            v = 1 / Math.pow(2, Math.max(0, Math.min(maxZoom - this._map.getZoom(), 12))),
            cellSize = r / 2,
            grid = [],
            panePos = this._map._getMapPanePos(),
            offsetX = panePos.x % cellSize,
            offsetY = panePos.y % cellSize,
            i, len, p, cell, x, y, j, len2, k,
			alt,
			radius,
			value;

        // console.time('process');
        for(i = 0, len = this._latlngs.length; i < len; i++) {
            if (bounds.contains(this._latlngs[i])) {
                p = this._map.latLngToContainerPoint(this._latlngs[i]);
                x = Math.floor((p.x - offsetX) / cellSize) + 2;
                y = Math.floor((p.y - offsetY) / cellSize) + 2;

                alt =
                    this._latlngs[i].alt !== undefined ? this._latlngs[i].alt :
                    this._latlngs[i][2] !== undefined ? +this._latlngs[i][2] : 1;
                k = alt * v;
				radius = ((this._latlngs[i][3] !== undefined) ? this._latlngs[i][3] : 0) * v;
				value = ((this._latlngs[i][4] !== undefined) ? this._latlngs[i][4] : 0);

                grid[y] = grid[y] || [];
                cell = grid[y][x];

                if(!cell) {
					//grid[y][x] = [p.x, p.y, k, k];
					grid[y][x] = [p.x, p.y, k, radius, value];
                }
				else {
                    cell[0] = (cell[0] * cell[2] + p.x * k) / (cell[2] + k); // x
                    cell[1] = (cell[1] * cell[2] + p.y * k) / (cell[2] + k); // y
                    cell[2] += k; // cumulated intensity value
					cell[3] = radius;
					cell[4] = value;
                }
            }
        }

        for (i = 0, len = grid.length; i < len; i++) {
            if (grid[i]) {
                for (j = 0, len2 = grid[i].length; j < len2; j++) {
                    cell = grid[i][j];
                    if(cell) {
                        data.push([
                            Math.round(cell[0]),
                            Math.round(cell[1]),
                            Math.min(cell[2], max),
							Math.min(cell[3], max),
							cell[4]
                        ]);
                    }
                }
            }
        }
        // console.timeEnd('process');

        // console.time('draw ' + data.length);
        this._heat.data(data).draw2(this.options.minOpacity); //this._heat.data(data).draw(this.options.minOpacity);
        // console.timeEnd('draw ' + data.length);
		
        this._frame = null;
    },

    _animateZoom: function (e) {
        var scale = this._map.getZoomScale(e.zoom),
            offset = this._map._getCenterOffset(e.center)._multiplyBy(-scale).subtract(this._map._getMapPanePos());

        if (L.DomUtil.setTransform) {
           L.DomUtil.setTransform(this._canvas, offset, scale);

        } else {
            this._canvas.style[L.DomUtil.TRANSFORM] = L.DomUtil.getTranslateString(offset) + ' scale(' + scale + ')';
        }
    }
});

L.heatLayer = function (latlngs, options) {
    return new L.HeatLayer(latlngs, options);
};
