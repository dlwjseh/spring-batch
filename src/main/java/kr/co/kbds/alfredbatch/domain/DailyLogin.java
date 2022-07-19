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

	private String yearMonth;
	private String day;
	private Long productId;
	private Integer loginCount;

	public DailyLogin(String yearMonth, String day, Long productId, Integer loginCount) {
		this.yearMonth = yearMonth;
		this.day = day;
		this.productId = productId;
		this.loginCount = loginCount;
	}
}
