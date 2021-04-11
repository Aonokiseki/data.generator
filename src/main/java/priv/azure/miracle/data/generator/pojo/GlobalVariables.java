package priv.azure.miracle.data.generator.pojo;

import java.awt.Font;
import java.time.LocalDateTime;

public class GlobalVariables{
	private String startDateTime;
	private LocalDateTime dateTimeStart;
	private String endDateTime;
	private LocalDateTime dateTimeEnd;
	private String columnValueCycle;
	private String selfIncreamentColumn;
	private Double minNumber;
	private Double maxNumber;
	private String textsDirectory;
	private String linePrefix;
	private Font font;
	
	public String getStartDateTime() {
		return startDateTime;
	}
	public void setStartDateTime(String startDateTime) {
		this.startDateTime = startDateTime;
	}
	public LocalDateTime getDateTimeStart() {
		return dateTimeStart;
	}
	public void setDateTimeStart(LocalDateTime dateTimeStart) {
		this.dateTimeStart = dateTimeStart;
	}
	public String getEndDateTime() {
		return endDateTime;
	}
	public void setEndDateTime(String endDateTime) {
		this.endDateTime = endDateTime;
	}
	public LocalDateTime getDateTimeEnd() {
		return dateTimeEnd;
	}
	public void setDateTimeEnd(LocalDateTime dateTimeEnd) {
		this.dateTimeEnd = dateTimeEnd;
	}
	public Double getMinNumber() {
		return minNumber;
	}
	public void setMinNumber(Double minNumber) {
		this.minNumber = minNumber;
	}
	public Double getMaxNumber() {
		return maxNumber;
	}
	public void setMaxNumber(Double maxNumber) {
		this.maxNumber = maxNumber;
	}
	public String getTextsDirectory() {
		return textsDirectory;
	}
	public void setTextsDirectory(String textsDirectory) {
		this.textsDirectory = textsDirectory;
	}
	public String getLinePrefix() {
		return linePrefix;
	}
	public void setLinePrefix(String linePrefix) {
		this.linePrefix = linePrefix;
	}
	public Font getFont() {
		return font;
	}
	public void setFont(Font font) {
		this.font = font;
	}
	public String getColumnValueCycle() {
		return columnValueCycle;
	}
	public void setColumnValueCycle(String columnValueCycle) {
		this.columnValueCycle = columnValueCycle;
	}
	public String getSelfIncreamentColumn() {
		return selfIncreamentColumn;
	}
	public void setSelfIncreamentColumn(String selfIncreamentColumn) {
		this.selfIncreamentColumn = selfIncreamentColumn;
	}
}