package org.lzg.meeting.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.lzg.meeting.model.entity.File;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 文件表 Mapper 接口
 * </p>
 *
 * @author lzg
 * @since 2025-10-15
 */
@Mapper
public interface FileMapper extends BaseMapper<File> {

}
