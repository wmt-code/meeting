package org.lzg.meeting.service;

import org.lzg.meeting.model.dto.UserLoginDTO;
import org.lzg.meeting.model.dto.UserPasswordDTO;
import org.lzg.meeting.model.dto.UserRegisterDTO;
import org.lzg.meeting.model.dto.UserUpdateDTO;
import org.lzg.meeting.model.entity.User;
import org.lzg.meeting.model.vo.CaptchaVO;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author lzg
 * @since 2025-09-29
 */
public interface IUserService extends IService<User> {
    /**
     * 登录
     * @param userLoginDTO 登录参数
     * @return 用户token
     */
	String login(UserLoginDTO userLoginDTO);
    /**
     * 注册
     * @param userRegisterDTO 注册参数
     * @return 是否注册成功
     */
    Boolean register(UserRegisterDTO userRegisterDTO);

	/**
	 * 获取验证码
	 * @return 验证码
	 */
	CaptchaVO getCaptcha();

	/**
	 * 更新用户信息
	 * @param userId 用户ID
	 * @param userUpdateDTO 用户信息
	 * @return 是否更新成功
	 */
	Boolean updateUserInfo(Long userId, UserUpdateDTO userUpdateDTO);

	/**
	 * 修改密码
	 * @param userId 用户ID
	 * @param userPasswordDTO 密码信息
	 * @return 是否修改成功
	 */
	Boolean updatePassword(Long userId, UserPasswordDTO userPasswordDTO);

	/**
	 * 上传头像
	 * @param userId 用户ID
	 * @param file 头像文件
	 * @return 头像URL
	 */
	String uploadAvatar(Long userId, MultipartFile file);

}
