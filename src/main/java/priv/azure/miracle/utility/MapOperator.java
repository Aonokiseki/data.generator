package priv.azure.miracle.utility;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public final class MapOperator {
	
	private MapOperator(){}
	/**
	 * 对<code>Map&ltString, ?&gt</code>的values按自然排序放入<code>LinkedHashMap&ltString, Object&gt</code>对象中
	 * @param map 待处理的<code>Map&ltString, ?&gt</code>
	 * @return LinkedHashMap&ltString, Object&gt
	 */
	public static LinkedHashMap<String, Object> sortKeyOrderByNatural(Map<String, ?> map){
		ArrayList<Map.Entry<String, ?>> list = new ArrayList<Map.Entry<String, ?>>(map.entrySet());
		Collections.sort(list, 
				(Entry<String, ?> e1, Entry<String, ?> e2) -> e1.getKey().compareTo(e2.getKey()));
		LinkedHashMap<String, Object> newMap = new LinkedHashMap<String, Object>();
		for(int i=0; i<list.size(); i++)
            newMap.put(list.get(i).getKey(), list.get(i).getValue());   
        return newMap;  
	}
	/**
	 * 对<code>Map&ltString, Calendar&gt</code>的values按大小排序放入<code>LinkedHashMap&ltString, Calendar&gt</code>对象中
	 * @param map  待处理的<code>Map&ltString, Calendar&gt</code>
	 * @return LinkedHashMap&ltString, Calendar&gt
	 */
	public static LinkedHashMap<String, Calendar> sortValueOrderByCalendar(Map<String, Calendar> map){
		ArrayList<Map.Entry<String, Calendar>> list = new ArrayList<Map.Entry<String, Calendar>>(map.entrySet());
		Collections.sort(list, 
				(Entry<String, Calendar> e1, Entry<String, Calendar> e2) -> e1.getValue().compareTo(e2.getValue()));
		LinkedHashMap<String, Calendar> newMap = new LinkedHashMap<String, Calendar>();
		for(int i=list.size()-1; i>=0; i--)
			newMap.put(list.get(i).getKey(), list.get(i).getValue());   
        return newMap;  
	}
	/**
	 * 对<code>Map&ltString, LocalDateTime&gt</code>的values按大小排序放入<code>LinkedHashMap&ltString, <br>
	 * LocalDateTime&gt</code>对象中
	 * @param map 待处理的<code>Map&ltString, LocalDateTime&gt</code>
	 * @return LinkedHashMap&ltString, LocalDateTime&gt
	 */
	public static LinkedHashMap<String, LocalDateTime> sortValueOrderByLocalDateTime(Map<String, LocalDateTime>map, 
			boolean desc){
		ArrayList<Map.Entry<String, LocalDateTime>> list = 
				new ArrayList<Map.Entry<String, LocalDateTime>>(map.entrySet());
		Collections.sort(list, 
				(Entry<String, LocalDateTime> e1, Entry<String, LocalDateTime> e2) -> 
						e1.getValue().compareTo(e2.getValue()));
		LinkedHashMap<String, LocalDateTime> newMap = new LinkedHashMap<String, LocalDateTime>();
		for(int i=list.size()-1; i>=0; i--)
			newMap.put(list.get(i).getKey(), list.get(i).getValue());   
        return newMap;
	}
	/**
	 *    对<code>Map&ltString, T extends Number&gt</code>的values按大小排序放入<code>LinkedHashMap&ltString, Double&gt</code>
	 * <br/>对象中
	 * @param map  待处理的<code>Map&ltString, T&gt</code>, value的类型必须为Number或其子类
	 * @return LinkedHashMap&ltString, Double&gt
	 */
	public static <T extends Number> LinkedHashMap<String, Double> sortValueOrderByNumber(Map<String, T> map){
		ArrayList<Map.Entry<String, T>> list = new ArrayList<Map.Entry<String, T>>(map.entrySet());
		Collections.sort(list, 
				(Entry<String, T> e1, Entry<String, T> e2) -> 
						compareResult(e1.getValue().doubleValue(), e2.getValue().doubleValue()));
		LinkedHashMap<String, Double> newMap = new LinkedHashMap<String, Double>();
		for(int i=0; i<list.size(); i++)
			newMap.put(list.get(i).getKey(), list.get(i).getValue().doubleValue());
        return newMap;  
	}
	/**
	 * 将<code>Map&ltString, String&gt</code>对象转换为<code>Map&ltString, Double&gt</code>对象
	 * @param map
	 * @return
	 */
	public static Map<String,Double> parseValuesToDouble(Map<String,String> map){
		Map<String, Double> result = new HashMap<String,Double>();
		for(Entry<String,String> e: map.entrySet())
			result.put(e.getKey(), Double.valueOf(e.getValue()));
		return result;
	}
	/**
	 * 将<code>Map&ltString, String&gt</code>对象转换为<code>Map&ltString, LocalDateTime&gt</code>对象
	 * @param map
	 * @param pattern
	 * @return
	 */
	public static Map<String, LocalDateTime> parseValuesToLocalDateTime(Map<String, String> map, String pattern){
		Map<String, LocalDateTime> result = new HashMap<String, LocalDateTime>();
		for(Entry<String, String> e: map.entrySet())
			result.put(e.getKey(), ChronosOperator.stringToLocalDateTime(e.getValue(), pattern));
		return result;
	}
	
	private static int compareResult(double n1, double n2){
		if (n1 - n2 > 0) return 1;
		else if (n1 - n2 < 0) return -1;
		else return 0;
	}
	
	/**
	 * 安全获取map中的value
	 * @param map
	 * @param key
	 * @param resultWhenICannotGetValue 当获取指定value失败时的返回值, 此值由使用者决定
	 * @return T
	 */
	public static <T> T safetyGet(Map<String, T> map, String key, T resultWhenICannotGetValue){
		if(map == null || map.isEmpty() || map.size() == 0 || key == null || "".equals(key.trim()))
			return resultWhenICannotGetValue;
		if(!map.containsKey(key))
			return resultWhenICannotGetValue;
		return map.get(key);
	}
	/**
	 * 自定义排序
	 * @param map 待排序的map
	 * @param comparator 比较器
	 * @return LinkedHashMap&ltString, Double&gt
	 */
	public static <T> LinkedHashMap<String, Object> customSort(Map<String, T> map, 
			Comparator<Entry<String, T>> comparator){
		ArrayList<Map.Entry<String, T>> list = new ArrayList<Map.Entry<String, T>>(map.entrySet());
		Collections.sort(list, comparator);
		LinkedHashMap<String, Object> newMap = new LinkedHashMap<String, Object>();
		for(int i=list.size()-1; i>=0; i--)
			newMap.put(list.get(i).getKey(), list.get(i).getValue());
        return newMap;  
	}
	/**
	 * (浅拷贝)合并两个map成为一个新的, 不破坏原有map
	 * @param first
	 * @param second
	 * @return
	 */
	public static Map<String, Integer> merge(Map<String, Integer> first, Map<String, Integer> second){
		Map<String, Integer> result = new HashMap<String, Integer>();
		for(Entry<String, Integer> e: first.entrySet())
			result.put(e.getKey(), e.getValue());
		for(Entry<String, Integer> e: second.entrySet()){
			if(result.containsKey(e.getKey())){
				result.put(e.getKey(), result.get(e.getKey())+second.get(e.getKey()));
				continue;
			}
			result.put(e.getKey(), e.getValue());
		}
		return result;
	}
}
