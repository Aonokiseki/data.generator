package priv.azure.miracle.formatter;

import priv.azure.miracle.data.generator.pojo.Record;

public interface IRecordFormatter {
	String handle(Record record, boolean isLast);
	String start();
	String end();
	String suffix();
}
