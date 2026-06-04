package com.friendmod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class FriendData {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, FriendData> CACHE = new HashMap<>();
    private static final Path DIR = FabricLoader.getInstance().getConfigDir().resolve("friendmod");

    private Set<String> friends = new HashSet<>();

    public static FriendData get(String uuid) {
        return CACHE.computeIfAbsent(uuid, FriendData::load);
    }

    public void addFriend(String name) { friends.add(name); }
    public void removeFriend(String name) { friends.remove(name); }
    public boolean isFriend(String name) { return friends.contains(name); }

    public static void save(String uuid) {
        try {
            Files.createDirectories(DIR);
            Path file = DIR.resolve(uuid + ".json");
            try (Writer w = Files.newBufferedWriter(file)) {
                GSON.toJson(CACHE.get(uuid).friends, w);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static FriendData load(String uuid) {
        FriendData data = new FriendData();
        Path file = DIR.resolve(uuid + ".json");
        if (Files.exists(file)) {
            try (Reader r = Files.newBufferedReader(file)) {
                Set<String> set = GSON.fromJson(r, new TypeToken<Set<String>>(){}.getType());
                if (set != null) data.friends = set;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return data;
    }
}
