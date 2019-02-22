package com.warpaint.challengeservice.service;

import com.warpaint.challengeservice.dataprovider.YahooFinanceClient;
import com.warpaint.challengeservice.model.Asset;
import com.warpaint.challengeservice.model.Pricing;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class ChallengeService {

    private final YahooFinanceClient dataProvider;

    public List<Pricing> getHistoricalAssetData(Asset asset) {
        log.info("Fetching historical price data");
        // TODO Implement getHistoricalAssetData()
        return null;
    }

    public List<Pricing> getProjectedAssetData(Asset asset) {
        log.info("Generating projected price data");
        // TODO Implement getProjectedAssetData()
        return null;
    }

}
