@artifact.package@import groovyx.gaelyk.graelyk.domain.*
import com.googlecode.objectify.Key
import com.googlecode.objectify.annotation.*
import groovyx.gaelyk.graelyk.annotation.*
import com.google.appengine.api.datastore.*
import com.google.appengine.api.users.User
import groovyx.gaelyk.GaelykCategory

class @artifact.name@ extends Graelyk@artifact.idType@IdDomainClass implements Serializable
{

	@artifact.name@(){super(null)}
	@artifact.name@(Object callingScript){super(callingScript)}
	
	//Todo: Add domain properties here
	
	//Todo: Add constraints
	static constraints = {
		use(GaelykCategory)
		{
			
		}
	}
	
	//Todo: Add custom String->Object conversion closures
	//e.g. myPropertyName:{valueToTransform-> return valueToTransform}
	static transformOnReceive = [	                             
	:]
	                     	
	//Todo: Add custom Object->String conversion closures
	//e.g. myPropertyName:{valueToTransform-> return valueToTransform}
	static transformOnDisplay = [
	:]
	
	//Todo: You can override the preSave, postSave, preValidate, and postValidate methods. Otherwise you can simply delete the commented code below.
	/*
	 * @Override
	 */
	/*
	public boolean preSave()
	{
		//Override this method to perform operations before a save.
		//Return true to continue with the save, false to cancel the save.
		return true
	}
	*/
	
	/*
	 * @Override
	 */
	/*
	public Key postSave(Key savedKey)
	{
		//Override this method to perform operations after a save.
		//Normally you should return the Key of the saved object if it still exists.
		return savedKey
	}
	*/
	
	/*
	 * @Override
	 */
	/*
	public boolean preValidate()
	{
		//Override this method to perform operations before validation.
		//Return true to continue with the validation, false to cancel the validation & return invalid.
		return true
	}
	*/
	
	/*
	 * @Override
	 */
	/*
	public boolean postValidate(boolean valid)
	{
		//Override this method to perform operations after validation.
		//Your overridden method may change the validation status that was passed in.
		return valid
	}
	*/
}