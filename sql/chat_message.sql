-- 聊天消息表 SQL 脚本
-- 支持根据会议ID分表（使用ShardingSphere）

-- 创建聊天消息表模板（实际表名会根据分表规则生成，如 chat_message_0, chat_message_1 等）
CREATE TABLE IF NOT EXISTS `chat_message` (
    `id` BIGINT NOT NULL COMMENT '消息id',
    `meetingId` BIGINT NOT NULL COMMENT '会议id',
    `senderId` BIGINT NOT NULL COMMENT '发送者用户id',
    `senderName` VARCHAR(100) NOT NULL COMMENT '发送者用户名',
    `receiverId` BIGINT DEFAULT NULL COMMENT '接收者用户id（私聊时使用）',
    `receiverName` VARCHAR(100) DEFAULT NULL COMMENT '接收者用户名（私聊时使用）',
    `messageType` INT NOT NULL COMMENT '消息类型 1-文本 2-图片 3-视频 4-文件 5-语音',
    `messageScope` INT NOT NULL COMMENT '消息范围 1-私聊 2-群聊',
    `content` TEXT DEFAULT NULL COMMENT '消息内容（文本消息）',
    `fileUrl` VARCHAR(500) DEFAULT NULL COMMENT '文件URL（图片、视频、文件消息）',
    `thumbnailUrl` VARCHAR(500) DEFAULT NULL COMMENT '缩略图URL（图片、视频消息）',
    `fileName` VARCHAR(255) DEFAULT NULL COMMENT '文件名（文件消息）',
    `fileSize` BIGINT DEFAULT NULL COMMENT '文件大小（字节）',
    `fileType` VARCHAR(100) DEFAULT NULL COMMENT '文件类型/MIME类型',
    `status` INT NOT NULL DEFAULT 1 COMMENT '消息状态 1-正常 0-已删除 2-已撤回',
    `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_meetingId` (`meetingId`),
    KEY `idx_senderId` (`senderId`),
    KEY `idx_receiverId` (`receiverId`),
    KEY `idx_messageScope` (`messageScope`),
    KEY `idx_createTime` (`createTime`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';

-- 创建实际的分表（4个表，从1开始编号）
-- 注意：使用 ShardingSphere 后，这些表会自动路由，无需手动创建
-- 这里只是示例，实际表名和数量由 ShardingSphere 配置决定

CREATE TABLE IF NOT EXISTS `chat_message_1` LIKE `chat_message`;
CREATE TABLE IF NOT EXISTS `chat_message_2` LIKE `chat_message`;
CREATE TABLE IF NOT EXISTS `chat_message_3` LIKE `chat_message`;
CREATE TABLE IF NOT EXISTS `chat_message_4` LIKE `chat_message`;
