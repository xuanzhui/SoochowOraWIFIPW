package com.xz.net;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class HttpClientUtils {

	public static CloseableHttpClient createSSLClientDefault() {
		try {
			SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(
					null, new TrustStrategy() {
						// 信任所有
						public boolean isTrusted(X509Certificate[] chain,
												 String authType) throws CertificateException {
							return true;
						}
					}).build();
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
					sslContext);
			return HttpClients.custom().setSSLSocketFactory(sslsf).build();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		}
		return HttpClients.createDefault();
	}

	public static void simulateBrower(HttpRequestBase req){
		req.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:34.0) Gecko/20100101 Firefox/34.0");
		req.setHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		req.setHeader("Accept-Language","en-us,zh-cn;q=0.8,zh;q=0.5,en;q=0.3");
		req.setHeader("Accept-Encoding","gzip, deflate");
		req.setHeader("Content-Type","application/x-www-form-urlencoded");
		req.setHeader("Connection","keep-alive");
	}

	public static void printDebugInfo(HttpResponse resp, HttpEntity entity) throws IllegalStateException, IOException{

		System.out.println("page status line : "
				+ resp.getStatusLine());
		System.out.println("page status code : "
				+ resp.getStatusLine().getStatusCode());

		Header[] headers=resp.getAllHeaders();

		System.out.println("--all headers info --");
		for (Header h : headers){
			System.out.println(h.getName() + " --> "+h.getValue());
		}

		System.out.println(htmlContent(entity));

	}

	//返回response html 文本信息
	public static String htmlContent(HttpEntity entity) throws IllegalStateException, IOException{
		InputStream instream;
		BufferedReader br;
		String line;

		if (entity == null)
			return null;

		instream = entity.getContent();
		br = new BufferedReader(new InputStreamReader(
				new BufferedInputStream(instream)));

		StringBuilder sb=new StringBuilder();

		while ((line = br.readLine()) != null)
			sb.append(line+"\n");

		return sb.toString();
	}

	/**
	 * cookiemap为传入的已有cookie
	 * @param httpResponse
	 * @param cookiemap
	 * @return
	 */
	public static String keepCookies(HttpResponse httpResponse, Map<String, String> cookiemap){

		Header[] headers = httpResponse.getHeaders("Set-Cookie");

		StringBuilder buff=new StringBuilder();
		for (Header header : headers) {
			String cookieline=header.getValue();
			if (header.getValue().startsWith("BIGipServergmp_oracle_com_http") ||
					header.getValue().startsWith("ORASSO_AUTH_HINT") ||
					header.getValue().startsWith("ORA_UCM_INFO") ||
					header.getValue().startsWith("OAM_ID"))

				cookiemap.put(cookieline.substring(0, cookieline.indexOf('=')), cookieline.substring(cookieline.indexOf('=')+1, cookieline.indexOf(';')));
		}

		for (Map.Entry<String, String> entry : cookiemap.entrySet()){
			buff.append(entry.getKey()+"="+entry.getValue()+"; ");
		}

		return buff.toString();
	}
}
