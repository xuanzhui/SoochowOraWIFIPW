package com.xz.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;


/**
 * 可以不手动保留传递cookie
 * HttpClient自动保留转发
 */
public class OraContentViaSSO {

	Map<String, String> ssoparam;
	private String ssousername;
	private String password;
	
	/**
	 * 设置SSO用户名和密码
	 * @param ssousername
	 * @param password
	 */
	public void setSSOAuth(String ssousername, String password){
		this.ssousername=ssousername;
		this.password=password;
	}
	
	/**
	 * 获取目标页面内容
	 * @param url
	 * @param reqhost
	 * @return
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	public String getOraUrlContent(String url, String reqhost) throws IllegalStateException, IOException{
		CloseableHttpClient httpclient = HttpClientUtils.createSSLClientDefault();
		String redirectUrl;
		redirectUrl=requestBeforeSSO(httpclient, url, false);
		loadSSOpage(httpclient, redirectUrl, false);
		redirectUrl=realSSOLogin(httpclient, false);
		return retrieveRequestPage(httpclient, redirectUrl, reqhost, false);
	}

	/**
	 * call this method to get wifi password directly
	 * @param url
	 * @param reqhost
	 * @return
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	public String parseWifiPassword(String url, String reqhost) throws IllegalStateException, IOException {
		String htmlcont=getOraUrlContent(url, reqhost);
		return parseWifiPassword(htmlcont);
	}

	private String parseWifiPassword(String htmlcont) throws IOException{
		BufferedReader br=new BufferedReader(new StringReader(htmlcont));

		String line;
		while ((line=br.readLine())!=null){
			if (line.contains("Password")){
				line=line.trim();
				line=line.substring("Password:".length());
				line=line.trim();
				break;
			}
		}

		return line;
	}

	/**
	 * SSO重定向之前的请求
	 * 该请求的response将包含进入SSO的前提信息
	 * @param httpclient
	 * @return
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	private String requestBeforeSSO(CloseableHttpClient httpclient, String url, boolean debugFlag) throws IllegalStateException, IOException{
		String location=null;

		if (debugFlag)
			System.out.println("---- request before SSO ----");

		HttpGet req = new HttpGet(url);

		//禁止重定向
		req.setConfig(RequestConfig .custom().setRedirectsEnabled(false).build());

		HttpClientUtils.simulateBrower(req);

		CloseableHttpResponse resp = httpclient.execute(req);

		Header header = resp.getFirstHeader("Location");
		if (header != null)
			location=header.getValue();

		HttpEntity entity = resp.getEntity();

		if (debugFlag)
			HttpClientUtils.printDebugInfo(resp, entity);

		EntityUtils.consume(entity);

		resp.close();

		if (debugFlag)
			System.out.println("---- step1 done ----");

		return location;
	}

	/**
	 * 加载SSO页面，传入上级跳转url
	 *
	 * 加载出来的页面发现是一段功能并不完善的页面，
	 * 但是里面的input 参数是接下来成功登录的重要信息
	 * 正式执行时，务必设置debugFlag=FALSE
	 *
	 * @param httpclient
	 * @param preloc
	 * @param debugFlag
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	private String loadSSOpage(CloseableHttpClient httpclient, String preloc, boolean debugFlag) throws ClientProtocolException, IOException{
		String location=null;

		if (debugFlag)
			System.out.println("---- in SSO ----");


		HttpGet req = new HttpGet(preloc);
		//禁止重定向
		req.setConfig(RequestConfig.custom().setRedirectsEnabled(false).build());

		HttpClientUtils.simulateBrower(req);

		//System.out.println("--set cookie : "+cookie + "---");

		//req.setHeader("Cookie", cookie);
		req.setHeader("Host", "login.oracle.com");
		req.setHeader("Referer", preloc);

		CloseableHttpResponse resp = httpclient.execute(req);

		Header header = resp.getFirstHeader("Location");
		if (header != null)
			location=header.getValue();

		HttpEntity entity = resp.getEntity();

		if (debugFlag)
			HttpClientUtils.printDebugInfo(resp, entity);
		else
			ssoparam=parseSSOlogon(entity, debugFlag);

		EntityUtils.consume(entity);

		resp.close();

		if (debugFlag)
			System.out.println("---- step2 done ----");

		return location;
	}

	//解析SSO登录页面
	private Map<String, String> parseSSOlogon(HttpEntity entity, boolean debugFlag) throws IllegalStateException, IOException{
		if (debugFlag)
			System.out.println("Parsing params---");

		Map<String, String> parammap=new HashMap<String, String>();

		String line=HttpClientUtils.htmlContent(entity);

		if (debugFlag)
			System.out.println(line);

		//起始点
		int startindex=line.indexOf("type=\"hidden\" name=");

		while (true){
			int nameindex=line.indexOf("name", startindex);

			int valueindex=line.indexOf("value",startindex);

			if (nameindex==-1 || valueindex==-1)
				break;

			int nameend=line.indexOf("\"", nameindex+6);
			int valueend=line.indexOf("\"", valueindex+7);
			String name=line.substring(nameindex+6, nameend);
			String value=line.substring(valueindex+7, valueend);

			if (debugFlag)
				System.out.println(name + " --> "+value);

			parammap.put(name, value);

			startindex=nameend>valueend?nameend:valueend;

		}

		return parammap;
	}

	/**
	 * 实验性第一次请求SSO log in
	 * 直接把上一步的表单提交，
	 * 虽然提交失败，因为没用户名和密码，
	 * 但是此时返回的页面保留了实际登录所需的信息
	 *
	private String loadSSOstep3(CloseableHttpClient httpclient) throws ClientProtocolException, IOException{
		String location=null;

		System.out.println("---- in SSO first request----");


		HttpPost req = new HttpPost("https://login.oracle.com/mysso/signon.jsp");
		//禁止重定向
		req.setConfig(RequestConfig .custom().setRedirectsEnabled(false).build());

		HttpClientUtils.simulateBrower(req);

		//System.out.println("--set cookie : "+cookie + "---");

		//req.setHeader("Cookie", cookie);
		req.setHeader("Host", "login.oracle.com");
		//req.setHeader("Referer", preloc);

		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		for (Map.Entry<String, String> entry : ssoparam.entrySet()){
			nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}
		req.setEntity(new UrlEncodedFormEntity(nvps));

		CloseableHttpResponse resp = httpclient.execute(req);

		Header header = resp.getFirstHeader("Location");
		if (header != null)
			location=header.getValue();

		HttpEntity entity = resp.getEntity();

		HttpClientUtils.printDebugInfo(resp, entity);
		//ssoparam=parseSSOlogon(entity);

		EntityUtils.consume(entity);

		resp.close();

		System.out.println("---- step3 done ----");

		return location;
	}*/

	/**
	 * 这步开始正式login
	 * 这步最重要的是要保存cookie信息
	 *
	 * @param httpclient
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	private String realSSOLogin(CloseableHttpClient httpclient, boolean debugFlag) throws ClientProtocolException, IOException{
		if (this.ssousername==null || this.password==null)
			throw new ClientProtocolException("you must provide sso username and password!");
		
		String location=null;

		if (debugFlag)
			System.out.println("---- in SSO first request----");


		HttpPost req = new HttpPost("https://login.oracle.com/oam/server/sso/auth_cred_submit");
		//禁止重定向
		req.setConfig(RequestConfig .custom().setRedirectsEnabled(false).build());

		HttpClientUtils.simulateBrower(req);

		//System.out.println("--set cookie : "+cookie + "---");

		//req.setHeader("Cookie", cookie);
		req.setHeader("Host", "login.oracle.com");
		req.setHeader("Accept-Encoding","gzip, deflate");
		req.setHeader("Content-Type","application/x-www-form-urlencoded");
		//req.setHeader("Referer", preloc);

		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		for (Map.Entry<String, String> entry : ssoparam.entrySet()){
			if (entry.getKey().equalsIgnoreCase("v") ||
					entry.getKey().equalsIgnoreCase("request_id") ||
					entry.getKey().equalsIgnoreCase("OAM_REQ") ||
					entry.getKey().equalsIgnoreCase("site2pstoretoken") ||
					entry.getKey().equalsIgnoreCase("locale"))
				nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}

		nvps.add(new BasicNameValuePair("ssousername", this.ssousername));
		nvps.add(new BasicNameValuePair("password", this.password));

		req.setEntity(new UrlEncodedFormEntity(nvps));

		CloseableHttpResponse resp = httpclient.execute(req);

		Header header = resp.getFirstHeader("Location");
		if (header != null)
			location=header.getValue();

		HttpEntity entity = resp.getEntity();

		if (debugFlag)
			HttpClientUtils.printDebugInfo(resp, entity);

		EntityUtils.consume(entity);

		resp.close();

		if (debugFlag)
			System.out.println("---- step3 done ----");

		return location;
	}

	/**
	 * 这步进入到登录成功之后页面
	 * 转发后有可能又是一个重定向页面
	 * 而且包含一个新的cookie信息
	 *
	 * 此时去掉重定向限制，直接获取目标页面
	 */
	private String retrieveRequestPage(CloseableHttpClient httpclient, String preloc, String requestHost, boolean debugFlag) throws IOException{
		String htmlcont=null;

		if (debugFlag)
			System.out.println("---- go to target page ----");


		HttpGet req = new HttpGet(preloc);
		//禁止重定向
		//req.setConfig(RequestConfig .custom().setRedirectsEnabled(false).build());

		HttpClientUtils.simulateBrower(req);

		//System.out.println("--set cookie : "+cookie + "---");

		//req.setHeader("Cookie", cookie);
		req.setHeader("Host", requestHost);
		req.setHeader("Referer", "https://login.oracle.com/mysso/signon.jsp");

		CloseableHttpResponse resp = httpclient.execute(req);

		HttpEntity entity = resp.getEntity();

		htmlcont=HttpClientUtils.htmlContent(entity);

		EntityUtils.consume(entity);

		resp.close();

		if (debugFlag)
			System.out.println("---- final step done ----");

		return htmlcont;
	}
}
