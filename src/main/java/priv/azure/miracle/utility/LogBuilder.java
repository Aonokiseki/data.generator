package priv.azure.miracle.utility;

public class LogBuilder {
	private LogBuilder() {}
	/**
	 * 构造切面 beforeMethod 日志语句
	 * @param methodName
	 * @param arguments
	 * @return
	 */
	public static String beforeSentence(String methodName, Object[] arguments) {
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append(methodName).append("(");
		for(int i=0; i<arguments.length; i++) {
			logBuilder.append((String)arguments[i]);
			if(i < arguments.length-1)
				logBuilder.append(", ");
		}
		logBuilder.append(");");
		return logBuilder.toString();
	}
}
