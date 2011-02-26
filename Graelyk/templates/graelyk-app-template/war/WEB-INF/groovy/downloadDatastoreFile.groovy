import org.kokelaka.image.*
import groovyx.gaelyk.obgaektify.ObgaektifyCategory
import groovyx.gaelyk.graelyk.util.FileHolder
import com.google.appengine.api.datastore.Blob

def key = params.key
def field = params.field
def index = params.index
def contentType = params.contentType //optional
def filename = params.filename //optional

def object = ObgaektifyCategory.fetch(key)
if(object)
{
	def fileObjects = object[field]
	if(!(fileObjects instanceof List || fileObjects instanceof Object[]))
	{
		fileObjects = [fileObjects]
	}
	
	if(index == null)
	{
		index = 0
	}
	index = index as int
	
	fileObject = fileObjects[index]
	if(fileObject instanceof Blob)
	{
		sout << fileObject.getBytes()
	}
	else if(fileObject instanceof FileHolder)
	{
		if(filename)
		{
			response.setHeader("Content-Disposition","inline; filename=\"${filename}\"")
		}
		else if(fileObject.filename)
		{
			response.setHeader("Content-Disposition","inline; filename=\"${fileObject.filename}\"")
		}
		
		if(contentType)
		{
			response.setContentType(contentType)
		}
		else if(fileObject.contentType)
		{
			response.setContentType(fileObject.contentType)
		}
		else
		{
			response.setContentType("application/octet-stream")
		}
		sout << fileObject.blob.getBytes()
	}
}

//If you had a single Blob named file in your domain class, you could link to this page like this from a template file:
//<img src="/downloadDatastoreFile.groovy?key=${myObgaektifiableInstance.getKeyString()}&field=file" />
//If you had a Blob[] named files in your domain class, you could link to this page like this from a template file to get the 4th file in the array:
//<img src="/downloadDatastoreFile.groovy?key=${myObgaektifiableInstance.getKeyString()}&field=files&index=3" />