package priv.azure.miracle.data.generator.pojo;

public enum TextValueType {
	EMPTY("empty"),
	ID_CARD_NUMBER("idcardnumber"),
	IP("ip"),
	LIBRARY("library"), 
	NAME("name"), 
	SUMMARY("summary"),
	UID("uid");
	
	private String name;
	private TextValueType(String name) {
		this.name = name;
	}
	@Override
	public String toString() {
		return this.name;
	}
	
	public static TextValueType transfer(String textValueTypeStr) {
		if(textValueTypeStr == null || textValueTypeStr.isEmpty())
			return TextValueType.EMPTY;
		textValueTypeStr = textValueTypeStr.trim().toLowerCase();
		if("idcardnumber".equals(textValueTypeStr))
			return TextValueType.ID_CARD_NUMBER;
		if("name".equals(textValueTypeStr))
			return TextValueType.NAME;
		if("ip".equals(textValueTypeStr))
			return TextValueType.IP;
		if("library".equals(textValueTypeStr))
			return TextValueType.LIBRARY;
		if("uid".equals(textValueTypeStr))
			return TextValueType.UID;
		if("uuid".equals(textValueTypeStr))
			return TextValueType.UID;
		if("summary".equals(textValueTypeStr))
			return TextValueType.SUMMARY;
		return TextValueType.EMPTY;
	}
}
