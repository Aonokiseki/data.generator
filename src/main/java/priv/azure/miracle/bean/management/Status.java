package priv.azure.miracle.bean.management;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class Status {
	/* 如果选择了"初始化时加载所有文本库的文本(initialize.load.all=true)", 
	 * 则存储所有文本库的文本 */
	private List<String> texts;
	/* 当前已创建的记录数量 */
	private AtomicLong recordsCreatingCount;
	/* 当前已写记录数量 */
	private AtomicLong recordsWritingCount;
	/* 需创建并写入的记录总量 */
	private long totalRecordsCount;
	/* 需写入磁盘的图片总数 */
	private long totalPhotosCount;
	/* 当前已写图片数量 */
	private AtomicLong photoWritingCount;
	/* 起始时间, 用于定时任务, 统计进度 */
	private LocalDateTime startDateTime;
	/* 循环生成日期时(single.cycle.generate=true), 记录全局生成的最后一个日期 */
	private LocalDateTime theLastDateTimeGenerated;
	/* 自增字段的值 */
	private AtomicLong idColumnValue;
	/* Map, 记录所有错误 */
	private Map<String,String> errors;
	/* 检查模板和配置文件, 并初始化以后, 是否允许真正生成记录 */
	private boolean allowCreateRecords = true;

	public Status() {
		texts = new ArrayList<String>();
		recordsCreatingCount = new AtomicLong(0);
		recordsWritingCount = new AtomicLong(0);
		photoWritingCount = new AtomicLong(0);
		idColumnValue = new AtomicLong(0);
		startDateTime = LocalDateTime.now();
		errors = new HashMap<String,String>();
	}
	
	public boolean checkRecordsCreatingSucceed() {
		return recordsCreatingCount.longValue() >= totalRecordsCount;
	}
	public boolean checkRecordsWritingSucceed() {
		return recordsWritingCount.longValue() >= totalRecordsCount;
	}
	public boolean checkPhotosWritingSucceed() {
		return photoWritingCount.longValue() >= totalPhotosCount;
	}
	
	public AtomicLong getRecordsCreatingCount() {
		return recordsCreatingCount;
	}
	public long addRecordCreatingCountAndGet() {
		return this.recordsCreatingCount.addAndGet(1);
	}
	public AtomicLong getRecordsWritingCount() {
		return recordsWritingCount;
	}
	public long addRecordsWritingCountAndGet() {
		return this.recordsWritingCount.addAndGet(1);
	}
	public List<String> getTexts() {
		return texts;
	}
	public void setTexts(List<String> texts) {
		this.texts = texts;
	}
	public long getTotalRecordsCount() {
		return totalRecordsCount;
	}
	public void setTotalRecordsCount(long totalRecordsCount) {
		this.totalRecordsCount = totalRecordsCount;
	}
	public AtomicLong getPhotoWritingCount() {
		return photoWritingCount;
	}
	public long addPhotoWritingCoundAndGet() {
		return this.photoWritingCount.addAndGet(1);
	}
	public long getTotalPhotosCount() {
		return totalPhotosCount;
	}
	public void setTotalPhotosCount(long totalPhotosCount) {
		this.totalPhotosCount = totalPhotosCount;
	}
	public LocalDateTime getStartDateTime() {
		return startDateTime;
	}
	public LocalDateTime getTheLastDateTimeGenerated() {
		return theLastDateTimeGenerated;
	}
	public void setTheLastDateTimeGenerated(LocalDateTime theLastDateTimeGenerated) {
		this.theLastDateTimeGenerated = theLastDateTimeGenerated;
	}
	public AtomicLong getIdColumnValue() {
		return idColumnValue;
	}
	public long addIdColumnValueAndGet() {
		return idColumnValue.addAndGet(1);
	}
	public Map<String,String> getErrors() {
		return Collections.unmodifiableMap(errors);
	}
	public void addOneErrorInfo(String key, String value) {
		errors.put(key, value);
	}
	public boolean isAllowCreateRecords() {
		return allowCreateRecords;
	}
	public void setAllowCreateRecords(boolean allowCreateRecords) {
		this.allowCreateRecords = allowCreateRecords;
	}
}
