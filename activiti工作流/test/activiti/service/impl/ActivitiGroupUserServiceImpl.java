package com.richfit.jobticket.activiti.service.impl;

import com.richfit.jobticket.activiti.service.ActivitiGroupUserService;
import com.richfit.jobticket.base.util.UUIDGenerator;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ActivitiGroupUserServiceImpl implements ActivitiGroupUserService {

    @Autowired
    RepositoryService repositoryService;
    @Autowired
    IdentityService identityService;
    @Autowired
    HistoryService historyService; //历史服务
    @Autowired
    TaskService taskService; //任务服务
    @Autowired
    private RuntimeService runtimeService;

    @Override
    public Map<String, Object> getAllActivitiFile(Integer starPage, Integer size) {
        Map<String, Object> map = new HashMap<>();
        Integer totalCount = repositoryService.createProcessDefinitionQuery()
                .latestVersion()
                .list()
                .size();
        List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery()
                .latestVersion()
                .listPage((starPage-1)*size,size);
        List<Map<String,Object>> list1 = new  ArrayList();
        if(list != null && list.size()>0){
            for(ProcessDefinition processDefinition:list){
                Map<String,Object> m = new HashMap<>();
                if(processDefinition.getKey().equals("certigicate")){
                    m.put("processDefinitionKey","证书审批流程");//使用流程图的名字作为待办的类型名称
                }
                if(processDefinition.getKey().equals("contractSettlement")){
                    m.put("processDefinitionKey","合同结算审批流程");//使用流程图的名字作为待办的类型名称
                }
                if(processDefinition.getKey().equals("newContract2")){
                    m.put("processDefinitionKey","合同编制确认流程");//使用流程图的名字作为待办的类型名称
                }
                if(processDefinition.getKey().equals("newContract3")){
                    m.put("processDefinitionKey","合同管理员确认合同流程");//使用流程图的名字作为待办的类型名称
                }
                if(processDefinition.getKey().equals("newWork1")){
                    m.put("processDefinitionKey","普通检定申请审批流程");//使用流程图的名字作为待办的类型名称
                }
                if(processDefinition.getKey().equals("newWork2")){
                    m.put("processDefinitionKey","框架检定申请审批流程");//使用流程图的名字作为待办的类型名称
                }
                if(processDefinition.getKey().equals("weekplan3")){
                    m.put("processDefinitionKey","周计划审批流程");//使用流程图的名字作为待办的类型名称
                }
                if(processDefinition.getKey().equals("certificate1")){
                    m.put("processDefinitionKey","证书生成审批流程");//使用流程图的名字作为待办的类型名称
                }
                if(processDefinition.getKey().equals("certificateOfSettlement")){
                    m.put("processDefinitionKey","结算凭证审批流程");//使用流程图的名字作为待办的类型名称
                }
                if(processDefinition.getKey().equals("invoiceApplicationForm")){
                    m.put("processDefinitionKey","开票申请单审批流程");//使用流程图的名字作为待办的类型名称
                }



                if(processDefinition.getName().equals("ver00000120000001")){
                    m.put("processDefinitionName","普通检定");//使用流程图的名字作为待办的类型名称
                }
                if(processDefinition.getName().equals("ver00000120000002")){
                    m.put("processDefinitionName","框架检定流");//使用流程图的名字作为待办的类型名称
                }
                if(processDefinition.getName().equals("ver00000120000003")){
                    m.put("processDefinitionName","合同");//使用流程图的名字作为待办的类型名称
                }
                if(processDefinition.getName().equals("ver00000120000005")){
                    m.put("processDefinitionName","证书");//使用流程图的名字作为待办的类型名称
                }
                if(processDefinition.getName().equals("ver00000120000006")){
                    m.put("processDefinitionName","周计划");//使用流程图的名字作为待办的类型名称
                }
                if(processDefinition.getName().equals("ver00000120000007")){
                    m.put("processDefinitionName","临时计划");//使用流程图的名字作为待办的类型名称
                }
                m.put("processDefinitionId",processDefinition.getId());//流程定义的key+版本+随机生成数
                //对应HelloWorld.bpmn文件中的name属性值
                m.put("processDefinitionVersion",processDefinition.getVersion());//当流程定义的key值相同的情况下，版本升级，默认从1开始
                m.put("processDefinitionResourceName",processDefinition.getResourceName());
                m.put("processDefinitionDiagramResourceName",processDefinition.getDiagramResourceName());
                m.put("processDefinitionDeploymentId",processDefinition.getDeploymentId());
                list1.add(m);

            }
        }
        map.put("totalSize",totalCount);
        map.put("data",list1);
        return map;
    }

    @Override
    public Map<String, Object> getOneActivitiAllNode(String processDefinitionId) {
        Map<String,Object> map  = new HashMap<>();
        List<Map<String,Object>> list = new  ArrayList();
        BpmnModel model = repositoryService.getBpmnModel(processDefinitionId);
        if (model != null) {
            Collection<FlowElement> flowElements = model.getMainProcess().getFlowElements();
            for (FlowElement e : flowElements) {
                if(e instanceof UserTask){
                    UserTask userTask = (UserTask) e;
                    if(userTask!=null){
                        Map<String,Object> m  = new HashMap<>();

                        m.put("nodeId",userTask.getId());
                        m.put("nodeName",userTask.getName());
                        m.put("nodeFormKey",userTask.getFormKey());
                        List<String> gs = userTask.getCandidateGroups();
                        List<Map<String,Object>> groupList = new ArrayList<>();
                        String groupStr = "";
                        List<String> userId = new ArrayList<>();
                        for (String str:gs) {
                            if (str!=null){
                                Map<String,Object> mm = new HashMap<>();
                                List<User> userList = new ArrayList<>();
                                userList = identityService.createUserQuery().memberOfGroup(str).list();
                                Group g = identityService.createGroupQuery().groupId(str).singleResult();
                                mm.put("group",g);
                                mm.put("users",userList);
                                groupList.add(mm);
                                if (g!=null){
                                    groupStr+=g.getName();
                                }
                            }
                        }
                        String userStr = "";
                        for (Map<String,Object> mmm:groupList) {
                            if (mmm.get("users")!=null){
                                List<User> users = (List<User>)mmm.get("users");
                                for (int i = 0; i <users.size() ; i++) {
                                    userStr += users.get(i).getFirstName();
                                    if (i <users.size()-1){
                                        userStr += ",";
                                    }
                                }
                            }
                        }
                        m.put("groupStr",groupStr);
                        m.put("userStr",userStr);
                        m.put("nodeGroup",groupList);
                        m.put("groupUserIds",userId);
                        list.add(m);
                    }
                }
            }
        }
        map.put("data",list);
        return map;
    }

    @Override
    public Map<String, Object> getOneActivitiAllNodeUser(String processInstanceId) {
        Map<String,Object> map  = new HashMap<>();
        String userId0 = null;
        String userId1 = null;
        String userId2 = null;
        String userId3 = null;
        List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().list();
        String processDefinitionId =null;
        if(list != null && list.size()>0){
            for(ProcessDefinition processDefinition:list){
                if(processDefinition.getKey().equals("certificate1")){
                    processDefinitionId = processDefinition.getId();
                }
            }
        }
        BpmnModel model = repositoryService.getBpmnModel(processDefinitionId);
        if (model != null) {
            Collection<FlowElement> flowElements = model.getMainProcess().getFlowElements();
            for (FlowElement e : flowElements) {
                if(e instanceof UserTask){
                    UserTask userTask = (UserTask) e;
                    if(userTask!=null){
                        if ("证书抽查".equals(userTask.getName())){
                            List<String> userStr = userTask.getCandidateUsers();
                            for (int i = 0; i < userStr.size(); i++) {
                                userId2 = userStr.get(i);
                            }
                        }


                    }
                }
            }
        }
       ProcessInstance nstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
       Task t = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
       userId1 = (String)taskService.getVariable(t.getId(), "assignee1");
       userId3 = (String)taskService.getVariable(t.getId(), "assignee3");
       userId0 = nstance.getStartUserId();
       map.put("userId0",userId0);
       map.put("userName0",this.getUserName(userId0));
       map.put("userId1",userId1);
       map.put("userName1",this.getUserName(userId1));
       map.put("userId2",userId2);
       map.put("userName2",this.getUserName(userId2));
       map.put("userId3",userId3);
       map.put("userName3",this.getUserName(userId3));
       return map;
    }

    private String getUserName(String userId){
        String userName = "";
        if (userId != null) {
            User user = identityService.createUserQuery().userId(userId).singleResult();
            if (user != null) {
                userName = user.getFirstName();
            }
        }
        return userName;
    }

    @Override
    public Map<String, Object> activitiAddGroup(String processDefinitionId, String nodeId, String groupId) {
        Map<String,Object> map  = new HashMap<>();
        List<String> candidateGroups=new ArrayList<String>();
        candidateGroups.add(groupId);
        BpmnModel model = repositoryService.getBpmnModel(processDefinitionId);
        if (model != null) {
            Collection<FlowElement> flowElements = model.getMainProcess().getFlowElements();
            for (FlowElement e : flowElements) {
                if(e instanceof UserTask){
                    UserTask userTask = (UserTask) e;
                    if(userTask!=null){
                       if (userTask.getId().equals(nodeId)){
                           userTask.setCandidateGroups(candidateGroups);
                           map.put("message","success");
                       }
                    }
                }
            }
        }
        return map;
    }

    @Override
    public Map<String, Object> getAllGroup(Integer starPage, Integer size) {
        Map<String,Object> map  = new HashMap<>();
        List<Map<String,Object>> list =new ArrayList<>();
        Integer totalCount = identityService.createGroupQuery()
                .list().size();
        List<Group> groupList = identityService.createGroupQuery().listPage((starPage-1)*size,size);
        for (Group group:groupList) {
            Map<String,Object> m  = new HashMap<>();
            List<User> userList = identityService.createUserQuery().memberOfGroup(group.getId()).list();
            m.put("group",group);
            m.put("users",userList);
            list.add(m);
        }
        map.put("totalSize",totalCount);
        map.put("data",list);
        return map;
    }

    @Override
    public Map<String, Object> getAllUserByGroupId(String groupId) {
        Map<String,Object> map  = new HashMap<>();
        List<User> userList = identityService.createUserQuery().memberOfGroup(groupId).list();
        map.put("data",userList);
        return map;
    }

    @Override
    public Map<String, Object> addGroup(String groupName) {
        Map<String,Object> map  = new HashMap<>();
        String groupId = null;
        Group group = identityService.createGroupQuery().groupName(groupName).singleResult();
        if(group==null){
            groupId = UUIDGenerator.getUUID();
            Group g =identityService.newGroup(groupId);
            g.setName(groupName);
            identityService.saveGroup(g);
            map.put("message","success");
            System.out.println("@@@@@@@@@@@@@#"+groupName+"："+groupId+"@@@@@@@@@@@@@");
        }
        return map;
    }

    @Override
    public Map<String, Object> removeGroup(String groupId) {
        Map<String,Object> map  = new HashMap<>();
        List<User> userList = identityService.createUserQuery().memberOfGroup(groupId).list();
        for (User user:userList) {
            identityService.deleteMembership(user.getId(), groupId);
        }
        identityService.deleteGroup(groupId);
        map.put("message","success");
        return map;
    }

    @Override
    public Map<String, Object> groupAddUser(String groupId, List<Map<Object,Object>> userIds) {
        Map<String,Object> map  = new HashMap<>();
        Group group = identityService.createGroupQuery().groupId(groupId).singleResult();
        if (group!=null){
            for (Map<Object,Object> m:userIds) {
                String userId = (String)m.get("userId");
                if (identityService.createUserQuery().userId(userId).singleResult()!=null){
                    identityService.createMembership(userId,groupId);
                    map.put("message","success");
                }else{
                    User user = identityService.newUser(userId);
                    user.setFirstName((String)m.get("userName"));
                    identityService.saveUser(user);
                    identityService.createMembership(userId,groupId);
                    map.put("message","success");
                }
            }
        }else{
            /*identityService.saveGroup(identityService.newGroup(groupId));
            for (String userId:userIds) {
                if (identityService.createUserQuery().userId(userId)!=null){
                    identityService.createMembership(userId,groupId);
                }else{
                    identityService.saveUser(identityService.newUser(userId));
                    identityService.createMembership(userId,groupId);
                }
            }*/
            map.put("message","false");
        }
        return map;
    }

    @Override
    public Map<String, Object> groupRemoveUser(String groupId, List<String> userIds) {
        Map<String,Object> map  = new HashMap<>();
        Group group = identityService.createGroupQuery().groupId(groupId).singleResult();
        if (group!=null) {
            for (String userId : userIds) {
                if (identityService.createUserQuery().userId(userId).singleResult()!=null){
                    identityService.deleteMembership(userId, groupId);
                }
            }
        }
        map.put("message","success");
        return map;
    }
}
