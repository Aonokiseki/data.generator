package priv.azure.miracle.utility;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Map;

import priv.azure.miracle.data.generator.pojo.Constants;

public class Checking {
	private Checking() {}
	/**
	 * 根据给定字符串解析成两个日期并确定包含在这两个日期内的范围
	 * @param firstDateString
	 * @param secondDateString
	 * @return 二元组, Tuple&ltLocalDateTime, LocalDateTime&gt first为起始日期, second为终止日期
	 */
	public static Tuple.Two<LocalDateTime,LocalDateTime> arrangeDateRange(String firstDateString, String secondDateString){
		if(firstDateString == null || firstDateString.trim().isEmpty())
			firstDateString = LocalDateTime.now().format(Constants.DEFAULT_DATE_TIME_FORMATTER);
		if(secondDateString == null || secondDateString.trim().isEmpty())
			secondDateString = LocalDateTime.now().format(Constants.DEFAULT_DATE_TIME_FORMATTER);
		LocalDateTime firstDateTime = LocalDateTime.parse(firstDateString, Constants.DEFAULT_DATE_TIME_FORMATTER);
		LocalDateTime secondDateTime = LocalDateTime.parse(secondDateString, Constants.DEFAULT_DATE_TIME_FORMATTER);
		if(firstDateTime.isAfter(secondDateTime))
			return new Tuple.Two<LocalDateTime, LocalDateTime>(secondDateTime, firstDateTime);
		else
			return new Tuple.Two<LocalDateTime, LocalDateTime>(firstDateTime, secondDateTime);
	}
	/**
	 * 确定数值的左右端点
	 * @param minNumberString
	 * @param maxNumberString
	 * @return
	 */
	public static double[] ensureEndpoints(String minNumberString, String maxNumberString) {
		Double number1 = Double.valueOf(minNumberString);
		Double number2 = Double.valueOf(maxNumberString);
		return ensureEndpoints(number1, number2);
	}
	public static double[] ensureEndpoints(Double number1, Double number2) {
		if(number1 > number2)
			return new double[] {number2, number1};
		return new double[]{number1, number2};
	}
	/**
	 * 根据所给路径获取文件名的本名
	 * @param path 路径
	 * @param defaultValue 当路径不存在时返回的默认值
	 * @return
	 */
	public static String extractFileNameFromPath(String path, String defaultValue) {
		File file = new File(path);
		if(!file.exists())
			return defaultValue;
		if(file.isDirectory())
			return file.getName();
		String fileName = file.getName();
		return fileName.substring(0, fileName.lastIndexOf("."));
	}
	/**
	 * 检查map里的某个key的值是不是true
	 * @param map
	 * @param key
	 * @return 以下情况会返回false: 1-map不存在;2-key不存在;3-value不存在或空串;4-value的值为false
	 */
	public static boolean keyIsTrue(Map<String,String> map, String key) {
		return Boolean.valueOf(MapOperator.safetyGet(map, key, "false"));
	}
}
