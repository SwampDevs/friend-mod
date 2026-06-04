package com.friendmod;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class FriendMod implements ModInitializer {

    @Override
    public void onInitialize() {

        // Comandi client-side
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("friend")
                .then(ClientCommandManager.literal("add")
                    .then(ClientCommandManager.argument("name", StringArgumentType.word())
                        .executes(ctx -> {
                            String name = StringArgumentType.getString(ctx, "name");
                            MinecraftClient client = MinecraftClient.getInstance();
                            if (client.player == null) return 0;
                            String uuid = client.player.getUuidAsString();
                            FriendData.get(uuid).addFriend(name);
                            FriendData.save(uuid);
                            client.player.sendMessage(Text.literal("§aAggiunto §e" + name + " §aai tuoi amici!"), false);
                            return 1;
                        })))
                .then(ClientCommandManager.literal("remove")
                    .then(ClientCommandManager.argument("name", StringArgumentType.word())
                        .executes(ctx -> {
                            String name = StringArgumentType.getString(ctx, "name");
                            MinecraftClient client = MinecraftClient.getInstance();
                            if (client.player == null) return 0;
                            String uuid = client.player.getUuidAsString();
                            FriendData.get(uuid).removeFriend(name);
                            FriendData.save(uuid);
                            client.player.sendMessage(Text.literal("§cRimosso §e" + name + " §cdai tuoi amici."), false);
                            return 1;
                        })))
                .then(ClientCommandManager.literal("list")
                    .executes(ctx -> {
                        MinecraftClient client = MinecraftClient.getInstance();
                        if (client.player == null) return 0;
                        String uuid = client.player.getUuidAsString();
                        String list = FriendData.get(uuid).getFriends().toString();
                        client.player.sendMessage(Text.literal("§6Amici: §f" + list), false);
                        return 1;
                    })));
        });

        // (c) in chat -> sostituisce il messaggio con le coordinate (client-side)
        ClientSendMessageEvents.ALLOW_CHAT.register(message -> {
            if (message.trim().equals("(c)")) {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.player != null) {
                    int x = (int) client.player.getX();
                    int y = (int) client.player.getY();
                    int z = (int) client.player.getZ();
                    String name = client.player.getName().getString();
                    // Manda le coordinate come messaggio normale in chat
                    client.player.networkHandler.sendChatMessage(x + ", " + y + ", " + z);
                }
                return false; // blocca il messaggio "(c)" originale
            }
            return true;
        });

        // Notifica morte amici (server-side, funziona se la mod è sul server)
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity instanceof ServerPlayerEntity dead)) return;
            String deadName = dead.getName().getString();
            int x = (int) dead.getX();
            int y = (int) dead.getY();
            int z = (int) dead.getZ();
            String coords = x + ", " + y + ", " + z;

            if (dead.getServer() == null) return;
            for (ServerPlayerEntity online : dead.getServer().getPlayerManager().getPlayerList()) {
                if (online.getUuidAsString().equals(dead.getUuidAsString())) continue;
                if (FriendData.get(online.getUuidAsString()).isFriend(deadName)) {
                    online.sendMessage(Text.literal("§c☠ §e" + deadName + " §cè morto a: §f" + coords), false);
                }
            }
        });

        // (c) server-side come fallback se la mod è sul server
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
            String content = message.getContent().getString();
            if (content.contains("è a:") && !content.startsWith("(c)")) {
                return true; // lascia passare i messaggi coordinate già formattati
            }
            return true;
        });
    }
}
