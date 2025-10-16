package org.lzg.meeting.service;

import org.lzg.meeting.model.dto.*;
import org.lzg.meeting.model.entity.User;
import org.lzg.meeting.model.vo.CaptchaVO;
import org.lzg.meeting.model.vo.UserPageVO;
import org.lzg.meeting.model.vo.UserVO;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

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

	// ==================== 管理员功能 ====================

	/**
	 * 分页查询用户列表（管理员）
	 * @param queryDTO 查询条件
	 * @return 用户分页列表
	 */
	UserPageVO listUsersByPage(UserQueryDTO queryDTO);

	/**
	 * 根据ID获取用户详情（管理员）
	 * @param id 用户ID
	 * @return 用户详情
	 */
	UserVO getUserVOById(Long id);

	/**
	 * 更新用户信息（管理员）
	 * @param updateDTO 更新信息
	 * @return 是否更新成功
	 */
	Boolean updateUserByAdmin(AdminUpdateUserDTO updateDTO);

	/**
	 * 删除用户（管理员）
	 * @param id 用户ID
	 * @return 是否删除成功
	 */
	Boolean deleteUserById(Long id);

	/**
	 * 批量删除用户（管理员）
	 * @param ids 用户ID列表
	 * @return 是否删除成功
	 */
	Boolean batchDeleteUsers(List<Long> ids);

	/**
	 * 更新用户状态（管理员）
	 * @param id 用户ID
	 * @param status 状态 1启用 0禁用
	 * @return 是否更新成功
	 */
	Boolean updateUserStatus(Long id, Integer status);

	/**
	 * 重置用户密码（管理员）
	 * @param id 用户ID
	 * @return 新密码
	 */
	String resetUserPassword(Long id);

}
