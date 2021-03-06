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
	/* photoShop, ιεδΊ2333... */
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
	 * ε¨ηζηθ?°ε½δΈ­, ζ£ζ₯εΉΆηζζε?εηζζ¬ζθ¦
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
	 * ζι δΈζ‘ζ°ηΊͺε½
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
	 * ζ Ήζ?ζη»ε­ζ?΅ε±ζ§ε€ε?ζζ¬η±»ε
	 * @param properties
	 * @return
	 */
	private TextValueType checkTextValueType(Map<String,String> properties) {
		String textValueTypeStr = MapOperator.safetyGet(properties, Key.TEXT_VALUE_TYPE, "uid");
		TextValueType textValueType = TextValueType.transfer(textValueTypeStr);
		return textValueType;
	}
	/**
	 * ζι CHARη±»εηε­ζ?΅εΌ
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
				/* θ?Ύε?οΌCHARη±»εηζζ¬θ?Ύη½?δΈΊ"δ»ζζ¬εΊθ―»ε" ζ "ζ½εζθ¦" ζΆ, ε±ζ§ε€±ζ */
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
	 * ζι  NUMBER η±»εε­ζ?΅ηεΌ
	 * @param name
	 * @param properties
	 * @param global
	 * @return
	 */
	private Column createNumberColumnValue(String name, Map<String,String> properties, GlobalVariables global) {
		/* θ½ηΆι»θ?€εΌη»δΊ0.0, δΈθΏε¨εε§εηζΆεθͺζ£ε°±ε·²η»ζι€θ·εδΈε°ε±ζ§ηζε΅δΊ, θΏδΈͺι»θ?€εΌεΊθ―₯η¨δΈδΈζε―Ή */
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
	 * idεε­ζ?΅ε€η
	 * @return
	 */
	private String idColumnHandling() {
		return String.valueOf(status.addIdColumnValueAndGet());
	}
	/**
	 * ζ?ιζ°εΌε­ζ?΅ει¨ε€η
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
	 * ζι DATETIMEη±»εε­ζ?΅εΌ
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
	 * ζ"εΎͺη―ηζ"ηζ θ?°ζΆ, ηζζ₯ζηει¨ι»θΎ
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
	 * ζ²‘ζ"εΎͺη―ηζ"ηζ θ?°ζΆ, ηζζ₯ζηει¨ι»θΎ
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
	 * θ?‘η?δΈδΈδΈͺθ―₯ηζηζ₯ζ
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
	 * ζ―ε¦ιθ¦ε¨ε¨ε±ηΆζ(Status)ιη½?"ζεδΈζ¬‘ηζηζ₯ζ"<br>
	 * ζ»‘θΆ³δ»₯δΈδ»»δΈζ‘δ»Άε³ε―:<br>
	 * 1. ζεδΈζ¬‘ηζηζ₯ζθ·εδΈΊη©ΊεΌ(η¬¬δΈζ¬‘ηζζ₯ζηζε΅)<br>
	 * 2. ζεδΈζ¬‘ηζηζ₯ζε·²η»θΎΎε°ε³η«―ηΉ<br>
	 * @return
	 */
	private boolean resetLastDateTime(LocalDateTime end) {
		if(status.getTheLastDateTimeGenerated() == null)
			return true;
		/* 
		 * ζ³¨ζε«εΏδΊplusDays(1)
		 * εε¦ε?δΈε­ε¨, ζοΌ
		 * start=2020/01/01 00:00:00
		 * end=2020/12/31 23:59:59
		 * status.getTheLastDateTimeGenerated=2020/12/31 00:00:00
		 * 2020/12/31 00:00:00 ε°δΊ 2020/12/31 23:59:59
		 * θΏε"δΈιθ¦ιη½?", θΏζ ·δΈζ₯δΈδΈδΈͺζ₯ζε°±δΌηζ 2021/01/01 00:00:00
		 * δΊζ―ε°±θΆηδΊ
		 */
		return status.getTheLastDateTimeGenerated().plusDays(1).isAfter(end);
	}
	/**
	 * ζι ε¨ζε­ζ?΅ηεΌ
	 * @param name
	 * @param properties
	 * @param global
	 * @return
	 */
	private Column createFullTextColumnValue(String name, Map<String,String> properties, GlobalVariables global) {
		/* ε¦ζζε?ε―ΉεΆδ»ε¨ζε­ζ?΅ηζζθ¦, εδΈηζεΌ 
		 * P.S.ε¨εε§εηζΆε, ε·²η»ζ£ζ₯θΏε­ζ?΅ζ¨‘ζΏεθ‘¨, ζ²‘ζθΏδΈͺε±ζ§η, ζεε°ηζΊε­ζ?΅εΉΆδΈε­ε¨ζΆ, ε°±δ»€θΏδΈͺε±ζ§εΌδΈΊη©ΊδΈ²
		 * θΏιεͺθ¦ζ£ζ₯ε±ζ§εΌ, ε¦ζδΈζ―η©ΊδΈ², θ―΄ζη‘?ε?ζζθ¦ζ₯ζΊε­ζ?΅, ε°±δΈηζεΌδΊ*/
		if(!"".equals(MapOperator.safetyGet(properties, Key.SUMMARY_SOURCE_COLUMN, "")))
			return new Column(name, "");
		/* ιθ¦ηζεΌηζε΅ */
		TextValueType textValueType = checkTextValueType(properties);
		String text = null;
		if(textValueType == TextValueType.ID_CARD_NUMBER) {
			text = IdCardNumberGenerator.buildARandomIdCardNumber().toString();
		}else if(textValueType == TextValueType.NAME) {
			text = StringOperator.getAName();
		}else if(textValueType == TextValueType.LIBRARY) {
			text = prepareTextFromLibrary();
		}else if(textValueType == TextValueType.SUMMARY) {
			/* Q: θΏιζδΉθΏζζθ¦ηζηι»θΎ?
			 * A: θΏιηζηζθ¦δΈζ₯θͺεΆδ»ε­ζ?΅εΌ, θζ―η΄ζ₯δ»ζζ¬εΊδΈ­εεΊηδΈη―ζζ¬ηζηζθ¦,
			 * ε¦ζεΈζδΈ’εΌεζ, εͺθ¦ζθ¦, ε°±δΌθΏε₯ζ­€ζ‘δ»Άε */
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
	 * δ»ζζ¬εΊδΈ­εεΊζζ¬<br>
	 * ε¦ζθ?Ύη½?δΊ"εε§εζΆε θ½½ζζζζ¬ε°εε­(initialize.load.all=true)", ειζΊζιδΈη―ζζ¬(η±Statusη±»ε­ε¨)<br>
	 * ε¦ζδΈζ―, εδ»textShopδΈ­θ―»ειεε€΄ηδΈη―ζζ¬
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
	 * ζι εΎηθΎεΊη?ε½
	 * @return
	 */
	private String preparePhotoOutputDirectory() {
		if(setting.getWriteIntoHdfs())
			return setting.getHdfsWriteinPath() + "/" + setting.getTableName() + "/attachment"; 
		return setting.getOutputDirectory() + "/" + setting.getTableName() + "/attachment";
			   
	}
	/**
	 * ζι  Binary ε­ζ?΅εΌ
	 * @param name
	 * @param properties
	 * @param outputDirectory
	 * @return
	 */
	private Column createBinaryColumnValue(
			String name, Map<String,String> properties, GlobalVariables global) {
		String outputPath = preparePhotoOutputDirectory();
		if(!setting.getWriteIntoHdfs()) {
			/* εζ¬ε°ζδ»Ά, ιθ¦ζεεε»Ίη?ε½ */
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
	 * εε»ΊεεΌ εΎη
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
	 * εε€Binaryε­ζ?΅ιθ¦ηζζ¬<br>
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
	 * ζι δΈη¨ζζ¬εθ‘¨, ζΉθ½εε₯εΎη
	 * @param text
	 * @param textValueType
	 * @return
	 */
	private List<PhotoOperator.Text> preparePhotoTexts(String text, TextValueType textValueType, GlobalVariables global){
		/* θΏδΈͺε°ζΉζδΈͺε, ε¨ε±ειδΈ­δΏε­ηε­δ½δΏ‘ζ―(η±Fontη±»δΏε­)ζ―η»η±jsonεεΊεεεΎε°, δΏ‘ζ―ζηΌΊε€±
		 * η΄ζ₯η¨θΏδΈͺη±»ηε―Ήθ±‘ηζεΎηδΌε δΈΊεΎηε€§ε°δΈΊ0θε‘δ½, ζδ»₯ε¨θΏιθ¦newδΈδΈͺεΊζ₯, θΏζ ·ε°±ε―δ»₯ζ­£εΈΈηζδΊ */
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
	 * ζι  Object ε­ζ?΅εΌ<br>
	 * @param name
	 * @return
	 */
	private Column createJsonColumnValue(String name) {
		return new Column(name, Constants.GSON.toJson(iObjectColumn.next(setting)));
	}
}
