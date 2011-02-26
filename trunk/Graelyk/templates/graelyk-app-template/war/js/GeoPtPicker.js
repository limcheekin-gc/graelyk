jQuery(function() {
	var geoPtPicker = {
		allMaps: new Object(),
		allMarkers: new Object(),
		
		ORDER_NONE: "none",
		ORDER_NUMERIC: "numeric",
		ORDER_ALPHA: "alpha",
		ORDER_ALPHANUMERIC: "alphanumeric",
		
		numericLabels: ["1","2","3","4","5","6","7","8","9"],
		numericMarkers: ["/images/GeoPtPicker/marker_1.png",
		"/images/GeoPtPicker/marker_2.png",
		"/images/GeoPtPicker/marker_3.png",
		"/images/GeoPtPicker/marker_4.png",
		"/images/GeoPtPicker/marker_5.png",
		"/images/GeoPtPicker/marker_6.png",
		"/images/GeoPtPicker/marker_7.png",
		"/images/GeoPtPicker/marker_8.png",
		"/images/GeoPtPicker/marker_9.png"
		],
		numericShadow: "/images/GeoPtPicker/marker_shadow.png",
		
		alphaLabels: ["A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"],
		alphaMarkers: ["/images/GeoPtPicker/marker_a.png", "/images/GeoPtPicker/marker_b.png", "/images/GeoPtPicker/marker_c.png",
		"/images/GeoPtPicker/marker_d.png", "/images/GeoPtPicker/marker_e.png", "/images/GeoPtPicker/marker_f.png",
		"/images/GeoPtPicker/marker_g.png", "/images/GeoPtPicker/marker_h.png", "/images/GeoPtPicker/marker_i.png",
		"/images/GeoPtPicker/marker_j.png", "/images/GeoPtPicker/marker_k.png", "/images/GeoPtPicker/marker_l.png",
		"/images/GeoPtPicker/marker_m.png", "/images/GeoPtPicker/marker_n.png", "/images/GeoPtPicker/marker_o.png",
		"/images/GeoPtPicker/marker_p.png", "/images/GeoPtPicker/marker_q.png", "/images/GeoPtPicker/marker_r.png",
		"/images/GeoPtPicker/marker_s.png", "/images/GeoPtPicker/marker_t.png", "/images/GeoPtPicker/marker_u.png",
		"/images/GeoPtPicker/marker_v.png", "/images/GeoPtPicker/marker_w.png", "/images/GeoPtPicker/marker_x.png",
		"/images/GeoPtPicker/marker_y.png", "/images/GeoPtPicker/marker_z.png"
		],
		alphaShadow: "/images/GeoPtPicker/marker_shadow.png",
		
		alphaNumericLabels: ["1","2","3","4","5","6","7","8","9","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"],
		alphaNumericMarkers: ["/images/GeoPtPicker/marker_1.png", "/images/GeoPtPicker/marker_2.png", "/images/GeoPtPicker/marker_3.png",
		"/images/GeoPtPicker/marker_4.png", "/images/GeoPtPicker/marker_5.png", "/images/GeoPtPicker/marker_6.png",
		"/images/GeoPtPicker/marker_7.png", "/images/GeoPtPicker/marker_8.png", "/images/GeoPtPicker/marker_9.png",
		"/images/GeoPtPicker/marker_a.png", "/images/GeoPtPicker/marker_b.png", "/images/GeoPtPicker/marker_c.png",
		"/images/GeoPtPicker/marker_d.png", "/images/GeoPtPicker/marker_e.png", "/images/GeoPtPicker/marker_f.png",
		"/images/GeoPtPicker/marker_g.png", "/images/GeoPtPicker/marker_h.png", "/images/GeoPtPicker/marker_i.png",
		"/images/GeoPtPicker/marker_j.png", "/images/GeoPtPicker/marker_k.png", "/images/GeoPtPicker/marker_l.png",
		"/images/GeoPtPicker/marker_m.png", "/images/GeoPtPicker/marker_n.png", "/images/GeoPtPicker/marker_o.png",
		"/images/GeoPtPicker/marker_p.png", "/images/GeoPtPicker/marker_q.png", "/images/GeoPtPicker/marker_r.png",
		"/images/GeoPtPicker/marker_s.png", "/images/GeoPtPicker/marker_t.png", "/images/GeoPtPicker/marker_u.png",
		"/images/GeoPtPicker/marker_v.png", "/images/GeoPtPicker/marker_w.png", "/images/GeoPtPicker/marker_x.png",
		"/images/GeoPtPicker/marker_y.png", "/images/GeoPtPicker/marker_z.png"
		],
		alphaNumericShadow: "/images/GeoPtPicker/marker_shadow.png",
		
		createMap: function (fieldName, options)
		{
			var draggableCursor = 'pointer';
			
			var latitudeLongitudeArray = new Array()
			if(options.points != null){latitudeLongitudeArray = options.points;}

			var zoom = 1;
			if(options.zoom != null){zoom = options.zoom;}
			
			var type = "roadmap";
			if(options.type != null){type = options.type;}
			
			var enabled = true;
			if(options.enabled != null){enabled = options.enabled;}
			if(enabled)
			{
				draggableCursor = 'crosshair';
			}			
			var maxMarkers = 1;
			if(options.maxMarkers != null){maxMarkers = options.maxMarkers;}
			
			var orderType = this.ORDER_NONE;
			if(options.orderType != null){orderType = options.orderType;}
			
			var customMarkers;
			var customShadow;
			var customLabels;
			if(orderType == this.ORDER_NUMERIC)
			{
				customMarkers = this.numericMarkers;
				customShadow = this.numericShadow;
				customLabels = this.numericLabels;
			}
			else if(orderType == this.ORDER_ALPHA)
			{
				customMarkers = this.alphaMarkers;
				customShadow = this.alphaShadow;
				customLabels = this.alphaLabels;
			}
			else if(orderType == this.ORDER_ALPHANUMERIC)
			{
				customMarkers = this.alphaNumericMarkers;
				customShadow = this.alphaNumericShadow;
				customLabels = this.alphaNumericLabels;
			}

			if(options.customMarkers != null){customMarkers = options.customMarkers;}
			if(options.customShadow != null){customShadow = options.customShadow;}
			if(options.customLabels != null){customLabels = options.customLabels;}
			if(customMarkers != null && (maxMarkers > customMarkers.length || maxMarkers == -1))
			{
				maxMarkers = customMarkers.length;
			}

						
			//Convert the array of [latitude and longitude pairs] to geoPts, and find the center of these points so the map can be centered on and contain all the points
			var geoPts = new Array();
			var bounds = new google.maps.LatLngBounds(0.0, 0.0);
			for (var i = 0; i < latitudeLongitudeArray.length; i++)
			{
				//Create the geoPt for this latitude/longitude
				geoPts[i] = new google.maps.LatLng(latitudeLongitudeArray[i][0], latitudeLongitudeArray[i][1]);
				// Extend the LatLngBound object
				bounds.extend(geoPts[i]);
			}
			
			var map = new google.maps.Map(document.getElementById(fieldName + "_map"), {
					center: bounds.getCenter(),
					zoom: zoom,
					mapTypeId: type,
					draggableCursor: draggableCursor, 
					enabled: enabled, //this extra property tells whether to allow clicking and dragging
					fieldName:fieldName, //this extra property tells the unique field name (in this html document) that identifies this map and its markers
					currentMarker:0, //this extra property is added to tell what the current marker being added/moved is
					maxMarkers:maxMarkers, //this extra property tells how many markers are allowed on the map
					orderType:orderType, //this extra property, when true, indicates numbers should be used to order the list of GeoPts
					customLabels:customLabels,
					customMarkers:customMarkers,
					customShadow:customShadow,
					clickCallback: function(event) {
							if(this.currentMarker+1 < this.maxMarkers || maxMarkers == -1) //if maxMarker is -1, an infinite number of markers can be placed
							{
								geoPtPicker.createMarker(this.fieldName, this.currentMarker, event.latLng.lat().toFixed(6), event.latLng.lng().toFixed(6));
								this.currentMarker++;
								geoPtPicker.updateData(fieldName);
							}
					}
				});
			map.panToBounds(bounds);
			geoPtPicker.allMaps[fieldName] = map;

			
			var markers = new Array();
			geoPtPicker.allMarkers[fieldName] = markers;
			for(var i=0; i<geoPts.length; i++)
			{
				geoPtPicker.createMarker(fieldName, i, geoPts[i].lat(), geoPts[i].lng());
			}
				
			if(enabled)
			{
				google.maps.event.addListener(map, 'click', map.clickCallback);
			}
			
			geoPtPicker.updateData(fieldName);
			
			return false;
		}, // end of createMap
		
		
		createMarker: function(fieldName, markerNumber, latitude, longitude)
		{
			var map = geoPtPicker.allMaps[fieldName];
			var markers = geoPtPicker.allMarkers[fieldName];
			var marker = new google.maps.Marker({
					map: map,
					position: new google.maps.LatLng(latitude, longitude),
					draggable: map.enabled,
					dragEndCallback: function(event) {
						marker.position = new google.maps.LatLng(event.latLng.lat().toFixed(6), event.latLng.lng().toFixed(6));
						geoPtPicker.updateData(fieldName);
					}
				});
				if(map.orderType != geoPtPicker.ORDER_NONE)
				{
					marker.setTitle(map.customLabels[markerNumber]);
					marker.setIcon(map.customMarkers[markerNumber]);
					marker.setShadow(map.customShadow);
				}
				if(map.enabled)
				{
					google.maps.event.addListener(marker, 'dragend', marker.dragEndCallback);
				}
				markers[markers.length] = marker;
		},
		
		
		clearData: function(fieldName)
		{
			jQuery("#" + fieldName + "_select" + " option").remove()
			jQuery("#" + fieldName + "_hiddenDiv").empty()
		},
		
		
		updateData : function(fieldName, selectedList) {
			geoPtPicker.clearData(fieldName);
			var hiddenDiv = jQuery("#" + fieldName + "_hiddenDiv");
			var select = jQuery("#" + fieldName + "_select");
			var map = geoPtPicker.allMaps[fieldName];
			for(var i = 0; i < geoPtPicker.allMarkers[fieldName].length; i++)
			{
				var selected = "";
				if(selectedList != null)
				{
					if(selectedList.length > i)
					{
						if(selectedList[i] == true)
						{
							selected = "selected";
						}
					}
				}
				var marker = geoPtPicker.allMarkers[fieldName][i]
				var latLong = marker.position.lat() + ", " + marker.position.lng();
				if(map.orderType != geoPtPicker.ORDER_NONE)
				{
					combo = map.customLabels[i] + ": " + latLong;
					marker.setTitle(map.customLabels[i] );
					marker.setIcon(map.customMarkers[i]);
					marker.setShadow(map.customShadow);
				}
				jQuery("<option></option>").val(combo).html(combo).attr("selected",selected).appendTo(jQuery(select));
				jQuery(hiddenDiv).append(jQuery('<input type="hidden" name="' + fieldName + '" value="' + latLong + '" />'));
			}
		},
		
		addData : function(fieldName, latitude, longitude) {
			var hiddenDiv = jQuery("#" + fieldName + "_hiddenDiv");
			var select = jQuery("#" + fieldName + "_select");
			var marker = geoPtPicker.allMarkers[fieldName];
			var combo = marker.position.lat() + ", " + marker.position.lng();
			jQuery("<option></option>").val(combo).html(combo).appendTo(jQuery(select));
			jQuery(hiddenDiv).append(jQuery('<input type="hidden" name="' + fieldName + '" value="' + jQuery(this).val() + '" />'));
		},
		
		removeSelected : function(fieldName) {
			var oldMarkers = geoPtPicker.allMarkers[fieldName];
			var newMarkers = new Array();
			jQuery(jQuery("#" + fieldName + "_select option:selected").get().reverse()).each(function(index, option){
				index = option.index
				oldMarkers[index].getMap().currentMarker--;
				oldMarkers[index].setMap(null);
				oldMarkers[index] = null;
			});
			var j = 0;
			for(var i=0; i<oldMarkers.length; i++)
			{
				if(oldMarkers[i] != null)
				{
					newMarkers[j] = oldMarkers[i];
					j++;
				}
			}
			geoPtPicker.allMarkers[fieldName] = newMarkers;
			geoPtPicker.updateData(fieldName);
		},
		
		moveSelectedUp : function(fieldName) {
			var markers = geoPtPicker.allMarkers[fieldName];
			var temp;
			var selectedList = new Array();
			jQuery("#" + fieldName + "_select option:selected").each(function(index, option){
				index = option.index
				if(index > 0)
				{
					temp = markers[index - 1];
					markers[index - 1] = markers[index];
					markers[index] = temp;
					selectedList[index-1] = true;
				}
				else
				{
					selectedList[index] = true;
				}
			});			
			geoPtPicker.updateData(fieldName, selectedList);
		},
		
		moveSelectedDown : function(fieldName) {
			var markers = geoPtPicker.allMarkers[fieldName];
			var temp;
			var selectedList = new Array();
			jQuery(jQuery("#" + fieldName + "_select option:selected").get().reverse()).each(function(index, option){
				index = option.index
				if(index+1 < markers.length)
				{
					temp = markers[index + 1];
					markers[index + 1] = markers[index];
					markers[index] = temp;
					selectedList[index+1] = true;
				}
				else
				{
					selectedList[index] = true;
				}
			});
			geoPtPicker.updateData(fieldName, selectedList);
		},
	};
	
	// Expose geoPtPicker namespace
	window.geoPtPicker = geoPtPicker;
});