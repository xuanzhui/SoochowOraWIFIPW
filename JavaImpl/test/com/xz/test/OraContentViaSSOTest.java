package com.xz.test;

import java.io.IOException;

import com.xz.auth.SSOAuthRespository;
import org.junit.Test;

import com.xz.net.OraContentViaSSO;

public class OraContentViaSSOTest {
	@Test
	public void test() throws IllegalStateException, IOException{
		String url="https://gmp.oracle.com/captcha/files/airespace_pwd_apac.txt";
		String reqhost="gmp.oracle.com";
		OraContentViaSSO tmp = new OraContentViaSSO();

		tmp.setSSOAuth(SSOAuthRespository.getSsoUsername(), SSOAuthRespository.getPassword());
		String pw=tmp.parseWifiPassword(url, reqhost);
		
		System.out.println(pw);
	}
}
