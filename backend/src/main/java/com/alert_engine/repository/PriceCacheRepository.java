package com.alert_engine.repository;

import com.alert_engine.model.PriceCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriceCacheRepository extends JpaRepository<PriceCache, String> {

    /** Bulk fetch for the dashboard: one call returns all watched tickers' latest prices. */
    List<PriceCache> findByTickerIn(List<String> tickers);
}