package groovyx.gaelyk.graelyk.taglyk


/**
*  A  tag lib that provides tags for use with JQuery, sometimes as substitutes for tags in other tag libs
*
* @author Jeremy Brown
* @since 23-Dec-2010
*/

class JQueryTaglyk 
{
	/**
     * Creates a button that submits to an action in the controller specified by the form action
     * The name of the action attribute is translated into the action name, for example "Edit" becomes
     * "_action_edit" or "List People" becomes "_action_listPeople"
     * If the action attribute is not specified, the value attribute will be used as part of the action name
	 * The inputSelector gives the jQuery selector for the hidden input element that should transmit the action name (default: "#action")
	 * The formSelector gives the jQuer selector for the form that should be submitted (default: "#mainForm")
	 * If specified, icon and icon2 can take jQuery UI CSS classes for the first and second icons, e.g. icon:"plus"
	 * secondary:true can be specified to change the CSS class of the button from ui-button-text-icon-primary to ui-button-text-icon-secondary. The default is secondary:false
	 * corners: if specified, this list of jQuery UI CSS classes will override the default corner class of ui-corner-all
     *
     *  <g_jQueryActionSubmit formSelector:"#mainForm", inputSelector:"#action", value="Edit" />
     *  <g_jQueryActionSubmit formSelector:"#mainForm", inputSelector:"#action, action="Edit", icon:"pencil", value="Some label for editing" />
     *
     */
	static g_jQueryActionSubmit(script, attrs){script.out << script.jQueryActionSubmit(attrs)}
    static jQueryActionSubmit(script, attrs)
	{
		def out = new StringBuffer()

		script.jQueryButtonInnerHTML(attrs)
		def icon = attrs.remove("icon")
		def icon2 = attrs.remove("icon2")
		def text = attrs.remove("text")
		
        // add action and value
        def value = ""
		if(attrs.value){value = attrs.remove('value')}
		
        def action = value
		if(attrs.action){action = attrs.remove('action')}
		
		def inputSelector = attrs.inputSelector ? attrs.remove("inputSelector") : "#action"
		def formSelector = attrs.formSelector ? attrs.remove("formSelector") : "#mainForm"
		def onclick = ""
		if(attrs.onclick){onclick = attrs.remove("onclick")}

		out << """<button onclick='${onclick} jQuery("${inputSelector}").val("${action}"); jQuery("${formSelector}").submit();' """

        // process remaining attributes
        script.outputAttributes(out, attrs)

        // close tag
        out << """>${icon}${text}${icon2}</button>"""

		return out.toString()
    }
	
	
	static g_jQueryButtonLink(script, attrs, body=null){script.out << script.jQueryButtonLink(attrs, body)}
	static jQueryButtonLink(script, attrs, body=null)
	{
		def out = new StringBuffer()
		
		if(body)
		{
			attrs.value =  script.captureOut(body)
		}
		
		script.jQueryButtonInnerHTML(attrs)
		def icon = attrs.remove("icon")
		def icon2 = attrs.remove("icon2")
		def text = attrs.remove("text")

		out << script.link(attrs){icon + text + icon2}
		
		return out.toString()
	}
	
	

	static jQueryButtonInnerHTML(script, attrs)
	{
		def icon = ""
		if(attrs.icon)
		{
			icon = attrs.remove("icon")
			if(!icon.startsWith("ui-icon-"))
			{
				icon = "ui-icon-" + icon
			}
			icon = """<span class="ui-button-icon-primary ui-icon ${icon}"></span>"""
		}
		def icon2 = "" 
		if(attrs.icon2)
		{
			icon2 = attrs.remove("icon2")
			if(!icon2.startsWith("ui-icon-"))
			{
				icon2 = "ui-icon-" + icon2
			}
			icon2 = """<span class="ui-button-icon-secondary ui-icon ${icon2}"></span>"""
		}
		def secondary = false
		if(attrs.secondary){secondary = true}
		def corners = "ui-corner-all"
		if(attrs.corners){corners = attrs.remove("corners")}
		
		def text = ""
		if(attrs.value){text = """<span class="ui-button-text">${attrs.remove("value")}</span>"""}
		else if(attrs.text){text = """<span class="ui-button-text">${attrs.remove("text")}</span>"""}
		attrs.remove("text")
		attrs.remove("value")
		
		def cssClass = ""
		if(attrs["class"]){cssClass = attrs.remove("class") + " "}
		cssClass += "ui-button ui-state-default"
		cssClass += " " + corners
		cssClass += secondary ? " ui-priority-secondary" : " ui-priority-primary"
		if(!icon && !icon2){cssClass += " ui-button-text-only"}
		else if(icon && icon2 && !text){cssClass += " ui-button-icons-only"}
		else if(icon && icon2 && text){cssClass += " ui-button-text-icons"}
		else if(icon && !text){cssClass += " ui-button-icon-primary"}
		else if(icon2 && !text){cssClass += " ui-button-icon-secondary"}
		else if(icon && text){cssClass += " ui-button-text-icon ui-button-text-icon-primary"}
		else if(icon2 && text){cssClass += " ui-button-text-icon ui-button-text-icon-secondary"}
		
		attrs.icon = icon
		attrs.icon2 = icon2
		attrs.text = text
		attrs.put("class", cssClass)
	}
}