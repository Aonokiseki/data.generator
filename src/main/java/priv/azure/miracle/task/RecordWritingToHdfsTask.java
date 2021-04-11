package priv.azure.miracle.task;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import priv.azure.miracle.bean.management.Shop;
import priv.azure.miracle.bean.management.Status;
import priv.azure.miracle.data.generator.pojo.Record;
import priv.azure.miracle.data.generator.pojo.Setting;
import priv.azure.miracle.formatter.IRecordFormatter;

@Component
@Scope("singleton")
public class RecordWritingToHdfsTask implements Runnable{
	
	private Setting setting;
	private Shop<Record> recordShop;
	private IRecordFormatter formatter;
	private Status status;
	private Configuration configuration;
	
	public RecordWritingToHdfsTask(Setting setting, Shop<Record> recordShop, IRecordFormatter formatter, Status status) {
		this.setting = setting;
		this.recordShop = recordShop;
		this.formatter = formatter;
		this.status = status;
		configuration = new Configuration();
		System.setProperty("HADOOP_USER_NAME", setting.getHdfsUserName());
	}

	@Override
	public void run() {
		Record record;
		String fileName = null;
		long writed;
		boolean isLast = false;
		FileSystem fileSystem = null;
		FSDataOutputStream fsDataOutputStream = null;
		StringBuilder contentBuilder = new StringBuilder();
		try {
			for(int i=0, size=setting.getFilesNumber(); i<size; i++) {
				contentBuilder.delete(0, contentBuilder.length());
				fileName = setting.getHdfsWriteinPath()+ "/" +setting.getTableName()+ "/" + i + formatter.suffix();
				fileSystem = FileSystem.get(URI.create(fileName), configuration);
				fsDataOutputStream = fileSystem.create(new Path(fileName));
				if(formatter.start() != null) {
					contentBuilder.append(formatter.start());
				}
				for(int j=0, recordsSize=setting.getRecordsNumberPerFile(); j<recordsSize; j++) {
					if(status.checkRecordsWritingSucceed())
						return;
					record = recordShop.offer();
					contentBuilder.append(formatter.handle(record, isLast));
					writed = status.addRecordsWritingCountAndGet();
					if(writed == status.getTotalRecordsCount() - 1)
						isLast = true;
				}
				if(formatter.end() != null)
					contentBuilder.append(formatter.end());
				IOUtils.write(contentBuilder.toString(), fsDataOutputStream, Charset.forName(setting.getFileEncoding()));
			}
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
}
