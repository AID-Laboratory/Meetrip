# https://www.youtube.com/watch?v=I1eskLk0exg

import pyrebase # pip install pyrebase4

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

def file_download(path_cloud, path_local, file_name):
    # Download
    storage.child(path_cloud).download(path = path_local, filename = file_name)

if __name__ == "__main__" :

    file_name = "SampleImage_001.jpg"
    path_cloud = "Sample Images/" + file_name
    path_local = "./"
    
    file_download(path_cloud, path_local, file_name)