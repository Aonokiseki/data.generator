package priv.azure.miracle.bean.management;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import com.google.gson.JsonSyntaxException;

import priv.azure.miracle.data.generator.pojo.Constants;
import priv.azure.miracle.data.generator.pojo.IObjectColumn;
import priv.azure.miracle.data.generator.pojo.RecordModule;
import priv.azure.miracle.data.generator.pojo.Setting;
import priv.azure.miracle.data.generator.pojo.SimpleObjectColumn;
import priv.azure.miracle.formatter.IRecordFormatter;
import priv.azure.miracle.formatter.JsonFormatter;
import priv.azure.miracle.formatter.SQLFormatter;
import priv.azure.miracle.formatter.TRSFormatter;
import priv.azure.miracle.formatter.XMLFormatter;
import priv.azure.miracle.utility.Checking;

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass=true)
@ComponentScan(basePackages= {
	"priv.azure.miracle.bean.management",
	"priv.azure.miracle.taskpool",
	"priv.azure.miracle.task"
})
@PropertySource("file:./config/module.properties")
@Import({
	priv.azure.miracle.schedule.SchedulerConfig.class
})
public class Configurer {
	
	@Bean
	public static PropertySourcesPlaceholderConfigurer placeHolderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}
	
	@Bean
	public static Setting initialize(
			@Value("${file.format}") String fileFormat,
			@Value("${file.encoding}") String fileEncoding,
			@Value("${records.module.path}") String recordModulePath,
			@Value("${records.output.directory}") String outputDirectory,
			@Value("${records.number.perFile}") Integer recordsNumberPerFile,
			@Value("${records.files.number}") Integer filesNumber,
			@Value("${generating.thread.count}") Integer generatingThreadCount,
			@Value("${initialize.load.all}") Boolean initializeLoadAll,
			@Value("${write.buffer.size}") Integer writeBufferSize,
			@Value("${write.into.hdfs}") Boolean writeIntoHdfs,
			@Value("${hdfs.writein.path}") String hdfsWriteinPath,
			@Value("${hdfs.user.name}") String hdfsUserName
			) throws JsonSyntaxException, IOException {
		Setting setting = new Setting();
		setting.setFileFormat(fileFormat);
		setting.setFileEncoding(fileEncoding);
		RecordModule recordModule = Constants.parseJson(recordModulePath, RecordModule.class);
		setting.setRecordModule(recordModule);
		setting.setOutputDirectory(outputDirectory);
		setting.setTableName(Checking.extractFileNameFromPath(recordModulePath, "demo"));
		setting.setRecordsNumberPerFile(recordsNumberPerFile);
		setting.setFilesNumber(filesNumber);
		setting.setGeneratingThreadCount(generatingThreadCount);
		setting.setInitializeLoadAll(initializeLoadAll);
		setting.setWriteBufferSize(writeBufferSize);
		setting.setWriteIntoHdfs(writeIntoHdfs);
		setting.setHdfsUserName(hdfsUserName);
		setting.setHdfsWriteinPath(hdfsWriteinPath);
		return setting.checking();
	}
	
	@Bean
	public static IRecordFormatter formatter(
			@Value("${file.format}") String fileFormat,
			@Autowired Setting setting) {
		if("xml".equals(fileFormat.trim().toLowerCase()))
			return new XMLFormatter(setting);
		if("sql".equals(fileFormat.trim().toLowerCase()))
			return new SQLFormatter(setting);
		if("json".equals(fileFormat.trim().toLowerCase()))
			return new JsonFormatter(setting);
		return new TRSFormatter(setting);
	}
	
	@Bean
	public static IObjectColumn objectColumn() {
		return new SimpleObjectColumn();
	}
	
}