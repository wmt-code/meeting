package org.lzg.meeting.service.impl;

import org.lzg.meeting.model.entity.MeetingMember;
import org.lzg.meeting.mapper.MeetingMemberMapper;
import org.lzg.meeting.service.IMeetingMemberService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 会议成员关联表 服务实现类
 * </p>
 *
 * @author lzg
 * @since 2025-10-03
 */
@Service
public class MeetingMemberServiceImpl extends ServiceImpl<MeetingMemberMapper, MeetingMember> implements IMeetingMemberService {

}
