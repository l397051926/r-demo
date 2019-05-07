/**
 * copyRight
 */
package com.gennlife.rws.datasource;

import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;

/**
 * Created by liuzhen.
 * Date: 2017/10/22
 * Time: 9:24
 */
@WebServlet(urlPatterns = "/druid/*",
        initParams={
                // IP白名单 (没有配置或者为空，则允许所有访问)@WebInitParam(name="allow",value="127.0.0.1"),/
                @WebInitParam(name="deny",value="192.168.16.111"),// IP黑名单 (存在共同时，deny优先于allow)
                @WebInitParam(name="loginUsername",value="root"),// 用户名
                @WebInitParam(name="loginPassword",value="123456"),// 密码
                @WebInitParam(name="resetEnable",value="false")// 禁用HTML页面上的“Reset All”功能
        })
public class DruidConfig extends StatViewServlet{
    /*@Bean
    public ServletRegistrationBean servletRegistration() {
        ServletRegistrationBean servletRegistration = new ServletRegistrationBean(new StatViewServlet());        //添加初始化参数：initParams
        servletRegistration.addUrlMappings("/druid*//*");
        //白名单
        //servletRegistration.addInitParameter("allow", "127.0.0.1");
        //IP黑名单 (存在共同时，deny优先于allow) : 如果满足deny的话提示:Sorry, you are not permitted to view this page.
        //servletRegistration.addInitParameter("deny", "192.168.1.73");
        //登录查看信息的账号密码.a
        servletRegistration.addInitParameter("loginUsername", "admin");
        servletRegistration.addInitParameter("loginPassword", "123456");
        //是否能够重置数据.
        servletRegistration.addInitParameter("resetEnable", "false");
        return servletRegistration;
    }

    @Bean
    public FilterRegistrationBean filterRegistrationBean() {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(new WebStatFilter());
        //添加过滤规则.
        filterRegistrationBean.addUrlPatterns("*//*");
        //添加不需要忽略的格式信息.
        filterRegistrationBean.addInitParameter("exclusions", "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid*//*");
        return filterRegistrationBean;
    }*/
}
