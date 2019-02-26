package com.warpaint.challengeservice.service;

import java.time.LocalDate;
import java.util.Optional;

public interface DateRangeProvider {

    boolean validateDateRange(LocalDate startDate, LocalDate endDate);

    LocalDate validateStartDate(Optional<LocalDate> startDate, LocalDate now);

    LocalDate validateEndDate(Optional<LocalDate> endDate, LocalDate now);

    LocalDate getProjectionStartDate(LocalDate now);
}
