package com.warpaint.challengeservice.controller;

import com.warpaint.challengeservice.model.Asset;
import com.warpaint.challengeservice.model.Pricing;
import com.warpaint.challengeservice.service.ChallengeService;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@AllArgsConstructor
@RequestMapping("market-data")
public class ChallengeController {

    private final ChallengeService challengeService;

    @RequestMapping("{asset}/historical")
    public List<Pricing> getHistoricalAssetData(@PathVariable Asset asset,
                                                @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd")
                                                        Optional<LocalDate> startDate,
                                                @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd")
                                                            Optional<LocalDate> endDate) {
        return challengeService.getHistoricalAssetData(asset, startDate, endDate);
    }

    @RequestMapping("{asset}/projected")
    public List<Pricing> getProjectedAssetData(@PathVariable Asset asset) {
        return challengeService.getProjectedAssetData(asset);
    }
}
