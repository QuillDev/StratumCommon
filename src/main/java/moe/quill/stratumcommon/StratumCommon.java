package moe.quill.stratumcommon;

import moe.quill.StratumCommon.KeyManager.IKeyManager;
import moe.quill.StratumCommon.Serialization.StratumSerializer;
import moe.quill.stratumcommon.Services.KeyManager;
import moe.quill.stratumcommon.Services.StratumSerialization;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public final class StratumCommon extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic

        //Create our services
        final var stratumSerialization = new StratumSerialization();
        final var keyManager = new KeyManager();
        //Get the services manager
        final var servicesManager = getServer().getServicesManager();

        //Register our services
        servicesManager.register(StratumSerializer.class, stratumSerialization, this, ServicePriority.Highest);
        servicesManager.register(IKeyManager.class, keyManager, this, ServicePriority.Highest);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
