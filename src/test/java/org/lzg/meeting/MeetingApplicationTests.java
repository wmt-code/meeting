package org.lzg.meeting;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.lzg.meeting.constant.UserConstant;
import org.lzg.meeting.utils.RedisUtil;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MeetingApplicationTests {
	@Resource
	private RedisUtil redisUtil;
	@Test
	void contextLoads() {
		Long expire = redisUtil.getExpire(UserConstant.TOKEN + "b83dc70c115b13a0f8158c998e9a7bd8");
		System.out.println(expire);
	}
}
