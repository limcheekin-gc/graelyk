<%
import com.google.appengine.api.datastore.GeoPt
import groovyx.gaelyk.graelyk.util.ObjectUtils;

try{unwrapVariables()}catch(Exception e){e.toString()}
request.includeReturn = captureOut(){
		def script = this
		def attrs = geoPtPickerAttributes

		//Attributes:
		//multiple: true or false (default false) Should multiple locale selections be possible?
		//format: "horizontal", "vertical"
		//width: the width of the map
		//height: the height of the map
		//zoom: the zoom level
		//mapType: the map type ('roadmap', 'satellite', 'hybrid', 'terrain') 
		//enabled: (default: true) if false, markers cannot be added or moved on the map
		//maxMarkers: (default: 1) number of markers allowed to be added (limited by number of custom markers if alpha, numeric, or alphanumeric is specified for the orderType. -1 indicates no limit
		//orderType: (default: none) "none", "numeric", "alpha", "alphanumeric" - specifies the marker used to order an ordered list of markers for the GeoPts
		//size: (default: 20): size property for the select box
		//value or geoPt: the currently selected GeoPt(s)
		//showButtons: (default: true) whether to show the buttons that allow manipulating the order in the select list and removing items from the select list
		//showSelect: (default: true) whether to show the select list or not (if not shown, it will still be there, simply hidden with a style of display:none)

		def multiple = ""
		if(attrs.multiple){multiple='testing'}

		def width = attrs.width ? attrs.remove("width") : "400px"
		def height = attrs.height ? attrs.remove("height") : "400px"
		def zoom = attrs.zoom ? attrs.remove("zoom") : "1"
		def format = "horizontal"
		if(attrs.containsKey("format")){format = attrs.remove("format").toLowerCase()}
		def mapType = attrs.mapType ? attrs.remove("mapType").toUpperCase() : "roadmap"

		def enabled = true
		if(attrs.containsKey("enabled")){enabled = attrs.remove("enabled")}

		def maxMarkers = 1
		if(multiple){maxMarkers = -1}
		if(attrs.containsKey("maxMarkers")){maxMarkers = attrs.remove("maxMarkers")}

		def orderType = "none"
		if(attrs.containsKey("orderType")){orderType = attrs.remove("orderType")}

		def size = 20
		if(attrs.containsKey("size")){size = attrs.remove("size")}
		
		def showButtons = true
		if(attrs.containsKey("showButtons")){showButtons = attrs.remove("showButtons")}
		
		def showSelect = true
		if(attrs.containsKey("showSelect")){showSelect = attrs.remove("showSelect")}
		def hiddenSelectClass = ""
		if(showSelect)
		{
			hiddenSelectClass = " hidden"
		}

		def name = attrs.remove('name')
		def values = attrs['value'] ? attrs['value'] : (attrs['geoPt'] ? attrs['geoPt'] : new GeoPt(0.0, 0.0))
		//If values isn't a List or array, turn it into a list
		if(!ObjectUtils.isListOrArray(values))
		{
			values = [values]
		}

		values = values.collect{"[${it.latitude}, ${it.longitude}]"}.join(",")

		if(format == "vertical")
		{
%>		


<script type="text/javascript">jQuery(function(){geoPtPicker.createMap("<%=name%>", {points:[<%=values%>], zoom:<%=zoom%>, type:"<%=mapType%>", enabled:<%=enabled%>, maxMarkers:<%=maxMarkers%>, orderType:"<%=orderType%>"})});</script>
<table class="geoPtPickerArea">
<tr>
<td class="geoPtPickerMap"><div class="geoPtPickerMap" id="<%=name%>_map" style="width: <%=width%>; height: <%=height%>"></div></td>
</tr>
<tr>
<td class="geoPtPickerList">
<select id="<%=name%>_select" size="<%=size%>" multiple="multiple" class="geoPtPickerSelect${hiddenSelectClass}"></select>
<% if(showButtons) { %>
<input type="button" value="${message(code:"default.geopt.picker.button.remove", default:"&darr;")}" onclick="geoPtPicker.removeSelected('<%=name%>');">
<input type="button" value="${message(code:"default.geopt.picker.button.up", default:"&uarr;")}" onclick="geoPtPicker.moveSelectedUp('<%=name%>');">
<input type="button" value="${message(code:"default.geopt.picker.button.down", default:"&darr;")}" onclick="geoPtPicker.moveSelectedDown('<%=name%>');">
<% } %>
<div id="<%=name%>_hiddenDiv" style="display:none"></div>
</td>
</tr>
</table>


<% } else { %>


<script type="text/javascript">jQuery(function(){geoPtPicker.createMap("<%=name%>", {points:[<%=values%>], zoom:<%=zoom%>, type:"<%=mapType%>", enabled:<%=enabled%>, maxMarkers:<%=maxMarkers%>, orderType:"<%=orderType%>"})});</script>
<table class="geoPtPickerArea">
<tr>
<td class="geoPtPickerMap"><div class="geoPtPickerMap" id="<%=name%>_map" style="width: <%=width%>; height: <%=height%>"></div></td>
<td class="geoPtPickerList">
<select id="<%=name%>_select" size="20" multiple="multiple" class="geoPtPickerSelect${hiddenSelectClass}"></select>
<% if(showButtons) { %>
<input type="button" value="${message(code:"default.geopt.picker.button.remove", default:"&darr;")}" onclick="geoPtPicker.removeSelected('<%=name%>');">
<input type="button" value="${message(code:"default.geopt.picker.button.up", default:"&uarr;")}" onclick="geoPtPicker.moveSelectedUp('<%=name%>');">
<input type="button" value="${message(code:"default.geopt.picker.button.down", default:"&darr;")}" onclick="geoPtPicker.moveSelectedDown('<%=name%>');">
<% } %>
<div id="<%=name%>_hiddenDiv" style="display:none"></div>
</td>
</tr>
</table>
		
		
<% } 
} %>