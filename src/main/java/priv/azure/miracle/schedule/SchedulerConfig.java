package priv.azure.miracle.schedule;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@ComponentScan(basePackageClasses = {
	priv.azure.miracle.schedule.ScheduleLog.class
})
@Configuration
@EnableScheduling
public class SchedulerConfig {
	@Bean
	public static ScheduleLog scheduleLog() {
		return new ScheduleLog();
	}
}
