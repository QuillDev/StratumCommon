package moe.quill.stratumcommon.Database;

import moe.quill.StratumCommon.Database.DataTypes.MarketData;
import moe.quill.StratumCommon.Database.DataTypes.RPGPlayer;
import moe.quill.StratumCommon.Database.IDatabaseService;
import moe.quill.stratumcommon.db.tables.records.MarketdataRecord;
import moe.quill.stratumcommon.db.tables.records.RpgplayersRecord;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.UpdateConditionStep;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import static moe.quill.stratumcommon.db.Tables.MARKETDATA;
import static moe.quill.stratumcommon.db.Tables.RPGPLAYERS;

public class SQLDataService implements IDatabaseService {

    private DSLContext context;
    private Connection connection;
    private String url;
    private String user;
    private String pass;

    public SQLDataService(String url, String user, String pass) {
        this.url = url;
        this.user = user;
        this.pass = pass;
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        this.connect();
    }

    /**
     * Forces a sql database connection if one does not exist
     */

    public void connect() {

        try {
            if (connection == null || context == null || connection.isClosed()) {
                this.connection = DriverManager.getConnection(url, user, pass);
                context = DSL.using(connection, SQLDialect.POSTGRES);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Get market data from the db
    public MarketData getMarketData(Material queryMat) {
        connect();
        final var result = context
                .select()
                .from(MARKETDATA)
                .where(MARKETDATA.MATERIAL.eq(queryMat.name()))
                .limit(1)
                .fetch();
        if (result.size() == 0) {
            return null;
        }

        final var record = result.get(0);
        final var materialString = record.get(MARKETDATA.MATERIAL);
        final var buyAmount = record.get(MARKETDATA.BUYAMOUNT);
        final var sellAmount = record.get(MARKETDATA.SELLAMOUNT);
        if (materialString == null || buyAmount == null || sellAmount == null) return null;
        final var material = Material.valueOf(materialString.toUpperCase());

        return new MarketData(material, buyAmount, sellAmount);
    }

    //save market data to the db
    public void saveMarketData(MarketData marketData) {
        connect();
        final var existingData = getMarketData(marketData.getMaterial());
        //If there is no existing data, create a new entry
        if (existingData == null) {
            createMarketData(marketData);
            return;
        }

        //Update the existing market data
        context.update(MARKETDATA)
                .set(MARKETDATA.BUYAMOUNT, marketData.getBuyAmount())
                .set(MARKETDATA.SELLAMOUNT, marketData.getSellAmount())
                .set(MARKETDATA.VOLUME, marketData.getBuyAmount() + marketData.getSellAmount())
                .where(MARKETDATA.MATERIAL.eq(marketData.getMaterial().name()))
                .execute();
    }

    //Save a list of market data to the db
    public void saveMarketData(Collection<MarketData> marketDataList) {
        final ArrayList<UpdateConditionStep<MarketdataRecord>> updates = new ArrayList<>();
        for (final var marketData : marketDataList) {
            updates.add(context.update(MARKETDATA)
                    .set(MARKETDATA.BUYAMOUNT, marketData.getBuyAmount())
                    .set(MARKETDATA.SELLAMOUNT, marketData.getSellAmount())
                    .set(MARKETDATA.VOLUME, marketData.getBuyAmount() + marketData.getSellAmount())
                    .where(MARKETDATA.MATERIAL.eq(marketData.getMaterial().name())));
        }
        context.batch(updates).execute();
    }

    //create a new instance of market data in the db
    public void createMarketData(MarketData marketData) {
        connect();
        context.insertInto(
                MARKETDATA,
                MARKETDATA.MATERIAL,
                MARKETDATA.BUYAMOUNT,
                MARKETDATA.SELLAMOUNT,
                MARKETDATA.VOLUME
        ).values(
                marketData.getMaterial().name(),
                marketData.getBuyAmount(),
                marketData.getSellAmount(),
                marketData.getBuyAmount() + marketData.getSellAmount()
        ).execute();
    }

    @Override
    public RPGPlayer getPlayer(UUID uuid) {
        connect();
        final var result = context
                .select()
                .from(RPGPLAYERS)
                .where(RPGPLAYERS.UUID.eq(uuid))
                .limit(1)
                .fetch();
        if (result.size() == 0) {
            return null;
        }

        final var record = result.get(0);
        int swordsLevel = record.get(RPGPLAYERS.SWORDS_LEVEL);
        int loggingLevel = record.get(RPGPLAYERS.LOGGING_LEVEL);
        int miningLevel = record.get(RPGPLAYERS.MINING_LEVEL);
        int foragingLevel = record.get(RPGPLAYERS.FORAGING_LEVEL);
        int archeryLevel = record.get(RPGPLAYERS.ARCHERY_LEVEL);
        int fishingLevel = record.get(RPGPLAYERS.FISHING_LEVEL);
        float swordsExperience = record.get(RPGPLAYERS.SWORDS_EXPERIENCE, Float.class);
        float loggingExperience = record.get(RPGPLAYERS.LOGGING_EXPERIENCE, Float.class);
        float miningExperience = record.get(RPGPLAYERS.MINING_EXPERIENCE, Float.class);
        float foragingExperience = record.get(RPGPLAYERS.FORAGING_EXPERIENCE, Float.class);
        float archeryExperience = record.get(RPGPLAYERS.ARCHERY_EXPERIENCE, Float.class);
        float fishingExperience = record.get(RPGPLAYERS.FISHING_EXPERIENCE, Float.class);

        return new RPGPlayer(
                uuid,
                swordsLevel,
                loggingLevel,
                miningLevel,
                foragingLevel,
                archeryLevel,
                fishingLevel,
                swordsExperience,
                loggingExperience,
                miningExperience,
                archeryExperience,
                fishingExperience,
                foragingExperience
        );
    }

    @Override
    public void savePlayer(RPGPlayer rpgPlayer) {
        connect();
        //Update the existing market data
        savePlayer(Collections.singletonList(rpgPlayer));
    }

    @Override
    public void savePlayer(Collection<RPGPlayer> collection) {
        connect();
        final ArrayList<UpdateConditionStep<RpgplayersRecord>> updates = new ArrayList<>();
        for (final var rpgPlayer : collection) {
            final var existingData = getPlayer(rpgPlayer.getUuid());
            //If there is no existing data, create a new entry
            if (existingData == null) {
                createPlayer(rpgPlayer.getUuid());
                continue;
            }
            updates.add(context.update(RPGPLAYERS)
                    .set(RPGPLAYERS.SWORDS_LEVEL, rpgPlayer.getSwordsLevel())
                    .set(RPGPLAYERS.LOGGING_LEVEL, rpgPlayer.getLoggingLevel())
                    .set(RPGPLAYERS.MINING_LEVEL, rpgPlayer.getMiningLevel())
                    .set(RPGPLAYERS.FORAGING_LEVEL, rpgPlayer.getForagingLevel())
                    .set(RPGPLAYERS.ARCHERY_LEVEL, rpgPlayer.getArcheryLevel())
                    .set(RPGPLAYERS.FISHING_LEVEL, rpgPlayer.getFishingLevel())
                    .set(RPGPLAYERS.SWORDS_EXPERIENCE, (double) rpgPlayer.getSwordsExperience())
                    .set(RPGPLAYERS.LOGGING_EXPERIENCE, (double) rpgPlayer.getLoggingExperience())
                    .set(RPGPLAYERS.MINING_EXPERIENCE, (double) rpgPlayer.getMiningExperience())
                    .set(RPGPLAYERS.FORAGING_EXPERIENCE, (double) rpgPlayer.getForagingExperience())
                    .set(RPGPLAYERS.ARCHERY_EXPERIENCE, (double) rpgPlayer.getArcheryExperience())
                    .set(RPGPLAYERS.FISHING_EXPERIENCE, (double) rpgPlayer.getFishingExperience())
                    .where(RPGPLAYERS.UUID.eq(rpgPlayer.getUuid())));
        }
        context.batch(updates).execute();
    }

    @Override
    public void createPlayer(UUID uuid) {
        context.insertInto(
                RPGPLAYERS,
                RPGPLAYERS.UUID
        ).values(
                uuid
        ).execute();
    }
}
