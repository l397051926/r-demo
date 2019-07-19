/**
 * copyRight
 */
package com.gennlife.rws.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gennlife.rws.content.IndexContent;
import com.gennlife.rws.exception.SearchUqlCasetException;
import com.gennlife.rws.query.BuildIndexRws;
import com.gennlife.rws.query.QuerySearch;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;


/**
 * @author liuzhen
 * Created by liuzhen.
 * Date: 2017/10/23
 * Time: 13:07
 */
@Component
public class HttpUtils {
    private static Logger LOG = LoggerFactory.getLogger(HttpUtils.class);
    @Value("${es.searchIndexName}")
    private String searchIndexName;
    @Value("${es.esServiceUrl}")
    private String esServiceUrl;
    @Value("${download.pageSize:50}")
    private Integer pageSize;

    @Value("${es.esSearchUql}")
    private String esSearchUql;
    @Value("${es.buildIndex}")
    private String buildIndex;
    @Value("${es.esExceport}")
    private String esExceport;
    @Value("${es.deleteIndex}")
    private String deleteIndex;
    @Value("${es.esExceportRws}")
    private String esExceportRws;
    @Value("${es.crfBuildIndex}")
    private String crfBuildIndex;
    @Value("${es.esSearchUqlCompress}")
    private String esSearchUqlCompress;

    private RequestConfig requestConfig = RequestConfig.custom()
        .setSocketTimeout(30 * 60 * 1000)
        .setConnectTimeout(30 * 60 * 1000)
        .setConnectionRequestTimeout(30 * 60 * 1000)
        .build();

    public HttpUtils() {
    }

    public String querySearch(String projectId, String newSql, Integer pageNum, Integer pageSize, String sourceFilter, JSONArray source, String crfId) {
        return querySearch(projectId, newSql, pageNum, pageSize, sourceFilter, source, crfId, false);
    }

    public String querySearch(String projectId, String newSql, Integer pageNum, Integer pageSize, String sourceFilter, JSONArray source, boolean fetchAllGroupByResult) {
        return querySearch(projectId, newSql, pageNum, pageSize, sourceFilter, source, IndexContent.EMR_CRF_ID, fetchAllGroupByResult);
    }

    public String querySearch(String projectId, String newSql, Integer pageNum, Integer pageSize, String sourceFilter, JSONArray source, String crfId, boolean fetchAllGroupByResult) {
        return querySearch(projectId, newSql, pageNum, pageSize, sourceFilter, source, crfId, fetchAllGroupByResult, null);
    }

    public String querySearch(String projectId, String newSql, Integer pageNum, Integer pageSize, String sourceFilter, JSONArray source, String crfId, boolean fetchAllGroupByResult, JSONObject agges) {
        Long startTime = System.currentTimeMillis();
        QuerySearch querySearch = new QuerySearch();
        querySearch.setIndexName(IndexContent.getIndexName(crfId, projectId));
        querySearch.setQuery(newSql);
        querySearch.setPage(pageNum);
        querySearch.setSize(pageSize);
        querySearch.setSource_filter(sourceFilter);
        querySearch.setSource(source);
        if (agges != null) {
            querySearch.setAggs(agges);
        }
        querySearch.setFetchAllGroupByResult(fetchAllGroupByResult);
        String url = getEsSearchUqlCompress();
        String param = JSON.toJSONString(querySearch);
        String result = "";
        try {
            result = GzipUtil.uncompress(httpPost(GzipUtil.compress(param), url).trim());
        } catch (IllegalArgumentException e) {
            LOG.error("gzip 解析失败 传统方式 重新请求");
            result = httpPost(param, getEsSearchUql());
        } catch (Exception e) {
            LOG.error("计算发生异常 error: " + e.getMessage() + " 参数为： " + param);
            throw new SearchUqlCasetException("计算发生问题");
        }
        Long time = System.currentTimeMillis() - startTime;
        LOG.info("搜索 --消耗时间为：" + time);
        if (time > 40 * 1000) {
            LOG.warn("查询速度过慢 超过 40 秒 参数为:" + param);
        }
        JSONObject data = JSON.parseObject(result);
        Object error = data.get("error");
        if (error != null) {
            LOG.error("计算发生发生异常  error： " + error);
            LOG.error("参数为： " + param);
            throw new SearchUqlCasetException("计算发生问题");
        }
        return result;
    }

    private byte[] zipVisits(String visists) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            // 使用默认缓冲区大小创建新的输出流
            GZIPOutputStream gzip = new GZIPOutputStream(out);
            // 将 b.length 个字节写入此输出流
            gzip.write(visists.getBytes());
            gzip.close();
            byte[] bytes = out.toByteArray();
            return bytes;
        } catch (IOException e) {
        }
        return null;
    }

    public int[] byteArrayToInt(byte[] b) {
        int length = b.length;
        if (b == null || length <= 0) {
            return new int[1];
        }
        int[] result = new int[length];
        int i = 0;
        for (byte by : b) {
            result[i] = by;
            i++;
        }
        return result;
    }

    public String httpPost(String param, String url) {
        HttpPost post = null;
        post = new HttpPost(url);

        StringEntity entity = getStringEntity(param);
        post.setEntity(entity);
        //获取数据
        return post(post);
    }

    public String deleteIndex(String param) {
        String url = this.deleteIndex;
        return httpPost(param, url);
    }

    public String fromEsGetData(String param) {
        LOG.info("es 地址:{},参数={}", esServiceUrl, param);
        HttpPost post = new HttpPost(esServiceUrl);
        StringEntity entity = getStringEntity(param);
        post.setEntity(entity);
        //获取数据
        return post(post);
    }

    private StringEntity getStringEntity(String param) {
        StringEntity entity = new StringEntity(param, "utf-8");
        entity.setContentEncoding("UTF-8");
        entity.setContentType("application/json");
        return entity;
    }

    public int getResultTotal(String param) {
        String result = fromEsGetData(param);
        JSONObject object1 = JSONObject.parseObject(result);
        JSONObject hits = object1.getJSONObject("hits");
        if (hits == null) {
            return 0;
        }
        Integer total = hits.getInteger("total");
        int totals = total == null ? 0 : total.intValue();
        return totals;
    }

    private String post(HttpPost post) {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        HttpEntity entity = null;
        String responseContent = null;
        try {
            // 创建默认的httpClient实例.
            httpClient = HttpClients.createDefault();
            post.setConfig(requestConfig);
            // 执行请求
            response = httpClient.execute(post);
            if (response.getStatusLine().getStatusCode() == 504) {
                LOG.info("超时了 504 url 超时时间 getConnectTimeout" + requestConfig.getConnectTimeout() + " socketTimeout:  " + requestConfig.getSocketTimeout() + "result : " + EntityUtils.toString(response.getEntity()));
            }
            entity = response.getEntity();
            responseContent = EntityUtils.toString(entity, "UTF-8");
        } catch (Exception e) {
            JSONObject object = new JSONObject();
            object.put("status", 500);
            object.put("message", e.getMessage());
            responseContent = object.toJSONString();
            LOG.error("请求packaging service 出错{}", e.getMessage());
        } finally {
            try {
                // 关闭连接,释放资源
                if (response != null) {
                    response.close();
                }
                if (httpClient != null) {
                    httpClient.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return responseContent;
    }

    public String buildIndexRws(BuildIndexRws buildIndexRws) {
        String param = JSON.toJSONString(buildIndexRws);
        LOG.info("导出数据 参数:" + param);
        String result = httpPost(param, esExceportRws);
        LOG.info("导出数据结果: " + result);
        return result;
    }

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

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getEsSearchUql() {
        return esSearchUql;
    }

    public void setEsSearchUql(String esSearchUql) {
        this.esSearchUql = esSearchUql;
    }

    public String getBuildIndex() {
        return buildIndex;
    }

    public void setBuildIndex(String buildIndex) {
        this.buildIndex = buildIndex;
    }

    public String getEsExceport() {
        return esExceport;
    }

    public void setEsExceport(String esExceport) {
        this.esExceport = esExceport;
    }

    public String getDeleteIndex() {
        return deleteIndex;
    }

    public void setDeleteIndex(String deleteIndex) {
        this.deleteIndex = deleteIndex;
    }

    public String getEsExceportRws() {
        return esExceportRws;
    }

    public void setEsExceportRws(String esExceportRws) {
        this.esExceportRws = esExceportRws;
    }

    public String getEsSearchUqlCompress() {
        return esSearchUqlCompress;
    }

    public void setEsSearchUqlCompress(String esSearchUqlCompress) {
        this.esSearchUqlCompress = esSearchUqlCompress;
    }

    public String getCrfBuildIndex() {
        return crfBuildIndex;
    }

    public void setCrfBuildIndex(String crfBuildIndex) {
        this.crfBuildIndex = crfBuildIndex;
    }
}
