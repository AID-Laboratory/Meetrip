import requests
import pandas as pd
from tqdm import tqdm

PRTC = "http"
HOST = "203.252.23.46"
PORT = "7579"
AENA = "01077943675_NetworkInfo"
CONT = "LTE"
TIME = "20220420T230122"

course_array = []

url = PRTC + "://"+HOST+":"+PORT + "/Mobius/" + AENA + "/" + CONT + "?fu=1&cra=" + TIME

payload = {}
headers = {
    'Accept': 'application/json',
    'X-M2M-RI': '12345',
    'X-M2M-Origin': 'SOrigin'
}

for i in tqdm(requests.request("GET", url, headers=headers, data=payload).json()["m2m:uril"]):
    
    url = PRTC + "://"+HOST+":"+PORT + "/" + i
    response_json_obj = requests.request("GET", url, headers=headers, data=payload).json()
    
    lon = response_json_obj["m2m:cin"]["con"]["longitude"]
    lat = response_json_obj["m2m:cin"]["con"]["latitude"]
    
    course_array.append([lon, lat])

f = open("course_data.txt", 'w')
f.write(str(course_array))
f.close()
