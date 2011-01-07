package groovyx.gaelyk.graelyk.util

import com.google.appengine.api.datastore.Blob

class FileHolder implements Serializable
{
	Blob file
	String filename = ""
	String contentType = ""
	long size = 0
	
	FileHolder()
	{
		this(null, "", "", 0)
	}

	FileHolder(Blob file)
	{
		this.file = file
		this.filename = ""
		this.contentType = ""
		this.size = file.getBytes().size()
	}
	
	FileHolder(Blob file, String filename, String contentType, long size)
	{
		this.file = file
		this.filename = filename
		this.contentType = contentType
		this.size = size
	}
	
	Blob getBlob(){return file}
	void setBlob(Blob blob){this.file = blob}
}
