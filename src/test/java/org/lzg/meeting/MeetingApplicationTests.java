package org.lzg.meeting;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.lzg.meeting.model.entity.User;
import org.lzg.meeting.service.IUserService;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MeetingApplicationTests {
	@Resource
	private IUserService userService;

	@Test
	void contextLoads() {
		Page<User> page = userService.page(new Page<>(1, 10));
		Page<User> userPage = new Page<>();
		userPage.setCurrent(page.getCurrent());
		userPage.setSize(page.getSize());
		userPage.setTotal(page.getTotal());
		userPage.setRecords(page.getRecords());
		System.out.println(userPage);
	}
}
