package priv.azure.miracle.bean.management;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

@Component
@Scope("singleton")
public class Summary {
	
	public enum Method{
		STD, DEFAULT
	}
	
	private final static String SYMBOL_BE_ERASED = 
			"(&rdquo;|&ldquo;|&mdash;|&lsquo;|&rsquo;|&middot;|&quot;|&darr;|&bull;)";
	
	/* 默认最大截取前多少个关键词 */
	private final static int DEFAULT_MAX_KEYWORDS_COUNT = 50;
	/* 关键词间的距离默认阈值, 根据此值划分不同簇 */
	private final static int DEFAULT_CLUSTER_THRESHOLD = 5;
	/* 前top-n个句子 */
	private final static int DEFAULT_TOP_N_SENTENCES = 10;
	/* 默认返回所有关键句的前百分比, 默认100%, 即全部 */
	private final static double DEFAULT_TOP_SENTENCES_RATE = 1.0;
	
	/* 关键词截取数量 */
	private int maxCount;
	/* 关键词间距阈值 */
	private int threshold;
	/* 最大保留前n句 */
	private int topNSentence;
	/* 最大保留关键句的比例 */
	private double topSentenceRate;
	/* 停用词列表 */
	private Set<String> stopWords;
	
	public Summary() {
		this.stopWords = new HashSet<String>();
		this.maxCount = DEFAULT_MAX_KEYWORDS_COUNT;
		this.threshold = DEFAULT_CLUSTER_THRESHOLD;
		this.topNSentence = DEFAULT_TOP_N_SENTENCES;
		this.topSentenceRate = DEFAULT_TOP_SENTENCES_RATE;
	}
	public Summary setTopSentenceRate(double topSentenceRate) {
		if(topSentenceRate > 1.0) topSentenceRate = 1.0;
		if(topSentenceRate < 0.0) topSentenceRate = 0.0;
		this.topSentenceRate = topSentenceRate;
		return this;
	}
	public Summary setMaxCount(int maxCount) {
		this.maxCount = maxCount;
		return this;
	}
	public Summary setThreadhold(int threshold) {
		this.threshold = threshold;
		return this;
	}
	public Summary setTopNSentence(int topNSentence) {
		this.topNSentence = topNSentence;
		return this;
	}
	public Summary setStopWords(Set<String> stopWords) {
		this.stopWords = stopWords;
		return this;
	}
	public int getMaxCount() {
		return this.maxCount;
	}
	public int getThreshold() {
		return this.threshold;
	}
	public int getTopNSentence() {
		return this.topNSentence;
	}
	public Set<String> getStopWords(){
		return this.stopWords;
	}
	
	public String execute(String text, Method method) throws IOException {
		/* 文章分句 */
		List<String> sentences = splitSentences(text);
		if(sentences.isEmpty())
			return "";
		if(sentences.size() == 1)
			return sentences.get(0);
		if(sentences.size() == 2)
			return sentences.get(0) + sentences.get(1);
		/* 文章分词 */
		List<String> segments = ikSegment(text);
		/* 对各分词分类统计并按照频数降序 */
		LinkedHashMap<String, Integer> sortedStatistic = wordFrequencyStatistic(segments);
		/* 获取前 maxCount 类(前 maxCount 个高频词) */
		List<String> highestFrequencyWords = getHighestFrequencyWords(sortedStatistic);
		/* 给每个句子打分 */
		Map<Integer, Double> sentenceIndexAndScores = calculateSentencesScore(sentences, highestFrequencyWords);
		/* 经过进一步处理(默认不处理, 也可以调用方差法进一步处理), 计算出文本中最关键的句子 */
		Map<Integer, String> keySentences = buildKeySentences(method, sentenceIndexAndScores, sentences);
		StringBuilder summaryBuilder = new StringBuilder();
		String sentence = null;
		for(int index : keySentences.keySet()) {
			sentence = keySentences.get(index);
			summaryBuilder.append(sentence);
		}
		return summaryBuilder.toString();
	}
	/**
	 * 构造关键句
	 * @param sentenceIndexAndScores
	 * @param sentences
	 * @return
	 */
	private Map<Integer, String> buildKeySentences(Method method, Map<Integer, Double> sentenceIndexAndScores, List<String> sentences){
		List<Map.Entry<Integer, Double>> sortedSentences = new ArrayList<Map.Entry<Integer, Double>>(sentenceIndexAndScores.entrySet());
		Collections.sort(sortedSentences, new Comparator<Map.Entry<Integer, Double>>(){
			@Override
			public int compare(Entry<Integer, Double> entry1, Entry<Integer, Double> entry2) {
				return entry2.getValue().compareTo(entry1.getValue());
			}
		});
		Map<Integer, Double> finalSentenceScore = null;
		switch(method) {
			case STD: finalSentenceScore = meanStd(sortedSentences); break;
			default : finalSentenceScore = defaultFunction(sortedSentences); break;
		}
		Map<Integer, String> keySentences = new TreeMap<Integer, String>();
		int count = 0;
		double rate = 0.0;
		for(Map.Entry<Integer, Double> entry : finalSentenceScore.entrySet()) {
			count++;
			keySentences.put(entry.getKey(), sentences.get(entry.getKey()));
			rate = ((double)count) / finalSentenceScore.size();
			if(count >= topNSentence || rate >= topSentenceRate )
				break;
		}
		return keySentences;
	}
	/**
	 * 标准差过滤
	 * @param sortedSentList
	 * @return
	 */
	private Map<Integer, Double> meanStd(List<Map.Entry<Integer, Double>> sortedSentList){
		/* 每项各自平方后的总和 */
		double squareAll = 0.0;
		/* 每项的总和 */
		double all = 0.0;
		for(Map.Entry<Integer, Double> sentenceScore : sortedSentList) {
			squareAll += Math.pow(sentenceScore.getValue().intValue(), 2);
			all += sentenceScore.getValue().intValue();
		}
		/* 期望 */
		double expectation = all / sortedSentList.size();
		/* 期望的平方 */
		double squareOfexpectation = Math.pow(expectation, 2);
		/* 平方的期望 */
		double expectationOfSquare = squareAll / sortedSentList.size();
		/* 方差 */
		double variance = expectationOfSquare - squareOfexpectation;
		/* 标准差 */
		double std = Math.pow(variance, 0.5);
		Map<Integer, Double> result = new HashMap<Integer, Double>();
		for(Map.Entry<Integer, Double> sentenceScore : sortedSentList) {
			if(sentenceScore.getValue() > (expectation + 0.5 * std))
				result.put(sentenceScore.getKey(), sentenceScore.getValue());
		}
		return result;
	}
	/**
	 * 默认策略, 不做任何处理
	 * @param sortedSentList
	 * @return
	 */
	private Map<Integer, Double> defaultFunction(List<Map.Entry<Integer, Double>> sortedSentList){
		Map<Integer, Double> result = new HashMap<Integer, Double>();
		for(Entry<Integer, Double> entry : sortedSentList)
			result.put(entry.getKey(), entry.getValue());
		return result;
	}
	/**
	 * 切分文本成为句子
	 * @param text
	 * @return
	 */
	public List<String> splitSentences(String text){
		List<String> sentences = new ArrayList<String>();
		String splitRegex = "[!?.！？。]";
		Pattern pattern = Pattern.compile(splitRegex);
		String[] sentenceArray = pattern.split(text);
		Matcher matcher = pattern.matcher(text);
		if(sentenceArray.length > 0)
			for(int i=0; i<sentenceArray.length; i++)
				if(matcher.find())
					sentenceArray[i] += matcher.group();
		for(String sentence : sentenceArray) {
			sentence = sentence.replaceAll(SYMBOL_BE_ERASED, "");
			sentences.add(sentence);
		}
		return sentences;
	}
	/**
	 * 利用IK分词器分词
	 * @param text
	 * @param stopWords 停用词列表
	 * @return
	 * @throws IOException
	 */
	private List<String> ikSegment(String text) throws IOException {
		List<String> words = new ArrayList<String>();
		StringReader reader = new StringReader(text);
		IKSegmenter ikSegmenter = new IKSegmenter(reader, true);
		Lexeme lexeme = null;
		String word = null;
		while((lexeme = ikSegmenter.next()) != null) {
			word = lexeme.getLexemeText();
			if(word.equals("nbsp") || stopWords.contains(word))
				continue;
			if(word.length() > 1 && !word.equals("\t"))
				words.add(word);
		}
		return words;
	}
	/**
	 * 词频统计并降序排序
	 * @param words
	 * @return
	 */
	private LinkedHashMap<String, Integer> wordFrequencyStatistic(List<String> words){
		/* 分类统计 */
		Map<String,Integer> statistic = new HashMap<String,Integer>();
		Integer count = 0;
		for(String word : words) {
			count = statistic.get(word);
			statistic.put(word, count == null ? 1 : count + 1);
		}
		/* 排序 */
		ArrayList<Map.Entry<String, Integer>> entries = 
				new ArrayList<Map.Entry<String, Integer>>(statistic.entrySet());
		Collections.sort(entries, new Comparator<Map.Entry<String, Integer>>(){
			@Override
			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
				return o2.getValue().intValue() - o1.getValue().intValue();
			}
		});
		LinkedHashMap<String,Integer> sortedStatistic = new LinkedHashMap<String, Integer>();
		Map.Entry<String, Integer> entry = null;
		for(int i=0, size=entries.size(); i<size; i++) {
			entry = entries.get(i);
			sortedStatistic.put(entry.getKey(), entry.getValue());
		}
		return sortedStatistic;
	}
	/**
	 * 从词频统计中取频率最高的前 [maxCount] 个词
	 * @param sortedStatistic 词频统计
	 * @return
	 */
	private List<String> getHighestFrequencyWords(LinkedHashMap<String,Integer> sortedStatistic){
		if(maxCount > sortedStatistic.size())
			maxCount = sortedStatistic.size();
		List<String> highestFrequencyWords = new ArrayList<String>(maxCount);
		for(Entry<String, Integer> entry : sortedStatistic.entrySet()) {
			highestFrequencyWords.add(entry.getKey());
			if(highestFrequencyWords.size() >= maxCount)
				break;
		}
		return highestFrequencyWords;
	}
	/**
	 * 给每个句子打分
	 * @param sentences
	 * @param highestFrequencyWords
	 * @return
	 * @throws IOException
	 */
	private Map<Integer, Double> calculateSentencesScore(
			List<String> sentences, List<String> highestFrequencyWords) throws IOException{
		LinkedHashMap<Integer, Double> scoresStatistic = new LinkedHashMap<Integer, Double>();
		String sentence = null;
		List<String> segments = null;
		List<Integer> segmentWordsIndexs = null;
		List<List<Integer>> clusters = null;
		double score;
		for(int sentenceIndex=0, size=sentences.size(); sentenceIndex<size; sentenceIndex++) {
			/* 取出单句 */
			sentence = sentences.get(sentenceIndex);
			/* 单句分词 */
			segments = ikSegment(sentence);
			/* 构造关键词位置向量 */
			segmentWordsIndexs = calculateSegmentWordsIndexAndSort(segments, highestFrequencyWords);
			/* 按照距离阈值分簇 */
			clusters = clusters(segmentWordsIndexs, threshold);
			/* 计算整个簇集的最高分 */
			score = calculateMaxScore(clusters);
			scoresStatistic.put(sentenceIndex, score);
		}
		return scoresStatistic;
	}
	/**
	 * 遍历单个句子的每个分词(segmentWords), 判断其是否为高频词。若是, 记录其在句子的起始位置。然后排序<br><br> 
	 * 
	 * 为方便说明, 设<br>
	 * highestFrequencyWords= [农民, 革命, 地主, 一个, 谁个, 土豪劣绅, 农会, 所谓, 举动, 第二]<br>
	 * segments=[又有, 一般人, 农会, 要办, 但是, 现在, 农会, 举动, 未免太, 分了]<br><br>
	 * 
	 * "农会" 和 "举动" 这两个分词在 highestFrequencyWords中出现,
	 * 则这两个词为高频词, 分别获取 "农会" 和 "举动" 在分词序列的位置, 一个是2号位置, 另一个是7号位置<br>
	 * 就有<br>
	 * segmentWordsIndexs=[2, 7]<br>
	 * 
	 * @param segmentWords
	 * @param highestFrequencyWords
	 * @return List&ltInteger&gt 高频词索引构成的向量
	 */
	private List<Integer> calculateSegmentWordsIndexAndSort(
			List<String> segmentWords, List<String> highestFrequencyWords){
		List<Integer> segmentWordsIndex = new ArrayList<Integer>();
		String word = null;
		for(int i=0, size=highestFrequencyWords.size(); i<size; i++) {
			word = highestFrequencyWords.get(i);
			if(segmentWords.contains(word))
				segmentWordsIndex.add(segmentWords.indexOf(word));
		}
		Collections.sort(segmentWordsIndex);
		return segmentWordsIndex;
	}
	/**
	 * 高频词位置向量按照阈值分簇<br><br>
	 * 假设 threshold = 5<br>
	 * segmentWordsIndexs=[2, 7]<br>
	 * index 0 is 2<br>
	 * index 1 is 7<br><br>
	 * 
	 * 7 - 2 = 5 不满足小于 threshold的条件<br>
	 * 进入else块<br>
	 * 把cluster(只有一个元素2)放入clusters<br>
	 * 新声明一个cluster, 存入segmentWordsIndexs.get(1)(也即7) 放入clusters里<br>
	 * 当前的clusters如下<br>
	 * clusters = [<br>
	 *     [2],<br>
	 *     [7]<br>
	 * ]<br>
	 * @param segmentWordsIndexs 高频词位置向量
	 * @param threshold 阈值
	 * @return 簇集
	 */
	private List<List<Integer>> clusters(List<Integer> segmentWordsIndexs, int threshold){
		List<List<Integer>> clusters = new ArrayList<List<Integer>>();
		if(segmentWordsIndexs == null || segmentWordsIndexs.isEmpty())
			return clusters;
		List<Integer> cluster = new ArrayList<Integer>();
		cluster.add(segmentWordsIndexs.get(0));
		int currentIndex, previousIndex;
		for(int i=1, size=segmentWordsIndexs.size(); i<size; i++) {
			currentIndex = segmentWordsIndexs.get(i);
			previousIndex = segmentWordsIndexs.get(i - 1);
			if(currentIndex - previousIndex < threshold) {
				cluster.add(currentIndex);
			}else {
				clusters.add(cluster);
				cluster = new ArrayList<Integer>();
				cluster.add(currentIndex);
			}
		}
		clusters.add(cluster);
		return clusters;
	}
	/**
	 * 计算簇集(clusters)的最高分
	 * @param clusters
	 * @return
	 */
	private double calculateMaxScore(List<List<Integer>> clusters) {
		double maxScore = 0.0;
		List<Integer> cluster = null;
		int keyWordsSize = 0;
		int totalWordsSize = 0;
		double score;
		for(int i=0, size=clusters.size(); i<size; i++) {
			cluster = clusters.get(i);
			keyWordsSize = cluster.size();
			totalWordsSize = cluster.get(keyWordsSize - 1) - cluster.get(0) + 1;
			score = 1.0 * keyWordsSize * keyWordsSize / totalWordsSize;
			if(score > maxScore)
				maxScore = score;
		}
		return maxScore;
	}
}
