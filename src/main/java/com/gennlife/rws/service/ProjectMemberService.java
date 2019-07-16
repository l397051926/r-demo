package com.gennlife.rws.service;

import com.alibaba.fastjson.JSONObject;
import com.gennlife.rws.entity.ProjectMember;

import java.util.List;

public interface ProjectMemberService {

    /**
     * @param obj
     * @return List<ProjectMember>
     * @author zhengguohui
     * @description 根据项目ID查询项目成员列表
     * @date 2018年6月28日
     */
    List<ProjectMember> getProjectMemberList(JSONObject obj);

    /**
     * @param obj
     * @return ProjectMember
     * @author zhengguohui
     * @description 根据项目ID查询项目成员
     * @date 2018年6月28日
     */
    ProjectMember getProjectMember(JSONObject obj);

    /**
     * @param obj
     * @author zhengguohui
     * @description 保存项目成员信息
     * @date 2018年6月28日
     */
    List<ProjectMember> saveProjectMember(JSONObject obj);

    /**
     * @param obj
     * @author zhengguohui
     * @description 编辑项目成员信息
     * @date 2018年6月28日
     */
    ProjectMember updateProjectMember(JSONObject obj);

    /**
     * @param obj
     * @author zhengguohui
     * @description 删除项目成员信息
     * @date 2018年6月28日
     */
    void deleteProjectMember(JSONObject obj);

    /**
     * 根据项目id 获取项目的用户总数
     *
     * @param object
     * @return
     */
    int getProjectMemberCount(JSONObject object);
}
