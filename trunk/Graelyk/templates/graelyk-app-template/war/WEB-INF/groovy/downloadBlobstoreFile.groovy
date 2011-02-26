import com.google.appengine.api.blobstore.BlobKey
import com.google.appengine.api.blobstore.BlobInfo
BlobKey blob = new BlobKey(params.key)
BlobInfo blobinfo = blob.info
response.setContentType(blobinfo.contentType)
response.setHeader("Content-Disposition","inline; filename=\"${blobinfo.filename}\"")
blob.serve(response)
