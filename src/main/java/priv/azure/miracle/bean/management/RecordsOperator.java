package priv.azure.miracle.bean.management;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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
import priv.azure.miracle.formatter.IRecordFormatter;
import priv.azure.miracle.task.LibraryInputTask;
import priv.azure.miracle.task.PhotoWritingTask;
import priv.azure.miracle.task.PhotoWritingToHdfsTask;
import priv.azure.miracle.task.RecordCreatingTask;
import priv.azure.miracle.task.RecordWritingTask;
import priv.azure.miracle.task.RecordWritingToHdfsTask;
import priv.azure.miracle.taskpool.RecordsCreatingTaskPool;
import priv.azure.miracle.taskpool.SingletonTaskPool;
import priv.azure.miracle.utility.Checking;
import priv.azure.miracle.utility.FileOperator;
import priv.azure.miracle.utility.FileOperator.IExecuter;
import priv.azure.miracle.utility.MapOperator;
import priv.azure.miracle.utility.Other;
import priv.azure.miracle.utility.Tuple;

@Component
@Scope("singleton")
public class RecordsOperator {
	
	private final static Logger LOGGER = LogManager.getLogger();
	
	@Autowired
	private Setting setting;
	@Autowired
	private Status status;
	@Autowired
	private Shop<String> textShop;
	@Autowired
	private Shop<Record> recordShop;
	@Autowired
	private IRecordFormatter iRecordFormatter;
	@Autowired
	private RecordsCreatingTaskPool recordsCreatingTaskPool;
	@Autowired
	private Summary summary;
	@Autowired
	private Shop<Photo> photoShop;
	@Autowired
	private SingletonTaskPool singletonTaskPool;
	@Autowired
	private IObjectColumn iObjectColumn;
	
	public void create() {
		LOGGER.info("Task start.");
		initialize();
		submitWritingTask();
		submitPhotoWritingTask();
		sumbitCreatingTask();
		shutdownPools();
		LOGGER.info("Task completed.");
	}
	/**
	 * ????????????????????????, ????????????????????????????????????<br>
	 * ????????????????????????????????????
	 */
	private void shutdownPools() {
		boolean allPoolsAllowShutdown = false;
		while(!allPoolsAllowShutdown) {
			allPoolsAllowShutdown = 
					status.checkPhotosWritingSucceed() && 
					status.checkRecordsCreatingSucceed() && 
					status.checkPhotosWritingSucceed();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		recordsCreatingTaskPool.shutdown();
		singletonTaskPool.shutdown();
		LOGGER.info("Thread pools have been shutdown.");
	}
	/**
	 * ????????????????????????
	 */
	private void sumbitCreatingTask() {
		LOGGER.info("Submitting record creating task.");
		RecordCreatingTask recordCreatingTask = 
				new RecordCreatingTask(setting, recordShop, textShop, status, summary, photoShop, iObjectColumn);
		int poolSize = setting.getGeneratingThreadCount();
		recordsCreatingTaskPool.setPoolSize(poolSize, poolSize);
		while(!status.checkRecordsCreatingSucceed()) {
			if(recordsCreatingTaskPool.allowSubmitNewTask())
				recordsCreatingTaskPool.submit(recordCreatingTask);
		}
		LOGGER.info("All record creating task have been submitted.");
	}
	/**
	 * ?????????????????????
	 */
	private void submitWritingTask() {
		if(setting.getWriteIntoHdfs()) {
			submitWritingHdfsTask();
			return;
		}
		submitWritingLocalTask();
	}
	
	private void submitWritingHdfsTask() {
		RecordWritingToHdfsTask recordWritingToHDFSTask = 
				new RecordWritingToHdfsTask(setting, recordShop, iRecordFormatter, status);
		singletonTaskPool.submit(recordWritingToHDFSTask);
		LOGGER.debug("submitWritingHdfsTask completed.");
	}
	private void submitWritingLocalTask() {
		RecordWritingTask recordWritingTask = new RecordWritingTask(setting, recordShop, iRecordFormatter, status);
		singletonTaskPool.submit(recordWritingTask);
		LOGGER.debug("submitWritingLocalTask completed.");
	}
	
	/**
	 * ?????????????????????
	 */
	private void submitPhotoWritingTask() {
		if(status.getTotalPhotosCount() == 0) {
			LOGGER.debug("status.getTotalPhotosCount()=0, don't need to write photos.");
			return;
		}
		if(setting.getWriteIntoHdfs()) {
			PhotoWritingToHdfsTask photoWritingToHdfsTask = new PhotoWritingToHdfsTask(status, photoShop);
			singletonTaskPool.submit(photoWritingToHdfsTask);
			LOGGER.info("Writing photos in hdfs task submitted.");
			return;
		}
		PhotoWritingTask photoWritingTask = new PhotoWritingTask(status, photoShop);
		singletonTaskPool.submit(photoWritingTask);
		LOGGER.info("Writing photos task submitted.");
	}
	/**
	 * ?????????
	 * @throws IOException 
	 */
	private void initialize(){
		/* ?????????????????? */
		recordModuleChecking();
		/* ?????????????????????????????? */
		calculateTotalRecordsCount();
		/* ?????????????????? */
		initializeTextLibrary();
		/* ?????????????????????????????????, ?????????0, ???????????????????????????, ?????????????????? */
		calculateTotalPhotosCount();
		/* ????????????????????? */
		initializeOutput();
		LOGGER.info("Initialization Complete.");
	}
	/**
	 * ?????????????????????
	 */
	private void initializeOutput() {
		if(setting.getWriteIntoHdfs()) {
			LOGGER.info("Writing to hdfs, don't need to initialize output directory.");
			return;
		}
		File directory = new File(
				setting.getOutputDirectory() + FileOperator.FILE_SEPARATOR + setting.getTableName());
		LOGGER.debug("directory="+directory.getAbsolutePath());
		if(!directory.exists())
			directory.mkdirs();
		LOGGER.debug("Initializing output directory completed.");
	}
	/**
	 * ??????????????????????????????
	 */
	private void calculateTotalPhotosCount() {
		int totalPhotosCountNeeded = statisticHowManyPhotosNeed();
		LOGGER.debug("totalPhotosCountNeeded="+totalPhotosCountNeeded);
		status.setTotalPhotosCount(totalPhotosCountNeeded);
		if(totalPhotosCountNeeded == 0) 
			destoryPhotoWritingTaskComponents();
		LOGGER.debug("Calculated total photos count completed.");
	}
	/**
	 * ??????????????????
	 */
	private void initializeTextLibrary() {
		LOGGER.debug("Initialize text library, start.");
		boolean isTextLibraryMusted = isTextsLibraryMusted();
		boolean initializeLoadAll = setting.getInitializeLoadAll();
		boolean initLoadTextsInMemory = isTextLibraryMusted && initializeLoadAll;
		LOGGER.debug(String.format("isTextLibraryMusted=%b, initializeLoadAll=%b, initLoadTextsInMemory=%b", 
				isTextLibraryMusted, initializeLoadAll, initLoadTextsInMemory));
		if(initLoadTextsInMemory) {
			/* ?????????????????????????????????????????????????????? */
			destoryLibraryInputTaskComponents();
			String path = setting.getRecordModule().getGlobalVariables().getTextsDirectory();
			LOGGER.debug("path="+path);
			FileExecuter fileExecuter = new FileExecuter();
			try {
				FileOperator.traversal(path, Pattern.compile("txt$"), false, fileExecuter, null);
				LOGGER.debug(String.format("text.size=%d", fileExecuter.texts.size()));
				status.setTexts(fileExecuter.texts);
			} catch (IOException e) {
				LOGGER.error(Other.stackTraceToString(e));
				System.exit(1);
			}
		}else if(isTextLibraryMusted) {
			/*  
			 *  ????????????????????????????????????, ??????????????????????????????, 
			 *  ??????????????????????????????????????????????????????????????????????????????(???????????????????????????????????????) 
			 *  ??????????????????????????????????????????, ?????????????????????????????? 
			 */
			LibraryInputTask libraryInputTask = new LibraryInputTask(setting, textShop, status);
			singletonTaskPool.submit(libraryInputTask);
			LOGGER.debug("Library input task submitted.");
		}else {
			/* ??????????????????????????????????????? */
			destoryLibraryInputTaskComponents();
		}
		LOGGER.debug("Initializing text library completed.");
	}
	/**
	 * ??????????????????????????????
	 * */
	private void calculateTotalRecordsCount() {
		long totalRecordsCount = setting.getFilesNumber() * setting.getRecordsNumberPerFile();
		LOGGER.debug("totalRecordCount="+totalRecordsCount);
		status.setTotalRecordsCount(totalRecordsCount);
		LOGGER.debug("Calculating total records count completed.");
	}
	/**
	 * ?????????, ???????????????????????????????????????????????????????????????
	 * @author
	 *
	 */
	private static class FileExecuter implements IExecuter{
		List<String> texts;
		public FileExecuter() {
			this.texts = new ArrayList<String>();
		}
		@Override
		public boolean execute(File file) {
			try {
				texts.add(FileOperator.read(file.getAbsolutePath()));
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		}
	}
	/**
	 * ????????????????????????????????????
	 * @param setting
	 * @return
	 */
	private boolean isTextsLibraryMusted() {
		Set<ColumnModule> columnsModule = setting.getRecordModule().getColumns();
		ColumnType type = null;
		String value = null;
		Map<String,String> properties = null;
		for(ColumnModule columnModule : columnsModule) {
			type = columnModule.getType();
			if(type == ColumnType.NUMBER || type == ColumnType.DATETIME || 
					type == ColumnType.CHAR)
				continue;
			if(type == ColumnType.OBJECT)
				return true;
			properties = columnModule.getProperties();
			value = MapOperator.safetyGet(properties, Key.TEXT_VALUE_TYPE, "").trim().toLowerCase();
			if(TextValueType.LIBRARY.toString().equals(value) || TextValueType.SUMMARY.toString().equals(value))
				return true;
		}
		return false;
	}
	/**
	 * ??????????????????????????????????????????
	 * @return
	 */
	 private int statisticHowManyPhotosNeed() {
		Set<ColumnModule> columnsModule = setting.getRecordModule().getColumns();
		ColumnType type = null;
		int totalPhotoCount = 0;
		Map<String,String> properties = null;
		int photosCountSingleRecord = 0;
		for(ColumnModule columnModule : columnsModule) {
			type = columnModule.getType();
			properties = columnModule.getProperties();
			if(type != ColumnType.BINARY)
				continue;
			photosCountSingleRecord += Integer.valueOf(MapOperator.safetyGet(properties, Key.VALUE_COUNT, "1"));
		}
		totalPhotoCount = setting.getFilesNumber() * setting.getRecordsNumberPerFile() * photosCountSingleRecord;
		return totalPhotoCount;
	 }
	
	/**
	 * ?????? ????????????(RecordModule)?????????????????????
	 */
	private void recordModuleChecking() {
		RecordModule recordModule = setting.getRecordModule();
		GlobalVariables global = recordModule.getGlobalVariables();
		Set<ColumnModule> columns = recordModule.getColumns();
		globalVariablesChecking(global);
		columnsModuleChecking(global, columns);
		LOGGER.debug("Checking record module completed.");
	}
	/**
	 * ?????? ????????????(RecordModule)???????????????(GlobalVariables)??????<br>
	 * ???????????????, ????????????????????????
	 */
	private void globalVariablesChecking(GlobalVariables globalVariables) {
		Tuple.Two<LocalDateTime, LocalDateTime> dateRange = 
				Checking.arrangeDateRange(globalVariables.getStartDateTime(), globalVariables.getEndDateTime());
		globalVariables.setDateTimeStart(dateRange.first);
		globalVariables.setStartDateTime(dateRange.first.format(Constants.DEFAULT_DATE_TIME_FORMATTER));
		globalVariables.setDateTimeEnd(dateRange.second);
		globalVariables.setEndDateTime(dateRange.second.format(Constants.DEFAULT_DATE_TIME_FORMATTER));
		double[] endPoints = Checking.ensureEndpoints(globalVariables.getMinNumber(), globalVariables.getMaxNumber());
		globalVariables.setMinNumber(endPoints[0]);
		globalVariables.setMaxNumber(endPoints[1]);
		LOGGER.debug("Checking global variables completed.");
	}
	/**
	 * ?????? ????????????(RecordModule)???, ????????????(ColumnModule)????????????<br>
	 * number??????????????????????????????, datetime?????????????????????????????????, fulltext????????????????????????????????????
	 */
	private void columnsModuleChecking(GlobalVariables globalVariables, Set<ColumnModule> columns){
		ColumnType type = null;
		Map<String,String> properties = null;
		String startDateStr = null;
		String endDateStr = null;
		String minNumberStr = null;
		String maxNumberStr = null;
		Tuple.Two<LocalDateTime, LocalDateTime> dateRange;
		double[] endpoints = null;
		Set<String> columnNames = new HashSet<String>();
		/* ???????????????????????? */
		for(ColumnModule columnModule : columns)
			columnNames.add(columnModule.getName());
		/* ??????????????????, ?????????????????? */
		for(ColumnModule columnModule : columns) {
			type = columnModule.getType();
			properties = columnModule.getProperties();
			if(properties == null)
				properties = new HashMap<String,String>();
			/* DATETIME??????, ???????????? */
			if(type == ColumnType.DATETIME) {
				startDateStr = MapOperator.safetyGet(properties, 
						Key.START_DATETIME, globalVariables.getStartDateTime());
				endDateStr = MapOperator.safetyGet(properties, 
						Key.END_DATETIME, globalVariables.getEndDateTime());
				dateRange = Checking.arrangeDateRange(startDateStr, endDateStr);
				properties.put(Key.START_DATETIME, dateRange.first.format(Constants.DEFAULT_DATE_TIME_FORMATTER));
				properties.put(Key.END_DATETIME, dateRange.second.format(Constants.DEFAULT_DATE_TIME_FORMATTER));
				/* NUMBER??????, ???????????? */
			}else if(type == ColumnType.NUMBER) {
				minNumberStr = MapOperator.safetyGet(properties, 
						Key.MIN_VALUE, String.valueOf(globalVariables.getMinNumber()));
				maxNumberStr = MapOperator.safetyGet(properties, 
						Key.MAX_VALUE, String.valueOf(globalVariables.getMaxNumber()));
				endpoints = Checking.ensureEndpoints(minNumberStr, maxNumberStr);
				properties.put(Key.MIN_VALUE, String.valueOf(endpoints[0]));
				properties.put(Key.MAX_VALUE, String.valueOf(endpoints[1]));
			}else if(type == ColumnType.FULLTEXT) {
				/* ????????????????????????, ???????????????????????????????????????(????????????????????????) */
				String originColumnName = MapOperator.safetyGet(properties, Key.SUMMARY_SOURCE_COLUMN, "");
				if("".equals(originColumnName) || !columnNames.contains(originColumnName)) {
					/* ???????????????????????????, ???????????????????????????, ??????????????????????????????*/
					properties.put(Key.SUMMARY_SOURCE_COLUMN, "");
				}
			}
			columnModule.setProperties(properties);
		}
		LOGGER.debug("Checking columns module completed.");
	}
	
	private void destoryLibraryInputTaskComponents() {
		LOGGER.debug("Destory library input task components.");
		textShop = null;
	}
	private void destoryPhotoWritingTaskComponents() {
		LOGGER.debug("Destory photo writing task components.");
		photoShop = null;
	}
}
