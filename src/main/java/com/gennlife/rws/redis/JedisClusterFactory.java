package com.gennlife.rws.redis;


import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;

@Component
@Scope("singleton")
@ConfigurationProperties(prefix = "redis.config")
public class JedisClusterFactory implements InitializingBean, DisposableBean {
    @Autowired
    private GenericObjectPoolConfig genericObjectPoolConfig;
    private JedisCluster jedisCluster;
    private int connectionTimeout = 2000;
    private int soTimeout = 3000;
    private int maxRedirections = 5;

    private String jedisClusterNodes;
    private static final Logger logger = LoggerFactory.getLogger(JedisClusterFactory.class);
    public JedisClusterFactory getObject() throws Exception {
        return this;
    }

    public Class<?> getObjectType() {
        return this.jedisCluster != null ? this.jedisCluster.getClass() : JedisCluster.class;
    }

    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (jedisClusterNodes == null || jedisClusterNodes.length() == 0) {
            throw new NullPointerException("jedisClusterNodes is null.");
        }
        logger.info("redis node " + jedisClusterNodes);
        Set<HostAndPort> haps = new HashSet<HostAndPort>();
        for (String node : jedisClusterNodes.split(",")) {
            String[] arr = node.split(":");
            if (arr.length != 2) {
                throw new ParseException("node address error !", node.length() - 1);
            }
            haps.add(new HostAndPort(arr[0], Integer.valueOf(arr[1])));
        }
        jedisCluster = new JedisCluster(haps, connectionTimeout, soTimeout, maxRedirections, genericObjectPoolConfig);
    }

    public GenericObjectPoolConfig getGenericObjectPoolConfig() {
        return this.genericObjectPoolConfig;
    }

    public void setGenericObjectPoolConfig(GenericObjectPoolConfig genericObjectPoolConfig) {
        this.genericObjectPoolConfig = genericObjectPoolConfig;
    }

    public JedisCluster getJedisCluster() {
        return this.jedisCluster;
    }

    public void setJedisCluster(JedisCluster jedisCluster) {
        this.jedisCluster = jedisCluster;
    }

    public int getConnectionTimeout() {
        return this.connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getSoTimeout() {
        return this.soTimeout;
    }

    public void setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
    }

    public int getMaxRedirections() {
        return this.maxRedirections;
    }

    public void setMaxRedirections(int maxRedirections) {
        this.maxRedirections = maxRedirections;
    }

    public String getJedisClusterNodes() {
        return this.jedisClusterNodes;
    }

    public void setJedisClusterNodes(String jedisClusterNodes) {
        this.jedisClusterNodes = jedisClusterNodes;
    }

    @Order
    @Bean
    @ConfigurationProperties(prefix = "ui.redis.config.pool")
    public GenericObjectPoolConfig createGenericObjectPoolConfig() {
        return new GenericObjectPoolConfig();
    }

    @Override
    public void destroy() throws Exception {
        jedisCluster.close();
    }
}
