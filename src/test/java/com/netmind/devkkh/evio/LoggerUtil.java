package com.netmind.devkkh.evio;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 * Created by netmind on 16. 9. 2.
 */
public class LoggerUtil {
	public static void initLogger() {

		PatternLayout layout = new PatternLayout("%-5p [%t] %d{HH:mm:ss.SSS} %m%n");
		ConsoleAppender appender = new ConsoleAppender(layout);
		appender.setName("console");
		appender.activateOptions();

//		String filePath = "/tmp/eviotest.log";
//		RollingFileAppender appender = null;
//		try {
//			appender = new RollingFileAppender(layout, filePath);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		appender.setName("myFirstLog");
//		appender.setMaxFileSize("1MB");
//		appender.activateOptions();

		Logger.getRootLogger().addAppender(appender);
	}
}
