-- 文件表 SQL 脚本
-- 用于记录用户上传的文件信息

CREATE TABLE IF NOT EXISTS `file` (
    `id` BIGINT NOT NULL COMMENT '文件id',
    `userId` BIGINT NOT NULL COMMENT '用户id',
    `fileName` VARCHAR(255) NOT NULL COMMENT '文件名',
    `originalName` VARCHAR(255) NOT NULL COMMENT '文件原始名称',
    `fileSize` BIGINT NOT NULL COMMENT '文件大小（字节）',
    `fileType` VARCHAR(100) DEFAULT NULL COMMENT '文件类型/MIME类型',
    `filePath` VARCHAR(255) NOT NULL COMMENT '文件存储路径',
    `fileUrl` VARCHAR(500) NOT NULL COMMENT '文件访问URL',
    `status` INT NOT NULL DEFAULT 1 COMMENT '文件状态 1正常 0已删除',
    `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_userId` (`userId`),
    KEY `idx_createTime` (`createTime`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件表';
