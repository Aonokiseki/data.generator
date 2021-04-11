package priv.azure.miracle.data.generator.pojo;

import java.util.Set;

public class RecordModule {
	private GlobalVariables globalVariables;
	private Set<ColumnModule> columns;
	
	public RecordModule() {}

	public GlobalVariables getGlobalVariables() {
		return globalVariables;
	}
	public void setGlobalVariables(GlobalVariables globalVariables) {
		this.globalVariables = globalVariables;
	}
	public Set<ColumnModule> getColumns() {
		return columns;
	}
	public void setColumns(Set<ColumnModule> columns) {
		this.columns = columns;
	}
	@Override
	public String toString() {
		return "RecordModule [globalVariables=" + globalVariables + ", columns=" + columns + "]";
	}
}
