package priv.azure.miracle.data.generator.pojo;

public enum ColumnType {
	CHAR("char"), NUMBER("number"), DATETIME("datetime"), FULLTEXT("fulltext"), BINARY("binary"), OBJECT("object");
	
	private String name;
	private ColumnType(String name) {
		this.name = name;
	}
	@Override
	public String toString() {
		return this.name;
	}
	
	public static ColumnType parse(String name) {
		name = name.toLowerCase().trim();
		switch(name) {
			case "char" : return CHAR;
			case "number" : return NUMBER;
			case "datetime" : return DATETIME;
			case "fulltext" : return FULLTEXT;
			case "binary" : return BINARY;
			case "object" : return OBJECT;
			default : return CHAR;
		}
	}
}
