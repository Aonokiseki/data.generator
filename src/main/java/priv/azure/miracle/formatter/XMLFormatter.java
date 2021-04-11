package priv.azure.miracle.formatter;

import java.util.List;

import priv.azure.miracle.data.generator.pojo.Column;
import priv.azure.miracle.data.generator.pojo.Record;
import priv.azure.miracle.data.generator.pojo.Setting;

public class XMLFormatter implements IRecordFormatter{
	
	private String linePrefix;
	private String lineSeparator;
	
	public XMLFormatter(Setting setting) {
		linePrefix = setting.getRecordModule().getGlobalVariables().getLinePrefix();
		if(linePrefix == null) linePrefix = "";
		lineSeparator = System.lineSeparator();
	}
	@Override
	public String start() {
		return linePrefix + "<TRS>" + lineSeparator;
	}
	@Override
	public String end() {
		return linePrefix + "</TRS>" + lineSeparator;
	}
	@Override
	public String suffix() {
		return ".xml";
	}
	@Override
	public String handle(Record record, boolean isLast) {
		StringBuilder recordBuilder = new StringBuilder();
		recordBuilder.append(linePrefix).append("<REC>").append(lineSeparator);
		List<Column> columns = record.getColumns();
		Column column = null;
		String columnName = null;
		String columnValue = null;
		for(int i=0,size=columns.size(); i<size; i++) {
			column = columns.get(i);
			columnName = column.getName();
			columnValue = column.getValue();
			recordBuilder.append(linePrefix)
						 .append("<").append(columnName).append(">")
						 .append(columnValue)
						 .append("</").append(columnName).append(">")
						 .append(System.lineSeparator());
		}
		recordBuilder.append(linePrefix).append("</REC>").append(lineSeparator);
		return recordBuilder.toString();
	}
}
