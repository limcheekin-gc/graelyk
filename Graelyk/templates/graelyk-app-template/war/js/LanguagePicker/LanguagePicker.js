jQuery(function() {
	
	var languagePicker = {
	
		//Language Name Type Codes:
		LANGUAGE : "L",
		LANGUAGE_ALTERNATE : "LA",
		LANGUAGE_PEJORATIVE : "LP",
		DIALECT : "D",
		DIALECT_ALTERNATE : "DA",
		DIALECT_PEJORATIVE : "DP",
		EVERY_TYPE : ["L", "LA", "LP", "D", "DA", "DP"],
		NON_PEJORATIVE : ["L", "LA", "D", "DA"],

		//Language Status Codes:
		LIVING : "L",
		NEARLY_EXTINCT : "N",
		EXTINCT : "X",
		SECOND_LANGUAGE_ONLY : "S",
		EVERY_STATUS : ["L", "N", "X", "S"],
		NOT_EXTINCT : ["L", "N", "S"],
		
		//Root LanguagePicker directory
		jsonRoot : "/js/LanguagePicker/",
		
		autoLoadCountries : function(fromSelectID, toSelectID, options) {
			var fromSelect = jQuery(fromSelectID);
			var toSelect = jQuery(toSelectID);
			
			//Set default values
			var countryFilter = [];
			if(options != undefined)
			{
				countryFilter = options.countryFilter;
			}
			
			//Create a map of true values for only those constants that exist in the type and status arrays
			var filterCountry = false;
			if(countryFilter.length > 0){filterCountry = true;}
			var filterMap = {};
			for(var i=0; i<countryFilter.length; i++)
			{
				filterMap[countryFilter[i]] = true;
			}
			
			//Get current region
			var regionCode = fromSelect.val();
			
			//Remove all options from toSelect
			toSelect.find("option").remove();
			
			//If the regionCode is blank (i.e. maybe it is an instruction line like "Select a Region and Country" with value=""), don't try to load a file.
			if(regionCode == "")
			{
				return;
			}
			
			//Get the list of countries for the currently selected region. Add options for each country to the country select list.
			var languageStruct = jQuery.getJSON(this.jsonRoot + "RegionCountries/" + regionCode + ".json",
				function(data){ 
					jQuery.each(data.countries, function(i,country){
						if(!filterCountry || filterMap[country[0]])
						{
							jQuery("<option value='" + country[0] + "'>" + country[1] + " (" + country[0] + ")</option>").appendTo(toSelect); 
						}
					}); 
				});
		},
		
		
		autoLoadLanguages : function(fromSelectID, toSelectID, options) {
			//Set default values
			var appendCountryCode, showCode, type, status;
			if(options != undefined)
			{
				appendCountryCode = options.appendCountryCode;
				showCode = options.showCode;
				type = options.type;
				status = options.status;
			}
			if(appendCountryCode == undefined){appendCountryCode = true;}
			if(showCode == undefined){showCode = true;}
			if(type == undefined){type = [this.LANGUAGE, this.LANGUAGE_ALTERNATE];}
			if(status == undefined){status = this.NOT_EXTINCT;}

			//Create a map of true values for only those constants that exist in the type and status arrays
			var filterMap = {}
			for(var i=0; i<type.length; i++)
			{
				filterMap[type[i]] = true;
			}
			for(var i=0; i<status.length; i++)
			{
				filterMap[type[i]] = true;
			}

			//Set select list variables
			var fromSelect = jQuery(fromSelectID);
			var toSelect = jQuery(toSelectID);
			
			//Get current country code
			var countryCode
			if(fromSelectID.match(/^[A-Z]{2,2}$/))
			{
				countryCode = fromSelectID;
			}
			else
			{
				countryCode = fromSelect.val();
			}
			
			//Remove all options from toSelect
			toSelect.find("option").remove();
			
			//Get the list of languages for the currently selected country. Add options for each language to the language select list.
			var languageStruct = jQuery.getJSON(this.jsonRoot + "CountryLanguages/" + countryCode + ".json",
				function(data){ 
					jQuery.each(data.languages, function(i,lang){
						var code = lang[0];
						var name = lang[1];
						var type = lang[2];
						var status = lang[3];
						//Filter based on name-type and status. Only show elements that are included in the filter.
						if(filterMap[type] && filterMap[status])
						{
							var value = code;
							if(appendCountryCode)
							{
								value = value + "_" + countryCode;
							}
							var text = name;
							if(showCode)
							{
								text = text + " (" + code + ")";
							}
							jQuery("<option value='" + value + "'>" + text+ "</option>").appendTo(toSelect); 
						}
					}); 
				});
		},
		
		copySelectedTo : function(fromSelectID, toSelectID, hiddenDivID, hiddenInputName, options) {
			//Set default values
			var countrySelectID, appendCountryCode, appendLanguageName, showCountry, showCode, single;
			if(options != undefined)
			{
				countrySelectID = options.countrySelect;
				appendCountryCode = options.appendCountryCode;
				appendLanguageName = options.appendLanguageName;
				showCountry = options.showCountry;
				showCode = options.showCode;
				single = options.single;
			}
			if(appendCountryCode == undefined){appendCountryCode = true;}
			if(appendLanguageName == undefined){appendLanguageName = true;}
			if(showCountry == undefined){showCountry = true;}
			if(showCode == undefined){showCode = true;}
			if(single == undefined){single = false;}
			
			if(single)
			{
				languagePicker.removeAllFrom(toSelectID, hiddenDivID)
			}
			
			jQuery(fromSelectID + " option:selected").each(function(){
				
				//Get the languageCode, the countryCode, and the combined localeCode from the value of the "from" select list
				var codes = jQuery(this).val().split("_");
				var languageCode = codes[0];
				var countryCode = codes[1];
				var localeCode = languageCode;

				//Get the languageName from the text of the "from" select list
				var languageName = jQuery(this).text().replace(" \(" + languageCode + "\)", "");
				
				//Create the localeCode that will be sent with the form submission
				if(appendCountryCode)
				{
					localeCode = localeCode + "_" + countryCode;
				}
				if(appendLanguageName)
				{
					if(!appendCountryCode)
					{
						localeCode = localeCode + "_";
					}
					localeCode = localeCode + "_" + languageName
				}
				if(localeCode.match(/__$/))
				{
					localeCode =  localeCode.replace(/__$/, "")
				}

				//Get the countryName from the text of the country select list (if one was provided)
				var countryName = "";
				if(countrySelectID != null && countrySelectID != undefined)
				{
					countryName = " [" + jQuery(countrySelectID + " option:selected").text().replace(" \(" + countryCode + "\)", "") + "]";
				}

				//Check that the localeCode does not already exist as an option in the "to" select list. If it doesn't exist, add it.
				var alreadyExists = jQuery(toSelectID + " option[value='" + localeCode + "']").val();
				if(alreadyExists != localeCode)
				{
					var text = languageName
					if(showCountry)
					{
						text = text + countryName;
					}
					if(showCode)
					{
						text = text + " (" + localeCode + ")";
					}
					jQuery("<option></option>").val(localeCode).html(text).appendTo(jQuery(toSelectID))
				}
			});
			languagePicker.updateHidden(toSelectID, hiddenDivID, hiddenInputName)
		},
		
		removeAllFrom : function(fromSelectID, hiddenDivID, hiddenInputName) {
			jQuery(fromSelectID + " option").remove()
			languagePicker.updateHidden(fromSelectID, hiddenDivID, hiddenInputName)
		},		
		
		removeSelectedFrom : function(fromSelectID, hiddenDivID, hiddenInputName) {
			jQuery(fromSelectID + " option:selected").remove()
			languagePicker.updateHidden(fromSelectID, hiddenDivID, hiddenInputName)
		},
		
		moveSelectedUp : function(fromSelectID, hiddenDivID, hiddenInputName) {
			jQuery(fromSelectID + " option:selected").each(function(){
				jQuery(this).insertBefore(jQuery(this).prev());
			});			
			languagePicker.updateHidden(fromSelectID, hiddenDivID, hiddenInputName)
		},
		
		moveSelectedDown : function(fromSelectID, hiddenDivID, hiddenInputName) {
			jQuery(jQuery(fromSelectID + " option:selected").get().reverse()).each(function(){
				jQuery(this).insertAfter(jQuery(this).next());
			});
			languagePicker.updateHidden(fromSelectID, hiddenDivID, hiddenInputName)
		},
		
		updateHidden : function(fromSelectID, hiddenDivID, hiddenInputName) {
			jQuery(hiddenDivID).empty()
			jQuery(fromSelectID + " option").each(function(){
				jQuery(hiddenDivID).append(jQuery('<input type="hidden" name="' + hiddenInputName + '" value="' + jQuery(this).val() + '" />'))
			});
		},

		
		openEthnologue : function(fromSelectID) {
			jQuery(fromSelectID + " option:selected").each(function(){
				var languageCode = jQuery(this).val().split("_")[0]
				window.open("http://www.ethnologue.com/show_language.asp?code=" + languageCode)
			});
		},
		
		openEthnologueFamily : function(fromSelectID) {
			jQuery(fromSelectID + " option:selected").each(function(){
				var languageCode = jQuery(this).val().split("_")[0]
				window.open("http://www.ethnologue.com/show_lang_family.asp?code=" + languageCode)
			});
		}
	};

	// Expose languagePicker namespace
	window.languagePicker = languagePicker;
});