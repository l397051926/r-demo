/**
 * copyRight
 */
package com.gennlife.rws.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by liuzhen.
 * Date: 2017/10/23
 * Time: 14:28
 */
@ConfigurationProperties(prefix = "es")
public class RemoteInfo {
    @Value("${es.searchIndexName}")
    private String searchIndexName;
    @Value("${es.esServiceUrl}")
    private String esServiceUrl;

    public String getSearchIndexName() {
        return searchIndexName;
    }

    public void setSearchIndexName(String searchIndexName) {
        this.searchIndexName = searchIndexName;
    }

    public String getEsServiceUrl() {
        return esServiceUrl;
    }

    public void setEsServiceUrl(String esServiceUrl) {
        this.esServiceUrl = esServiceUrl;
    }
}
