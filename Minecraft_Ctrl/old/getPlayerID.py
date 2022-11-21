from mcpi.minecraft import Minecraft
import mcpi.block as block

mc = Minecraft.create()

# ID = mc.getPlayerEntityId

ids = mc.getPlayerEntityIds()

# print(ID)
npc_player_id = mc.getPlayerEntityId("aid_lab_Lee")
print(npcPlayer)



mc.player.setPos(npcPlayer, x, y, z)

