package com.alert_engine.repository;

import com.alert_engine.model.AlertConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlertConfigRepository extends JpaRepository <AlertConfig, Long> {
    /** All configs for a given user, regardless of enabled state. UI listing. */
    List<AlertConfig> findByUserId(Long userId);

    /** User-scoped single-config lookup — every mutation path should go through this
     *  (never plain findById) so users can't touch each other's configs  */
    Optional<AlertConfig> findByIdAndUserId(Long id, Long userId);

    /** Active configs the evaluator needs to check. Grouped by ticker so we can
     *  iterate over each quote's configs efficiently. */
    List<AlertConfig> findByTickerAndEnabledTrue(String ticker);

    /** All distinct tickers currently being watched — used by the price poller
     *  to know which symbols to fetch from Finnhub. */
    @org.springframework.data.jpa.repository.Query(
            "SELECT DISTINCT a.ticker FROM AlertConfig a WHERE a.enabled = true"
    )
    List<String> findDistinctActiveTickers();
}
