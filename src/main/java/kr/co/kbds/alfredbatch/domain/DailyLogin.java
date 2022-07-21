package kr.co.kbds.alfredbatch.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class DailyLogin {
    private String loginDay;
    private Long productId;
    private Integer loginCount;

    public DailyLogin(String loginDay, Long productId, Integer loginCount) {
        this.loginDay = loginDay;
        this.productId = productId;
        this.loginCount = loginCount;
    }
}
