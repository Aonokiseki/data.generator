package priv.azure.miracle.data.generator.pojo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;

import com.google.gson.Gson;

public class Constants {
	private Constants() {}
	
	/* 默认生成记录的线程数 */
	public final static int DEFAULT_RECORDS_CREATING_THREAD_COUNT = 1;
	/* 单例任务(写记录, 写图片, 读文本库)的线程池大小 */
	public final static int SINGLETON_TASK_POOL_SIZE = 3;
	/* 默认临时存储区的上限 */
	public final static int DEFAULT_TEMP_STOCKPILE_MAX_SIZE = 32;
	/* 写文件任务的缓冲区大小, 单位MB */
	public final static int WRITER_BUFFER_SIZE = 32;
	/* 默认日期格式 */
	public final static DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
	/* 默认文件输出目录 */
	public final static String DEFAULT_OUTPUT_PATH = "./output";
	/* 默认log4j2配置文件地址 */
	public final static String DEFAULT_LOG4J2_XML_PATH = "config/log4j2.xml";
	/* Gson对象 */
	public final static Gson GSON = new Gson();
	
	
	public static void reconfigureLog4j2() {
		reconfigureLog4j2(DEFAULT_LOG4J2_XML_PATH);
	}
	/* 重新指定log4j2配置文件 */
	public static void reconfigureLog4j2(String path) {
		LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
		loggerContext.setConfigLocation(new File(path).getAbsoluteFile().toURI());
		loggerContext.reconfigure();
	}
	/**
	 * json反序列化
	 * @param <T>
	 * @param path json所在文件的路径
	 * @param classOfT 解析模板类
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public static <T> T parseJson(String path, Class<?> classOfT) throws IOException{
		InputStreamReader reader = new InputStreamReader(new FileInputStream(path));
		T result = (T) GSON.fromJson(reader, classOfT);
		reader.close();
		return result;
	}
	
	public static class Key{
		public final static String START_DATETIME = "start.datetime";
    	public final static String END_DATETIME = "end.datetime";
    	public final static String VALUE_COUNT = "value.count";
    	public final static String MIN_VALUE = "min.value";
    	public final static String MAX_VALUE = "max.value";
    	public final static String IS_FLOAT = "is.float";
    	public final static String TEXT_VALUE_TYPE = "text.value.type";
    	/* 摘要来源字段, 如果此属性有效(指定的字段存在, 且指定的字段是全文字段), 忽略text.value.type属性 */
    	public final static String SUMMARY_SOURCE_COLUMN = "summary.source.column";
	}
}
