package groovyx.gaelyk.graelyk.util

import java.io.ByteArrayOutputStream;
import javax.servlet.http.HttpServletRequest
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams
import org.apache.commons.fileupload.servlet.ServletRequestContext
import com.google.appengine.api.datastore.Blob

class FileUpload
{
	static String characterEncoding = "UTF-8" //You can change this if you need to.
	
	static Map getFiles(HttpServletRequest request, Map params, Closure acceptFile=null)
	{
		return FileUpload.getFilesAndFields(request, params, true, false, acceptFile)
	}
	
	static Map getFields(HttpServletRequest request, Map params)
	{
		return FileUpload.getFilesAndFields(request, params, false, true)
	}
	
	static Map getFilesAndFields(HttpServletRequest request, Map params, Closure acceptFile=null)
	{
		return getFilesAndFields(request, params, true, true, acceptFile)
	}
	
	//Return a Map containing all file uploads and form fields.
	//Multiple values will be returned
	//If a file is loaded, the file will be stored in a FileHolder, along with the filename, content type, and size, if known.
	//(FileHolder will be automatically cast to a Blob if the field type in the GraelykDomainClass is a Blob.)
	//
	//This method accepts a Closure that can look at the byte array, filename, content type, and size,
	//and return true to accept the file or false to reject the file. If the file is rejected it will
	//not be saved to the params map. If the closure is null, the file will be automatically accepted.
	//e.g. {paramsMap, byteArray, fileName, contentType, size-> return true}
	static Map getFilesAndFields(HttpServletRequest request, Map params, boolean getFiles, boolean getFields, Closure acceptFile=null, String charEnc = FileUpload.characterEncoding)
	{
		//If this is not a multipart form, return null
		if(!ServletFileUpload.isMultipartContent(request))
		{
			return params
		}
		
		//Gaelyk puts form post data and query string data in the params variable
		//but getFilesAndFields only detects form post data. In order to preserve
		//any query string variables that may be passed in the params map, while 
		//avoiding duplicating form post data, we keep track of all the keys that
		//are found, and the first time a key is found, we delete it from params
		List foundKeys = [] 
		
		//Handle file uploads:
		ServletFileUpload upload = new ServletFileUpload();
		FileItemIterator iterator = upload.getItemIterator(request)
		while(iterator.hasNext())
		{
			FileItemStream item = iterator.next()
			InputStream stream = item.openStream()
			def name = item.getFieldName()
			
			//This is where we delete existing data from params the first time we find a key.
			//Subsequent occurrences of the key won't delete data first, they'll keep adding it to the list of data.
			if(!(foundKeys.contains(name)))
			{
				foundKeys << name
				params.remove(name)
			}

			if(getFields && item.isFormField())
			{
				def value = Streams.asString(stream, charEnc)
				FileUpload.addToMap(params, name, value)
			}
			else if(getFiles)
			{
				ByteArrayOutputStream baos = new ByteArrayOutputStream()
				Streams.copy(stream, baos, true)
				
				def byteArray = baos.toByteArray()
				def fileSize = byteArray.size()
				def contentType = item.getContentType()
				def fileName = item.getName()
				if(fileName != null)
				{
					//Some browsers return a full path and not just a file name. Get rid of the path
					//by keeping everything in the name after the last separator / or \
					fileName = fileName.substring(Math.max(fileName.lastIndexOf("/"), fileName.lastIndexOf("\\")) + 1)
				}
				
				if(acceptFile == null || (acceptFile != null && acceptFile.call(params, byteArray, fileName, contentType, fileSize)))
				{
					FileUpload.addToMap(params, name, new FileHolder(new Blob(byteArray), fileName, contentType, fileSize))
				}
			}
		}
		FileUpload.convertLists(params)
		return params
	}
	
	//Add a value to the params map.
	//If a value already exists for a given field name, start storing the values in a list.
	static void addToMap(Map params, String name, Object value)
	{
		if(params.containsKey(name))
		{
			if(params[name] instanceof List)
			{
				params[name] << value
			}
			else
			{
				params[name] = [params[name], value]
			}
		}
		else
		{
			params[name] = value
		}
	}
	
	//Convert all Lists in the Map to Object[] since
	//multi-valued fields are returned as arrays in Groovlets (see groovy.servlet.ServletBinding)
	static void convertLists(Map params)
	{
		//params.each{key, value->
		for(kvEntry in params)
		{
			def key = kvEntry.key
			def value = kvEntry.value
			if(value instanceof List)
			{
				params[key] = value as Object[]
			}
		}
	}
}
