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

package com.hsu.aidlab.meetrip.nCubeThyme.service.nCubeCore.tasserver;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import com.hsu.aidlab.meetrip.nCubeThyme.service.nCubeCore.resource.Container;
import com.hsu.aidlab.meetrip.nCubeThyme.service.nCubeCore.resource.ResourceRepository;

/**
 * class for send the control message to TAS 
 * @author NakMyoung Sung (nmsung@keti.re.kr)
 *
 */
public class TasSender {
	
	/**
	 * sendMessage Method
	 * Send the message to TAS for control
	 * @param containerName
	 * @param content
	 * @throws IOException
	 */
    public static void sendMessage(String containerName, String content) throws IOException {
        ArrayList<Container> containers = ResourceRepository.getContainersInfo();
        Container tempContainer;

		for (int i = 0; i < containers.size(); i++) {
			tempContainer = containers.get(i);

			if (containerName.equals(tempContainer.ctname)) {
				Socket tasSocket = tempContainer.tasSocket;
				if (tasSocket.isConnected()) {
					PrintWriter writer = new PrintWriter(tasSocket.getOutputStream(), true);
					JSONObject contentObj = new JSONObject();
					try {
						contentObj.put("ctname", containerName);
						contentObj.put("con", content);
					} catch (JSONException e) {
						e.printStackTrace();
					}
                    writer.println(contentObj.toString());
					System.out.println("[&CubeThyme] Send to TAS \"" + contentObj.toString() + "\"\n");
                    break;
				}
				else {
					System.out.println("[&CubeThyme] TAS connection is closed\n");
					break;
				}
			}
			else {
				System.out.println("[&CubeThyme] Could not found container\n");
			}
		}
	}
}