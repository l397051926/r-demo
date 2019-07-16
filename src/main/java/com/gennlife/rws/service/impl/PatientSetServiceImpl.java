/**
 *
 */
package com.gennlife.rws.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gennlife.darren.collection.keypath.KeyPath;
import com.gennlife.rws.content.CommonContent;
import com.gennlife.rws.content.IndexContent;
import com.gennlife.rws.content.LiminaryContent;
import com.gennlife.rws.content.SeparatorContent;
import com.gennlife.rws.dao.*;
import com.gennlife.rws.entity.*;
import com.gennlife.rws.exception.CustomerException;
import com.gennlife.rws.service.*;
import com.gennlife.rws.util.*;
import com.gennlife.rws.vo.CustomerStatusEnum;
import com.gennlife.rws.vo.DataCheckEmpty;
import com.gennlife.rws.vo.DelFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Future;

import static java.util.stream.Collectors.*;

@Service
@Transactional(rollbackFor = RuntimeException.class)
public class PatientSetServiceImpl implements PatientSetService {
    private static final Logger LOG = LoggerFactory.getLogger(PatientSetServiceImpl.class);

    @Autowired
    private PatientsSetMapper patientsSetMapper;
    @Autowired
    private SearchLogMapper searchLogMapper;
    @Autowired
    private ContrastiveAnalysisCountMapper conAnlyCountMapper;
    @Autowired
    private LogUtil logUtil;
    @Autowired
    private ActiveIndexService activeIndexService;
    @Autowired
    private GroupPatientDataMapper groupPatientDataMapper;
    @Autowired
    private GroupDataMapper groupDataMapper;
    @Autowired
    private HttpUtils httpUtils;
    @Autowired
    private GroupMapper groupMapper;
    @Autowired
    private SearchByuqlService searchByuqlService;
    @Autowired
    private InputTaskMapper inputTaskMapper;
    @Autowired
    private SearchCrfByuqlService searchCrfByuqlService;
    @Autowired
    private ProjectMapper projectMapper;
    @Autowired
    private InputTaskService inputTaskService;
    @Autowired
    private PatientsIdSqlMapMapper patientsIdSqlMapMapper;
    @Autowired
    private LiminaryContent liminaryContent;

    private static final int exportMax = 2000;

    @Override
    public List<PatientsSet> getPatientSetList(JSONObject obj) {
        Map<String, Object> map = new HashMap<>();
        String crfId = obj.getString("crfId");
        String projectId = obj.getString("projectId");
        map.put("projectId", projectId);
        DataCheckEmpty.dataCheckEmpty(obj.getString("projectId"));
        List<PatientsSet> patientsSetList = patientsSetMapper.getPatientsSetList(map);
        for (PatientsSet patientsSet : patientsSetList) {
            String patientSetId = patientsSet.getPatientsSetId();

            Integer isFlush = patientsSet.getIsFlush() == null ? 0 : patientsSet.getIsFlush(); //1 是刷新并置为0 0是不刷新
            if (isFlush != null && isFlush == 0) {
                continue;
            } else {
                patientsSetMapper.updateIsFlush(--isFlush, patientSetId);
            }
            //自动更新下面组的筛选功能
            SingleExecutorService.getInstance().getFlushCountGroupExecutor().submit(() -> {
                flushCountGroup(patientSetId, projectId, crfId);
            });
        }
        return patientsSetList;
    }

    @Override
    public PatientsSet getPatientSet(JSONObject obj) {
        String patientsSetId = obj.getString("patientsSetId");
        DataCheckEmpty.dataCheckEmpty(patientsSetId);
        return patientsSetMapper.selectByPatSetId(patientsSetId);
    }

    @Override
    public PatientsSet savePatientSet(JSONObject obj) {
        PatientsSet patSet = JSONObject.toJavaObject(obj, PatientsSet.class);
        DataCheckEmpty.dataCheckEmpty(patSet.getPatientsSetName());
        patSet.setPatientsSetId(UUIDUtil.getUUID());
        patSet.setIsDelete(DelFlag.AVIABLE.toString());// 枚举值
        // 服务器时间
        patSet.setCreateTime(new Date());
        patSet.setUpdateTime(new Date());
        patientsSetMapper.insert(patSet);
        String content = patSet.getCreateName() + "新增了患者集： " + patSet.getPatientsSetName();
        logUtil.saveLog(patSet.getProjectId(), content, patSet.getCreateId(), patSet.getCreateName());
        return patSet;
    }

    @Override
    public PatientsSet updatePatientSet(JSONObject obj) {
        PatientsSet patSet = JSONObject.toJavaObject(obj, PatientsSet.class);
        PatientsSet patSet1 = patientsSetMapper.selectByPatSetId(patSet.getPatientsSetId());
        String oldName = patSet1.getPatientsSetName();
        if (patSet1 != null) {
            patSet.setId(patSet1.getId());
            patSet.setUpdateTime(new Date());
            if (patSet.getPatientsCount() == 0) {
                patSet.setPatientsCount(null);
            }
            patientsSetMapper.updateById(patSet);
            String content = patSet.getUpdateName() + "编辑了患者集： " + oldName;
            logUtil.saveLog(patSet.getProjectId(), content, patSet.getCreateId(), patSet.getCreateName());
            //更新 任务中 患者集名称
            Map<String, String> inputMaps = new HashMap<>();
            inputMaps.put("patientSetId", patSet.getPatientsSetId());
            inputMaps.put("patientSetName", patSet.getPatientsSetName());
            inputTaskMapper.updateInputTaskByMap(inputMaps);
        } else {
            throw new CustomerException(CustomerStatusEnum.UNKONW_ERROR.toString(), "该患者集不存在无法更新");
        }
        return patSet;
    }

    @Override
    public void deletePatientSet(JSONObject obj) throws IOException {
        String patientsSetId = obj.getString("patientsSetId");
        DataCheckEmpty.dataCheckEmpty(patientsSetId);
        PatientsSet patSet = patientsSetMapper.selectByPatSetId(patientsSetId);
        String createId = obj.getString("createId");
        String crfId = obj.getString("crfId");
        String createName = obj.getString("createName");
        String projectId = patSet.getProjectId();
        String projectName = "";
        if (StringUtils.isNotEmpty(projectId)) {
            projectName = projectMapper.getProjectNameByProjectId(projectId);
        }
        if (patSet != null) {
            patSet.setUpdateTime(new Date());
            patSet.setIsDelete(DelFlag.LOSE.toString());
            // 删除患者集和分组的关系
            List<GroupPatientData> listGpData = groupPatientDataMapper.selectByPatientSetId(patientsSetId);
            if (listGpData != null && listGpData.size() > 0) {
                for (GroupPatientData gpdata : listGpData) {
                    // 可考虑批量删除
                    groupPatientDataMapper.deleteById(gpdata.getId());
                    List<String> patSns = getPatientSetSn(patientsSetId, patSet.getProjectId(), crfId);
                    List<String> otherPatSns = getGroupIdPatientSn(gpdata.getGroupId(), patSet.getProjectId(), crfId);
                    patSns.removeAll(otherPatSns);
                    if (patSns != null && patSns.size() > 0) {
                        groupDataMapper.deleteByPatSn(gpdata.getGroupId(), patSns);
                        //若 group ID 有子组  同时删掉数据
                        deleteGroupData(gpdata.getGroupId(), patSns);
                    }

                }
            }
            patientsSetMapper.updateById(patSet);
            String content = createName + "删除了患者集： " + patSet.getPatientsSetName();
            logUtil.saveLog(patSet.getProjectId(), content, createId, createName);

            //患者集删除 任务进行失败更改
            if (StringUtils.isNotEmpty(projectId) && StringUtils.isNotEmpty(patientsSetId) && StringUtils.isNotEmpty(createId)) {
                inputTaskService.cencelInputTasksOnDelPatSet(patientsSetId, createId, projectId, projectName, crfId);
            }

        }

    }

    @Override
    public List<PatientsSet> getPatientSetByProjectId(JSONObject paramObj) {
        String projectId = paramObj.getString("projectId");
        DataCheckEmpty.dataCheckEmpty(projectId);
        return patientsSetMapper.getPatientSetByProjectId(projectId);

    }

    @Override
    public void savePatientImport(JSONObject obj) throws IOException {
        String patientSetId = obj.getString("patientSetId");
        Integer count = patientsSetMapper.getPatientSetCount(patientSetId);
        Long localCount = getPatientSetLocalCountByExclude(patientSetId, 1); //历史数量
        Long currentCount = obj.getLong("curenntCount"); //本次导入的数量

        PatientsSet patientsSet = new PatientsSet();
        patientsSet.setPatientsSetId(obj.getString("patientSetId"));
        patientsSet.setPatientsCount(currentCount);
        patientsSet.addComUqlQuery(obj.getString("uqlQuery"));
        patientsSet.setIsFlush(5);//让相关患者分组进行更新
        if (count == 0) {
            patientsSetMapper.insert(patientsSet);
        } else {
            patientsSetMapper.updatePatientsCountAndQuery(obj.getString("patientSetId"), localCount + currentCount, null, 5);
        }
        updatePatientSqlMap(obj);
    }

    @Override
    public Long getPatientSetLocalCountByExclude(String patientSetId, Integer export) {
        List<PatientsIdSqlMap> pids = patientsIdSqlMapMapper.getPatientsSqlMapByDataSourceIdAndExclude(patientSetId, export);
        Integer count = pids.stream().map(o -> o.getPatientSnIds().split(SeparatorContent.getRegexVartivalBar())).flatMap(Arrays::stream).collect(toSet()).size();
        return Long.valueOf(count);
    }

    @Override
    public Long getPatientSetLocalCount(String patientSetId) {
        List<PatientsIdSqlMap> pids = patientsIdSqlMapMapper.getPatientsSqlMapByDataSourceId(patientSetId);
        Integer count = pids.stream().map(o -> o.getPatientSnIds().split(SeparatorContent.getRegexVartivalBar())).flatMap(Arrays::stream).collect(toSet()).size();
        return Long.valueOf(count);
    }

    @Override
    public Integer getPatientSetLocalCountByListForPatientSets(List<String> patientSetIds) {
        List<PatientsIdSqlMap> pids = patientsIdSqlMapMapper.getPatientsSqlMapBypatientSetIdsAndExclude(patientSetIds, 1);
        return pids.stream().map(o -> o.getPatientSnIds().split(SeparatorContent.getRegexVartivalBar())).flatMap(Arrays::stream).collect(toSet()).size();
    }

    @Override
    public String getPatientSetLocalSql(String patientSetId) {
        List<PatientsIdSqlMap> pids = patientsIdSqlMapMapper.getPatientsSqlMapByDataSourceIdAndExclude(patientSetId, 1);
        return pids.stream().map(o -> o.getPatientSnIds().split(SeparatorContent.getRegexVartivalBar())).flatMap(Arrays::stream).distinct().collect(joining(SeparatorContent.VERTIVAL_BAR));
    }

    @Override
    public List<String> getPatientSetLocalSqlByList(String patientSetId) {
        List<PatientsIdSqlMap> pids = patientsIdSqlMapMapper.getPatientsSqlMapByDataSourceIdAndExclude(patientSetId, 1);
        return pids.stream().map(o -> o.getPatientSnIds().split(SeparatorContent.getRegexVartivalBar())).flatMap(Arrays::stream).distinct().collect(toList());
    }

    @Override
    public Set<String> getPatientSetLocalSqlListById(Integer id) {
        PatientsIdSqlMap pid = patientsIdSqlMapMapper.getPatientsSqlMapByIdAndExclude(id, 1);
        return Arrays.stream(pid.getPatientSnIds().split(SeparatorContent.getRegexVartivalBar())).collect(toSet());
    }

    @Override
    public void saveGroupDataByGroupBlock(String groupId, List<String> datas, int num) {
        patientsIdSqlMapMapper.deleteByDataSourceId(groupId);
        Integer groupDataBlock = liminaryContent.getGroupDataBlock();
        for (int i = 0; i < datas.size() / groupDataBlock + 1; i++) {
            Set<String> resultDatas = PagingUtils.getPageContentForString(datas, i + 1, groupDataBlock).stream().collect(toSet());
            savePatientSetGroupBlock(groupId, resultDatas, num);
        }
    }

    @Override
    public List<String> getPatientSetLocalSqlByListForPatientSets(List<String> patientSetIds) {
        List<PatientsIdSqlMap> pids = patientsIdSqlMapMapper.getPatientsSqlMapBypatientSetIdsAndExclude(patientSetIds, 1);
        return pids.stream().map(o -> o.getPatientSnIds().split(SeparatorContent.getRegexVartivalBar())).flatMap(Arrays::stream).distinct().collect(toList());
    }

    @Override
    public List<PatientsIdSqlMap> getPatientSetByListForInitialSql(List<String> patientSetIds) {
        return patientsIdSqlMapMapper.getPatientsSqlMapBypatientSetIdsAndExclude(patientSetIds, 1);
    }

    private void updatePatientSqlMap(JSONObject obj) {
        String patientSetId = obj.getString("patientSetId");
        patientsIdSqlMapMapper.updateExportByPatientSetId(patientSetId, 1);
    }

    @Override
    public void savePatientSetGroupBlock(String dataSourceId, Set<String> allPats, Integer num) {
        String query = String.join(SeparatorContent.VERTIVAL_BAR, allPats);
        PatientsIdSqlMap patientsIdSqlMap = new PatientsIdSqlMap();
        patientsIdSqlMap.setDataSourceId(dataSourceId);
        patientsIdSqlMap.setPatientSnIds(query);
        patientsIdSqlMap.setExport(num);
        patientsIdSqlMapMapper.insertForGroupid(patientsIdSqlMap);
    }

    @Override
    public List<ContrastiveAnalysisCount> getContrasAnalyList(JSONObject obj) {
        try {
            String uid = obj.getString("uid");
            String projectId = obj.getString("projectId");
            DataCheckEmpty.dataCheckEmpty(uid, projectId);
            return conAnlyCountMapper.getContrastiveByUidAndPro(uid, projectId);
        } catch (Exception e) {
            throw new CustomerException(CustomerStatusEnum.UNKONW_ERROR.toString(), e.getMessage());
        }
    }

    @Override
    public List<SearchLog> getSearchLog(JSONObject param) {
        String patientsSetId = param.getString("patientsSetId");
        DataCheckEmpty.dataCheckEmpty(patientsSetId);
        return searchLogMapper.selectByPrtisntId(patientsSetId);
    }

    private void flushCountGroup(String patientSetId, String projectId, String crfId) {
        List<String> groupIds = groupPatientDataMapper.getGroupIds(patientSetId);
        for (String groupId : groupIds) {
            List<String> patientSetIds = groupPatientDataMapper.getPatSetByGroupId(groupId);
            Group group = groupMapper.getGroupByGroupId(groupId);
            ActiveIndex active = null;
            try {
                active = activeIndexService.findByActiveId(groupId);
                if (active == null) savePatientToGroup(patientSetIds, projectId, crfId, groupId);
                active = active == null ? new ActiveIndex() : active;
                JSONObject obj = (JSONObject) JSONObject.toJSON(active);
                String isVariant = active.getIsVariant();
                obj.put("patientSetId", patientSetIds);
                obj.put("groupToId", groupId);
                obj.put("groupFromId", null);
                Integer activeType = active.getActiveType();
                int isSearch = CommonContent.ACTIVE_TYPE_NOTEMP;
                List<PatientsIdSqlMap> patientSql = searchByuqlService.getInitialSQLTmp(null, isVariant, groupId, JSONArray.parseArray(JSON.toJSONString(patientSetIds)), projectId, crfId);
                searchByuqlService.computationalInitialization(isSearch, active.getId(), groupId, projectId, crfId, activeType, null, JSONArray.parseArray(JSON.toJSONString(patientSetIds)), null, null);

                Integer finalActiveType = activeType;
                List<Future> futures = new LinkedList<>();
                patientSql.forEach(o -> futures.add(SingleExecutorService.getInstance().getSearchUqlExecutor().submit(() -> {
                    try {
                        if (StringUtils.isNotEmpty(crfId) && !crfId.equals("EMR")) {
                            searchCrfByuqlService.SearchByExclude(obj, null, 0, o, crfId);
                            searchCrfByuqlService.searchCalcExculeByUql(groupId, projectId, 1, 1, new JSONArray(), crfId, "1", groupId, group.getGroupName(),
                                JSONArray.parseArray(JSON.toJSONString(patientSetIds)), group.getCreateId(), group.getCreateName(), null, true);
                        } else {
                            searchByuqlService.SearchByExclude(obj, null, 0, o, crfId);
                            searchByuqlService.searchCalcExculeByUql(groupId, projectId, 1, 1, new JSONArray(), "1", groupId, group.getGroupName(), JSONArray.parseArray(JSON.toJSONString(patientSetIds)), group.getCreateId(), group.getCreateName(), null, true, crfId);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })));
                for (Future future : futures) {
                    future.get();
                }


            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    private void savePatientToGroup(List<String> patientSns, String projectId, String crfId, String groupId) throws IOException {
        boolean isExport = false;
        List<List<GroupData>> list = new ArrayList<>();
        List<GroupData> listdata = new ArrayList<>();

        for (int i = 0; i < patientSns.size(); i++) {
            String patientSetId = patientSns.get(i);
            List<Patient> listPatients = searchByuqlService.getpatentByUql(patientSetId, isExport, projectId, crfId);
            for (int j = 0; j < listPatients.size(); j++) {
                Patient patient = listPatients.get(j);
                if (j != 0 && j % exportMax == 0) {
                    list.add(listdata);
                    listdata = new ArrayList<>();
                }
                GroupData groupData = new GroupData();
                groupData.setPatientSetId(patientSetId);
                groupData.setGroupId(groupId);
                groupData.setPatientSn(patient.getPatientSn());
                groupData.setPatientDocId(patient.getDOC_ID());
                groupData.setRemove(DelFlag.AVIABLE.toString());
                groupData.setCreateTime(new Date());
                groupData.setUpdateTime(new Date());
                listdata.add(groupData);
            }
        }
        list.add(listdata);
        for (List<GroupData> li : list) {
            if (li != null && !li.isEmpty()) {
                groupDataMapper.batchInsert(li);
            }
        }
    }

    private List<String> getPatientSetSn(String patientsSetId, String projectId, String crfId) throws IOException {
        String querWhere = TransPatientSql.getUncomPatientSnSql(patientsSetMapper.getPatientsetSql(patientsSetId));
        String newquerWhere = TransPatientSql.getAllPatientSql(querWhere, crfId);
        JSONArray sourceFilter = new JSONArray();
        String newSql = "select " + IndexContent.getPatientDocId(crfId) + " as pSn from " + IndexContent.getIndexName(crfId, projectId) + " where " + newquerWhere + IndexContent.getGroupBy(crfId);
        String response = httpUtils.querySearch(projectId, newSql, 1, Integer.MAX_VALUE - 1, null, sourceFilter, crfId, true);
        List<String> patients = new KeyPath("hits", "hits", "_id")
            .fuzzyResolve(JSON.parseObject(response))
            .stream()
            .map(String.class::cast)
            .collect(toList());
        return patients;
    }

    private List<String> getGroupIdPatientSn(String groupId, String projectId, String crfId) {
        List<String> patSetSql = patientsSetMapper.getPatientSetSqlByGroupId(groupId);
        String sqlWhere = String.join(" or ", patSetSql.stream().map(x -> "(" + TransPatientSql.getPatientSnSql(TransPatientSql.getUncomPatientSnSql(x), crfId) + ")").collect(toList()));
        JSONArray sourceFilter = new JSONArray();
        if (StringUtils.isEmpty(sqlWhere)) {
            return new ArrayList<>();
        }
        String newSql = "select " + IndexContent.getPatientDocId(crfId) + " as pSn from " + IndexContent.getIndexName(crfId, projectId) + " where " + sqlWhere + IndexContent.getGroupBy(crfId);
        String response = httpUtils.querySearch(projectId, newSql, 1, Integer.MAX_VALUE - 1, null, sourceFilter, crfId, true);
        List<String> patients = new KeyPath("hits", "hits", "_id")
            .fuzzyResolve(JSON.parseObject(response))
            .stream()
            .map(String.class::cast)
            .collect(toList());
        return patients;
    }

    private void deleteGroupData(String groupId, List<String> patSns) {
        List<Group> groupChildList = groupMapper.getgroupChildIds(groupId);
        for (Group group : groupChildList) {

            groupDataMapper.deleteByPatSn(group.getGroupId(), patSns);
            deleteGroupData(group.getGroupId(), patSns);
        }
    }


}
