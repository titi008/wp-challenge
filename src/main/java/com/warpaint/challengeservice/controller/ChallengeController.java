package com.warpaint.challengeservice.controller;

import com.warpaint.challengeservice.model.Asset;
import com.warpaint.challengeservice.model.Pricing;
import com.warpaint.challengeservice.service.ChallengeService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("market-data")
public class ChallengeController {

    private final ChallengeService challengeService;

    @RequestMapping("{asset}/historical")
    public List<Pricing> getHistoricalAssetData(@PathVariable Asset asset) {
        return challengeService.getHistoricalAssetData(asset);
    }

    @RequestMapping("{asset}/projected")
    public List<Pricing> getProjectedAssetData(@PathVariable Asset asset) {
        return challengeService.getProjectedAssetData(asset);
    }
}
