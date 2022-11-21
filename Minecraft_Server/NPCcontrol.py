from mcpi.minecraft import Minecraft

# mc = Minecraft.create(address="localhost", port=4711)
mc = Minecraft.create()

pos = mc.player.getTilePos()

print(pos)
print(pos.x)
print(pos.y)
print(pos.z)

def calpos(x, y, z):
    x += 0
    y += -64
    z += 0

    return x, y, z

game_pos = (-4, 74, 49)
# code_pos = (-17, -17, 30)

print(calpos(-4, 74, 49))
print(pos)
# mc.setBlock(calpos(288, 63, 96), 35, 0)