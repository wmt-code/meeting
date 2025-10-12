package org.lzg.meeting.service.impl;

import org.lzg.meeting.model.entity.Friendship;
import org.lzg.meeting.mapper.FriendshipMapper;
import org.lzg.meeting.service.IFriendshipService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 好友关系表 服务实现类
 * </p>
 *
 * @author lzg
 * @since 2025-10-11
 */
@Service
public class FriendshipServiceImpl extends ServiceImpl<FriendshipMapper, Friendship> implements IFriendshipService {

}
