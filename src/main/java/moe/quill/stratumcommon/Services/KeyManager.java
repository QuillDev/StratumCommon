package moe.quill.stratumcommon.Services;

import moe.quill.StratumCommon.Annotations.IgnoreDynamicLoading;
import moe.quill.StratumCommon.Annotations.Keyable;
import moe.quill.StratumCommon.KeyManager.IKeyManager;
import moe.quill.StratumCommon.Utils.PackageUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public final class KeyManager implements IKeyManager {

    private static final Logger logger = LoggerFactory.getLogger(KeyManager.class.getName());

    @Override
    public void loadKeyablesDynamically(Plugin plugin) {
        final var reflector = new Reflections(PackageUtils.getReflectivePackageName(plugin.getClass()));
        final var keyableClasses = reflector.getTypesAnnotatedWith(Keyable.class);

        //Iterate through the keyable classes we found
        for (final var keyableClass : keyableClasses) {
            //If the class wants to skip dynamic loading, skip it
            if (keyableClass.isAnnotationPresent(IgnoreDynamicLoading.class)) continue;

            //Iterate through the enum keys and create keys for them
            final var enumKeys = (Enum<?>[]) keyableClass.getEnumConstants();
            for (final var enumKey : enumKeys) {
                keyMap.put(enumKey.name(), new NamespacedKey(plugin, enumKey.name()));
            }
        }
    }

    @Override
    public void registerKey(Plugin plugin, String keyName) {
        final var newKey = new NamespacedKey(plugin, keyName);
        keyMap.put(keyName, newKey);
        logger.info(String.format("Registered new key -> [%s]", newKey));
    }

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
