package moe.quill.stratumcommon.Services;

import moe.quill.StratumCommon.KeyManager.IKeyManager;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;

public final class KeyManager implements IKeyManager {

    @Override
    public <E extends Enum<?>> void loadPluginKeys(Plugin plugin, Class<E> enumClass) {
        for (final var enumKey : enumClass.getEnumConstants()) {
            keyMap.put(enumKey.name(), new NamespacedKey(plugin, enumKey.name()));
        }
    }

    @Override
    public HashMap<String, NamespacedKey> getKeyMap() {
        return keyMap;
    }

    @Override
    public NamespacedKey getKey(Enum<?> anEnum) {
        return keyMap.get(anEnum.name().toUpperCase());
    }
}
