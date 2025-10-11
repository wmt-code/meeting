CREATE TABLE `user` (
                        `id` bigint NOT NULL COMMENT '用户id',
                        `userAccount` varchar(32) COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户账号',
                        `userPassword` varchar(255) COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户密码',
                        `userName` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '用户名称',
                        `avatar` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '用户头像',
                        `email` varchar(128) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '用户邮箱',
                        `status` tinyint NOT NULL DEFAULT '1' COMMENT '用户状态 1启用 0禁用',
                        `userRole` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'user' COMMENT '用户角色',
                        `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `editTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '编辑时间',
                        `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除 1已删除 0 未删除',
                        `meetingNo` int DEFAULT NULL COMMENT '会议号',
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `idx_userAccount` (`userAccount`) USING BTREE COMMENT '用户账号唯一索引',
                        UNIQUE KEY `idx_email` (`email`) USING BTREE COMMENT '邮箱唯一索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';


CREATE TABLE `meeting_reserve_member` (
                                          `meetingId` bigint NOT NULL COMMENT '会议ID',
                                          `invitateUserId` bigint NOT NULL COMMENT '受邀用户的ID',
                                          PRIMARY KEY (`meetingId`,`invitateUserId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='邀请用户表';

CREATE TABLE `meeting_reserve` (
                                   `meetingId` bigint NOT NULL COMMENT '会议ID',
                                   `meetingName` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '会议名称',
                                   `createTime` datetime DEFAULT NULL COMMENT '创建时间',
                                   `createUserId` bigint DEFAULT NULL COMMENT '创建人ID',
                                   `joinType` tinyint(1) DEFAULT NULL COMMENT '加入会议的类型，例如直接加入或者要密码加入',
                                   `joinPassword` varchar(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '加入密码',
                                   `status` tinyint(1) DEFAULT NULL COMMENT '预约的会议状态 0-已预约 1-已取消 2-已完成',
                                   `startTime` datetime DEFAULT NULL COMMENT '会议开始时间',
                                   `duration` int DEFAULT NULL COMMENT '会议持续时间',
                                   PRIMARY KEY (`meetingId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='会议预约表';


CREATE TABLE `meeting_member` (
                                  `id` int unsigned NOT NULL COMMENT 'id',
                                  `meetingId` bigint DEFAULT NULL COMMENT '会议ID',
                                  `userId` bigint DEFAULT NULL COMMENT '用户ID',
                                  `nickName` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '昵称',
                                  `lastJoinTime` datetime DEFAULT NULL COMMENT '最后加入时间',
                                  `status` tinyint(1) DEFAULT NULL COMMENT '成员状态 被踢出、拉入黑名单',
                                  `memberType` tinyint(1) DEFAULT NULL COMMENT '成员类型 主持人、普通成员',
                                  `meetingStatus` tinyint(1) DEFAULT NULL COMMENT '会议状态 进行中、已关闭',
                                  PRIMARY KEY (`id`),
                                  UNIQUE KEY `idx_meetingId_userId` (`meetingId`,`userId`) USING BTREE COMMENT '会议ID和用户ID唯一索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='会议成员关联表';

CREATE TABLE `meeting` (
                           `id` bigint NOT NULL COMMENT '会议ID',
                           `meetingName` varchar(128) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '会议名称',
                           `createTime` datetime DEFAULT NULL COMMENT '创建时间',
                           `createUserId` bigint DEFAULT NULL COMMENT '创建人ID',
                           `joinType` tinyint(1) DEFAULT NULL COMMENT '加入会议的类型，例如直接加入或者要密码加入',
                           `joinPassword` varchar(5) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '加入密码',
                           `startTime` datetime DEFAULT NULL COMMENT '会议开始时间',
                           `endTime` datetime DEFAULT NULL COMMENT '会议结束时间',
                           `status` tinyint(1) DEFAULT NULL COMMENT '会议状态 1：进行中 0：已结束 -1：已取消',
                           `meetingNo` int DEFAULT NULL COMMENT '会议号',
                           PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='会议表';

CREATE TABLE `friendship` (
                              `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
                              `userId` bigint NOT NULL COMMENT '用户ID',
                              `friendId` bigint NOT NULL COMMENT '好友ID',
                              `status` tinyint NOT NULL DEFAULT '0' COMMENT '关系状态：0待通过 1好友 2被删除 3被拉黑',
                              `createTime` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              `updateTime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `uk_user_friend` (`userId`,`friendId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='好友关系表';


CREATE TABLE `friend_request` (
                                  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
                                  `fromUserId` bigint NOT NULL COMMENT '申请人ID',
                                  `toUserId` bigint NOT NULL COMMENT '接收人ID',
                                  `message` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '申请附言',
                                  `status` tinyint DEFAULT '0' COMMENT '状态：0待处理 1同意 2拒绝',
                                  `createTime` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
                                  `updateTime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='好友申请表';
