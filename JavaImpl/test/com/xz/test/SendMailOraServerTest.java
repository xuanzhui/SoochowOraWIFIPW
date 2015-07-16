package com.xz.test;

import java.io.IOException;

import javax.mail.MessagingException;

import com.xz.auth.SSOAuthRespository;
import org.junit.Test;

import com.xz.net.SendMailOraServer;

public class SendMailOraServerTest {

	@Test
	public void test() throws MessagingException, IOException {
		SendMailOraServer sendm = new SendMailOraServer(SSOAuthRespository.getSsoUsername(),SSOAuthRespository.getPassword());
		//sendm.setMailTo("wei.dai@oracle.com,daiwei1215@126.com");
		sendm.setMailTo("wei.dai@oracle.com");
		sendm.sendMessage();
	}

}
