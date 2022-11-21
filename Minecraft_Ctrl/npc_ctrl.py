from mcpi.minecraft import Minecraft
import mcpi.block as block
from pynput.keyboard import Key, Controller as KeyController
import pynput
from pynput.mouse import Button, Controller
from pynput import mouse
from pynput import keyboard
import time

mouse = Controller()
keyboard = KeyController()

mc = Minecraft.create()

npc_player_id = mc.getPlayerEntityId("aid_lab_Lee")

angle = mc.player.getRotation()


#위치 조정 함수
def calpos(x, y, z):return x + 0, y - 84, z - 32

coordination = []

#사진 설치 함수
# player_id:int
def setPhoto(img_name:str, frame_num:int):

    time.sleep(1)

    x, y, z = get_frame_info(frame_num)

    mc.setBlock(calpos(x, y, z), block.WOOL, 2)
    mc.entity.setPos(npc_player_id, calpos(x, y + 1, z))

    set_player_orient(frame_num)

    keyboard.press('/')
    time.sleep(0.1)
    
    keyboard.type(f'imagemap place {img_name} true true true')
    keyboard.press(Key.enter)
    
    mouse.press(Button.right)
    mouse.release(Button.right)

    time.sleep(0.3)

    mc.entity.setPos(npc_player_id, calpos(17, 70, 42))

    mc.setBlock(calpos(21, 80, -12), block.AIR)

def set_player_orient(frame_num:int):

    if frame_num == 6 or frame_num == 7 or frame_num == 8 or frame_num == 9 : # south
        mc.player.setDirection(0.23, -0.05, 0.97)
    
    elif frame_num == 1 or frame_num == 2 or frame_num == 3 or frame_num == 4 or frame_num == 5  : # east
        mc.player.setDirection(0.97, -0.05, 0.23)


def get_frame_info(frame_num:int):

    x,y,z = 0,0,0

    if frame_num == 1 :
        x = 21
        y = 80
        z = -12

    elif frame_num == 2 :
        x = 21
        y = 80
        z = -12

    elif frame_num == 3 :
        x = 21
        y = 80
        z = -12

    elif frame_num == 4 :
        x = 21
        y = 80
        z = -12

    elif frame_num == 5 :
        x = 21
        y = 80
        z = -12

    elif frame_num == 6 :
        x = 21
        y = 80
        z = -12

    elif frame_num == 7 :
        x = 21
        y = 80
        z = -12

    elif frame_num == 8 :
        x = 21
        y = 80
        z = -12

    elif frame_num == 9 :
        x = 21
        y = 80
        z = -12

    return x, y, z

setPhoto("Lenna_resized.png", 1)

def erase(frame_num=int):

    time.sleep(1)

    x, y, z = get_frame_info(1)

    mc.setBlock(calpos(x+1, y-3, z+3), block.WOOL, 2)
    mc.entity.setPos(npc_player_id, calpos(x+1, y-2, z+3))

    keyboard.press('/')
    time.sleep(0.1)
    
    keyboard.type('kill @e[type=minecraft:glow_item_frame, distance=..7]')
    keyboard.press(Key.enter)

    time.sleep(0.1)

    mc.setBlock(calpos(x+1, y-3, z+3), block.AIR)
    mc.entity.setPos(npc_player_id, calpos(17, 70, 42))

time.sleep(3)
erase(1)