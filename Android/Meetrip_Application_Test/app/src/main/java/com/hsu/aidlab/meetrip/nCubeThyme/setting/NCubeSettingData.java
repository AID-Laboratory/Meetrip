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

package com.hsu.aidlab.meetrip.nCubeThyme.setting;

import java.io.Serializable;

/**
 * Created by Sung on 2015-03-02.
 */
public class NCubeSettingData  implements Serializable {
    public String cseId = null;
    public String csePasscode = null;
    public String cseLabels = null;
    public String csePointOfAccess = null;
    public String firmwareName = null;
    public String firmwareDescription = null;
    public String firmwareVersion = null;
    public String firmwareURL = null;
    public String firmwareUpdateStatus = null;
    public String deviceName = null;
    public String deviceDescription = null;
    public String deviceManufacturer = null;
    public String deviceModel = null;
    public String deviceType = null;
    public String deviceFwVersion = null;
    public String deviceHwVersion = null;
    public String inCSEAddress = null;
    public String mqttBrokerAddress = null;
}
