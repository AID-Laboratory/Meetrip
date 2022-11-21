import cv2
import os
from PIL import Image

def imageCutter(src:str,size:int):

    root, extension = os.path.splitext(src)
    resized_img_name = root + "_resized" + extension

    src = cv2.imread("20221024_172211_6948885350853292209.jpg", cv2.IMREAD_COLOR)
    dst = src.copy() 
    roi = src[0:3024, 0:3024]
    dst[0:3024, 0:3024] = roi

    resized_img = cv2.resize(dst, (size, size))

    cv2.imwrite(resized_img_name, resized_img)

    cv2.waitKey()
    cv2.destroyAllWindows()

if __name__ == "__main__" :

    src = "./20221024_172211_6948885350853292209.jpg"

    print(imageCutter(src, 1024))