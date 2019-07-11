/**
 * copyRight
 */
package com.gennlife.rws.timer;

import com.gennlife.rws.catche.CortrastiveCache;
import com.gennlife.rws.dao.ActiveSqlMapMapper;
import com.gennlife.rws.entity.ActiveIndexTask;
import com.gennlife.rws.service.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by liuzhen.
 * Date: 2017/11/1
 * Time: 16:12
 */
@Component
@Configuration
@EnableScheduling
public class ReSubmitTaskTimer {
    private Logger LOG = LoggerFactory.getLogger(ReSubmitTaskTimer.class);

    private Map<String,ActiveIndexTask> taskMap = new HashMap<String,ActiveIndexTask>();
    @Value("${task.failure.isRunningTimmer:0}")
    private int isRunningTimmer;
    @Autowired
    private ActiveSqlMapMapper activeSqlMapMapper;
    @Autowired
    private ProjectService projectService;

    /**
     * 定时清理_tmp 数据的数据 半夜十二点 清理
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void reSubmitTask(){
        activeSqlMapMapper.deleteByTmpActiveId();
        LOG.info("------执行了删除MySQL Tmp的 语句 ---  目前写死 每次 删除200条数据");
    }

    /**
     * 定时清理_tmp 数据的数据 半夜十二点 清理
     */
    @Scheduled(cron = "0 0 0 */15 * ?")
    public void deleteIndexName(){
        projectService.deleteProjectDelIndex();

    }

    /**
     * 定时清理redisMap  每30分钟 清楚一次
     */
    @Scheduled(cron = "0 0/30 0 * * ?")
    public void clearRedisMap(){
        CortrastiveCache.cleanRedisMap();

    }

}
