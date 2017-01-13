package com.devkkh.evio;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

import java.io.IOException;

/**
 * Created by netmind on 16. 9. 2.
 */
public class LoggerUtil {
	public static void initLogger() {
		String filePath = "/tmp/eviotest.log";
		PatternLayout layout = new PatternLayout("%-5p [%t] %d{HH:mm:ss.SSS} %m%n");
//		ConsoleAppender appender = new ConsoleAppender(layout);
//		appender.setName("myFirstLog");
//		appender.activateOptions();

		RollingFileAppender appender = null;
		try {
			appender = new RollingFileAppender(layout, filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		appender.setName("myFirstLog");
		appender.setMaxFileSize("1MB");
		appender.activateOptions();

		Logger.getRootLogger().addAppender(appender);
	}
}
