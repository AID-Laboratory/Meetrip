/*
 * ------------------------------------------------------------------------
 * Copyright 2014 Korea Electronics Technology Institute
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ------------------------------------------------------------------------
 */

package com.hsuniv.aidlab.meetrip.nCubeThyme.service.nCubeCore.httpclient;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import com.hsuniv.aidlab.meetrip.nCubeThyme.service.nCubeCore.resource.AE;
import com.hsuniv.aidlab.meetrip.nCubeThyme.service.nCubeCore.resource.CSEBase;
import com.hsuniv.aidlab.meetrip.nCubeThyme.service.nCubeCore.resource.Container;
import com.hsuniv.aidlab.meetrip.nCubeThyme.service.nCubeCore.resource.Subscription;

/**
 * Class for sending the HTTP request to Mobius 
 * @author NakMyoung Sung (nmsung@keti.re.kr)
 *
 */
public class HttpClientRequest {
	
	private static int requestId = 0;
	
	/**
	 * aeCreateRequest Method
	 * Send to Mobius for oneM2M AE resource create
	 * @param cse
	 * @param ae
	 * @return
	 * @throws Exception
	 */
	public static int aeCreateRequest(CSEBase cse, AE ae) throws Exception {

		System.out.println("[&CubeThyme] AE \"" + ae.appName + "\" create request.......");

		String requestBody =
				"<m2m:ae\n" +
						"xmlns:m2m=\"http://www.onem2m.org/xml/protocols\"\n" +
						"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" rn = \"" + ae.appName + "\">\n" +
						"<api>" + ae.appId + "</api>\n" +
						"<lbl>" + ae.label + "</lbl>\n" +
						"<rr>true</rr>\n" +
						"<poa>" + ae.pointOfAccess + "</poa>\n" +
				"</m2m:ae>";

		StringEntity entity = new StringEntity(
						new String(requestBody.getBytes()));

		URI uri = URIUtils.createURI("http", cse.CSEHostAddress, Integer.parseInt(cse.CSEPort), "/" + cse.CSEName, null, null);

		HttpPost post = new HttpPost(uri);
				post.setHeader("Content-Type", "application/vnd.onem2m-res+xml;ty=2");
				post.setHeader("Accept", "application/xml");
				post.setHeader("locale", "ko");
				post.setHeader("X-M2M-Origin", "S");
				post.setHeader("X-M2M-RI", Integer.toString(requestId++));
				post.setEntity(entity);

        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setLinger(httpParams, 5);
        HttpClient httpClient = new DefaultHttpClient(httpParams);

        HttpResponse response = httpClient.execute(post);

        HttpEntity responseEntity = response.getEntity();

        String responseString = EntityUtils.toString(responseEntity);

        int responseCode = response.getStatusLine().getStatusCode();

        Log.d("[&CubeThyme]", "HTTP Response Code : " + responseCode);
        Log.d("[&CubeThyme]", "HTTP Response String : " + responseString);

        responseEntity.consumeContent();
        httpClient.getConnectionManager().closeExpiredConnections();
        httpClient.getConnectionManager().closeIdleConnections(0, TimeUnit.MILLISECONDS);
        httpClient.getConnectionManager().shutdown();
        post.abort();

		String aei = HttpClientResponseParser.aeCreateParse(responseString);

		ae.aeId = aei;

		return responseCode;
	}

	public static int aeRetrieveRequest(CSEBase cse, AE ae) throws Exception {

		System.out.println("[&CubeThyme] AE \"" + ae.appName + "\" retrieve request.......");

		URI uri = URIUtils.createURI("http", cse.CSEHostAddress, Integer.parseInt(cse.CSEPort), "/" + cse.CSEName + "/" + ae.appName, null, null);

		HttpGet get = new HttpGet(uri);
				get.setHeader("Content-Type", "application/vnd.onem2m-res+xml;ty=2");
				get.setHeader("Accept", "application/xml");
				get.setHeader("locale", "ko");
				get.setHeader("X-M2M-Origin", "S");
				get.setHeader("X-M2M-RI", Integer.toString(requestId++));

		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setLinger(httpParams, 5);
		HttpClient httpClient = new DefaultHttpClient(httpParams);

		HttpResponse response = httpClient.execute(get);

		HttpEntity responseEntity = response.getEntity();

		String responseString = EntityUtils.toString(responseEntity);

		int responseCode = response.getStatusLine().getStatusCode();

		Log.d("[&CubeThyme]", "HTTP Response Code : " + responseCode);
		Log.d("[&CubeThyme]", "HTTP Response String : " + responseString);

		responseEntity.consumeContent();
		httpClient.getConnectionManager().closeExpiredConnections();
		httpClient.getConnectionManager().closeIdleConnections(0, TimeUnit.MILLISECONDS);
		httpClient.getConnectionManager().shutdown();
		get.abort();

		String aei = HttpClientResponseParser.aeCreateParse(responseString);

		ae.aeId = aei;

		return responseCode;
	}

	/**
	 * containerCreateRequest Method
	 * Send to Mobius for oneM2M container resource create
	 * @param cse
	 * @param ae
	 * @param container
	 * @return
	 * @throws Exception
	 */
	public static int containerCreateRequest(CSEBase cse, AE ae, Container container) throws Exception {

		System.out.println("[&CubeThyme] Container \"" + container.ctname + "\" create request.......");

		String requestBody =
				"<m2m:cnt\n" +
						"xmlns:m2m=\"http://www.onem2m.org/xml/protocols\"\n" +
						"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" rn = \"" + container.ctname + "\">\n" +
						"<lbl>" + container.label + "</lbl>\n" +
				"</m2m:cnt>";

		StringEntity entity = new StringEntity(
						new String(requestBody.getBytes()));

        URI uri = URIUtils.createURI("http", cse.CSEHostAddress, Integer.parseInt(cse.CSEPort), "/" + cse.CSEName + container.parentpath, null, null);

		HttpPost post = new HttpPost(uri);
				post.setHeader("Content-Type", "application/vnd.onem2m-res+xml;ty=3");
	        post.setHeader("Accept", "application/xml");
        post.setHeader("locale", "ko");
        post.setHeader("X-M2M-Origin", ae.aeId);
        post.setHeader("X-M2M-RI", "nCubeThyme" + Integer.toString(requestId++));
        post.setHeader("X-M2M-NM", container.ctname);
        post.setEntity(entity);

        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setLinger(httpParams, 5);
        HttpClient httpClient = new DefaultHttpClient(httpParams);

        HttpResponse response = httpClient.execute(post);

        HttpEntity responseEntity = response.getEntity();

        String responseString = EntityUtils.toString(responseEntity);

        int responseCode = response.getStatusLine().getStatusCode();

        Log.d("[&CubeThyme]", "HTTP Response Code : " + responseCode);
        Log.d("[&CubeThyme]", "HTTP Response String : " + responseString);

        responseEntity.consumeContent();
        httpClient.getConnectionManager().closeExpiredConnections();
        httpClient.getConnectionManager().closeIdleConnections(0, TimeUnit.MILLISECONDS);
        httpClient.getConnectionManager().shutdown();
        post.abort();

		return responseCode;
	}

	/**
	 * subscriptionCreateRequest Method
	 * Send to Mobius for oneM2M subscription resource create
	 * @param cse
	 * @param ae
	 * @param subscription
	 * @return
	 * @throws Exception
	 */
	public static int subscriptionCreateRequest(CSEBase cse, AE ae, Subscription subscription) throws Exception {

		System.out.println("[&CubeThyme] Subscription \"" + subscription.subname + "\" create request.......");

		if (subscription.useMQTT) {
			subscription.nu = subscription.nu + "/" + ae.aeId;
		}

		String requestBody =
				"<m2m:sub\n" +
						"xmlns:m2m=\"http://www.onem2m.org/xml/protocols\"\n" +
						"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" rn = \"" + subscription.subname + "\">\n" +
						"<enc>" +
						"<net>3</net>" +
						"</enc>" +
						"<nu>" + subscription.nu + "?ct=xml" + "</nu>" +
						"<nct>2</nct>" +
				"</m2m:sub>";

		StringEntity entity = new StringEntity( new String(requestBody.getBytes()));

        URI uri = URIUtils.createURI("http", cse.CSEHostAddress, Integer.parseInt(cse.CSEPort), "/" + cse.CSEName + subscription.parentpath, null, null);

		HttpPost post = new HttpPost(uri);
				post.setHeader("Content-Type", "application/vnd.onem2m-res+xml;ty=23");
				post.setHeader("Accept", "application/xml");
				post.setHeader("locale", "ko");
				post.setHeader("X-M2M-Origin", ae.aeId);
				post.setHeader("X-M2M-RI", "nCubeThyme" + Integer.toString(requestId++));
				post.setHeader("X-M2M-NM", subscription.subname);
				post.setEntity(entity);

        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setLinger(httpParams, 5);
        HttpClient httpClient = new DefaultHttpClient(httpParams);

        HttpResponse response = httpClient.execute(post);

        HttpEntity responseEntity = response.getEntity();

        String responseString = EntityUtils.toString(responseEntity);

        int responseCode = response.getStatusLine().getStatusCode();

        Log.d("[&CubeThyme]", "HTTP Response Code : " + responseCode);
        Log.d("[&CubeThyme]", "HTTP Response String : " + responseString);

        responseEntity.consumeContent();
        httpClient.getConnectionManager().closeExpiredConnections();
        httpClient.getConnectionManager().closeIdleConnections(0, TimeUnit.MILLISECONDS);
        httpClient.getConnectionManager().shutdown();
        post.abort();

		return responseCode;
	}

	public static int subscriptionDeleteRequest(CSEBase cse, AE ae, Subscription subscription) throws Exception {

		System.out.println("[&CubeThyme] Subscription \"" + subscription.subname + "\" delete request.......");

		URI uri = URIUtils.createURI("http", cse.CSEHostAddress, Integer.parseInt(cse.CSEPort), "/" + cse.CSEName + subscription.parentpath + "/" + subscription.subname, null, null);

		HttpDelete delete = new HttpDelete(uri);
					delete.setHeader("Accept", "application/xml");
					delete.setHeader("locale", "ko");
					delete.setHeader("X-M2M-Origin", ae.aeId);
					delete.setHeader("X-M2M-RI", "nCubeThyme" + Integer.toString(requestId++));

		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setLinger(httpParams, 5);
		HttpClient httpClient = new DefaultHttpClient(httpParams);

		HttpResponse response = httpClient.execute(delete);

		HttpEntity responseEntity = response.getEntity();

		String responseString = EntityUtils.toString(responseEntity);

		int responseCode = response.getStatusLine().getStatusCode();

		Log.d("[&CubeThyme]", "HTTP Response Code : " + responseCode);
		Log.d("[&CubeThyme]", "HTTP Response String : " + responseString);

		responseEntity.consumeContent();
		httpClient.getConnectionManager().closeExpiredConnections();
		httpClient.getConnectionManager().closeIdleConnections(0, TimeUnit.MILLISECONDS);
		httpClient.getConnectionManager().shutdown();
		delete.abort();

		return responseCode;
	}

	/**
	 * contentInstanceCreateRequest Method
	 * Send to Mobius for oneM2M contentInstance resource create
	 * @param cse
	 * @param ae
	 * @param container
	 * @param content
	 * @param contentInfo
	 * @return
	 * @throws Exception
	 */
	public static int contentInstanceCreateRequest(CSEBase cse, AE ae, Container container, String content, String contentInfo) throws Exception {

		System.out.println("[&CubeThyme] \"" + container.ctname + "\"'s contentInstance create request.......");

		String requestBody =
				"<m2m:cin\n" +
						"xmlns:m2m=\"http://www.onem2m.org/xml/protocols\"\n" +
						"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
						"<cnf>" + contentInfo + "</cnf>\n" +
						"<con>" + content + "</con>\n" +
				"</m2m:cin>";

		StringEntity entity = new StringEntity(
						new String(requestBody.getBytes()));

        URI uri = URIUtils.createURI("http", cse.CSEHostAddress, Integer.parseInt(cse.CSEPort), "/" + cse.CSEName + container.parentpath + "/" + container.ctname, null, null);

		HttpPost post = new HttpPost(uri);
				post.setHeader("Content-Type", "application/vnd.onem2m-res+xml;ty=4");
				post.setHeader("Accept", "application/xml");
				post.setHeader("locale", "ko");
				post.setHeader("X-M2M-Origin", ae.aeId);
				post.setHeader("X-M2M-RI", "nCubeThyme" + Integer.toString(requestId++));
				post.setEntity(entity);

        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setLinger(httpParams, 5);
        HttpClient httpClient = new DefaultHttpClient(httpParams);

        HttpResponse response = httpClient.execute(post);

        HttpEntity responseEntity = response.getEntity();

        String responseString = EntityUtils.toString(responseEntity);

        int responseCode = response.getStatusLine().getStatusCode();

        Log.d("[&CubeThyme]", "HTTP Response Code : " + responseCode);
        Log.d("[&CubeThyme]", "HTTP Response String : " + responseString);

        responseEntity.consumeContent();
        httpClient.getConnectionManager().closeExpiredConnections();
        httpClient.getConnectionManager().closeIdleConnections(0, TimeUnit.MILLISECONDS);
        httpClient.getConnectionManager().shutdown();
        post.abort();

		return responseCode;
	}
}