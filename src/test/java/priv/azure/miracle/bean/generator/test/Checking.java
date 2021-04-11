package priv.azure.miracle.bean.generator.test;

import static org.testng.Assert.assertTrue;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import priv.azure.miracle.bean.management.Configurer;
import priv.azure.miracle.data.generator.pojo.Constants;
import priv.azure.miracle.utility.Tuple;

public class Checking {
	private AnnotationConfigApplicationContext context;
	
	@BeforeClass
	public void beforeClass() {
		Constants.reconfigureLog4j2();
		context = new AnnotationConfigApplicationContext(Configurer.class);
	}
	
	@AfterClass
	public void afterClass() {
		if(context != null)
			context.close();
	}
	
	@DataProvider(name = "arrangeDateRangeDataProvider")
	public Object[][] arrangeDateRange(Method method){
		return new Object[][] {
			new Object[] {"2021/02/12 00:00:00", "2021/02/13 00:00:00"},
			new Object[] {"2021/02/13 00:00:00", "2021/02/12 00:00:00"}
		};
	}
	
	@Test(dataProvider = "arrangeDateRangeDataProvider")
	public void arrangeDateRange(String start, String end) {
		Tuple.Two<LocalDateTime, LocalDateTime> dateRange = 
				priv.azure.miracle.utility.Checking.arrangeDateRange(start, end);
		assertTrue(dateRange.first.isBefore(dateRange.second) || dateRange.first.isEqual(dateRange.second));
	}
	
	@DataProvider(name = "ensureEndPointsDataProvider")
	public Object[][] ensureEndPointsDataProvider(Method method){
		return new Object[][] {
			new Object[] {"1.1", "2.2"},
			new Object[] {"1.0", "1.0"},
			new Object[] {"2.2", "1.1"}
		};
	}
	
	@Test(dataProvider = "ensureEndPointsDataProvider")
	public void ensureEndPoints(String number1, String number2) {
		double[] endpoints = priv.azure.miracle.utility.Checking.ensureEndpoints(number1, number2);
		assertTrue(endpoints[0] <= endpoints[1]);
	}
}
