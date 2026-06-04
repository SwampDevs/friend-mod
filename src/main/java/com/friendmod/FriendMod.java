package com.friendmod;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class FriendMod implements ModInitializer {

    @Override
    public void onInitialize() {

        // Comando /friend
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("friend")
                .then(CommandManager.literal("add")
                    .then(CommandManager.argument("name", StringArgumentType.word())
                        .executes(ctx -> {
                            String name = StringArgumentType.getString(ctx, "name");
                            ServerPlayerEntity sender = ctx.getSource().getPlayer();
                            if (sender == null) return 0;
                            FriendData.get(sender.getUuidAsString()).addFriend(name);
                            FriendData.save(sender.getUuidAsString());
                            sender.sendMessage(Text.literal("§aAggiunto §e" + name + " §aai tuoi amici!"), false);
                            return 1;
                        })))
                .then(CommandManager.literal("remove")
                    .then(CommandManager.argument("name", StringArgumentType.word())
                        .executes(ctx -> {
                            String name = StringArgumentType.getString(ctx, "name");
                            ServerPlayerEntity sender = ctx.getSource().getPlayer();
                            if (sender == null) return 0;
                            FriendData.get(sender.getUuidAsString()).removeFriend(name);
                            FriendData.save(sender.getUuidAsString());
                            sender.sendMessage(Text.literal("§cRimosso §e" + name + " §cdai tuoi amici."), false);
                            return 1;
                        }))));
        });

        // Evento morte: notifica gli amici
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

        // Chat "(c)" -> invia coordinate
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
            String content = message.getContent().getString();
            if (content.trim().equals("(c)")) {
                int x = (int) sender.getX();
                int y = (int) sender.getY();
                int z = (int) sender.getZ();
                String name = sender.getName().getString();
                if (sender.getServer() != null) {
                    Text coords = Text.literal("§e" + name + " §7è a: §f" + x + ", " + y + ", " + z);
                    sender.getServer().getPlayerManager().broadcast(coords, false);
                }
                return false; // blocca il messaggio originale "(c)"
            }
            return true;
        });
    }
}
