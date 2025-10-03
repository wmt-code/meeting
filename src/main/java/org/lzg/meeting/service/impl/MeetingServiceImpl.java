package org.lzg.meeting.service.impl;

import org.lzg.meeting.model.entity.Meeting;
import org.lzg.meeting.mapper.MeetingMapper;
import org.lzg.meeting.service.IMeetingService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 会议表 服务实现类
 * </p>
 *
 * @author lzg
 * @since 2025-10-03
 */
@Service
public class MeetingServiceImpl extends ServiceImpl<MeetingMapper, Meeting> implements IMeetingService {

}
