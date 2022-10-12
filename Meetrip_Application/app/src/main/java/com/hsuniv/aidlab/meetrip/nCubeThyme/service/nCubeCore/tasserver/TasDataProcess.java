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

package com.hsuniv.aidlab.meetrip.nCubeThyme.service.nCubeCore.tasserver;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;

import mn.mobile.mugi.dailylog.nCubeThyme.service.nCubeCore.httpclient.HttpClientRequest;
import mn.mobile.mugi.dailylog.nCubeThyme.service.nCubeCore.resource.AE;
import mn.mobile.mugi.dailylog.nCubeThyme.service.nCubeCore.resource.CSEBase;
import mn.mobile.mugi.dailylog.nCubeThyme.service.nCubeCore.resource.Container;
import mn.mobile.mugi.dailylog.nCubeThyme.service.nCubeCore.resource.ResourceRepository;

/**
 * class for TAS data processing extends Thread
 * @author NakMyoung Sung (nmsung@keti.re.kr)
 *
 */
public class TasDataProcess extends Thread
{
	private String containerName;
	private String content;
	private String contentInfo;
	private boolean containerMatch;
	private CSEBase cse;
	private AE ae;
	private Container container;
	private ArrayList<Container> containers;
	private Socket tasSocket;

	public TasDataProcess(Socket tasSocket)
    {
        this.containerName = "";
        this.content = "";
        this.contentInfo = "";
        this.containerMatch = false;
        this.cse = ResourceRepository.getCSEInfo();
        this.ae = ResourceRepository.getAEInfo();
        this.containers = ResourceRepository.getContainersInfo();
        this.tasSocket = tasSocket;
	}

	public TasDataProcess(){}


	/**
	 * findContainer Method
	 * Matching the container name in repository
	 * @return
	 */
	private boolean findContainer() {
		Container tempContainer;
		boolean match = false;
		
		for (int i = 0; i < containers.size(); i++) {
			tempContainer = containers.get(i);
			if (containerName.equals(tempContainer.ctname)) {
				container = tempContainer;
				match = true;
			}
		}
		
		return match;
	}
	
	/**
	 * replaceContainer Method
	 * Replace the container in repository
	 * @return
	 */
	private void replaceContainer(Container replace) {
		Container tempContainer;
		
		for (int i = 0; i < containers.size(); i++) {
			tempContainer = containers.get(i);
			if (replace.ctname.equals(tempContainer.ctname)) {
				containers.set(i, replace);
				ResourceRepository.setContainersInfo(containers);
			}
		}
	}

	/**
	 * Reading outputStream from InputStream and create new String from outputStream
	 * @param inputStream
	 * @return
	 * @throws IOException
	 */
	private String extract(InputStream inputStream) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int read = 0;
		while ((read = inputStream.read(buffer, 0, buffer.length)) != -1) {
			baos.write(buffer, 0, read);
		}
		baos.flush();
		return  new String(baos.toByteArray(), "UTF-8");
	}

	/**
	 * run Method
	 * Receive the TAS message and processing
	 */
	public void run()
	{
		String receiveDataString;
		while(true)
		{
			try {
				/**
				 * get InputStream from client socket and extract String object from inputStream
				 */
                receiveDataString = extract(tasSocket.getInputStream());;
                if(receiveDataString.isEmpty()) {
                    try {
                        tasSocket.close();
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    Log.d("TasDataProcess", "input empty: " + receiveDataString);
                }else {
                    Log.d("TasDataProcess", "input data: " + receiveDataString);

                    /**
                     * Create JSONObject from received data string
                     */
                    JSONObject jsonObj;
                    jsonObj = new JSONObject(receiveDataString);

                    /**
                     * Create JSONArray from JSONObject by key
                     */
                    JSONArray jsonArray = jsonObj.getJSONArray("jsonArray");

                    if (jsonArray != null) {
                        /**
                         * Iterate over all JSONArray
                         */
                        Integer total = jsonArray.length();

                        for (int i = 0; i < total; i++) {
                            /**
                             * get JSONObject from JSONArray index at [i]
                             */
                            JSONObject object = jsonArray.getJSONObject(i);

                            /**
                             * get String object from JSONObject by key
                             */
                            containerName = object.getString("ctname");
                            try {
                                content = object.getString("con");
                            } catch (JSONException e) {
                                content = object.getJSONObject("con").toString();
                            }

                            /**
                             * find container exist on Tas server
                             */
                            containerMatch = findContainer();

                            /**
                             * send data to the server by using Socket connection if container exists
                             */
                            if (containerMatch) {
                                if (content.equals("hello")) {
                                    container.tasSocket = tasSocket;
                                    replaceContainer(container);
                                    Log.d("[&CubeThyme]", "TAS registration success\n");
                                    TasSender.sendMessage(container.ctname, "hello");
                                } else {
                                    HttpClientRequest.contentInstanceCreateRequest(cse, ae, container, content, contentInfo);
                                }
                            } else {
                                Log.d("[&CubeThyme]", "Container is not matched");
                            }
                        }
                    }
                }
			} catch (IOException e) {
				// TODO Auto-generated catch block
				try {
					Log.d("[&CubeThyme]"," TAS connection is closed");
					tasSocket.close();
					break;
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				System.out.println("[&CubeThyme] Do not support the message type\n");
				e.printStackTrace();
			} catch (Exception e) {
				Log.d("[&CubeThyme]"," TAS connection is closed");
				try {
					tasSocket.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				break;
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		}
	}
}