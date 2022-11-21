from mcpi.minecraft import Minecraft
import mcpi.block as block

mc = Minecraft.create()

for x in range(0,1000):
    for y in range(0,1000):
        for z in range(0,1000):
            mc.setBlock(x,y,z,block.AIR,0)