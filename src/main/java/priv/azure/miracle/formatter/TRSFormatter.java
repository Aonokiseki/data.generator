package priv.azure.miracle.formatter;

import java.util.List;

import priv.azure.miracle.data.generator.pojo.Column;
import priv.azure.miracle.data.generator.pojo.Record;

public class TRSFormatter implements IRecordFormatter{
	
	private String linePrefix;
	private String lineSeparator;
	
	public TRSFormatter(priv.azure.miracle.data.generator.pojo.Setting setting) {
		linePrefix = setting.getRecordModule().getGlobalVariables().getLinePrefix();
		if(linePrefix == null) linePrefix = "";
		lineSeparator = System.lineSeparator();
	}
	@Override
	public String start() { return null; }
	@Override
	public String end() { return null; }
	@Override
	public String suffix() { return ".trs"; }
	
	@Override
	public String handle(Record record, boolean isLast) {
		StringBuilder recordBuilder = new StringBuilder();
		recordBuilder.append(linePrefix).append("<REC>").append(lineSeparator);
		List<Column> columns = record.getColumns();
		Column column = null;
		for(int i=0,size=columns.size(); i<size; i++) {
			column = columns.get(i);
			recordBuilder.append(linePrefix).append("<").append(column.getName()).append(">=")
						 .append(column.getValue().trim()).append(System.lineSeparator());
		}
		recordBuilder.append(lineSeparator);
		return recordBuilder.toString();
	}
}
