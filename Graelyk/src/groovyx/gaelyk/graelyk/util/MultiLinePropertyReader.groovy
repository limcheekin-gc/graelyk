package groovyx.gaelyk.graelyk.util

import java.io.*

class MultiLinePropertyReader extends HashMap
{
	File file
	String inputEncoding
	
	public MultiLinePropertyReader(String path, String encoding)
	{
		this(new File(path), encoding)
	}
	
	public MultiLinePropertyReader(File f, String encoding)
	{
		file = f
		inputEncoding = encoding
		loadProperties()
	}
	
	public void reload()
	{
		loadProperties()
	}
	
	public void loadProperties()
	{
		this.clear()
		if(file.exists())
		{
			try
			{
				FileInputStream fis = new FileInputStream(file)
				InputStreamReader isr = new InputStreamReader(fis, inputEncoding)
				BufferedReader inReader = new BufferedReader(isr)
				
				boolean firstLine = true
				boolean multiLine = false
				String multiLineKey = ""
				String line = ""
				
				//Read data in from file
				while(inReader.ready())
				{
					line = inReader.readLine()

					//ignore UTF-8 byte-order mark on first line if it is present
					if(firstLine && inputEncoding.equals("UTF-8") && line.indexOf("\ufeff") == 0)
					{
						line = line.substring(1, line.length())
					}
					
					//Convert special escapes like \u1234, \t, \r, \n
					line = loadConvert(line)
					
					//ignore comments. Comments specified by # or ! as first character (when not already processing a multi-line property)
					if(!multiLine && (line.indexOf("#") == 0 || line.indexOf("!") == 0))
					{
						continue
					}
					
					if(multiLine)
					{
						if(line.indexOf("\"\"\"") >= 0)
						{
							//if line contains """, truncate before """, add that to the current multiLineKey's entry, and terminate the multiLine
							int quotes = line.indexOf("\"\"\"")
							line = line.substring(0, quotes)
							put(multiLineKey, get(multiLineKey) + line)
							multiLine = false
							multiLineKey = ""
						}
						else
						{
							//add the current line to the current mutliLineKey's entry, and continue
							put(multiLineKey, get(multiLineKey) + line + "\n")
						}
					}
					else
					{
						if(line.trim().equals(""))
						{
							continue
						}
						int equal = line.indexOf("=")
						if(equal < 0)
						{
							//no equal sign. treat it as a key with a value of ""
							put(line.trim(), "")
						}
						else
						{
							String key = line.substring(0, equal)
							String value = line.substring(equal+1, line.length())
							if(value.matches("^\\s*\"\"\".*"))
							{
								//start of multi-line string
								int quotes = value.indexOf("\"\"\"")
								value = value.substring(quotes+3, value.length())
								quotes = value.indexOf("\"\"\"")
								if(quotes >= 0)
								{
									//multi-line string ends on same line
									value = value.substring(0, quotes)
									put(key, value)
								}
								else
								{
									//multi-line string must end on some other line
									multiLine = true
									multiLineKey = key
									put(key, value + "\n")
								}
							}
							else
							{
								//single-line property
								put(key, value)
							}
						}
					}
					
					firstLine = false
				}
				inReader.close()
				isr.close()
				fis.close()
			}
			catch(Exception e){System.out.println(e.toString())}
		}
	}
	
     /*
      * Converts encoded &#92;uxxxx to unicode chars
      * and changes special saved chars to their original forms
	  * This method was pulled out of the source for java.util.Properties.
      */
     String loadConvert (String text) 
	 {
		char[] input = text.getChars()
		int off = 0
		int len = text.length()
		char[] convtBuf = new char[text.size()]
	 
         if (convtBuf.length < len) {
             int newLen = len * 2;
             if (newLen < 0) {
                 newLen = Integer.MAX_VALUE;
             }
             convtBuf = new char[newLen];
         }
         char aChar;
         char[] out = convtBuf;
         int outLen = 0;
         int end = off + len;
         while (off < end) {
             aChar = input[off++];
             if (aChar == '\\') {
                 aChar = input[off++];
                 if(aChar == 'u') {
                     // Read the xxxx
                     int value=0;
                     for (int i=0; i<4; i++) {
                         aChar = input[off++];
                         switch (aChar) {
                           case '0': case '1': case '2': case '3': case '4':
                           case '5': case '6': case '7': case '8': case '9':
                              value = (value << 4) + aChar - ('0' as char);
                              break;
                           case 'a': case 'b': case 'c':
                           case 'd': case 'e': case 'f':
                              value = (value << 4) + 10 + aChar - ('a' as char);
                              break;
                           case 'A': case 'B': case 'C':
                           case 'D': case 'E': case 'F':
                              value = (value << 4) + 10 + aChar - ('A' as char);
                              break;
                           default:
                               throw new IllegalArgumentException(
                                            "Malformed \\uxxxx encoding.");
                         }
                      }
                     out[outLen++] = (char)value;
                 } else {
                     if (aChar == 't') aChar = '\t';
                     else if (aChar == 'r') aChar = '\r';
                     else if (aChar == 'n') aChar = '\n';
                     else if (aChar == 'f') aChar = '\f';
                     out[outLen++] = aChar;
                 }
             } else {
                 out[outLen++] = aChar;
             }
         }
         return new String (out, 0, outLen);
     }
}
