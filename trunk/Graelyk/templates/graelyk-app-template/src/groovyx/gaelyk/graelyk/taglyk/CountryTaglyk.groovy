/* Copyright 2004-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT c;pWARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
//Originally package org.codehaus.groovy.grails.plugins.web.taglib
package groovyx.gaelyk.graelyk.taglyk

import groovyx.gaelyk.graelyk.util.ObjectUtils;

/**
 * Tags for rendering country selection / display of country names.
 *
 * @todo add ISO language codes too
 *
 * @author Marc Palmer (marc@anyware.co.uk)
 */
class CountryTaglyk {
	static final ISO3166_2 = [
		"AF":"Afghanistan",
		"AX":"Åland Islands",
		"AL":"Albania",
		"DZ":"Algeria",
		"AS":"American Samoa",
		"AD":"Andorra",
		"AO":"Angola",
		"AI":"Anguilla",
		"AQ":"Antarctica",
		"AG":"Antigua And Barbuda",
		"AR":"Argentina",
		"AM":"Armenia",
		"AW":"Aruba",
		"AU":"Australia",
		"AT":"Austria",
		"AZ":"Azerbaijan",
		"BS":"Bahamas",
		"BH":"Bahrain",
		"BD":"Bangladesh",
		"BB":"Barbados",
		"BY":"Belarus",
		"BE":"Belgium",
		"BZ":"Belize",
		"BJ":"Benin",
		"BM":"Bermuda",
		"BT":"Bhutan",
		"BO":"Bolivia, Plurinational State of",
		"BA":"Bosnia And Herzegovina",
		"BW":"Botswana",
		"BV":"Bouvet Island",
		"BR":"Brazil",
		"IO":"British Indian Ocean Territory",
		"BN":"Brunei Darussalam",
		"BG":"Bulgaria",
		"BF":"Burkina Faso",
		"BI":"Burundi",
		"KH":"Cambodia",
		"CM":"Cameroon",
		"CA":"Canada",
		"CV":"Cape Verde",
		"KY":"Cayman Islands",
		"CF":"Central African Republic",
		"TD":"Chad",
		"CL":"Chile",
		"CN":"China",
		"CX":"Christmas Island",
		"CC":"Cocos (Keeling) Islands",
		"CO":"Colombia",
		"KM":"Comoros",
		"CG":"Congo",
		"CD":"Congo, The Democratic Republic of the",
		"CK":"Cook Islands",
		"CR":"Costa Rica",
		"CI":"Côte D'Ivoire",
		"HR":"Croatia",
		"CU":"Cuba",
		"CY":"Cyprus",
		"CZ":"Czech Republic",
		"DK":"Denmark",
		"DJ":"Djibouti",
		"DM":"Dominica",
		"DO":"Dominican Republic",
		"EC":"Ecuador",
		"EG":"Egypt",
		"SV":"El Salvador",
		"GQ":"Equatorial Guinea",
		"ER":"Eritrea",
		"EE":"Estonia",
		"ET":"Ethiopia",
		"FK":"Falkland Islands (Malvinas)",
		"FO":"Faroe Islands",
		"FJ":"Fiji",
		"FI":"Finland",
		"FR":"France",
		"GF":"French Guiana",
		"PF":"French Polynesia",
		"TF":"French Southern Territories",
		"GA":"Gabon",
		"GM":"Gambia",
		"GE":"Georgia",
		"DE":"Germany",
		"GH":"Ghana",
		"GI":"Gibraltar",
		"GR":"Greece",
		"GL":"Greenland",
		"GD":"Grenada",
		"GP":"Guadeloupe",
		"GU":"Guam",
		"GT":"Guatemala",
		"GG":"Guernsey",
		"GN":"Guinea",
		"GW":"Guinea-Bissau",
		"GY":"Guyana",
		"HT":"Haiti",
		"HM":"Heard Island And Mcdonald Islands",
		"VA":"Vatican City State (Holy See)",
		"HN":"Honduras",
		"HK":"Hong Kong",
		"HU":"Hungary",
		"IS":"Iceland",
		"IN":"India",
		"ID":"Indonesia",
		"IR":"Iran, Islamic Republic of",
		"IQ":"Iraq",
		"IE":"Ireland",
		"IM":"Isle Of Man",
		"IL":"Israel",
		"IT":"Italy",
		"JM":"Jamaica",
		"JP":"Japan",
		"JE":"Jersey",
		"JO":"Jordan",
		"KZ":"Kazakhstan",
		"KE":"Kenya",
		"KI":"Kiribati",
		"KP":"Korea, Democratic People's Republic of",
		"KR":"Korea, Republic of",
		"KW":"Kuwait",
		"KG":"Kyrgyzstan",
		"LA":"Lao People's Democratic Republic",
		"LV":"Latvia",
		"LB":"Lebanon",
		"LS":"Lesotho",
		"LR":"Liberia",
		"LY":"Libyan Arab Jamahiriya",
		"LI":"Liechtenstein",
		"LT":"Lithuania",
		"LU":"Luxembourg",
		"MO":"Macao",
		"MK":"Macedonia, The Former Yugoslav Republic of",
		"MG":"Madagascar",
		"MW":"Malawi",
		"MY":"Malaysia",
		"MV":"Maldives",
		"ML":"Mali",
		"MT":"Malta",
		"MH":"Marshall Islands",
		"MQ":"Martinique",
		"MR":"Mauritania",
		"MU":"Mauritius",
		"YT":"Mayotte",
		"MX":"Mexico",
		"FM":"Micronesia, Federated States of",
		"MD":"Moldova, Republic of",
		"MC":"Monaco",
		"MN":"Mongolia",
		"ME":"Montenegro",
		"MS":"Montserrat",
		"MA":"Morocco",
		"MZ":"Mozambique",
		"MM":"Myanmar",
		"NA":"Namibia",
		"NR":"Nauru",
		"NP":"Nepal",
		"NL":"Netherlands",
		"AN":"Netherlands Antilles",
		"NC":"New Caledonia",
		"NZ":"New Zealand",
		"NI":"Nicaragua",
		"NE":"Niger",
		"NG":"Nigeria",
		"NU":"Niue",
		"NF":"Norfolk Island",
		"MP":"Northern Mariana Islands",
		"NO":"Norway",
		"OM":"Oman",
		"PK":"Pakistan",
		"PW":"Palau",
		"PS":"Palestinian Territory, Occupied",
		"PA":"Panama",
		"PG":"Papua New Guinea",
		"PY":"Paraguay",
		"PE":"Peru",
		"PH":"Philippines",
		"PN":"Pitcairn",
		"PL":"Poland",
		"PT":"Portugal",
		"PR":"Puerto Rico",
		"QA":"Qatar",
		"RE":"Réunion",
		"RO":"Romania",
		"RU":"Russian Federation",
		"RW":"Rwanda",
		"BL":"Saint Barthélemy",
		"SH":"Saint Helena, Ascension and Tristan Da Cunha",
		"KN":"Saint Kitts And Nevis",
		"LC":"Saint Lucia",
		"MF":"Saint Martin",
		"PM":"Saint Pierre and Miquelon",
		"VC":"Saint Vincent and the Grenadines",
		"WS":"Samoa",
		"SM":"San Marino",
		"ST":"Sao Tome and Principe",
		"SA":"Saudi Arabia",
		"SN":"Senegal",
		"RS":"Serbia",
		"SC":"Seychelles",
		"SL":"Sierra Leone",
		"SG":"Singapore",
		"SK":"Slovakia",
		"SI":"Slovenia",
		"SB":"Solomon Islands",
		"SO":"Somalia",
		"ZA":"South Africa",
		"GS":"South Georgia and the South Sandwich Islands",
		"ES":"Spain",
		"LK":"Sri Lanka",
		"SD":"Sudan",
		"SR":"Suriname",
		"SJ":"Svalbard and Jan Mayen",
		"SZ":"Swaziland",
		"SE":"Sweden",
		"CH":"Switzerland",
		"SY":"Syrian Arab Republic",
		"TW":"Taiwan, Province of China",
		"TJ":"Tajikistan",
		"TZ":"Tanzania, United Republic of",
		"TH":"Thailand",
		"TL":"Timor-Leste",
		"TG":"Togo",
		"TK":"Tokelau",
		"TO":"Tonga",
		"TT":"Trinidad and Tobago",
		"TN":"Tunisia",
		"TR":"Turkey",
		"TM":"Turkmenistan",
		"TC":"Turks and Caicos Islands",
		"TV":"Tuvalu",
		"UG":"Uganda",
		"UA":"Ukraine",
		"AE":"United Arab Emirates",
		"GB":"United Kingdom",
		"US":"United States",
		"UM":"United States Minor Outlying Islands",
		"UY":"Uruguay",
		"UZ":"Uzbekistan",
		"VU":"Vanuatu",
		"VE":"Venezuela, Bolivarian Republic of",
		"VN":"Viet Nam",
		"VG":"Virgin Islands, British",
		"VI":"Virgin Islands, U.S.",
		"WF":"Wallis and Futuna",
		"EH":"Western Sahara",
		"YE":"Yemen",
		"ZM":"Zambia",
		"ZW":"Zimbabwe"
	]

	static final ISO3166_3 = [
		"afg":"Afghanistan",
		"alb":"Albania",
		"ata":"Antarctica",
		"dza":"Algeria",
		"asm":"American Samoa",
		"and":"Andorra",
		"ago":"Angola",
		"atg":"Antigua and Barbuda",
		"aze":"Azerbaijan",
		"arg":"Argentina",
		"aus":"Australia",
		"aut":"Austria",
		"bhs":"Bahamas",
		"bhr":"Bahrain",
		"bgd":"Bangladesh",
		"arm":"Armenia",
		"brb":"Barbados",
		"bel":"Belgium",
		"bmu":"Bermuda",
		"btn":"Bhutan",
		"bol":"Bolivia",
		"bih":"Bosnia and Herzegovina",
		"bwa":"Botswana",
		"bvt":"Bouvet Island",
		"bra":"Brazil",
		"blz":"Belize",
		"iot":"British Indian Ocean Territory",
		"slb":"Solomon Islands",
		"vgb":"British Virgin Islands",
		"brn":"Brunei Darussalam",
		"bgr":"Bulgaria",
		"mmr":"Myanmar",
		"bdi":"Burundi",
		"blr":"Belarus",
		"khm":"Cambodia",
		"cmr":"Cameroon",
		"can":"Canada",
		"cpv":"Cape Verde",
		"cym":"Cayman Islands",
		"caf":"Central African",
		"lka":"Sri Lanka",
		"tcd":"Chad",
		"chl":"Chile",
		"chn":"China",
		"twn":"Taiwan",
		"cxr":"Christmas Island",
		"cck":"Cocos (Keeling) Islands",
		"col":"Colombia",
		"com":"Comoros",
		"myt":"Mayotte",
		"cog":"Republic of the Congo",
		"cod":"The Democratic Republic Of The Congo",
		"cok":"Cook Islands",
		"cri":"Costa Rica",
		"hrv":"Croatia",
		"cub":"Cuba",
		"cyp":"Cyprus",
		"cze":"Czech Republic",
		"ben":"Benin",
		"dnk":"Denmark",
		"dma":"Dominica",
		"dom":"Dominican Republic",
		"ecu":"Ecuador",
		"slv":"El Salvador",
		"gnq":"Equatorial Guinea",
		"eth":"Ethiopia",
		"eri":"Eritrea",
		"est":"Estonia",
		"fro":"Faroe Islands",
		"flk":"Falkland Islands",
		"sgs":"South Georgia and the South Sandwich Islands",
		"fji":"Fiji",
		"fin":"Finland",
		"ala":"\u00C5land Islands",
		"fra":"France",
		"guf":"French Guiana",
		"pyf":"French Polynesia",
		"atf":"French Southern Territories",
		"dji":"Djibouti",
		"gab":"Gabon",
		"geo":"Georgia",
		"gmb":"Gambia",
		"pse":"Occupied Palestinian Territory",
		"deu":"Germany",
		"gha":"Ghana",
		"gib":"Gibraltar",
		"kir":"Kiribati",
		"grc":"Greece",
		"grl":"Greenland",
		"grd":"Grenada",
		"glp":"Guadeloupe",
		"gum":"Guam",
		"gtm":"Guatemala",
		"gin":"Guinea",
		"guy":"Guyana",
		"hti":"Haiti",
		"hmd":"Heard Island and McDonald Islands",
		"vat":"Vatican City State",
		"hnd":"Honduras",
		"hkg":"Hong Kong",
		"hun":"Hungary",
		"isl":"Iceland",
		"ind":"India",
		"idn":"Indonesia",
		"irn":"Islamic Republic of Iran",
		"irq":"Iraq",
		"irl":"Ireland",
		"isr":"Israel",
		"ita":"Italy",
		"civ":"C\u00F4te d'Ivoire",
		"jam":"Jamaica",
		"jpn":"Japan",
		"kaz":"Kazakhstan",
		"jor":"Jordan",
		"ken":"Kenya",
		"prk":"Democratic People's Republic of Korea",
		"kor":"Republic of Korea",
		"kwt":"Kuwait",
		"kgz":"Kyrgyzstan",
		"lao":"Lao People's Democratic Republic",
		"lbn":"Lebanon",
		"lso":"Lesotho",
		"lva":"Latvia",
		"lbr":"Liberia",
		"lby":"Libyan Arab Jamahiriya",
		"lie":"Liechtenstein",
		"ltu":"Lithuania",
		"lux":"Luxembourg",
		"mac":"Macao",
		"mdg":"Madagascar",
		"mwi":"Malawi",
		"mys":"Malaysia",
		"mdv":"Maldives",
		"mli":"Mali",
		"mlt":"Malta",
		"mtq":"Martinique",
		"mrt":"Mauritania",
		"mus":"Mauritius",
		"mex":"Mexico",
		"mco":"Monaco",
		"mng":"Mongolia",
		"mda":"Republic of Moldova",
		"msr":"Montserrat",
		"mar":"Morocco",
		"moz":"Mozambique",
		"omn":"Oman",
		"nam":"Namibia",
		"nru":"Nauru",
		"npl":"Nepal",
		"nld":"Netherlands",
		"ant":"Netherlands Antilles",
		"abw":"Aruba",
		"ncl":"New Caledonia",
		"vut":"Vanuatu",
		"nzl":"New Zealand",
		"nic":"Nicaragua",
		"ner":"Niger",
		"nga":"Nigeria",
		"niu":"Niue",
		"nfk":"Norfolk Island",
		"nor":"Norway",
		"mnp":"Northern Mariana Islands",
		"umi":"United States Minor Outlying Islands",
		"fsm":"Federated States of Micronesia",
		"mhl":"Marshall Islands",
		"plw":"Palau",
		"pak":"Pakistan",
		"pan":"Panama",
		"png":"Papua New Guinea",
		"pry":"Paraguay",
		"per":"Peru",
		"phl":"Philippines",
		"pcn":"Pitcairn",
		"pol":"Poland",
		"prt":"Portugal",
		"gnb":"Guinea-Bissau",
		"tls":"Timor-Leste",
		"pri":"Puerto Rico",
		"qat":"Qatar",
		"reu":"R\u00E9union",
		"rou":"Romania",
		"rus":"Russian Federation",
		"rwa":"Rwanda",
		"shn":"Saint Helena",
		"kna":"Saint Kitts and Nevis",
		"aia":"Anguilla",
		"lca":"Saint Lucia",
		"spm":"Saint-Pierre and Miquelon",
		"vct":"Saint Vincent and the Grenadines",
		"smr":"San Marino",
		"stp":"Sao Tome and Principe",
		"sau":"Saudi Arabia",
		"sen":"Senegal",
		"syc":"Seychelles",
		"sle":"Sierra Leone",
		"sgp":"Singapore",
		"svk":"Slovakia",
		"vnm":"Vietnam",
		"svn":"Slovenia",
		"som":"Somalia",
		"zaf":"South Africa",
		"zwe":"Zimbabwe",
		"esp":"Spain",
		"esh":"Western Sahara",
		"sdn":"Sudan",
		"sur":"Suriname",
		"sjm":"Svalbard and Jan Mayen",
		"swz":"Swaziland",
		"swe":"Sweden",
		"che":"Switzerland",
		"syr":"Syrian Arab Republic",
		"tjk":"Tajikistan",
		"tha":"Thailand",
		"tgo":"Togo",
		"tkl":"Tokelau",
		"ton":"Tonga",
		"tto":"Trinidad and Tobago",
		"are":"United Arab Emirates",
		"tun":"Tunisia",
		"tur":"Turkey",
		"tkm":"Turkmenistan",
		"tca":"Turks and Caicos Islands",
		"tuv":"Tuvalu",
		"uga":"Uganda",
		"ukr":"Ukraine",
		"mkd":"The Former Yugoslav Republic of Macedonia",
		"egy":"Egypt",
		"gbr":"United Kingdom",
		"imn":"Isle of Man",
		"tza":"United Republic Of Tanzania",
		"usa":"United States",
		"vir":"U.S. Virgin Islands",
		"bfa":"Burkina Faso",
		"ury":"Uruguay",
		"uzb":"Uzbekistan",
		"ven":"Venezuela",
		"wlf":"Wallis and Futuna",
		"wsm":"Samoa",
		"yem":"Yemen",
		"scg":"Serbia and Montenegro",
		"zmb":"Zambia"
	]

	// This needs to change, to sort on demand using the BROWSER's locale
	static final COUNTRY_CODES_3_BY_NAME_ORDER =
		ISO3166_3.entrySet().sort( { a, b -> a.value.compareTo(b.value) } ).collect() { it.key }
	static final COUNTRY_CODES_3_BY_NAME = new TreeMap()
	
	static final COUNTRY_CODES_2_BY_NAME_ORDER =
		ISO3166_2.entrySet().sort( { a, b -> a.value.compareTo(b.value) } ).collect() { it.key }
	static final COUNTRY_CODES_2_BY_NAME = new TreeMap()


	static {
		for(kvEntry in ISO3166_3)
		{
    	    COUNTRY_CODES_3_BY_NAME[kvEntry.value] = kvEntry.key
    	}
		for(kvEntry in ISO3166_2)
		{
    	    COUNTRY_CODES_2_BY_NAME[kvEntry.value] = kvEntry.key
    	}
	}

	/**
	 * Display a country selection combo box.
     * Attributes:
     * from - list of country codes or none for full list. Order is honoured
     * valueMessagePrefix - code prefix to use, if you want names of countries to come from message bundle
     * value - currently selected country code - ISO3166_3 (3 character, lowercase) form
     * default - currently selected country code - if value is null
     * codeSize - 2 or 3
     */
	static void g_countrySelect(script, attrs){script.out << script.countrySelect(attrs)}
	static String countrySelect(script, attrs)
	{
		def nameCodeMap = COUNTRY_CODES_2_BY_NAME_ORDER
		def codeMap = ISO3166_2
		if(attrs['codeSize'] == 3 || attrs['codeSize'] == "3")
		{
			nameCodeMap = COUNTRY_CODES_3_BY_NAME_ORDER
			codeMap = ISO3166_3
		}
        if (!attrs['from']) {
            attrs['from'] = nameCodeMap
        }
        def valuePrefix = attrs.remove('valueMessagePrefix')
		attrs['optionValue'] = { valuePrefix ? "${valuePrefix}.${it}" : codeMap[it] }
        if (!attrs['value']) {
            attrs['value'] = attrs.remove('default')
        }

        return script.select(attrs)
	}

    /**
     * Take a country code and output the country name, from the internal data
     * Note: to use message bundle to resolve name, use g_message tag
     */
	static void g_countryName(script, attrs){script.out << script.countryName(attrs)}
    static String countryName(script, attrs)
	{
    	if (!attrs.code) throw new TaglykException("[countryName] requires [code] attribute to specify the country code")
    	
		if(attrs['codeSize'] == 3 || attrs['codeSize'] == "3")
		{
			return ISO3166_3[attrs.code]
		}
		else
		{
			return ISO3166_2[attrs.code]
		}
    }
    
    
	static void g_formatCountry(script, attrs){script.out << script.formatCountry(attrs)}
    static formatCountry(script, attrs)
	{
    	if(attrs.code && !attrs.country){attrs.country = attrs.code}
		//if(!attrs.country) throw new TaglykException("[formatCountry] requires [country] attribute to specify the country code")
    	if(!attrs.country){return ""}
		def countries = attrs.country
		if(!ObjectUtils.isListOrArray(countries))
		{
			countries = [countries]
		}
		
		countries = countries.collect{script.message(code:"country.${it}", default:script.countryName(code:it))}
		
    	return script.formatList(countries, attrs.listStart, attrs.listJoin, attrs.listEnd)
    }
}