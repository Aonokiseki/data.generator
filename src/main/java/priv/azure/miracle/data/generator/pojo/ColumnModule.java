package priv.azure.miracle.data.generator.pojo;

import java.util.Map;
import java.util.Objects;

public class ColumnModule {
	private String name;
	private ColumnType type;
	private Map<String,String> properties;
	
	public ColumnModule() {}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ColumnType getType() {
		return type;
	}
	public void setType(ColumnType type) {
		this.type = type;
	}
	public Map<String, String> getProperties() {
		return properties;
	}
	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}
	@Override
	public int hashCode() {
		return Objects.hash(name);
	}
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if(!(o instanceof ColumnModule))
			return false;
		ColumnModule columnModule = (ColumnModule) o;
		return columnModule.name.equals(this.name);
	}
	@Override
	public String toString() {
		return "ColumnModule [name=" + name + ", type=" + type + ", properties=" + properties + "]";
	}
}
