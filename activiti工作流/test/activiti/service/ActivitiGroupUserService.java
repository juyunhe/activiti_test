package com.richfit.jobticket.activiti.service;

import java.util.List;
import java.util.Map;

public interface ActivitiGroupUserService {


    /**
     * 获取所有的工作流流程图列表展示进行维护
     * @return
     */
    Map<String,Object> getAllActivitiFile(Integer starPage, Integer size);

    /**
     * 获取一个工作流流程图列表展示其所有节点对每个节点的group和user进行维护
     * @return
     */
    Map<String,Object> getOneActivitiAllNode(String processDefinitionId);

    /**
     * 获取一个工作流流程图列表展示其所有节点对每个节点的user
     * @return
     */
    Map<String,Object> getOneActivitiAllNodeUser(String processInstanceId);

    /**
     * 工作流节点绑定用户组
     * @param roleId
     * @return
     */
    Map<String,Object> activitiAddGroup(String processDefinitionId,String nodeId,String roleId);

    /**
     * 获取所有工作组
     * @param
     * @return
     */
    Map<String,Object> getAllGroup(Integer starPage, Integer size);

    /**
     * 根据工作组id获取该组所有成员
     * @param
     * @return
     */
    Map<String,Object> getAllUserByGroupId(String groupId);

    /**
     * 添加工作组
     * @param groupName
     * @return
     */
    Map<String,Object> addGroup(String groupName);

    /**
     * 删除工作组
     * @param groupId
     * @return
     */
    Map<String,Object> removeGroup(String groupId);

    /**
     * 工作组添加用户
     * @param groupId
     * @return
     */
    Map<String,Object> groupAddUser(String groupId, List<Map<Object,Object>> userIds);

    /**
     * 工作组移除用户
     * @param groupId
     * @return
     */
    Map<String,Object> groupRemoveUser(String groupId, List<String> userIds);
}
