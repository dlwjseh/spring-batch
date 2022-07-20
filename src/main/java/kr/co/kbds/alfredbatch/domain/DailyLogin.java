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

	private String loginYearMonth;
	private String loginDay;
	private Long productId;
	private Integer loginCount;

	public DailyLogin(String loginYearMonth, String loginDay, Long productId, Integer loginCount) {
		this.loginYearMonth = loginYearMonth;
		this.loginDay = loginDay;
		this.productId = productId;
		this.loginCount = loginCount;
	}
}
