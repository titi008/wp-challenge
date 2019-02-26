package com.warpaint.challengeservice.controller;

import com.warpaint.challengeservice.model.Asset;
import com.warpaint.challengeservice.model.Pricing;
import com.warpaint.challengeservice.service.ChallengeService;
import com.warpaint.challengeservice.service.DateRangeProvider;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@AllArgsConstructor
@RequestMapping("market-data")
public class ChallengeController {

    public static final int DEFAULT_PREDICTABLE_MONTHS = 240;

    private final ChallengeService challengeService;

    private final DateRangeProvider dateRangeProvider;

    @RequestMapping("{asset}/historical")
    public List<Pricing> getHistoricalAssetData(@PathVariable Asset asset,
                                                @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd")
                                                        Optional<LocalDate> startDate,
                                                @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd")
                                                            Optional<LocalDate> endDate) throws Exception {
        LocalDate now = LocalDate.now();
        LocalDate validStartDate = dateRangeProvider.validateStartDate(startDate, now);
        LocalDate validEndDate = dateRangeProvider.validateEndDate(endDate, now);

        dateRangeProvider.validateDateRange(validStartDate, validEndDate);

        return challengeService.getHistoricalAssetData(asset, validStartDate, validEndDate);
    }

    @RequestMapping("{asset}/projected")
    public List<Pricing> getProjectedAssetData(@PathVariable Asset asset,
                                               @RequestParam("numberOfMonths") Optional<Integer> numberOfMonths)
            throws Exception {
        return challengeService.getProjectedAssetData(asset, numberOfMonths.orElse(DEFAULT_PREDICTABLE_MONTHS));
    }
}
