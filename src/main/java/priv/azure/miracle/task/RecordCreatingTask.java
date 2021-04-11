package priv.azure.miracle.task;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import priv.azure.miracle.bean.management.Shop;
import priv.azure.miracle.bean.management.Status;
import priv.azure.miracle.bean.management.Summary;
import priv.azure.miracle.bean.management.Summary.Method;
import priv.azure.miracle.data.generator.pojo.Column;
import priv.azure.miracle.data.generator.pojo.ColumnModule;
import priv.azure.miracle.data.generator.pojo.ColumnType;
import priv.azure.miracle.data.generator.pojo.Constants;
import priv.azure.miracle.data.generator.pojo.Constants.Key;
import priv.azure.miracle.data.generator.pojo.GlobalVariables;
import priv.azure.miracle.data.generator.pojo.IObjectColumn;
import priv.azure.miracle.data.generator.pojo.Photo;
import priv.azure.miracle.data.generator.pojo.Record;
import priv.azure.miracle.data.generator.pojo.RecordModule;
import priv.azure.miracle.data.generator.pojo.Setting;
import priv.azure.miracle.data.generator.pojo.TextValueType;
import priv.azure.miracle.utility.ChronosOperator;
import priv.azure.miracle.utility.IdCardNumberGenerator;
import priv.azure.miracle.utility.MapOperator;
import priv.azure.miracle.utility.PhotoOperator;
import priv.azure.miracle.utility.StringOperator;

@Component
@Scope("prototype")
public class RecordCreatingTask implements Runnable{
	
	private Setting setting;
	private Shop<Record> recordShop;
	private Shop<String> textShop;
	/* photoShop, 重名了2333... */
	private Shop<Photo> photoShop;
	private Status status;
	private Summary summary;
	private IObjectColumn iObjectColumn;
	
	public RecordCreatingTask(
			Setting setting, 
			Shop<Record> recordShop, 
			Shop<String> textShop, 
			Status status, 
			Summary summary,
			Shop<Photo> photoShop,
			IObjectColumn iObjectColumn) {
		this.setting = setting;
		this.recordShop = recordShop;
		this.textShop = textShop;
		this.status = status;
		this.summary = summary;
		this.photoShop = photoShop;
		this.iObjectColumn = iObjectColumn;
	}
	
	@Override
	public void run() {
		if(status.checkRecordsCreatingSucceed())
			return;
		Record record = checkAndGenerateSummary(createNewRecord());
		recordShop.add(record);
		status.addRecordCreatingCountAndGet();
	}
	
	/**
	 * 在生成的记录中, 检查并生成指定列的文本摘要
	 * @param record
	 * @return
	 */
	private Record checkAndGenerateSummary(Record record) {
		Set<ColumnModule> columnModules = setting.getRecordModule().getColumns();
		String originColumnName = null;
		Column originColumn = null;
		String summaryText = "";
		for(ColumnModule columnModule : columnModules) {
			if(columnModule.getType() != ColumnType.FULLTEXT)
				continue;
			originColumnName = MapOperator.safetyGet(columnModule.getProperties(), Key.SUMMARY_SOURCE_COLUMN, "");
			originColumn = record.getColumn(originColumnName);
			if(originColumn == null)
				continue;
			try {
				summaryText = summary.execute(originColumn.getValue(), Method.STD);
			} catch (IOException e) {
				e.printStackTrace();
			}
			record.getColumn(columnModule.getName()).setValue(summaryText);
		}
		return record;
	}
	
	/**
	 * 构造一条新纪录
	 * @return
	 */
	private Record createNewRecord() {
		Record record = new Record();
		String name = null; ColumnType type = null;
		Map<String,String> properties = null;Column column = null;
		RecordModule recordModule = setting.getRecordModule();
		GlobalVariables global = recordModule.getGlobalVariables();
		Set<ColumnModule> columnsModule = recordModule.getColumns();
		for(ColumnModule columnModule : columnsModule) {
			name = columnModule.getName();
			type = columnModule.getType();
			properties = columnModule.getProperties();
			switch(type) {
				case CHAR: column = createCharColumnValue(name, properties, global); break;
				case NUMBER: column = createNumberColumnValue(name, properties, global); break;
				case DATETIME: column = createDatetimeColumnValue(name, properties, global); break;
				case FULLTEXT: column = createFullTextColumnValue(name, properties, global); break;
				case BINARY: column = createBinaryColumnValue(name, properties, global); break;
				case OBJECT: column = createJsonColumnValue(name); break;
				default: break;
			}
			record.addColumn(column);
		}
		return record;
	}
	/**
	 * 根据所给字段属性判定文本类型
	 * @param properties
	 * @return
	 */
	private TextValueType checkTextValueType(Map<String,String> properties) {
		String textValueTypeStr = MapOperator.safetyGet(properties, Key.TEXT_VALUE_TYPE, "uid");
		TextValueType textValueType = TextValueType.transfer(textValueTypeStr);
		return textValueType;
	}
	/**
	 * 构造CHAR类型的字段值
	 * @param name
	 * @param properties
	 * @param global
	 * @return
	 */
	private Column createCharColumnValue(String name, Map<String,String> properties, GlobalVariables global) {
		TextValueType textValueType = checkTextValueType(properties);
		return new Column(name, innerCreateCharColumnValue(textValueType, properties));
	}
	private String innerCreateCharColumnValue(TextValueType textValueType, Map<String,String> properties) {
		int valueCount = Integer.valueOf(MapOperator.safetyGet(properties, Constants.Key.VALUE_COUNT, "1"));
		if(valueCount < 1) valueCount = 1;
		StringBuilder valueBuilder = new StringBuilder();
		for(int i=0; i<valueCount; i++) {
			switch(textValueType) {
				case ID_CARD_NUMBER: valueBuilder.append(IdCardNumberGenerator.buildARandomIdCardNumber());break;
				case NAME : valueBuilder.append(StringOperator.getAName()); break;
				case UID : valueBuilder.append(UUID.randomUUID().toString()); break;
				case IP : valueBuilder.append(StringOperator.getARandomIp()); break;
				/* 设定：CHAR类型的文本设置为"从文本库读取" 或 "抽取摘要" 时, 属性失效 */
				case LIBRARY : return "";
				case SUMMARY : return "";
				case EMPTY : return "";
				default: return "";
			}
			if(i < valueCount - 1)
				valueBuilder.append(";");
		}
		return valueBuilder.toString();
	}
	/**
	 * 构造 NUMBER 类型字段的值
	 * @param name
	 * @param properties
	 * @param global
	 * @return
	 */
	private Column createNumberColumnValue(String name, Map<String,String> properties, GlobalVariables global) {
		/* 虽然默认值给了0.0, 不过在初始化的时候自检就已经排除获取不到属性的情况了, 这个默认值应该用不上才对 */
		String minNumberStr = MapOperator.safetyGet(properties, Key.MIN_VALUE, "0.0");
		String maxNumberStr = MapOperator.safetyGet(properties, Key.MAX_VALUE, "0.0");
		String value = null;
		if(global.getIdColumn() != null && name.equals(global.getIdColumn()))
			value = idColumnHandling();
		else
			value = innerCommonCreateNumberColumnValue(properties, Double.valueOf(minNumberStr), Double.valueOf(maxNumberStr));
		return new Column(name, value);
	}
	/**
	 * id型字段处理
	 * @return
	 */
	private String idColumnHandling() {
		return String.valueOf(status.addIdColumnValueAndGet());
	}
	/**
	 * 普通数值字段内部处理
	 * @param properties
	 * @param min
	 * @param max
	 * @return
	 */
	private String innerCommonCreateNumberColumnValue(Map<String,String>properties, double min, double max) {
		boolean isFloat = Boolean.valueOf(MapOperator.safetyGet(properties, Constants.Key.IS_FLOAT, "false"));
		int valueCount = Integer.valueOf(MapOperator.safetyGet(properties, Constants.Key.VALUE_COUNT, "1"));
		if(valueCount < 1) valueCount = 1;
		StringBuilder valueBuilder = new StringBuilder();
		double random = 0.0;
		for(int i=0; i<valueCount; i++) {
			random = Math.random() * (max - min) + min;
			if(isFloat) 
				valueBuilder.append(random);
			else
				valueBuilder.append((long)random);
			if(i < valueCount - 1)
				valueBuilder.append(";");
		}
		return valueBuilder.toString();
	}
	/**
	 * 构造DATETIME类型字段值
	 * @param name
	 * @param properties
	 * @param global
	 * @return
	 */
	private Column createDatetimeColumnValue(String name, Map<String,String> properties, GlobalVariables global) {
		String startDateString = MapOperator.safetyGet(properties, Constants.Key.START_DATETIME, global.getStartDateTime());
		String endDateString = MapOperator.safetyGet(properties, Constants.Key.END_DATETIME, global.getEndDateTime());
		LocalDateTime start = LocalDateTime.parse(startDateString, Constants.DEFAULT_DATE_TIME_FORMATTER);
		LocalDateTime end = LocalDateTime.parse(endDateString, Constants.DEFAULT_DATE_TIME_FORMATTER);
		String value = null;
		if(global.getColumnValueCycle() != null && name.equals(global.getColumnValueCycle()))
			value = innerCycleCreateDatetimeColumnValue(start, end);
		else 
			value = innerCommonCreateDatetimeColumnValue(properties, start, end);
		return new Column(name, value);
	}
	/**
	 * 有"循环生成"的标记时, 生成日期的内部逻辑
	 * @param start
	 * @param end
	 * @return
	 */
	private String innerCycleCreateDatetimeColumnValue(LocalDateTime start, LocalDateTime end) {
		LocalDateTime next = decideNextDateTime(start, end);
		status.setTheLastDateTimeGenerated(next);
		return next.format(Constants.DEFAULT_DATE_TIME_FORMATTER);
	}
	/**
	 * 没有"循环生成"的标记时, 生成日期的内部逻辑
	 * @param properties
	 * @param start
	 * @param end
	 * @return
	 */
	private String innerCommonCreateDatetimeColumnValue(Map<String,String> properties, LocalDateTime start, LocalDateTime end) {
		int valueCount = Integer.valueOf(MapOperator.safetyGet(properties, Constants.Key.VALUE_COUNT, "1"));
		if(valueCount < 1) valueCount = 1;
		StringBuilder valueBuilder = new StringBuilder();
		String subValue = null;
		for(int i=0; i<valueCount; i++) {
			subValue = ChronosOperator.getARandomLocalDateTime(start, end).format(Constants.DEFAULT_DATE_TIME_FORMATTER);
			valueBuilder.append(subValue);
			if(i < valueCount - 1)
				valueBuilder.append(";");
		}
		return valueBuilder.toString();
	}
	/**
	 * 计算下一个该生成的日期
	 * @param start
	 * @param end
	 * @return
	 */
	private LocalDateTime decideNextDateTime(LocalDateTime start, LocalDateTime end) {
		if(resetLastDateTime(end))
			return start;
		return status.getTheLastDateTimeGenerated().plusDays(1);
	}
	/**
	 * 是否需要在全局状态(Status)重置"最后一次生成的日期"<br>
	 * 满足以下任一条件即可:<br>
	 * 1. 最后一次生成的日期获取为空值(第一次生成日期的情况)<br>
	 * 2. 最后一次生成的日期已经达到右端点<br>
	 * @return
	 */
	private boolean resetLastDateTime(LocalDateTime end) {
		if(status.getTheLastDateTimeGenerated() == null)
			return true;
		/* 
		 * 注意别忘了plusDays(1)
		 * 假如它不存在, 有：
		 * start=2020/01/01 00:00:00
		 * end=2020/12/31 23:59:59
		 * status.getTheLastDateTimeGenerated=2020/12/31 00:00:00
		 * 2020/12/31 00:00:00 小于 2020/12/31 23:59:59
		 * 返回"不需要重置", 这样一来下一个日期就会生成 2021/01/01 00:00:00
		 * 于是就越界了
		 */
		return status.getTheLastDateTimeGenerated().plusDays(1).isAfter(end);
	}
	/**
	 * 构造全文字段的值
	 * @param name
	 * @param properties
	 * @param global
	 * @return
	 */
	private Column createFullTextColumnValue(String name, Map<String,String> properties, GlobalVariables global) {
		/* 如果指定对其他全文字段生成摘要, 则不生成值 
		 * P.S.在初始化的时候, 已经检查过字段模板列表, 没有这个属性的, 或取到的源字段并不存在时, 就令这个属性值为空串
		 * 这里只要检查属性值, 如果不是空串, 说明确实有摘要来源字段, 就不生成值了*/
		if(!"".equals(MapOperator.safetyGet(properties, Key.SUMMARY_SOURCE_COLUMN, "")))
			return new Column(name, "");
		/* 需要生成值的情况 */
		TextValueType textValueType = checkTextValueType(properties);
		String text = null;
		if(textValueType == TextValueType.ID_CARD_NUMBER) {
			text = IdCardNumberGenerator.buildARandomIdCardNumber().toString();
		}else if(textValueType == TextValueType.NAME) {
			text = StringOperator.getAName();
		}else if(textValueType == TextValueType.LIBRARY) {
			text = prepareTextFromLibrary();
		}else if(textValueType == TextValueType.SUMMARY) {
			/* Q: 这里怎么还有摘要生成的逻辑?
			 * A: 这里生成的摘要不来自其他字段值, 而是直接从文本库中取出的一篇文本生成的摘要,
			 * 如果希望丢弃原文, 只要摘要, 就会进入此条件块 */
			text = prepareTextFromLibrary();
			try {
				text = summary.execute(text, Method.STD);
			} catch (IOException e) {
				text = "";
			}
		}else if(textValueType == TextValueType.UID) {
			text = UUID.randomUUID().toString();
		}else if(textValueType == TextValueType.IP) {
			text = StringOperator.getARandomIp();
		}else {
			text = "";
		}
		return new Column(name, text);
	}
	/**
	 * 从文本库中取出文本<br>
	 * 如果设置了"初始化时加载所有文本到内存(initialize.load.all=true)", 则随机挑选一篇文本(由Status类存储)<br>
	 * 如果不是, 则从textShop中读取队列头的一篇文本
	 * @return
	 */
	private String prepareTextFromLibrary() {
		String text = null;
		if(setting.getInitializeLoadAll()) {
			List<String> totalTexts = status.getTexts();
			int randomIndex = (int)(Math.random() * totalTexts.size());
			text = totalTexts.get(randomIndex);
		}else {
			text = textShop.offer();
		}
		return text;
	}
	/**
	 * 构造图片输出目录
	 * @return
	 */
	private String preparePhotoOutputDirectory() {
		if(setting.getWriteIntoHdfs())
			return setting.getHdfsWriteinPath() + "/" + setting.getTableName() + "/attachment"; 
		return setting.getOutputDirectory() + "/" + setting.getTableName() + "/attachment";
			   
	}
	/**
	 * 构造 Binary 字段值
	 * @param name
	 * @param properties
	 * @param outputDirectory
	 * @return
	 */
	private Column createBinaryColumnValue(
			String name, Map<String,String> properties, GlobalVariables global) {
		String outputPath = preparePhotoOutputDirectory();
		if(!setting.getWriteIntoHdfs()) {
			/* 写本地文件, 需要提前创建目录 */
			File filePointer = new File(outputPath);
			if(!filePointer.exists()) 
				filePointer.mkdirs();
		}
		int valueCount = Integer.valueOf(MapOperator.safetyGet(properties, Constants.Key.VALUE_COUNT, "1"));
		if(valueCount < 1) 
			valueCount = 1;
		StringBuilder valueBuilder = new StringBuilder();
		String fileName = null;
		for(int i=0; i<valueCount; i++) {
			fileName = createAPhoto(properties, outputPath, global);
			valueBuilder.append("@attachment/").append(fileName).append(".jpg^JPEG;");
		}
		return new Column(name, valueBuilder.toString());
	}
	/**
	 * 创建单张图片
	 * @param properties
	 * @param outputDirectory
	 * @param global
	 * @return
	 */
	private String createAPhoto(Map<String,String> properties, String outputPath, GlobalVariables global) {
		String fileName = UUID.randomUUID().toString();
		TextValueType textValueType = checkTextValueType(properties);
		String text = prepareTextForBinaryColumn(textValueType);
		List<PhotoOperator.Text> photoTextList = preparePhotoTexts(text, textValueType, global);
		try {
			BufferedImage image = PhotoOperator.transferTextToPicture(photoTextList, Color.WHITE);
			String path = outputPath + "/" + fileName +".jpg";
			photoShop.add(new Photo(path, image));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileName;
	}
	/**
	 * 准备Binary字段需要的文本<br>
	 * @param textValueType
	 * @return
	 */
	private String prepareTextForBinaryColumn(TextValueType textValueType) {
		String text = null;
		if(textValueType == TextValueType.ID_CARD_NUMBER) {
			text = IdCardNumberGenerator.buildARandomIdCardNumber().toString();
		}else if(textValueType == TextValueType.NAME) {
			text = StringOperator.getAName();
		}else if(textValueType == TextValueType.SUMMARY) {
			text = prepareTextFromLibrary();
			try {
				text = summary.execute(text, Method.STD);
			}catch(IOException e) {
				text = "";
			}
		}else if(textValueType == TextValueType.LIBRARY) {
			text = prepareTextFromLibrary();
		}else if(textValueType == TextValueType.UID) {
			text = UUID.randomUUID().toString();
		}else if(textValueType == TextValueType.IP) {
			text = StringOperator.getARandomIp();
		}else {
			text = "";
		}
		return text;
	}
	/**
	 * 构造专用文本列表, 方能写入图片
	 * @param text
	 * @param textValueType
	 * @return
	 */
	private List<PhotoOperator.Text> preparePhotoTexts(String text, TextValueType textValueType, GlobalVariables global){
		/* 这个地方有个坑, 全局变量中保存的字体信息(由Font类保存)是经由json反序列化得到, 信息有缺失
		 * 直接用这个类的对象生成图片会因为图片大小为0而卡住, 所以在这里要new一个出来, 这样就可以正常生成了 */
		Font font = new Font(global.getFont().getName(), global.getFont().getStyle(),global.getFont().getSize());
		PhotoOperator.Text photoText;
		List<PhotoOperator.Text> photoTextList = new ArrayList<PhotoOperator.Text>();
		if(textValueType == TextValueType.SUMMARY || textValueType == TextValueType.LIBRARY) {
			List<String> sentences = summary.splitSentences(text);
			for(int i=0, size=sentences.size(); i<size; i++) {
				photoText = PhotoOperator.Text.build(sentences.get(i)).setFont(font);
				photoTextList.add(photoText);
			}
		}else {
			photoTextList.add(PhotoOperator.Text.build(text).setFont(font));
		}
		return photoTextList;
	}
	/**
	 * 构造 Object 字段值<br>
	 * @param name
	 * @return
	 */
	private Column createJsonColumnValue(String name) {
		return new Column(name, Constants.GSON.toJson(iObjectColumn.next(setting)));
	}
}
