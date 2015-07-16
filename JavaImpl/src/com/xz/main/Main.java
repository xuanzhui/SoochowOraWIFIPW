package com.xz.main;

import java.io.IOException;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import com.xz.auth.SSOAuthRespository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.xz.net.OraContentViaSSO;
import com.xz.net.SendMailOraServer;
import com.xz.qr.GenerateQRCode;

public class Main {

	private static Logger logger = LogManager.getLogger(Main.class);

	public static void main(String[] args) {
		
		String wifiurl="https://gmp.oracle.com/captcha/files/airespace_pwd_apac.txt";
		String wifireqhost="gmp.oracle.com";

		//TODO set mail to addresses, separate by ','
		String mailto = ",";

		OraContentViaSSO ssoContent=new OraContentViaSSO();
				
		ssoContent.setSSOAuth(SSOAuthRespository.getSsoUsername(), SSOAuthRespository.getPassword());
		
		String password=null;

		//最多请求三次
		for (int i=0;i<3;i++){
			try {
				logger.info(" requesting password for the "+(i+1)+ " time ");
				password = ssoContent.parseWifiPassword(wifiurl, wifireqhost);
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

		//生成QR Code
		GenerateQRCode qr = new GenerateQRCode(password);
        try {
			qr.generateQRCode();
		} catch (Exception e) {
			logException(e);
			return;
		}
        
        //发邮件
        SendMailOraServer sendm = new SendMailOraServer(SSOAuthRespository.getSsoUsername(), SSOAuthRespository.getPassword());
		try {
			sendm.setMailTo(mailto);
			sendm.setWifiPassword(password);
			sendm.sendMessage();
		} catch (AddressException e) {
			logException(e);
		} catch (MessagingException e) {
			logException(e);
		} catch (IOException e) {
			logException(e);
		}
		logger.info(" mail sent successfully");
	}

	public static void logException(Exception e){
		logger.error(e.getMessage());

		for (StackTraceElement ele  : e.getStackTrace())
			logger.error(ele.toString());
	}

}
