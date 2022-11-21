from tqdm import tqdm

import time

from mcpi.minecraft import Minecraft
import mcpi.block as block

def calpos(x, y, z):
    x += 0
    y += -64
    z += 0

    return x, y, z

# 시작좌표 = 
# 종료좌표 = 

시작좌표 = calpos(39, 87, 73)
종료좌표 = calpos(-36, 194, -5)

print(f"시작좌표 : {시작좌표}")
print(f"종료좌표 : {종료좌표}")

print(len(range(종료좌표[0], 시작좌표[0])))
print(len(range(시작좌표[1], 종료좌표[1])))
print(len(range(종료좌표[2], 시작좌표[2])))

mc = Minecraft.create()

for x in tqdm(range(종료좌표[0], 시작좌표[0])):
    for y in range(시작좌표[1], 종료좌표[1]):
        for z in range(종료좌표[2], 시작좌표[2]):
            # print(x, y, z)
            if mc.getBlock(x,y,z) == 152:
                mc.setBlock(x,y,z,block.AIR,0)
                mc.setBlock(x,y,z,152,0)
                time.sleep(0.1)