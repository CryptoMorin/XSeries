package org.fastfoodplus.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.TestOnly;

import java.io.*;

@TestOnly
@SuppressWarnings("deprecation")
public class Examples {
    private static void print(String str) {
        Bukkit.getConsoleSender().sendMessage(str);
    }

    public static void printTest() {
		// I know this is a little messy...
        print("MELON Item: " + XMaterial.MELON.parseMaterial().name());
        print("MELON_SLICE Item: " + XMaterial.MELON_SLICE.parseMaterial().name());
        print("CARROT Item: " + XMaterial.CARROT.parseMaterial().name());
        print("CARROTS Item: " + XMaterial.CARROTS.parseMaterial().name());
        print("========================================================");
        print("CARROT: " + XMaterial.matchXMaterial("CARROT"));
        print("00000000000000000000000000000000000000000000000000");
        print("CARROTS: " + XMaterial.matchXMaterial("CARROTS"));
        print("========================================================");
        print("MELON: " + XMaterial.matchXMaterial("MELON"));
        print("00000000000000000000000000000000000000000000000000");
        print("MELON_SLICE: " + XMaterial.matchXMaterial("MELON_SLICE"));
        print("CARROT_ITEM match Item: " + XMaterial.matchXMaterial("CARROT_ITEM"));
        print("MELON_BLOCK match Item: " + XMaterial.matchXMaterial("MELON_BLOCK"));
        print("||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||");
        print(XMaterial.MAP.parseMaterial().name() + "  map");
        print(XMaterial.FILLED_MAP.parseMaterial().name() + "  filled map");
        print(XMaterial.matchXMaterial("MAP").name() + "  map parse");
        print(XMaterial.matchXMaterial("FILLED_MAP").name() + "  filled map parse");
        print("-------------------------------------------");
        print(XMaterial.BLACK_GLAZED_TERRACOTTA.parseMaterial(false) + "   non-suggested");
        print(XMaterial.BLACK_GLAZED_TERRACOTTA.parseMaterial(true).name() + "   suggested");
        print(XMaterial.COD_BUCKET.parseMaterial(true).name() + "   cod bucket");
        print(XMaterial.WHITE_DYE.parseMaterial(true).name() + " WHITE dye " +
                XMaterial.WHITE_DYE.parseItem(true).getDurability());
    }

    public static void convertYAMLMaterial(File file) {
        StringBuilder sb = new StringBuilder();

        try {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().startsWith("type:")) sb.append(line);
                    else {
                        int index = line.indexOf(':');
                        String material = line.substring(index + 1);
                        XMaterial mat = XMaterial.matchXMaterial(material);
                        if (mat == null || mat.name().contains(mat.parseMaterial().name()) || mat.parseMaterial().name().contains(mat.name())) {
                            sb.append(line).append(System.lineSeparator());
                            continue;
                        }
                        sb.append(line, 0, index).append(": ").append(mat.parseMaterial().name());
                        if (!XMaterial.isNewVersion() && mat.getData() != 0) {
                            sb.append(System.lineSeparator());
                            sb.append(line, 0, index - 4).append("damage: ").append(mat.getData());
                        }
                    }
                    sb.append(System.lineSeparator());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(sb.toString());
                writer.flush();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void theCakeIsaLie(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        boolean isCake = XMaterial.isNewVersion() ? block.getType() == Material.CAKE : block.getType() == Material.matchMaterial("CAKE_BLOCK");
        if (isCake) event.setCancelled(true);
    }
}
