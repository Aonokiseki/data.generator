package priv.azure.miracle.data.generator.pojo;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Record {
	private UUID uuid;
	private List<Column> columns;
	
	public Record() {
		uuid = UUID.randomUUID();
		columns = new LinkedList<Column>();
	}
	
	public String getUid() {
		return uuid.toString();
	}
	public boolean addColumn(Column column) {
		return columns.add(column);
	}
	public boolean addColumn(String name, String value) {
		Column column = new Column(name, value);
		return columns.add(column);
	}
	public Column getColumn(int index) {
		return columns.get(index);
	}
	public List<Column> getColumns(){
		return Collections.unmodifiableList(columns);
	}
	public Column getColumn(String name) {
		if(name == null || name.trim().isEmpty())
			return null;
		for(int i=0,size=columns.size(); i<size; i++)
			if(name.equals(columns.get(i).getName()))
				return columns.get(i);
		return null;
	}
	@Override
	public int hashCode() {
		return Objects.hash(uuid);
	}
	@Override
	public boolean equals(Object object) {
		if(this == object)
			return true;
		if(!(object instanceof Record))
			return false;
		Record record = (Record)object;
		return record.uuid.equals(this.uuid);
	}
	
	@Override
	public String toString() {
		return "Record [uuid=" + uuid + ", columns=" + columns + "]";
	}
}
