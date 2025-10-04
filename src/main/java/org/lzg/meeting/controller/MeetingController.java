package org.lzg.meeting.controller;


import org.lzg.meeting.annotation.GlobalInterceptor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 会议表 前端控制器
 * </p>
 *
 * @author lzg
 * @since 2025-10-03
 */
@RestController
@RequestMapping("/meeting")
public class MeetingController {
	@GetMapping("/hello")
	@GlobalInterceptor(checkAdmin = true)
	public String hello() {
		return "hello";
	}

}
