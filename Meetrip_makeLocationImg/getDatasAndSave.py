from urllib import request
import requests
import pandas as pd
from tqdm import tqdm
import matplotlib.pyplot as plt

def getCourseData(PRTC, HOST, PORT, AENA, CONT, TIME, start_time, end_time):

    lon_array = []
    lat_array = []

    rn_list = []

    url = PRTC + "://"+HOST+":"+PORT + "/Mobius/" + AENA + "/" + CONT + "?fu=1&cra=" + TIME

    payload = {}
    headers = {
        'Accept': 'application/json',
        'X-M2M-RI': '12345',
        'X-M2M-Origin': 'SOrigin'
    }

    request_result = requests.request("GET", url, headers=headers, data=payload).json()["m2m:uril"]

    for i in request_result:
        i = int(i.replace("Mobius/01077943675_NetworkInfo/LTE/4-",""))
        if i >= start_time and i <= end_time :
            rn_list.append(f"Mobius/01077943675_NetworkInfo/LTE/4-{i}")

    for i in tqdm(rn_list):
        
        url = PRTC + "://"+HOST+":"+PORT + "/" + i
        response_json_obj = requests.request("GET", url, headers=headers, data=payload).json()
        
        lon_array.append(response_json_obj["m2m:cin"]["con"]["longitude"])
        lat_array.append(response_json_obj["m2m:cin"]["con"]["latitude"])

    course_df = pd.DataFrame(zip(lon_array, lat_array))
    course_df.to_csv('./course_data.csv', sep=',', na_rep='NaN')

    plt.plot(lon_array, lat_array)
    plt.axis(False)
    plt.savefig(f'{AENA}_{CONT}_{TIME}.png')

if __name__ == "__main__" :

    PRTC = "http"
    HOST = "203.252.23.46"
    PORT = "7579"
    AENA = "01077943675_NetworkInfo"
    CONT = "LTE"
    TIME = "20220420T230122"

    start_time = 20220519213241703
    end_time = 20220519233241703

    getCourseData(PRTC, HOST, PORT, AENA, CONT, TIME, start_time, end_time)