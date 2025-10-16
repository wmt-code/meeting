package org.lzg.meeting.config;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * ShardingSphere 数据分片配置类
 * 用于配置 chat_message 表的分片规则
 *
 * @author lzg
 * @date 2025-10-16
 */
@Configuration
public class ShardingSphereConfig {

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    /**
     * 分表数量，默认4个表，编号从1开始
     */
    private static final int TABLE_COUNT = 4;

    /**
     * 创建 ShardingSphere 数据源
     * 配置分片规则并返回数据源对象
     *
     * @return ShardingSphere 数据源
     * @throws SQLException SQL异常
     */
    @Bean(name = "dataSource")
    @Primary
    public DataSource shardingSphereDataSource() throws SQLException {
        Map<String, DataSource> dataSourceMap = createDataSourceMap();
        Collection<RuleConfiguration> ruleConfigs = createRuleConfigurations();
        Properties props = new Properties();
        props.setProperty("sql-show", "true");
        return ShardingSphereDataSourceFactory.createDataSource(dataSourceMap, ruleConfigs, props);
    }

    /**
     * 创建真实数据源映射
     *
     * @return 数据源映射
     */
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(driverClassName);
        String fullJdbcUrl = jdbcUrl;
        if (!jdbcUrl.contains("?")) {
            fullJdbcUrl += "?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false";
        }
        dataSource.setJdbcUrl(fullJdbcUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setMaximumPoolSize(50);
        dataSource.setMinimumIdle(10);
        dataSourceMap.put("ds", dataSource);
        return dataSourceMap;
    }

    /**
     * 创建分片规则配置集合
     *
     * @return 规则配置集合
     */
    private Collection<RuleConfiguration> createRuleConfigurations() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTables().add(createChatMessageTableRule());
        shardingRuleConfig.setShardingAlgorithms(createShardingAlgorithms());
        return Collections.singleton(shardingRuleConfig);
    }

    /**
     * 创建聊天消息表的分片规则配置
     *
     * @return 表分片规则配置
     */
    private ShardingTableRuleConfiguration createChatMessageTableRule() {
        ShardingTableRuleConfiguration tableRuleConfig = new ShardingTableRuleConfiguration(
                "chat_message",
                "ds.chat_message_$->{1..4}"
        );
        tableRuleConfig.setTableShardingStrategy(
                new StandardShardingStrategyConfiguration("meetingId", "chat_message_inline")
        );
        return tableRuleConfig;
    }

    /**
     * 创建分片算法配置映射
     *
     * @return 分片算法配置映射
     */
    private Map<String, AlgorithmConfiguration> createShardingAlgorithms() {
        Map<String, AlgorithmConfiguration> algorithms = new HashMap<>();
        Properties props = new Properties();
        props.setProperty("algorithm-expression", "chat_message_$->{meetingId % " + TABLE_COUNT + " + 1}");
        AlgorithmConfiguration algorithmConfig = new AlgorithmConfiguration("INLINE", props);
        algorithms.put("chat_message_inline", algorithmConfig);
        return algorithms;
    }
}
