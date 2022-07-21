package kr.co.kbds.alfredbatch.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class MonthlyLogin {
    private String loginYearMonth;
    private Long productId;
    private Integer loginCount;

    public MonthlyLogin(String loginYearMonth, Long productId, Integer loginCount) {
        this.loginYearMonth = loginYearMonth;
        this.productId = productId;
        this.loginCount = loginCount;
    }
}
