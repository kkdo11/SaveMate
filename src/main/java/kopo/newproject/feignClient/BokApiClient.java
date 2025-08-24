package kopo.newproject.feignClient;

import kopo.newproject.dto.BokApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 한국은행(BOK) 경제통계시스템(ECOS) API와 연동하기 위한 Feign 클라이언트 인터페이스.
 * <p>
 * {@code @FeignClient} - 이 인터페이스가 Feign 클라이언트임을 선언합니다.
 *   - {@code name}: Feign 클라이언트의 논리적인 이름 (서비스 디스커버리 사용 시 유용).
 *   - {@code url}: 호출할 외부 API의 기본 URL.
 */
@FeignClient(name = "bokApiClient", url = "https://ecos.bok.or.kr/api")
public interface BokApiClient {

    /**
     * 한국은행 ECOS API의 'StatisticSearch' 엔드포인트를 호출하여 통계 데이터를 조회합니다.
     * <p>
     * 이 메소드는 {@code GET} 요청을 통해 지정된 파라미터에 해당하는 통계 데이터를 가져옵니다.
     *
     * @param apiKey     한국은행 ECOS API 인증키
     * @param format     응답 데이터 형식 (예: "json", "xml")
     * @param lang       응답 언어 (예: "kr", "en")
     * @param startCount 조회 시작 인덱스 (페이징 처리용)
     * @param endCount   조회 종료 인덱스 (페이징 처리용)
     * @param statCode   통계표 코드 (예: "901Y009" - 소비자물가지수)
     * @param period     주기 (예: "M" - 월별, "D" - 일별)
     * @param startDate  조회 시작일 (YYYYMM 또는 YYYYMMDD 형식)
     * @param endDate    조회 종료일 (YYYYMM 또는 YYYYMMDD 형식)
     * @return 한국은행 API 응답 데이터를 담은 {@link BokApiResponse} 객체
     */
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