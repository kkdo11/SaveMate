
package kopo.newproject.client;

import kopo.newproject.dto.BokApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "bokApiClient", url = "https://ecos.bok.or.kr/api")
public interface BokApiClient {

    @GetMapping("/StatisticSearch/{apiKey}/{format}/{lang}/{startCount}/{endCount}/{statCode}/{period}/{startDate}/{endDate}")
    BokApiResponse getStatistics(
            @PathVariable("apiKey") String apiKey,
            @PathVariable("format") String format,
            @PathVariable("lang") String lang,
            @PathVariable("startCount") int startCount,
            @PathVariable("endCount") int endCount,
            @PathVariable("statCode") String statCode,
            @PathVariable("period") String period,
            @PathVariable("startDate") String startDate,
            @PathVariable("endDate") String endDate
    );
}
