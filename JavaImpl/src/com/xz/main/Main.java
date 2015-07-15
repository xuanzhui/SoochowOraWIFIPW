package com.xz.main;

import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.xz.net.OraContentViaSSO;

public class Main {

	private static Logger logger = LogManager.getLogger(Main.class);

	public static void main(String[] args) {
		String proxyhost="";
		int proxyport=80;
		String proxytype="http";

		String wifiurl="https://gmp.oracle.com/captcha/files/airespace_pwd_apac.txt";
		String wifireqhost="gmp.oracle.com";

		OraContentViaSSO pw=new OraContentViaSSO();

		String password=null;

		//最多请求三次
		for (int i=0;i<3;i++){
			try {
				logger.info(" requesting password for the "+(i+1)+ " time ");
				password = pw.parseWifiPassword(wifiurl, wifireqhost);
			} catch (IllegalStateException e) {
				password=null;

				logException(e);

			} catch (IOException e) {
				password=null;
				logException(e);
			}

			if (password!=null)
				break;
		}

		if (password==null){
			logger.warn(" fail to get password after three tries! ");
			return;
		}

		logger.info(" get password "+password);

	}

	public static void logException(Exception e){
		logger.error(e.getMessage());

		for (StackTraceElement ele  : e.getStackTrace())
			logger.error(ele.toString());
	}

}
