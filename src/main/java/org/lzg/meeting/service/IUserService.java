package org.lzg.meeting.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.lzg.meeting.model.dto.UserLoginDTO;
import org.lzg.meeting.model.dto.UserRegisterDTO;
import org.lzg.meeting.model.entity.User;
import org.lzg.meeting.model.vo.UserVO;

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
	UserVO login(UserLoginDTO userLoginDTO);
    /**
     * 注册
     * @param userRegisterDTO 注册参数
     * @return 是否注册成功
     */
    Boolean register(UserRegisterDTO userRegisterDTO);
}
