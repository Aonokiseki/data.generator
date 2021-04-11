package priv.azure.miracle.task;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import priv.azure.miracle.bean.management.Shop;
import priv.azure.miracle.bean.management.Status;
import priv.azure.miracle.data.generator.pojo.Setting;
import priv.azure.miracle.utility.FileOperator;
import priv.azure.miracle.utility.Other;

@Component
@Scope("singleton")
public class LibraryInputTask implements Runnable{
	
	private Setting setting;
	private List<File> libraryFiles;
	private long totalRecordNumber;
	private Shop<String> textShop;
	private Status status;
	
	public LibraryInputTask(Setting setting, Shop<String> textShop, Status status) {
		this.setting = setting;
		this.textShop = textShop;
		this.status = status;
		this.libraryFiles = new LinkedList<File>();
		this.totalRecordNumber = 0;
	}
	
	private void initialize() throws IOException {
		String directory = setting.getRecordModule().getGlobalVariables().getTextsDirectory();
		libraryFiles = FileOperator.traversal(directory, ".txt", false);
		this.totalRecordNumber = setting.getFilesNumber() * setting.getRecordsNumberPerFile();
	}
	
	@Override
	public void run() {
		try {
			initialize();
		} catch (IOException e) {
			status.addOneErrorInfo("LibraryInputTask.run.initialize", Other.stackTraceToString(e));
			return;
		}
		List<File> fileQueue = new LinkedList<File>();
		for(File file : libraryFiles)
			fileQueue.add(file);
		String content = null; File file = null;
		while(status.getRecordsCreatingCount().longValue() < totalRecordNumber) {
			file = fileQueue.remove(0);
			try {
				content = FileOperator.read(file.getAbsolutePath());
				textShop.add(content);
			} catch (IOException e) {
				e.printStackTrace();
			}
			fileQueue.add(file);
		}
	}
}
