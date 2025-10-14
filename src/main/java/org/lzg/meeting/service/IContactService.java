package org.lzg.meeting.service;

import org.lzg.meeting.model.dto.ContactSearchDTO;
import org.lzg.meeting.model.vo.ContactVO;

import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * 联系人服务接口
 */
public interface IContactService {

	/**
	 * 搜索联系人
	 * 支持按用户名、用户账号模糊搜索
	 *
	 * @param searchDTO     搜索参数
	 * @param currentUserId 当前用户ID
	 * @return 联系人列表（分页）
	 */
	IPage<ContactVO> searchContacts(ContactSearchDTO searchDTO, Long currentUserId);
}
