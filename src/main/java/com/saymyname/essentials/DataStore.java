package com.saymyname.essentials;

import java.io.*;
import java.util.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.entity.player.EntityPlayerMP;

public class DataStore {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final File homesFile = new File("saymyname_homes.json");

    // Homes: playerName -> { homeName -> [x, y, z, dim, yaw, pitch] }
    private static Map<String, Map<String, double[]>> homes = new HashMap<String, Map<String, double[]>>();

    // TPA requests: targetName -> requesterName (active for 30s)
    public static Map<String, String> tpaRequests = new HashMap<String, String>();
    public static Map<String, Long> tpaTimestamps = new HashMap<String, Long>();

    // Back locations: playerName -> [x, y, z, dim]
    public static Map<String, double[]> backLocations = new HashMap<String, double[]>();

    // Reply targets: playerName -> lastMsgSender
    public static Map<String, String> replyTargets = new HashMap<String, String>();

    static { loadHomes(); }

    public static void setHome(String player, String name, double x, double y, double z, int dim, float yaw, float pitch) {
        if (!homes.containsKey(player)) homes.put(player, new HashMap<String, double[]>());
        homes.get(player).put(name.toLowerCase(), new double[]{x, y, z, dim, yaw, pitch});
        saveHomes();
    }

    public static double[] getHome(String player, String name) {
        Map<String, double[]> h = homes.get(player);
        return h != null ? h.get(name.toLowerCase()) : null;
    }

    public static boolean delHome(String player, String name) {
        Map<String, double[]> h = homes.get(player);
        if (h == null) return false;
        boolean r = h.remove(name.toLowerCase()) != null;
        if (r) saveHomes();
        return r;
    }

    public static Set<String> getHomeNames(String player) {
        Map<String, double[]> h = homes.get(player);
        return h != null ? h.keySet() : Collections.<String>emptySet();
    }

    public static void saveBack(EntityPlayerMP p) {
        backLocations.put(p.getCommandSenderName(), new double[]{p.posX, p.posY, p.posZ, p.dimension});
    }

    private static void saveHomes() {
        try {
            Writer w = new FileWriter(homesFile);
            gson.toJson(homes, w);
            w.close();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private static void loadHomes() {
        if (!homesFile.exists()) return;
        try {
            Reader r = new FileReader(homesFile);
            Map<String, Map<String, double[]>> loaded = gson.fromJson(r, new TypeToken<Map<String, Map<String, double[]>>>(){}.getType());
            if (loaded != null) homes = loaded;
            r.close();
        } catch (Exception e) { e.printStackTrace(); }
    }
}
