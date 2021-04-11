package priv.azure.miracle.formatter;

import java.util.List;

import priv.azure.miracle.data.generator.pojo.Column;
import priv.azure.miracle.data.generator.pojo.Record;
import priv.azure.miracle.data.generator.pojo.Setting;

public class JsonFormatter implements IRecordFormatter{

	private String lineSeparator;
	
	public JsonFormatter(Setting setting) {
		lineSeparator = System.lineSeparator();
	}
	
	@Override
	public String handle(Record record, boolean isLast) {
		StringBuilder jsonBuilder = new StringBuilder();
		jsonBuilder.append("{").append(lineSeparator);
		List<Column> columns = record.getColumns();
		Column column = null;
		String value = "";
		for(int i=0, size=columns.size(); i<size; i++) {
			column = columns.get(i);
			value = column.getValue().replaceAll("\\\"", "\\\\\"");
			jsonBuilder.append("    \"").append(column.getName()).append("\" : \"").append(value).append("\"");
			if(i < size - 1)
				jsonBuilder.append(",");
			jsonBuilder.append(lineSeparator);
		}
		jsonBuilder.append("}");
		if(!isLast)
			jsonBuilder.append(",");
		jsonBuilder.append(lineSeparator);
		return jsonBuilder.toString();
	}

	@Override
	public String start() {
		return "{" + lineSeparator;
	}

	@Override
	public String end() {
		return "}";
	}

	@Override
	public String suffix() {
		return ".json";
	}

}
