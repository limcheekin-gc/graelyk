System.out.println("Registering GraelykPlugin...")

// add new variables in the binding
binding {
}

//install new categories
categories groovyx.gaelyk.graelyk.GraelykCategory, 
			groovyx.gaelyk.graelyk.taglyk.ApplicationTaglyk,
			groovyx.gaelyk.graelyk.taglyk.CountryTaglyk,
			groovyx.gaelyk.graelyk.taglyk.FormTaglyk,
			groovyx.gaelyk.graelyk.taglyk.FormatTaglyk,
			groovyx.gaelyk.graelyk.taglyk.GeoPtTaglyk,
			groovyx.gaelyk.graelyk.taglyk.ISO639_3Taglyk,
			groovyx.gaelyk.graelyk.taglyk.MessageTaglyk,
			groovyx.gaelyk.graelyk.taglyk.RenderTaglyk,
			groovyx.gaelyk.graelyk.taglyk.UserTaglyk,
			groovyx.gaelyk.graelyk.taglyk.ValidationTaglyk
