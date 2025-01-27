package com.amazonaws.services.msf;

import java.sql.Timestamp;
import java.time.Instant;

import org.apache.commons.lang3.RandomUtils;
import org.apache.flink.connector.datagen.source.GeneratorFunction;

public class StockPriceGeneratorFunction implements GeneratorFunction<Long, StockPrice> {
    private static final String[] TICKERS = {"AAPL", "AMZN", "MSFT", "INTC", "TBV", "TSLA"};

    @Override
    public StockPrice map(Long aLong) {
        return new StockPrice(
                new Timestamp(Instant.now().toEpochMilli()),
                TICKERS[RandomUtils.nextInt(0, TICKERS.length)],
                RandomUtils.nextDouble(10,100)
        );
    }
}