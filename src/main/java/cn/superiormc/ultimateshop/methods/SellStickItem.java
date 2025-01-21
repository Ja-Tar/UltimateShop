package cn.superiormc.ultimateshop.methods;

import cn.superiormc.ultimateshop.UltimateShop;
import cn.superiormc.ultimateshop.managers.ConfigManager;
import cn.superiormc.ultimateshop.managers.ErrorManager;
import cn.superiormc.ultimateshop.managers.LanguageManager;
import cn.superiormc.ultimateshop.methods.Items.BuildItem;
import cn.superiormc.ultimateshop.utils.CommonUtil;
import cn.superiormc.ultimateshop.utils.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class SellStickItem {

    public static final NamespacedKey SELL_STICK_TIMES = new NamespacedKey(UltimateShop.instance, "sell_stick_usage");
    public static final NamespacedKey SELL_STICK_ID = new NamespacedKey(UltimateShop.instance, "sell_stick_id");
    public static final NamespacedKey SELL_STICK_INFINITE = new NamespacedKey(UltimateShop.instance, "sell_stick_infinite");
    public static byte b = 1;

    public static ItemStack getSellStick(Player player, String itemID, int amount) {
        ConfigurationSection section = ConfigManager.configManager.config.getConfigurationSection(
                "sell-stick-items." + itemID
        );
        if (section == null) {
            LanguageManager.languageManager.sendStringText("error-item-not-found",
                    "item",
                    itemID);
            return null;
        }
        int times = section.getInt("usage-times", -1);
        return getSellStick(player, itemID, amount, times, times <= 0);
    }

    public static ItemStack getSellStick(Player player, String itemID, int amount, int times, boolean infinite) {
        ConfigurationSection section = ConfigManager.configManager.config.getConfigurationSection(
                "sell-stick-items." + itemID
        );
        if (section == null) {
            LanguageManager.languageManager.sendStringText("error-item-not-found",
                    "item",
                    itemID);
            return null;
        }
        if (times <= 0 && !infinite) {
            return null;
        }
        ItemStack resultItem = BuildItem.buildItemStack(player, section, 1);
        resultItem.setAmount(amount);
        if (!resultItem.hasItemMeta()) {
            ItemMeta tempMeta = Bukkit.getItemFactory().getItemMeta(resultItem.getType());
            resultItem.setItemMeta(tempMeta);
        }
        ItemMeta meta = resultItem.getItemMeta();
        List<String> newLore = new ArrayList<>();
        if (meta.hasLore()) {
            for (String str : meta.getLore()) {
                if (!infinite) {
                    str = CommonUtil.modifyString(str, "times", String.valueOf(times));
                } else {
                    str = CommonUtil.modifyString(str, "times", TextUtil.parse(player, ConfigManager.configManager.getString("placeholder.sell-stick.infinite")));
                }
                newLore.add(str);
            }
            meta.setLore(newLore);
        }
        meta.getPersistentDataContainer().remove(SELL_STICK_ID);
        meta.getPersistentDataContainer().remove(SELL_STICK_TIMES);
        meta.getPersistentDataContainer().set(SELL_STICK_ID,
                PersistentDataType.STRING,
                itemID);
        if (!infinite) {
            meta.getPersistentDataContainer().set(SELL_STICK_TIMES,
                    PersistentDataType.INTEGER,
                    times);
        } else {
            if (CommonUtil.getMajorVersion(19)) {
                meta.getPersistentDataContainer().set(SELL_STICK_INFINITE,
                        PersistentDataType.BOOLEAN,
                        true);
            } else {
                meta.getPersistentDataContainer().set(SELL_STICK_INFINITE,
                        PersistentDataType.BYTE,
                        b);
            }
        }
        resultItem.setItemMeta(meta);
        return resultItem;
    }

    public static String getSellStickID(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.getPersistentDataContainer().has(SELL_STICK_ID, PersistentDataType.STRING)) {
            return null;
        }
        return meta.getPersistentDataContainer().get(SELL_STICK_ID, PersistentDataType.STRING);
    }

    public static int getSellStickValue(ItemStack item) {
        if (item == null) {
            return 0;
        }
        if (!item.hasItemMeta()) {
            return 0;
        }
        ItemMeta meta = item.getItemMeta();
        if (sellStickIsInfinite(item)) {
            return 1;
        }
        if (!meta.getPersistentDataContainer().has(SELL_STICK_TIMES, PersistentDataType.INTEGER)) {
            return 0;
        }
        return meta.getPersistentDataContainer().get(SELL_STICK_TIMES, PersistentDataType.INTEGER);
    }

    public static void removeSellStickValue(Player player, ItemStack item) {
        if (item == null) {
            return;
        }
        if (!item.hasItemMeta()) {
            return;
        }
        if (sellStickIsInfinite(item)) {
            return;
        }
        int nowValue = getSellStickValue(item);
        String id = getSellStickID(item);
        item.setAmount(item.getAmount() - 1);
        if (id == null) {
            ErrorManager.errorManager.sendErrorMessage("§x§9§8§F§B§9§8[UltimateShop] §cError: Can not found sell stick item ID");
        }
        else if (nowValue - 1 > 0) {
            ItemStack tempItem = getSellStick(player, id, 1, nowValue - 1, false);
            if (tempItem != null) {
                CommonUtil.giveOrDrop(player, tempItem);
            }
        }
    }

    public static boolean sellStickIsInfinite(ItemStack item) {
        if (!item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (CommonUtil.getMajorVersion(19)) {
            return meta.getPersistentDataContainer().has(SELL_STICK_INFINITE, PersistentDataType.BOOLEAN) && meta.getPersistentDataContainer().get(SELL_STICK_INFINITE, PersistentDataType.BOOLEAN);
        } else {
            return meta.getPersistentDataContainer().has(SELL_STICK_INFINITE, PersistentDataType.BYTE) && meta.getPersistentDataContainer().get(SELL_STICK_INFINITE, PersistentDataType.BYTE) == b;
        }
    }
}
