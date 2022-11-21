"""
requirement modules
"""
import paho.mqtt.client as mqtt, time   # pip install paho-mqtt
from strgen import StringGenerator      # pip install StringGenerator
## 1 : 추가로 작성한 코들에 대한 설명, 프로콜이 맞는지
# from utility.genSub import initSub                      # file : Utility/genSub.py
# from Utility.getLastContent import getLaCin             # file : Utility/getLastContent.py
# from Utility.jsonParser import parsJson                 # file : Utility/jsonParser.py
# from Utility.testMQTT import testMQTT                   # file : Utility/testMQTT.py
# from setBlock.dht11 import setHumBlock, setTempBlock    # file : setBlock/dht11.py
import json
from utility import initSub
from utility import testMQTT
from utility import parsJson
from utility import fire_download
from utility import image_resizer
from npc_ctrl import setPhoto
from npc_ctrl import erase

"""
declare a constant
"""
BROKER_ADDR = "203.252.23.46"                       # mosquitto hostname or url
AE_NAME     = "Meetrip"                              # AE Name for Mobius Server
CLIENT_ID   = str(StringGenerator("[\l\d]{32}"))    # Client ID for MQTT

post_count = 1
erase_bool = False

def onMessage(client, userData, message):

    global erase_bool, post_count

    # testMQTT(client, userData, message)

    # print("Message Type :", type(message))
    # print("Message :", message)

    containerArr, content = parsJson(message)

    # print(containerArr)

    if containerArr[1] == AE_NAME :
        if containerArr[4] == "Air_SUB" :
            pass
        elif containerArr[4] == "Wth_SUB" :
            pass
        elif containerArr[4] == "Img_SUB" :
            
            if type(content) == str :
            
                contentObj = json.loads(content.replace("\'", "\""))
            
            else :
            
                contentObj = content

            file_name = contentObj.get("ImgName")
            path_cloud = "Sample Images/" + file_name
            path_local = "./"
            downloaded_file = fire_download(path_cloud, path_local, file_name)
            resized_file = image_resizer(downloaded_file)
            print("resized_file :", resized_file)
            
            if erase_bool == True :
            
                erase(post_count)
            
            setPhoto(resized_file, post_count)
            post_count += 1
            
            if post_count >= 9 :
            
                post_count = 1
                erase_bool = True

        elif containerArr[4] == "GSR_SUB" :
            pass
        elif containerArr[4] == "HRT_SUB" :
            pass
        elif containerArr[4] == "SKT_SUB" :
            pass
        else :
            pass

if __name__ == "__main__":
    try :

        cnt_list = ["202242502/Image", "202242502/Band/Gsr", "202242502/Band/HeartRate", "202242502/Band/SkinTemperature"]

        initSub(BROKER_ADDR, AE_NAME, "202242502/AirPollution",  "Air_SUB")
        initSub(BROKER_ADDR, AE_NAME, "202242502/Weather",  "Wth_SUB")
        initSub(BROKER_ADDR, AE_NAME, "202242502/Image",  "Img_SUB")
        initSub(BROKER_ADDR, AE_NAME, "202242502/Band/Gsr", "GSR_SUB")
        initSub(BROKER_ADDR, AE_NAME, "202242502/Band/HeartRate",  "HRT_SUB")
        initSub(BROKER_ADDR, AE_NAME, "202242502/Band/SkinTemperature", "SKT_SUB")

        print(f"MQTT Client ID : {CLIENT_ID}")
        
        # start sub and noti
        subClient = mqtt.Client(CLIENT_ID)                              # generate Client for subscribe
        subClient.connect(BROKER_ADDR)                                  # connect mosquitto with hostname(or url)
        subClient.subscribe(f"/oneM2M/req/Mobius2/S{AE_NAME}/#")        # subscribe the topic
## 3 이 메소드의 동작방식을 설명할 수 있어야 함
        subClient.on_message = onMessage                                # will be called for every received message to target function
## 4 이 메소드가 정확히 무슨 일을 하는 아이인지
        subClient.loop_forever(timeout=1, retry_first_connection=True)  # timeout:  The time in seconds to wait for incoming/outgoing network traffic before timing out and returning.
                                                                        # retry_first_connection:   Should the first connection attempt be retried on failure.
                                                                        # Raises OSError/WebsocketConnectionError on first connection failures unless retry_first_connection=True

    except KeyboardInterrupt:
        pass