package moe.quill.stratumcommon.Database;

import moe.quill.StratumCommon.Database.DataTypes.MarketData;
import moe.quill.StratumCommon.Database.IDatabaseService;
import moe.quill.stratumcommon.db.tables.records.MarketdataRecord;
import org.bukkit.Material;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.UpdateConditionStep;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import static moe.quill.stratumcommon.db.Tables.MARKETDATA;

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
}
