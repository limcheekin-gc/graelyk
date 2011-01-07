package groovyx.gaelyk.graelyk.cast

class CastingSignature
{
	Class from
	Class to
	Class generic
	
	CastingSignature(Class fromClass, Class toClass)
	{
		from = fromClass
		to = toClass
		generic = null
	}
	
	CastingSignature(Class fromClass, Class toClass, Class genericClass)
	{
		from = fromClass
		to = toClass
		generic = genericClass
	}
	
	public boolean equals(Object o)
	{
		if(o == null){return false}
		if(o instanceof CastingSignature)
		{
			if(this.from == o.from && this.to == o.to && this.generic == o.generic)
			{
				return true
			}
		}
		return false
	}
	
	public String toString()
	{
		return "[" + to + " -> " + from + (generic?"<" + generic + ">":"") + "]"
	}
}
