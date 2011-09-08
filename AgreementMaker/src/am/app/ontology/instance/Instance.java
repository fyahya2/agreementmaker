package am.app.ontology.instance;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

public class Instance {
	protected String uri;
	protected String type;
	
	protected Hashtable<String,List<String>> properties;
	
	public Instance(String uri, String type) {
		this.uri = uri;
		properties = new Hashtable<String, List<String>>();
	}

	public String getUri() {
		return uri;
	}

	public Enumeration<String>  listProperties() {
		return properties.keys();
	}
	
	public List<String> getProperty( String key ) {
		return properties.get(key);
	}
	
	public String getSingleValuedProperty( String key ) {
		List<String> strings = properties.get(key);
		if(strings == null) return null;
		if(strings.size() != 1) return null;
		return strings.get(0);
	}
	
	/** Passing a null value will remove the key from the properties table. */
	public void setProperty( String key, List<String> value ) {
		if( value == null ) {
			properties.remove(key);
		} else {
			properties.put(key,value);
		}
	}
	
	public void setProperty( String key, String value ) {
		if( value == null ) {
			properties.remove(key);
		} else {
			List<String> values = properties.get(key);
			if(values == null){
				List<String> list = new ArrayList<String>();
				list.add(value);
				properties.put(key,list);
			}
			else {
				values.add(value);
			}
		}
	}

	public String getType() {
		return type;
	}
	
	@Override
	public String toString() {
		return uri + " " + properties;
	}
}
