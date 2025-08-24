package kopo.newproject.util;

public class CmmUtil {

    /**
     * 문자열이 null이거나 비어있는 경우, 지정된 대체 문자열로 변환합니다.
     * <p>
     * "Null Value Logic"의 약자로, 데이터 처리 시 NullPointerException을 방지하고
     * 기본값을 설정하는 데 유용합니다.
     *
     * @param str     원본 문자열
     * @param chg_str 원본 문자열이 null이거나 비어있을 경우 대체할 문자열
     * @return 변환된 문자열
     */
    public static String nvl(String str, String chg_str) {
        String res;
        if (str == null) {
            res = chg_str;
        } else if (str.isEmpty()) { // str.equals("") 대신 isEmpty() 사용 권장
            res = chg_str;
        } else {
            res = str;
        }
        return res;
    }

    /**
     * 문자열이 null이거나 비어있는 경우, 빈 문자열("")로 변환합니다.
     * <p>
     * {@link #nvl(String, String)} 메소드의 오버로드 버전으로, 대체 문자열을 빈 문자열로 고정합니다.
     *
     * @param str 원본 문자열
     * @return 변환된 문자열 (null 또는 빈 문자열인 경우 빈 문자열 반환)
     */
    public static String nvl(String str) {
        return nvl(str, "");
    }

    /**
     * HTML 폼의 체크박스(checkbox) 또는 라디오 버튼(radio button)에 'checked' 속성을 추가할지 결정합니다.
     * <p>
     * 원본 문자열과 비교 문자열이 같으면 " checked"를 반환하여 해당 요소가 선택되도록 합니다.
     *
     * @param str     원본 문자열 (예: 사용자가 선택한 값)
     * @param com_str 비교할 문자열 (예: 옵션의 값)
     * @return 두 문자열이 같으면 " checked", 그렇지 않으면 빈 문자열
     */
    public static String checked(String str, String com_str) {
        if (str.equals(com_str)) {
            return " checked";
        } else {
            return "";
        }
    }

    /**
     * HTML 폼의 체크박스(checkbox) 그룹에 'checked' 속성을 추가할지 결정합니다.
     * <p>
     * 문자열 배열 내에 비교할 문자열이 포함되어 있으면 " checked"를 반환합니다.
     *
     * @param str     원본 문자열 배열 (예: 사용자가 선택한 여러 값)
     * @param com_str 비교할 문자열 (예: 옵션의 값)
     * @return 배열 내에 비교할 문자열이 있으면 " checked", 그렇지 않으면 빈 문자열
     */
    public static String checked(String[] str, String com_str) {
        for (String s : str) { // 향상된 for 루프 사용
            if (s.equals(com_str))
                return " checked";
        }
        return "";
    }

    /**
     * HTML `<select>` 요소의 `<option>` 태그에 'selected' 속성을 추가할지 결정합니다.
     * <p>
     * 원본 문자열과 비교 문자열이 같으면 " selected"를 반환하여 해당 옵션이 기본 선택되도록 합니다.
     *
     * @param str     원본 문자열 (예: 사용자가 선택한 값)
     * @param com_str 비교할 문자열 (예: 옵션의 값)
     * @return 두 문자열이 같으면 " selected", 그렇지 않으면 빈 문자열
     */
    public static String select(String str, String com_str) {
        if (str.equals(com_str)) {
            return " selected";
        } else {
            return "";
        }
    }
}