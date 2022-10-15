
import pyrebase


config = {
    "apiKey": "AIzaSyD1qlgVMnRZS01Z2GDemd3dE6Z_InwRQTk",
    "authDomain": "nconnect-8d8e9.firebaseapp.com",
    "databaseURL": "https://nconnect-8d8e9.firebaseio.com",
    "projectId": "nconnect-8d8e9",
    "storageBucket": "nconnect-8d8e9.appspot.com",
    "messagingSenderId": "626679061034",
    "appId": "1:626679061034:web:7ddb02769eea8799560153",
    "measurementId": "G-80QZSPSMR0"
  }


def getImageFBS(path, fname):

    #init firebase stoage setting
    firebase = pyrebase.initialize_app(config)
    storage = firebase.storage()

    file = storage.child(path).download(path = "", filename = fname)
    f = open(fname,'wb')
    f.write(file)
    f.close()
    
    print(f"getImageFBS {path + fname}")


getImageFBS("videos/", "2021-04-05-17-25-17-RearCam.mp4")
