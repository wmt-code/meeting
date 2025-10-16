package org.lzg.meeting.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.lzg.meeting.component.CosComponent;
import org.lzg.meeting.component.RedisComponent;
import org.lzg.meeting.constant.UserConstant;
import org.lzg.meeting.enums.UserStatusEnum;
import org.lzg.meeting.exception.BusinessException;
import org.lzg.meeting.exception.ErrorCode;
import org.lzg.meeting.exception.ThrowUtils;
import org.lzg.meeting.mapper.UserMapper;
import org.lzg.meeting.model.dto.*;
import org.lzg.meeting.model.entity.User;
import org.lzg.meeting.model.vo.CaptchaVO;
import org.lzg.meeting.model.vo.UserPageVO;
import org.lzg.meeting.model.vo.UserVO;
import org.lzg.meeting.service.IUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author lzg
 * @since 2025-09-29
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
	@Resource
	private RedisComponent redisComponent;

	@Resource
	private CosComponent cosComponent;

	@Override
	public String login(UserLoginDTO userLoginDTO) {
		String userAccount = userLoginDTO.getUserAccount();
		String userPassword = userLoginDTO.getUserPassword();
		String captchaKey = userLoginDTO.getCaptchaKey();
		String captchaCode = userLoginDTO.getCaptchaCode();
		if (StrUtil.hasBlank(userAccount, userPassword, captchaKey, captchaCode)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// 验证码校验
		checkCaptcha(captchaKey, captchaCode);
		checkAccAndPwd(userAccount, userPassword);
		// 判断用户是否存在
		User user = this.getOne(new QueryWrapper<User>().eq("userAccount", userAccount));
		if (user == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
		}
		// 判断用户是否被禁用
		if (user.getStatus().equals(UserStatusEnum.DISABLE.getValue())) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已被禁用");
		}
		// 密码校验
		if (!encPassword(userPassword).equals(user.getUserPassword())) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
		}
		// 生成token
		String token = SecureUtil.md5("" + user.getId() + System.currentTimeMillis());
		TokenUserInfo tokenUserInfo = new TokenUserInfo();
		tokenUserInfo.setMeetingNo(user.getMeetingNo());
		tokenUserInfo.setUserRole(user.getUserRole());
		tokenUserInfo.setUserId(user.getId());
		tokenUserInfo.setUserName(user.getUserName());
		// redis存入 用户ID对应的token
		redisComponent.saveToken(user.getId(), token);
		// redis存入 token对应的用户信息
		redisComponent.saveTokenUserInfo(token, tokenUserInfo);
		return token;
	}

	@Override
	public Boolean register(UserRegisterDTO userRegisterDTO) {
		String userAccount = userRegisterDTO.getUserAccount();
		String userPassword = userRegisterDTO.getUserPassword();
		String checkPassword = userRegisterDTO.getCheckPassword();
		String captchaKey = userRegisterDTO.getCaptchaKey();
		String captchaCode = userRegisterDTO.getCaptchaCode();
		if (StrUtil.hasBlank(userAccount, userPassword, checkPassword, captchaKey, captchaCode)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		checkAccAndPwd(userAccount, userPassword);
		if (!userPassword.equals(checkPassword)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
		}
		if (this.getOne(new QueryWrapper<User>().eq("userAccount", userAccount)) != null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已存在");
		}
		User user = new User();
		user.setUserAccount(userAccount);
		user.setUserPassword(encPassword(userPassword));
		user.setUserName(userAccount);
		user.setUserRole(UserConstant.DEFAULT_ROLE);
		// 设置随机会议号
		user.setMeetingNo(Math.abs(RandomUtil.randomInt()));
		boolean save = this.save(user);
		ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR);
		return true;
	}

	@Override
	public CaptchaVO getCaptcha() {
		// 定义图形验证码的长和宽
		LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(128, 64);

		String captchaKey = "captcha:" + System.currentTimeMillis() + ":" + java.util.UUID.randomUUID();
		String code = lineCaptcha.getCode();
		String imageBase64 = lineCaptcha.getImageBase64();
		// 设置60s超时时间
		redisComponent.saveCaptcha(captchaKey, code);
		CaptchaVO captchaVO = new CaptchaVO();
		captchaVO.setCaptchaKey(captchaKey);
		captchaVO.setCaptchaBase64(imageBase64);
		return captchaVO;
	}

	private static void checkAccAndPwd(String userAccount, String userPassword) {
		// 账号密码校验
		if (userAccount.length() < 4 || userAccount.length() > 20) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度必须在4~20之间");
		}
		if (userPassword.length() < 8 || userPassword.length() > 20) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度必须在8~20之间");
		}
	}

	private String encPassword(String password) {
		return SecureUtil.md5(password + UserConstant.SALT);
	}

	private void checkCaptcha(String captchaKey, String captchaCode) {
		String code = redisComponent.get(captchaKey);
		// 验证码不存在或已过期
		if (code == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码已过期");
		}
		// 验证码校验
		if (!code.equals(captchaCode)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误");
		}
	}

	@Override
	public Boolean updateUserInfo(Long userId, UserUpdateDTO userUpdateDTO) {
		if (userId == null || userUpdateDTO == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}

		User user = this.getById(userId);
		if (user == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
		}

		// 更新用户信息
		User updateUser = new User();
		updateUser.setId(userId);

		if (StrUtil.isNotBlank(userUpdateDTO.getUserName())) {
			if (userUpdateDTO.getUserName().length() > 50) {
				throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名称长度不能超过50");
			}
			updateUser.setUserName(userUpdateDTO.getUserName());
		}

		if (StrUtil.isNotBlank(userUpdateDTO.getEmail())) {
			// 简单的邮箱格式验证
			if (!userUpdateDTO.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
				throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式不正确");
			}
			updateUser.setEmail(userUpdateDTO.getEmail());
		}

		boolean updated = this.updateById(updateUser);
		if (!updated) {
			throw new BusinessException(ErrorCode.OPERATION_ERROR, "更新用户信息失败");
		}

		// 更新Redis中的用户信息
		TokenUserInfo tokenUserInfo = redisComponent.getTokenUserInfoByUserId(userId);
		if (tokenUserInfo != null && StrUtil.isNotBlank(userUpdateDTO.getUserName())) {
			tokenUserInfo.setUserName(userUpdateDTO.getUserName());
			String token = redisComponent.getToken(userId);
			if (StrUtil.isNotBlank(token)) {
				redisComponent.saveTokenUserInfo(token, tokenUserInfo);
			}
		}

		return true;
	}

	@Override
	public Boolean updatePassword(Long userId, UserPasswordDTO userPasswordDTO) {
		if (userId == null || userPasswordDTO == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}

		String oldPassword = userPasswordDTO.getOldPassword();
		String newPassword = userPasswordDTO.getNewPassword();
		String confirmPassword = userPasswordDTO.getConfirmPassword();

		if (StrUtil.hasBlank(oldPassword, newPassword, confirmPassword)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码不能为空");
		}

		// 新密码和确认密码校验
		if (!newPassword.equals(confirmPassword)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的新密码不一致");
		}

		// 新密码长度校验
		if (newPassword.length() < 8 || newPassword.length() > 20) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "新密码长度必须在8~20之间");
		}

		// 查询用户
		User user = this.getById(userId);
		if (user == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
		}

		// 验证旧密码
		if (!encPassword(oldPassword).equals(user.getUserPassword())) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "旧密码错误");
		}

		// 新旧密码不能相同
		if (oldPassword.equals(newPassword)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "新密码不能与旧密码相同");
		}

		// 更新密码
		User updateUser = new User();
		updateUser.setId(userId);
		updateUser.setUserPassword(encPassword(newPassword));

		boolean updated = this.updateById(updateUser);
		if (!updated) {
			throw new BusinessException(ErrorCode.OPERATION_ERROR, "修改密码失败");
		}

		return true;
	}

	@Override
	public String uploadAvatar(Long userId, MultipartFile file) {
		if (userId == null || file == null || file.isEmpty()) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
		}

		// 验证文件类型
		String contentType = file.getContentType();
		if (contentType == null || !contentType.startsWith("image/")) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "只能上传图片文件");
		}

		// 验证文件大小（限制5MB）
		if (file.getSize() > 5 * 1024 * 1024) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "头像文件大小不能超过5MB");
		}

		// 查询用户
		User user = this.getById(userId);
		if (user == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
		}

		// 删除旧头像
		String oldAvatar = user.getAvatar();

		// 上传新头像
		String avatarUrl = cosComponent.uploadFile(file, "avatar");

		// 更新用户头像
		User updateUser = new User();
		updateUser.setId(userId);
		updateUser.setAvatar(avatarUrl);

		boolean updated = this.updateById(updateUser);
		if (!updated) {
			// 如果更新失败，删除新上传的头像
			cosComponent.deleteFile(avatarUrl);
			throw new BusinessException(ErrorCode.OPERATION_ERROR, "更新头像失败");
		}

		// 删除旧头像（如果存在）
		if (StrUtil.isNotBlank(oldAvatar)) {
			try {
				cosComponent.deleteFile(oldAvatar);
			} catch (Exception e) {
				// 删除旧头像失败不影响整体流程，只记录日志
				log.warn("删除旧头像失败: {}", oldAvatar, e);
			}
		}

		return avatarUrl;
	}

	// ==================== 管理员功能实现 ====================

	@Override
	public UserPageVO listUsersByPage(UserQueryDTO queryDTO) {
		if (queryDTO == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}

		// 构建分页对象
		long current = queryDTO.getCurrent() != null ? queryDTO.getCurrent().longValue() : 1L;
		long pageSize = queryDTO.getPageSize() != null ? queryDTO.getPageSize().longValue() : 10L;
		Page<User> page = new Page<>(current, pageSize);

		// 构建查询条件
		QueryWrapper<User> queryWrapper = new QueryWrapper<>();

		// 用户名模糊查询
		if (StrUtil.isNotBlank(queryDTO.getUserName())) {
			queryWrapper.like("userName", queryDTO.getUserName());
		}

		// 账号模糊查询
		if (StrUtil.isNotBlank(queryDTO.getUserAccount())) {
			queryWrapper.like("userAccount", queryDTO.getUserAccount());
		}

		// 邮箱模糊查询
		if (StrUtil.isNotBlank(queryDTO.getEmail())) {
			queryWrapper.like("email", queryDTO.getEmail());
		}

		// 状态精确查询
		if (queryDTO.getStatus() != null) {
			queryWrapper.eq("status", queryDTO.getStatus());
		}

		// 角色精确查询
		if (StrUtil.isNotBlank(queryDTO.getUserRole())) {
			queryWrapper.eq("userRole", queryDTO.getUserRole());
		}

		// 排序
		String sortField = queryDTO.getSortField();
		String sortOrder = queryDTO.getSortOrder();
		if (StrUtil.isNotBlank(sortField)) {
			boolean isAsc = "asc".equalsIgnoreCase(sortOrder);
			queryWrapper.orderBy(true, isAsc, sortField);
		} else {
			// 默认按创建时间倒序
			queryWrapper.orderByDesc("createTime");
		}

		// 执行查询
		IPage<User> userPage = this.page(page, queryWrapper);

		// 转换为VO
		List<UserVO> userVOList = userPage.getRecords().stream()
				.map(this::convertToUserVO)
				.collect(Collectors.toList());

		// 构建返回结果
		UserPageVO pageVO = new UserPageVO();
		pageVO.setRecords(userVOList);
		pageVO.setTotal(userPage.getTotal());
		pageVO.setCurrent(userPage.getCurrent());
		pageVO.setSize(userPage.getSize());
		pageVO.setPages(userPage.getPages());

		return pageVO;
	}

	@Override
	public UserVO getUserVOById(Long id) {
		if (id == null || id <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}

		User user = this.getById(id);
		if (user == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
		}

		return convertToUserVO(user);
	}

	@Override
	public Boolean updateUserByAdmin(AdminUpdateUserDTO updateDTO) {
		if (updateDTO == null || updateDTO.getId() == null || updateDTO.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}

		// 验证用户是否存在
		User existUser = this.getById(updateDTO.getId());
		if (existUser == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
		}

		// 构建更新对象
		User updateUser = new User();
		updateUser.setId(updateDTO.getId());

		// 更新用户名
		if (StrUtil.isNotBlank(updateDTO.getUserName())) {
			if (updateDTO.getUserName().length() > 20) {
				throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名长度不能超过20个字符");
			}
			updateUser.setUserName(updateDTO.getUserName());
		}

		// 更新邮箱
		if (StrUtil.isNotBlank(updateDTO.getEmail())) {
			// 邮箱格式验证
			if (!updateDTO.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
				throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式不正确");
			}
			updateUser.setEmail(updateDTO.getEmail());
		}

		// 更新状态
		if (updateDTO.getStatus() != null) {
			if (updateDTO.getStatus() != 0 && updateDTO.getStatus() != 1) {
				throw new BusinessException(ErrorCode.PARAMS_ERROR, "状态参数无效");
			}
			updateUser.setStatus(updateDTO.getStatus());
		}

		// 更新角色
		if (StrUtil.isNotBlank(updateDTO.getUserRole())) {
			if (!UserConstant.ADMIN_ROLE.equals(updateDTO.getUserRole()) 
					&& !UserConstant.DEFAULT_ROLE.equals(updateDTO.getUserRole())) {
				throw new BusinessException(ErrorCode.PARAMS_ERROR, "角色参数无效");
			}
			updateUser.setUserRole(updateDTO.getUserRole());
		}

		// 更新头像
		if (StrUtil.isNotBlank(updateDTO.getAvatar())) {
			updateUser.setAvatar(updateDTO.getAvatar());
		}

		// 执行更新
		boolean updated = this.updateById(updateUser);
		if (!updated) {
			throw new BusinessException(ErrorCode.OPERATION_ERROR, "更新用户信息失败");
		}

		return true;
	}

	@Override
	public Boolean deleteUserById(Long id) {
		if (id == null || id <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}

		User user = this.getById(id);
		if (user == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
		}

		// 不能删除管理员账号
		if (UserConstant.ADMIN_ROLE.equals(user.getUserRole())) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能删除管理员账号");
		}

		// 执行删除（逻辑删除）
		boolean deleted = this.removeById(id);
		if (!deleted) {
			throw new BusinessException(ErrorCode.OPERATION_ERROR, "删除用户失败");
		}

		// 删除Redis中的token信息
		try {
			redisComponent.cleanToken(id);
		} catch (Exception e) {
			log.warn("删除用户token失败: userId={}", id, e);
		}

		return true;
	}

	@Override
	public Boolean batchDeleteUsers(List<Long> ids) {
		if (ids == null || ids.isEmpty()) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}

		// 查询用户列表，验证不能删除管理员
		List<User> users = this.listByIds(ids);
		for (User user : users) {
			if (UserConstant.ADMIN_ROLE.equals(user.getUserRole())) {
				throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能删除管理员账号: " + user.getUserName());
			}
		}

		// 批量删除
		boolean deleted = this.removeByIds(ids);
		if (!deleted) {
			throw new BusinessException(ErrorCode.OPERATION_ERROR, "批量删除用户失败");
		}

		// 删除Redis中的token信息
		for (Long id : ids) {
			try {
				redisComponent.cleanToken(id);
			} catch (Exception e) {
				log.warn("删除用户token失败: userId={}", id, e);
			}
		}

		return true;
	}

	@Override
	public Boolean updateUserStatus(Long id, Integer status) {
		if (id == null || id <= 0 || status == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}

		if (status != 0 && status != 1) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "状态参数无效");
		}

		User user = this.getById(id);
		if (user == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
		}

		// 不能禁用管理员账号
		if (status == 0 && UserConstant.ADMIN_ROLE.equals(user.getUserRole())) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能禁用管理员账号");
		}

		User updateUser = new User();
		updateUser.setId(id);
		updateUser.setStatus(status);

		boolean updated = this.updateById(updateUser);
		if (!updated) {
			throw new BusinessException(ErrorCode.OPERATION_ERROR, "更新用户状态失败");
		}

		// 如果是禁用操作，删除Redis中的token
		if (status == 0) {
			try {
				redisComponent.cleanToken(id);
			} catch (Exception e) {
				log.warn("删除用户token失败: userId={}", id, e);
			}
		}

		return true;
	}

	@Override
	public String resetUserPassword(Long id) {
		if (id == null || id <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}

		User user = this.getById(id);
		if (user == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
		}

		// 生成8位随机密码
		String newPassword = RandomUtil.randomString(8);

		// 更新密码
		User updateUser = new User();
		updateUser.setId(id);
		updateUser.setUserPassword(encPassword(newPassword));

		boolean updated = this.updateById(updateUser);
		if (!updated) {
			throw new BusinessException(ErrorCode.OPERATION_ERROR, "重置密码失败");
		}

		// 删除Redis中的token，强制用户重新登录
		try {
			redisComponent.cleanToken(id);
		} catch (Exception e) {
			log.warn("删除用户token失败: userId={}", id, e);
		}

		return newPassword;
	}

	/**
	 * 将User实体转换为UserVO
	 */
	private UserVO convertToUserVO(User user) {
		if (user == null) {
			return null;
		}

		UserVO userVO = new UserVO();
		BeanUtils.copyProperties(user, userVO);
		return userVO;
	}
}
