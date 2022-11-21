from PIL import Image
import os

def image_resizer(img_f_name:str):

    """
    이미지의 크기를 size x size 으로 변환하는 함수
    input   : origin  image path(string)
    input   : image pixel size (int)
    output  : resized image path(string)
    """

    size = 1024

    root, extension = os.path.splitext(img_f_name)
    resized_img_name = root + "_resized" + extension

    Image.open(img_f_name).resize((size, size)).save(resized_img_name)

    return resized_img_name


# if __name__ == "__main__" :

#     img_name = "./Lenna.png"
#     img_name = "./doge3.jpg"

#     print(image_resizer(img_name))