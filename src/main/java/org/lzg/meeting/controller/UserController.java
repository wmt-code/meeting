package org.lzg.meeting.controller;


import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.lzg.meeting.common.BaseResponse;
import org.lzg.meeting.common.ResultUtils;
import org.lzg.meeting.exception.ErrorCode;
import org.lzg.meeting.exception.ThrowUtils;
import org.lzg.meeting.model.dto.UserLoginDTO;
import org.lzg.meeting.model.dto.UserRegisterDTO;
import org.lzg.meeting.model.vo.CaptchaVO;
import org.lzg.meeting.service.IUserService;
import org.lzg.meeting.utils.RedisUtil;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author lzg
 * @since 2025-09-29
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Resource
    private IUserService userService;
    @Resource
    private RedisUtil redisUtil;
    /**
     * 获取验证码
     *
     * @return 返回验证码的key和验证码图片base64
     */
    @GetMapping("/captcha")
    public BaseResponse<CaptchaVO> getCaptcha() {
        //定义图形验证码的长和宽
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(128, 64);

        String captchaKey = "captcha:" + System.currentTimeMillis() + ":" + java.util.UUID.randomUUID();
        String code = lineCaptcha.getCode();
        String imageBase64 = lineCaptcha.getImageBase64();
        //设置60s超时时间
        redisUtil.setEx(captchaKey, code, 60, TimeUnit.SECONDS);
        CaptchaVO captchaVO = new CaptchaVO();
        captchaVO.setCaptchaKey(captchaKey);
        captchaVO.setCaptchaBase64(imageBase64);
        return ResultUtils.success(captchaVO);
    }
    /**
     * 用户登录
     *
     * @param userLoginDTO 登录参数
     * @return 用户token
     */
    @PostMapping("/login")
    public BaseResponse<String> login(@RequestBody UserLoginDTO userLoginDTO) {
        ThrowUtils.throwIf(userLoginDTO == null, ErrorCode.PARAMS_ERROR);
        String token = userService.login(userLoginDTO);
        return ResultUtils.success(token);
    }
    @PostMapping("/register")
    public BaseResponse<Boolean> register(@RequestBody UserRegisterDTO userRegisterDTO) {
        ThrowUtils.throwIf(userRegisterDTO == null, ErrorCode.PARAMS_ERROR);
         Boolean result = userService.register(userRegisterDTO);
        return ResultUtils.success(result);
    }
}
