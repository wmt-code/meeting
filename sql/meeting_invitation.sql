-- 会议邀请表
CREATE TABLE `meeting_invitation`
(
    `id`            bigint  NOT NULL AUTO_INCREMENT COMMENT '主键',
    `meetingId`     bigint  NOT NULL COMMENT '会议ID',
    `inviterId`     bigint  NOT NULL COMMENT '邀请人ID',
    `inviteeId`     bigint  NOT NULL COMMENT '被邀请人ID',
    `inviteType`    tinyint NOT NULL                        DEFAULT '0' COMMENT '邀请方式：0站内消息 1邮件 2短信',
    `status`        tinyint NOT NULL                        DEFAULT '0' COMMENT '邀请状态：0未响应 1已接受 2已拒绝 3已过期',
    `inviteMessage` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '邀请附言',
    `createTime`    datetime                                DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime`    datetime                                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `responseTime`  datetime                                DEFAULT NULL COMMENT '响应时间',
    PRIMARY KEY (`id`),
    KEY `idx_meetingId` (`meetingId`) COMMENT '会议ID索引',
    KEY `idx_inviteeId` (`inviteeId`) COMMENT '被邀请人ID索引',
    UNIQUE KEY `uk_meeting_invitee` (`meetingId`, `inviteeId`) COMMENT '会议和被邀请人唯一索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci COMMENT ='会议邀请表';
