package moe.quill.stratumcommon;

import moe.quill.StratumCommonApi.Database.IDatabaseService;
import moe.quill.StratumCommonApi.Debug.IDebugService;
import moe.quill.StratumCommonApi.KeyManager.IKeyManager;
import moe.quill.StratumCommonApi.Serialization.ISerializer;
import moe.quill.stratumcommon.Database.SQLDataService;
import moe.quill.stratumcommon.Debug.DebugService;
import moe.quill.stratumcommon.Services.KeyManager;
import moe.quill.stratumcommon.Services.StratumSerialization;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public final class StratumCommon extends JavaPlugin {

    private static final Logger logger = LoggerFactory.getLogger(StratumCommon.class.getSimpleName());

    @Override
    public void onLoad() {
        super.onLoad();
        //Create our services
        final var stratumSerialization = new StratumSerialization();
        final var keyManager = new KeyManager();
        final var sqlDbService = new SQLDataService("jdbc:postgresql://localhost:5432/stratum", "postgres", "postgres");
        //Get the services manager
        final var servicesManager = getServer().getServicesManager();

        //Register our services
        servicesManager.register(ISerializer.class, stratumSerialization, this, ServicePriority.Highest);
        servicesManager.register(IKeyManager.class, keyManager, this, ServicePriority.Highest);
        servicesManager.register(IDatabaseService.class, sqlDbService, this, ServicePriority.Highest);
        //check that classes are registered successfully
        if (servicesManager.getRegistration(IDatabaseService.class) == null) {
            logger.error("Failed to register database service!");
        } else {
            logger.info(String.format("Registered database manager service [%s]", IDatabaseService.class.hashCode()));
        }

        if (servicesManager.getRegistration(IKeyManager.class) == null) {
            logger.error("Failed to register key manager service!");
        } else {
            logger.info(String.format("Registered key manager service [%s]", IKeyManager.class.hashCode()));
        }

        if (servicesManager.getRegistration(ISerializer.class) == null) {
            logger.error("Failed to register serializer service!");
        } else {
            logger.info(String.format("Registered serializer service [%s]", ISerializer.class.hashCode()));
        }
    }
}
