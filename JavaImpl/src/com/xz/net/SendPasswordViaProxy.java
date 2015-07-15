package com.xz.net;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class SendPasswordViaProxy {

	/**
	 *
	 * @param server
	 * @param proxyhost
	 * @param proxyport
	 * @param proxytype like "http"
	 * @param debugFlag
	 * @return
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public int sendPassword(String server, String proxyhost, int proxyport, String proxytype, boolean debugFlag) throws ClientProtocolException, IOException {
		int retCode=-1;

		CloseableHttpClient httpclient = HttpClients.createDefault();

		// 依次是代理地址，代理端口号，协议类型
		//HttpHost proxy = new HttpHost("cn-proxy.sg.oracle.com", 80, "http");
		HttpHost proxy = new HttpHost(proxyhost, proxyport, proxytype);
		RequestConfig config = RequestConfig.custom().setProxy(proxy).build();

		HttpGet req = new HttpGet(server);
		req.setConfig(config);

		HttpClientUtils.simulateBrower(req);

		CloseableHttpResponse resp = httpclient.execute(req);

		retCode=resp.getStatusLine().getStatusCode();

		HttpEntity entity = resp.getEntity();

		if (debugFlag)
			HttpClientUtils.printDebugInfo(resp, entity);

		EntityUtils.consume(entity);

		resp.close();

		return retCode;
	}

}
