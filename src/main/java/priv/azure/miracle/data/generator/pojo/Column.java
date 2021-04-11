package priv.azure.miracle.data.generator.pojo;

public class Column {
	private String name;
	private String value;
	
	public Column() {}
	public Column(String name, String value) {
		this.name = name;
		this.value = value;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	@Override
	public String toString() {
		return "Column [name=" + name + ", value=" + value + "]";
	}
}
