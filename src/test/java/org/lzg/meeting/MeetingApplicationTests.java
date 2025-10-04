package org.lzg.meeting;

import org.lzg.meeting.utils.JwtUtils;

import java.util.Map;

class MeetingApplicationTests {
	public static void main(String[] args) {
		Map<String, Object> parsedToken = JwtUtils.parseToken("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9" +
				".eyJzdWIiOiIxOTcyODk3NTQxNzk0NjU2MjU4IiwiZXhwIjoxNzYwMTc0MjYwLCJ1c2VyUm9sZSI6InVzZXIiLCJpYXQiOjE3NTk1Njk0NjAsInVzZXJJZCI6MTk3Mjg5NzU0MTc5NDY1NjI1OCwianRpIjoiMjg3Yjk3OWRjYjFiNDY3OTg4MTZmZDI5NmQ2Mzc0N2UifQ.vwtwUIc7gEq1oAFyGS3dmwrnn3Gwp_xIsBQ-IxroxT8");
		System.out.println(parsedToken);
	}
}
