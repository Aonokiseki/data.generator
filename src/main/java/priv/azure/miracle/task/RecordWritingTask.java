package priv.azure.miracle.task;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import priv.azure.miracle.bean.management.Shop;
import priv.azure.miracle.bean.management.Status;
import priv.azure.miracle.data.generator.pojo.Record;
import priv.azure.miracle.data.generator.pojo.Setting;
import priv.azure.miracle.formatter.IRecordFormatter;
import priv.azure.miracle.utility.FileOperator;

@Component
@Scope("singleton")
public class RecordWritingTask implements Runnable{
	
	private Setting setting;
	private Shop<Record> recordShop;
	private IRecordFormatter formatter;
	private Status status;
	private BufferedWriter writer;
	
	public RecordWritingTask(Setting setting, Shop<Record> recordShop, IRecordFormatter formatter, Status status) {
		this.setting = setting;
		this.recordShop = recordShop;
		this.formatter = formatter;
		this.status = status;
	}

	@Override
	public void run() {
		Record record;
		String fileName = null;
		long writed;
		String path = setting.getOutputDirectory() + FileOperator.FILE_SEPARATOR + setting.getTableName();
		boolean isLast = false;
		try {
			for(int i=0, size=setting.getFilesNumber(); i<size; i++) {
				fileName = path + FileOperator.FILE_SEPARATOR + i + formatter.suffix();
				writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), 
						setting.getFileEncoding()), setting.getWriteBufferSize() * FileOperator.ONE_MIB);
				if(formatter.start() != null)
					writer.append(formatter.start());
				for(int j=0, recordsSize=setting.getRecordsNumberPerFile(); j<recordsSize; j++) {
					if(status.checkRecordsWritingSucceed())
						return;
					record = recordShop.offer();
					writer.append(formatter.handle(record, isLast));
					writed = status.addRecordsWritingCountAndGet();
					if(writed == status.getTotalRecordsCount() - 1)
						isLast = true;
				}
				if(formatter.end() != null)
					writer.append(formatter.end());
				writer.close();
			}
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
}
