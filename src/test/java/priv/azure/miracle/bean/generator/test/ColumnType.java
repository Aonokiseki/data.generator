package priv.azure.miracle.bean.generator.test;

import static org.testng.Assert.assertEquals;

import java.lang.reflect.Method;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import priv.azure.miracle.bean.management.Configurer;

public class ColumnType {
private AnnotationConfigApplicationContext context;
	
	@BeforeClass
	public void beforeClass() {
		context = new AnnotationConfigApplicationContext(Configurer.class);
	}
	
	@AfterClass
	public void afterClass() {
		if(context != null)
			context.close();
	}
	
	@DataProvider(name = "columnTypeDataProvider")
	public Object[][] columnTypeDataProvider(Method method){
		return new Object[][] {
			new Object[] {"char", priv.azure.miracle.data.generator.pojo.ColumnType.CHAR},
			new Object[] {"number", priv.azure.miracle.data.generator.pojo.ColumnType.NUMBER},
			new Object[] {"date", priv.azure.miracle.data.generator.pojo.ColumnType.DATETIME},
			new Object[] {"fulltext", priv.azure.miracle.data.generator.pojo.ColumnType.FULLTEXT},
			new Object[] {"binary", priv.azure.miracle.data.generator.pojo.ColumnType.BINARY},
			new Object[] {"object", priv.azure.miracle.data.generator.pojo.ColumnType.OBJECT},
		};
	}
	
	@Test(dataProvider="columnTypeDataProvider")
	public void columnType(String typeStr, priv.azure.miracle.data.generator.pojo.ColumnType expectation) {
		priv.azure.miracle.data.generator.pojo.ColumnType type = 
				priv.azure.miracle.data.generator.pojo.ColumnType.parse(typeStr);
		assertEquals(type, expectation);
	}
}
