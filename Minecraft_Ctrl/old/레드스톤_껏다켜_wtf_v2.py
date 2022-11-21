from tqdm import tqdm
import time
from mcpi.minecraft import Minecraft
import mcpi.block as block

mc = Minecraft.create()

def calpos(x, y, z):
    x += 0
    y += -64
    z += 0

    return x, y, z

mc.setBlock(calpos(-12, 89, 24), 0, 0)
time.sleep(0.1)
mc.setBlock(calpos(-12, 89, 24), 152, 0)
# mc.setBlock(calpos(21, 94, 32), 152, 0)

# 시작좌표 = 
# 종료좌표 = 

# 시작좌표 = calpos(-5, 75, 45)
# 종료좌표 = calpos(-3, 74, 47)

# print(f"시작좌표 : {시작좌표}")
# print(f"종료좌표 : {종료좌표}")

# print(len(range(시작좌표[0], 종료좌표[0])))
# print(len(range(시작좌표[1], 종료좌표[1])))
# print(len(range(시작좌표[2], 종료좌표[2])))

# print(range(시작좌표[0], 종료좌표[0]))
# print(range(시작좌표[1], 종료좌표[1]))
# print(range(시작좌표[2], 종료좌표[2]))

# for x in tqdm(range(시작좌표[0], 종료좌표[0])):
#     for y in tqdm(range(종료좌표[1], 시작좌표[1])):
#         for z in tqdm(range(시작좌표[2], 종료좌표[2])):
#             # print(x, y, z)
#             if mc.getBlock(x,y,z) == 152:
#                 print("=======================================================================================================================")
#                 mc.setBlock(x,y,z,block.AIR,0)
#                 # mc.setBlock(x,y,z,152,0)
#                 time.sleep(1)