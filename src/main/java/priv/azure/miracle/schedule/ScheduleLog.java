package priv.azure.miracle.schedule;

import java.time.Duration;
import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import priv.azure.miracle.bean.management.Status;

public class ScheduleLog {
	private final static String LOG_FORMAT = "time:%d, current:%d, total:%d, rate:%f";
	private final static Logger LOGGER = LogManager.getLogger(ScheduleLog.class);
	
	@Autowired
	private Status status;
	
	@Scheduled(fixedRate = 1000)
	public void report() {
		long created = status.getRecordsCreatingCount().longValue();
		long total = status.getTotalRecordsCount();
		double rate = (double)created / total * 100;
		Duration duration = Duration.between(status.getStartDateTime(), LocalDateTime.now());
		LOGGER.info(String.format(LOG_FORMAT, duration.getSeconds(),created, total, rate));
	}
	
}
