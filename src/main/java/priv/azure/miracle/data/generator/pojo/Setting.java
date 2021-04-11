package priv.azure.miracle.data.generator.pojo;

import priv.azure.miracle.utility.FileOperator;

public class Setting {
	private String fileEncoding;
	private String fileFormat;
	private RecordModule recordModule;
	private String outputDirectory;
	private String tableName;
	private Integer recordsNumberPerFile;
	private Integer filesNumber;
	private Integer generatingThreadCount;
	private Boolean initializeLoadAll;
	private Integer writeBufferSize;
	private Boolean writeIntoHdfs;
	private String hdfsWriteinPath;
	private String hdfsUserName;
	
	public Setting() {}
	
	
	public Boolean getWriteIntoHdfs() {
		return writeIntoHdfs;
	}
	public void setWriteIntoHdfs(Boolean writeIntoHdfs) {
		this.writeIntoHdfs = writeIntoHdfs;
	}
	public String getHdfsWriteinPath() {
		return hdfsWriteinPath;
	}
	public void setHdfsWriteinPath(String hdfsWriteinPath) {
		this.hdfsWriteinPath = hdfsWriteinPath;
	}
	public String getHdfsUserName() {
		return hdfsUserName;
	}
	public void setHdfsUserName(String hdfsUserName) {
		this.hdfsUserName = hdfsUserName;
	}
	public String getFileEncoding() {
		return fileEncoding;
	}
	public void setFileEncoding(String fileEncoding) {
		this.fileEncoding = fileEncoding;
	}
	public String getFileFormat() {
		return fileFormat;
	}
	public void setFileFormat(String fileFormat) {
		this.fileFormat = fileFormat;
	}
	public RecordModule getRecordModule() {
		return recordModule;
	}
	public void setRecordModule(RecordModule recordModule) {
		this.recordModule = recordModule;
	}
	public String getOutputDirectory() {
		return outputDirectory;
	}
	public void setOutputDirectory(String outputDirectory) {
		this.outputDirectory = outputDirectory;
	}
	public Integer getRecordsNumberPerFile() {
		return recordsNumberPerFile;
	}
	public void setRecordsNumberPerFile(Integer recordsNumberPerFile) {
		this.recordsNumberPerFile = recordsNumberPerFile;
	}
	public Integer getFilesNumber() {
		return filesNumber;
	}
	public void setFilesNumber(Integer filesNumber) {
		this.filesNumber = filesNumber;
	}
	public Integer getGeneratingThreadCount() {
		return generatingThreadCount;
	}
	public void setGeneratingThreadCount(Integer generatingThreadCount) {
		this.generatingThreadCount = generatingThreadCount;
	}
	public Boolean getInitializeLoadAll() {
		return initializeLoadAll;
	}
	public void setInitializeLoadAll(Boolean initializeLoadAll) {
		this.initializeLoadAll = initializeLoadAll;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public Integer getWriteBufferSize() {
		return writeBufferSize;
	}
	public void setWriteBufferSize(Integer writeBufferSize) {
		this.writeBufferSize = writeBufferSize;
	}
	public Setting checking() {
		if(filesNumber < 1)
			filesNumber = 1;
		if(recordsNumberPerFile < 1)
			recordsNumberPerFile = 1;
		if(generatingThreadCount < 1)
			generatingThreadCount = 1;
		if(writeBufferSize == null || writeBufferSize < 1 || writeBufferSize > 4096)
			writeBufferSize = Constants.WRITER_BUFFER_SIZE;
		if(fileEncoding == null || fileEncoding.trim().isEmpty())
			fileEncoding = FileOperator.DEFAULT_ENCODING;
		return this;
	}
}
