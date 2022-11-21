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

package com.hsu.aidlab.meetrip.nCubeThyme.service.nCubeCore;

import android.util.Log;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import com.hsu.aidlab.meetrip.nCubeThyme.service.nCubeCore.httpserver.HttpServerHandler;
import com.hsu.aidlab.meetrip.nCubeThyme.service.nCubeCore.httpserver.HttpServerTestHandler;
import com.hsu.aidlab.meetrip.nCubeThyme.service.nCubeCore.mqttclient.MqttClientKetiPub;
import com.hsu.aidlab.meetrip.nCubeThyme.service.nCubeCore.mqttclient.MqttClientKetiSub;
import com.hsu.aidlab.meetrip.nCubeThyme.service.nCubeCore.resource.AE;
import com.hsu.aidlab.meetrip.nCubeThyme.service.nCubeCore.resource.CSEBase;
import com.hsu.aidlab.meetrip.nCubeThyme.service.nCubeCore.resource.Container;
import com.hsu.aidlab.meetrip.nCubeThyme.service.nCubeCore.resource.ResourceRepository;
import com.hsu.aidlab.meetrip.nCubeThyme.service.nCubeCore.resource.Subscription;
import com.hsu.aidlab.meetrip.nCubeThyme.service.nCubeCore.tasserver.TasServer;

/**
 * Main Class
 * @author NakMyoung Sung (nmsung@keti.re.kr)
 */
public class ThymeMain extends Thread {

	private static CSEBase hostingCSE = new CSEBase();
	private static AE hostingAE = new AE();
	private static ArrayList<Container> containers = new ArrayList<Container>();
	private static ArrayList<Subscription> subscriptions = new ArrayList<Subscription>();
	private static InetAddress ip;
	private static String configData;

	private static boolean windows = true;

	public static MqttClientKetiSub requestClient;
	public static MqttClientKetiSub responseClient;
	public static MqttClientKetiPub publishClient;

    public ThymeMain(String configXML) {
        this.configData = configXML;
        Log.d("[&CubeThyme]", "Thyme service start...");
    }

    public void run() {
        try
        {
            Log.d("conf", configData);
            runThyme();
        } catch (Exception e) {
        	Log.d("runThyme", e.toString());
            e.printStackTrace();
        }
    }

	/**
	 * configurationFileLoader Method
	 * Load the XML profile named 'thyme_conf.xml' from local storage for create the AE and container resources.
	 * @throws Exception
	 */
	private static void configurationFileLoader() throws Exception {

		System.out.println("[&CubeThyme] Configuration file loading...");

		// &CubeThyme configuration file open and build xml document
        //File file = new File(configData);
		JSONObject conf = new JSONObject(configData);

		JSONObject cseObj = conf.getJSONObject("cse");
		hostingCSE.CSEHostAddress = cseObj.getString("cbhost");
		System.out.println("[&CubeThyme] CSE - cbhost : " + hostingCSE.CSEHostAddress);
		hostingCSE.CSEPort = cseObj.getString("cbport");
		System.out.println("[&CubeThyme] CSE - cbport : " + hostingCSE.CSEPort);
		hostingCSE.CSEName = cseObj.getString("cbname");
		System.out.println("[&CubeThyme] CSE - cbname : " + hostingCSE.CSEName);
		hostingCSE.CSEId = cseObj.getString("cbcseid");
		System.out.println("[&CubeThyme] CSE - cbcseid : " + hostingCSE.CSEId);
		hostingCSE.mqttPort = cseObj.getString("mqttport");
		System.out.println("[&CubeThyme] CSE - mqttPort : " + hostingCSE.mqttPort);
		ResourceRepository.setCSEBaseInfo(hostingCSE);

		JSONObject aeObj = conf.getJSONObject("ae");
		hostingAE.aeId = aeObj.getString("aeid");
		System.out.println("[&CubeThyme] AE - aeId : " + hostingAE.aeId);
		hostingAE.appId = aeObj.getString("appid");
		System.out.println("[&CubeThyme] AE - appid : " + hostingAE.appId);
		hostingAE.appName = aeObj.getString("appname");
		System.out.println("[&CubeThyme] AE - appname : " + hostingAE.appName);
		hostingAE.appPort = aeObj.getString("appport");
		System.out.println("[&CubeThyme] AE - appport : " + hostingAE.appPort);
		hostingAE.bodyType = aeObj.getString("bodytype");
		System.out.println("[&CubeThyme] AE - bodytype : " + hostingAE.bodyType);
		hostingAE.tasPort = aeObj.getString("tasport");
		System.out.println("[&CubeThyme] AE - tasport : " + hostingAE.tasPort);
		ResourceRepository.setAEInfo(hostingAE);

		JSONArray cntArr = conf.getJSONArray("cnt");
		for (int i = 0; i < cntArr.length(); i++) {
			Container tempContainer = new Container();

			tempContainer.parentpath = cntArr.getJSONObject(i).getString("parentpath");
			System.out.println("[&CubeThyme] Container - parentpath : " + tempContainer.parentpath);
			tempContainer.ctname = cntArr.getJSONObject(i).getString("ctname");
			System.out.println("[&CubeThyme] Container - ctname : " + tempContainer.ctname);

			containers.add(tempContainer);
		}
		ResourceRepository.setContainersInfo(containers);

		JSONArray subArr = conf.getJSONArray("sub");
		for (int i = 0; i < subArr.length(); i++) {
			Subscription tempSubscription = new Subscription();

			tempSubscription.parentpath = subArr.getJSONObject(i).getString("parentpath");
			System.out.println("[&CubeThyme] Subscription - parentpath : " + tempSubscription.parentpath);
			tempSubscription.subname = subArr.getJSONObject(i).getString("subname");
			System.out.println("[&CubeThyme] Subscription - subname : " + tempSubscription.subname);
			tempSubscription.nu = subArr.getJSONObject(i).getString("nu");
			System.out.println("[&CubeThyme] Subscription - nu : " + tempSubscription.nu);

			subscriptions.add(tempSubscription);
		}
		ResourceRepository.setSubscriptionsInfo(subscriptions);
	}

	/**
	 * main Method
	 * @throws Exception
	 */
	public static void runThyme() throws Exception {

		System.out.println("[&CubeThyme] &CubeThyme SW start.......\n");

		// Load the &CubeThyme configuration file
		configurationFileLoader();

		// Initialize the HTTP server for receiving the notification messages
		System.out.println("[&CubeThyme] &CubeThyme initialize.......\n");

		if (windows) {
			HttpServer server = HttpServer.create(new InetSocketAddress(Integer.parseInt(hostingAE.appPort)), 0);
			server.createContext("/", new HttpServerTestHandler()); // HTTP server test url
			server.createContext("/notification", new HttpServerHandler()); // oneM2M notification url
			server.setExecutor(null); // creates a default executor
			server.start();
		}
		else {
			InetSocketAddress serverSocketAddress = new InetSocketAddress(ip.getHostAddress(), Integer.parseInt(hostingAE.appPort));
			HttpServer server = HttpServer.create(serverSocketAddress, 0);
			server.createContext("/", new HttpServerTestHandler()); // HTTP server test url
			server.createContext("/notification", new HttpServerHandler()); // oneM2M notification url
			server.setExecutor(null); // creates a default executor
			server.start();
		}

		// Registration sequence
		Registration regi = new Registration();
		regi.registrationStart();

		// TAS server start
		Thread tasServer = new TasServer(Integer.parseInt(hostingAE.tasPort));
        //System.out.println("mugi\n");
        Log.d("bindService", "tas start");
		tasServer.start();
	}
}