package priv.azure.miracle.formatter;

import java.util.List;
import java.util.Set;

import priv.azure.miracle.data.generator.pojo.Column;
import priv.azure.miracle.data.generator.pojo.ColumnModule;
import priv.azure.miracle.data.generator.pojo.Record;
import priv.azure.miracle.data.generator.pojo.Setting;

public class SQLFormatter implements IRecordFormatter{
	
	private String lineSeparator;
	private Setting setting;
	
	public SQLFormatter(Setting setting) {
		this.setting = setting;
		lineSeparator = System.lineSeparator();
	}

	@Override
	public String handle(Record record, boolean isLast) {
		StringBuilder sqlBuilder = new StringBuilder();
		List<Column> columns = record.getColumns();
		sqlBuilder.append("(");
		for(int i=0, size=columns.size(); i<size; i++) {
			sqlBuilder.append("'").append(columns.get(i).getValue()).append("'");
			if(i < size - 1)
				sqlBuilder.append(", ");
		}
		sqlBuilder.append(")");
		if(!isLast)
			sqlBuilder.append(",");
		sqlBuilder.append(lineSeparator);
		return sqlBuilder.toString();
	}

	@Override
	public String start() {
		String tableName = setting.getTableName();
		Set<ColumnModule> columnsModule = setting.getRecordModule().getColumns();
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("INSERT INTO ").append(tableName).append(" (");
		int i = 0, size = columnsModule.size();
		for(ColumnModule columnModule : columnsModule) {
			sqlBuilder.append("\"").append(columnModule.getName()).append("\"");
			if(i < size - 1)
				sqlBuilder.append(", ");
			i++;
		}
		sqlBuilder.append(")").append(lineSeparator).append("VALUES").append(lineSeparator);
		return sqlBuilder.toString();
	}

	@Override
	public String end() {
		return ";";
	}

	@Override
	public String suffix() {
		return ".sql";
	}
}
