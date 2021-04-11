package priv.azure.miracle.data.generator.pojo;

import java.util.UUID;

import priv.azure.miracle.utility.ChronosOperator;
import priv.azure.miracle.utility.StringOperator;

public class SimpleObjectColumn implements IObjectColumn{
	private String _char;
	private String _number;
	private String _datetime;
	private String _name;
	private String _ip;
	
	public SimpleObjectColumn() {}
	
	@Override
	public String toString() {
		return "SimpleObjectColumn [_char=" + _char + ", _number=" + _number + ", _datetime=" + _datetime + ", _name="
				+ _name + ", _ip=" + _ip + "]";
	}

	@Override
	public IObjectColumn next(Setting setting) {
		RecordModule recordModule = setting.getRecordModule();
		GlobalVariables global = recordModule.getGlobalVariables();
		_char = UUID.randomUUID().toString();
		_number = String.valueOf((int)(Math.random() * (global.getMaxNumber() - global.getMinNumber()) + global.getMinNumber()));
		_datetime = ChronosOperator.getARandomLocalDateTime(global.getDateTimeStart(), global.getDateTimeEnd()).format(Constants.DEFAULT_DATE_TIME_FORMATTER);
		_name = StringOperator.getAName();
		_ip = StringOperator.getARandomIp();
		return this;
	}
}
