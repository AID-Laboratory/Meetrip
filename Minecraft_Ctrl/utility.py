from PIL import Image
import os
import pyrebase # pip install pyrebase4
import requests
import json
import shutil

config = {
    "apiKey": "AIzaSyCFM1FdSAmNr5O8xIIeSwRhib6t1cuIv40",
    "authDomain": "meetrip-3f118.firebaseapp.com",
    "databaseURL":"https://meetrip-3f118.firebaseio.com",
    "projectId": "meetrip-3f118",
    "storageBucket": "meetrip-3f118.appspot.com",
    "messagingSenderId": "1025607844078",
    "appId": "1:1025607844078:web:cb07ba63342cb48249f3ff"
}
firebase = pyrebase.initialize_app(config)
storage = firebase.storage()



def getLaCin(HOST:str, AE_NAME:str, CNT_RN:str):
    url = "http://" + HOST + ":7579/Mobius/" + AE_NAME + "/" + CNT_RN + "/la"

    payload={}
    headers = {
    'Accept': 'application/json',
    'X-M2M-RI': '12345',
    'X-M2M-Origin': 'S' + AE_NAME
    }

    response = requests.request("GET", url, headers=headers, data=payload)
    jsonObject = json.loads(response.text)
    
    # print(response.text)

    content = jsonObject.get("m2m:cin").get("con")

    create_time = jsonObject.get("m2m:cin").get("ct")
    # create_time = ct2kst(create_time)
    
    # print(f"GET CIN(LA) | AE : {AE_NAME}, CNT : {CNT_RN}, CON : {content}, CT_Date : {create_time[0]}, CT_Date : {create_time[1]}")

    return content

def image_resizer(img_f_name:str):

    """
    이미지의 크기를 1024 x 1024 으로 변환하는 함수
    input   : origin  image path(string)
    output  : resized image path(string)
    """

    size = 1024

    img_f_name = "./Minecraft_Server/plugins/ImageMaps/images/" + img_f_name

    root, extension = os.path.splitext(img_f_name)
    resized_img_name = root + "_resized" + extension

    Image.open(img_f_name).resize((size, size)).save(resized_img_name)

    return resized_img_name.replace("./Minecraft_Server/plugins/ImageMaps/images/","")


def fire_download(path_cloud:str, path_local:str, file_name:str):

    """
    firebase 에서 이미지를 다운로드하는 함수
    file_name = "SampleImage_001.jpg"
    path_cloud = "Sample Images/" + file_name
    path_local = "./"
    """

    # Download
    storage.child(path_cloud).download(path = path_local, filename = file_name)

    shutil.move(path_local + file_name, "./Minecraft_Server/plugins/ImageMaps/images/" + file_name)

    # print(path_local + file_name)

    return file_name

def initSub(BROKER_ADDR = str, AE_NAME = str, CNT_RN = str, SUB_RN = str):

    url = "http://" + BROKER_ADDR + ":7579/Mobius/" + AE_NAME + "/" + CNT_RN

    payload = "{\n\"m2m:sub\": {\n\"rn\": \"" + SUB_RN + "\",\n\"enc\": {\n\"net\": [3]\n},\n\"nu\": [\"" + "mqtt://" + BROKER_ADDR + "/S" + AE_NAME + "/" + CNT_RN + "?ct=json\"],\n\"exc\": 0\n}\n}"
    headers = {
        'Accept': 'application/json',
        'X-M2M-RI': '12345',
        'X-M2M-Origin': "S" + AE_NAME,
        'Content-Type': 'application/vnd.onem2m-res+json; ty=23'
    }

    response = requests.request("POST", url, headers=headers, data=payload)
    # print("GenSub response state : ",response.status_code)
    # print(type(response.status_code))

    if response.status_code == 409 :
        url = url + "/" + SUB_RN
        response = requests.request("DELETE", url, headers=headers, data=payload)
        initSub(BROKER_ADDR, AE_NAME, CNT_RN, SUB_RN)


def parsJson(message):

    try :

        message = message.payload.decode('utf-8')   # decode to utf-8 recived message 
        # print("Message Type(1) :", type(message), message)
        jsonMSG = json.loads(message)               # convert to json Object
        # print("Message Type(3) :", type(jsonMSG), jsonMSG)
        
        containerJson = jsonMSG.get("pc").get("m2m:sgn").get("sur") # pars application_entity, container, subscription-resource_name
        containerArr = str(containerJson).split("/")                # ['Mobius', 'AID-HT', 'Hum', 'HumSub'] = [cb, AE, CNT, CON]

        # pars content
        content = jsonMSG.get("pc").get("m2m:sgn").get("nev").get("rep").get("m2m:cin").get("con")

        print(f"MQTT Noti | AE : {containerArr[1]}, CNT : {containerArr[2]}, CON : {content}, SUB-RN : {containerArr[3]}")

        return containerArr, content

    except :
        pass

def testMQTT(client, userData, message):

    print(f"message recevied\t\t: {str(message.payload.decode('utf-8'))}")
    print(f"message topic\t\t: {message.topic}")
    print(f"message qos\t\t: {message.qos}")
    print(f"message retain flag\t: {message.retain}")

    return