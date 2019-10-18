package com.richfit.jobticket.activiti.service.impl;

import com.richfit.jobticket.activiti.service.CertificateActivitiService;
import com.richfit.jobticket.certificate.mapper.CertificateMapper;
import com.richfit.jobticket.certificate.mapper.EndorsementMapper;
import com.richfit.jobticket.certificate.pojo.Zsjc;
import com.richfit.jobticket.platformBaseMaintain.mapper.WeekPlanMapper;
import com.richfit.jobticket.workApproval.mapper.ContractMapper;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CertificateActivitiServiceImpl implements CertificateActivitiService {
    @Autowired
    RuntimeService runtimeService; //流程控制服务
    @Autowired
    TaskService taskService; //任务服务
    @Autowired
    RepositoryService repositoryService; //管理流程仓库，部署，删除，读取
    @Autowired
    HistoryService historyService; //历史服务
    @Autowired
    IdentityService identityservice;
    @Autowired
    CertificateMapper certificateMapper;
    @Autowired
    EndorsementMapper endorsementMapper;

    @Override
    public Map<String, Object> CommitOneActivitiByUserId(String taskId, String userId, String opinions) throws Exception {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        ProcessInstance ins=runtimeService.createProcessInstanceQuery()//根据ProcessInstanceId获取当前运行的流程实例
                .processInstanceId(task.getProcessInstanceId())
                .singleResult();//需要配修改业务状态时使用
        Map<String,Object> map = new HashMap<>();
        Map<String,Object> variables=new HashMap<String,Object>();
        variables.put("users",userId);//可以是多个
        taskService.addComment(taskId,ins.getProcessInstanceId(),opinions);//储存批注信息
        taskService.complete(taskId,variables);
        map.put("magree","success");
        return map;
    }

    @Override
    public Map<String, Object> CommitOneActivitiByTaskId(String taskId) throws Exception {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        ProcessInstance ins=runtimeService.createProcessInstanceQuery()//根据ProcessInstanceId获取当前运行的流程实例
                .processInstanceId(task.getProcessInstanceId())
                .singleResult();//需要配修改业务状态时使用
        Map<String,Object> map = new HashMap<>();
        taskService.complete(taskId);
        map.put("magree","success");
        return map;
    }

    @Override
    public ProcessInstance startOneCertificateActiviti(String cerBaseId, String userId,String userId1,String userId2) throws Exception {
        Map<String, Object> variables = new HashMap<>();
        //设置核验人员
        variables.put("assignee1",userId1);
        //设置签批人员
        variables.put("assignee3",userId2);
        identityservice.setAuthenticatedUserId(userId);//记录启动人或办理人
        ProcessInstance instance=runtimeService.startProcessInstanceByKey("certificate1",cerBaseId,variables);//BusinessKey
        //修改证书基础
        endorsementMapper.updateProcessInstanceId(cerBaseId,instance.getProcessInstanceId());
        return instance;
    }

    @Override
    public Map<String, Object> CommitOneActiviti(String pid,String route,String opinions) throws Exception {
        Task t = taskService.createTaskQuery().processInstanceId(pid).singleResult();
        Map<String,Object> map = new HashMap<>();
        Map<String,Object> variables=new HashMap<String,Object>();
        variables.put("isgo",route);
        if (route.equals("true")){
            taskService.setVariable(t.getId(), "opinions", "同意");
        }else if (route.equals("false")){
            taskService.setVariable(t.getId(), "opinions", "返回");
        }
        taskService.addComment(t.getId(), pid, opinions);
        taskService.complete(t.getId(),variables);
        map.put("magree","success");
        return map;
    }
    @Override
    public Map<String, Object> getMywActivitisByType(String userId, String detailCode, Integer starPage, Integer size) throws Exception {
        Map<String,Object> map = new HashMap<>();
        List<Map<String,Object>> WaitLists = new ArrayList<>();
        List<Task> newTasks = new ArrayList<>();
        List<Task> tasks = taskService.createTaskQuery()
                .taskCandidateUser(userId)
                .list();

        for (Task t : tasks) {
            ProcessInstance ins = runtimeService.createProcessInstanceQuery()//根据ProcessInstanceId获取当前运行的流程实例
                    .processInstanceId(t.getProcessInstanceId())
                    .singleResult();
            if (ins == null) {
                continue;
            }
            if (ins.getProcessDefinitionName().equals(detailCode)){
                newTasks.add(t);
            }
        }
        Integer pageTotalCount =null;
        Integer totalCount =null;
        if (newTasks!=null){
            totalCount = newTasks.size();//总记录数
            pageTotalCount = totalCount%size == 0?totalCount/size:totalCount/size+1;//总页数
            if (starPage<1){
                starPage=1;
            }
            if (starPage>pageTotalCount){
                starPage=pageTotalCount;
            }
            Integer pageSize = totalCount - size*starPage;
            if (pageSize>size){
                pageSize = size;
            }
            if (pageSize<0){
                pageSize = totalCount;
            }
            int a = (starPage-1)*size;
            int b = (starPage-1)*size+pageSize;
            for (int i = a; i<b; i++) {
                Task t =newTasks.get(i);
                ProcessInstance ins=runtimeService.createProcessInstanceQuery()//根据ProcessInstanceId获取当前运行的流程实例
                        .processInstanceId(t.getProcessInstanceId())
                        .singleResult();
                if (ins==null){
                    continue;
                }
                //返回的数据接受getFrameContractApplyById
                //返回的数据接受
                Map<String,Object> m1 = new HashMap<>();
                m1.put("taskId",t.getId());
                m1.put("ProcessInstanceId",t.getProcessInstanceId());
                m1.put("data","");//证书实例
                WaitLists.add(m1);
            }
        }
        map.put("totalSize",totalCount);
        map.put("totalPages",pageTotalCount);
        map.put("data",WaitLists);
        return map;
    }

    @Override
    public Map<String, Object> getMyyActivitisByType(String userId, String detailCode, Integer starPage, Integer size) throws Exception {
        Map<String,Object> map = new HashMap<>();
        List<Map<String,Object>> WaitLists = new ArrayList<>();
        List<HistoricTaskInstance> newTasks = new ArrayList<>();
        List<HistoricTaskInstance> historicTaskInstances = historyService//与历史数据（历史表）相关的Service
                .createHistoricTaskInstanceQuery()//创建历史任务实例查询
                .taskCandidateUser(userId)//指定历史任务的办理人
                .finished()
                .list();

        for (HistoricTaskInstance t : historicTaskInstances) {
            ProcessInstance ins = runtimeService.createProcessInstanceQuery()//根据ProcessInstanceId获取当前运行的流程实例
                    .processInstanceId(t.getProcessInstanceId())
                    .singleResult();
            if (ins == null) {
                continue;
            }
            if (ins.getProcessDefinitionName().equals(detailCode)){
                newTasks.add(t);
            }
        }
        Integer pageTotalCount =null;
        Integer totalCount =null;
        if (newTasks!=null){
            totalCount = newTasks.size();//总记录数
            pageTotalCount = totalCount%size == 0?totalCount/size:totalCount/size+1;//总页数
            if (starPage<1){
                starPage=1;
            }
            if (starPage>pageTotalCount){
                starPage=pageTotalCount;
            }
            Integer pageSize = totalCount - size*starPage;
            if (pageSize>size){
                pageSize = size;
            }
            if (pageSize<0){
                pageSize = totalCount;
            }
            int a = (starPage-1)*size;
            int b = (starPage-1)*size+pageSize;
            for (int i = a; i<b; i++) {
                HistoricTaskInstance t =newTasks.get(i);
                ProcessInstance ins=runtimeService.createProcessInstanceQuery()//根据ProcessInstanceId获取当前运行的流程实例
                        .processInstanceId(t.getProcessInstanceId())
                        .singleResult();
                if (ins==null){
                    continue;
                }
                //返回的数据接受
                Map<String,Object> m1 = new HashMap<>();
                m1.put("taskId",t.getId());
                m1.put("ProcessInstanceId",t.getProcessInstanceId());
                m1.put("data",certificateMapper.selectCertificateBycerBaseId(ins.getBusinessKey()));//证书实例
                m1.put("endTime",t.getEndTime());
                WaitLists.add(m1);
            }
        }
        map.put("totalSize",totalCount);
        map.put("totalPages",pageTotalCount);
        map.put("data",WaitLists);
        return map;
    }

    @Override
    public Map<String, Object> getHeyan(String userId, String detailCode, Integer starPage, Integer size) {
        Map<String,Object> map = new HashMap<>();
        List<Map<String,Object>> WaitLists = new ArrayList<>();
        List<Task> newTasks = new ArrayList<>();
        List<Task> tasks = taskService.createTaskQuery()
                .taskCandidateUser(userId)
                .list();
        for (Task t : tasks) {
            ProcessInstance ins = runtimeService.createProcessInstanceQuery()//根据ProcessInstanceId获取当前运行的流程实例
                    .processInstanceId(t.getProcessInstanceId())
                    .singleResult();
            if (ins == null) {
                continue;
            }
            if (ins.getProcessDefinitionName().equals(detailCode)){
                System.out.println(t.getTaskDefinitionKey());
                if(t.getTaskDefinitionKey().equals("certificate1")){
                    newTasks.add(t);
                }
            }
            //至此已经获得到该用户处在核验的流程实例
        }
        Integer totalCount = newTasks.size();
        for (Task t: newTasks) {
            ProcessInstance ins = runtimeService.createProcessInstanceQuery()//根据ProcessInstanceId获取当前运行的流程实例
                    .processInstanceId(t.getProcessInstanceId())
                    .singleResult();
            System.out.println(ins.getBusinessKey());
            //证书主键
        }
        map.put("totalSize",totalCount);
        map.put("data",WaitLists);//分页部分
        return map;
    }

    @Override
    public Map<String, Object> getChoucha(String userId, String detailCode, Integer starPage, Integer size) {
        Map<String,Object> map = new HashMap<>();
        List<Map<String,Object>> WaitLists = new ArrayList<>();
        List<Task> newTasks = new ArrayList<>();
        List<Task> tasks = taskService.createTaskQuery()
                .taskCandidateUser(userId)
                .list();
        for (Task t : tasks) {
            ProcessInstance ins = runtimeService.createProcessInstanceQuery()//根据ProcessInstanceId获取当前运行的流程实例
                    .processInstanceId(t.getProcessInstanceId())
                    .singleResult();
            if (ins == null) {
                continue;
            }
            if (ins.getProcessDefinitionName().equals(detailCode)){
                System.out.println(t.getTaskDefinitionKey());
                if(t.getTaskDefinitionKey().equals("certificate3")){
                    newTasks.add(t);
                }
            }
            //至此已经获得到该用户处在核验的流程实例
        }
        Integer totalCount = newTasks.size();
        for (Task t: newTasks) {
            ProcessInstance ins = runtimeService.createProcessInstanceQuery()//根据ProcessInstanceId获取当前运行的流程实例
                    .processInstanceId(t.getProcessInstanceId())
                    .singleResult();
            System.out.println(ins.getBusinessKey());
            //证书主键
        }
        map.put("totalSize",totalCount);
        map.put("data",WaitLists);//分页部分
        return map;
    }

    @Override
    public Map<String, Object> getQianpi(String userId, String detailCode, Integer starPage, Integer size) {
        Map<String,Object> map = new HashMap<>();
        List<Map<String,Object>> WaitLists = new ArrayList<>();
        List<Task> newTasks = new ArrayList<>();
        List<Task> tasks = taskService.createTaskQuery()
                .taskCandidateUser(userId)
                .list();
        for (Task t : tasks) {
            ProcessInstance ins = runtimeService.createProcessInstanceQuery()//根据ProcessInstanceId获取当前运行的流程实例
                    .processInstanceId(t.getProcessInstanceId())
                    .singleResult();
            if (ins == null) {
                continue;
            }
            if (ins.getProcessDefinitionName().equals(detailCode)){
                System.out.println(t.getTaskDefinitionKey());
                if(t.getTaskDefinitionKey().equals("certificate4")){
                    newTasks.add(t);
                }
            }
            //至此已经获得到该用户处在核验的流程实例
        }
        Integer totalCount = newTasks.size();
        for (Task t: newTasks) {
            ProcessInstance ins = runtimeService.createProcessInstanceQuery()//根据ProcessInstanceId获取当前运行的流程实例
                    .processInstanceId(t.getProcessInstanceId())
                    .singleResult();
            System.out.println(ins.getBusinessKey());
            //证书主键
        }
        map.put("totalSize",totalCount);
        map.put("data",WaitLists);//分页部分
        return map;
    }

    @Override
    public Map<String, Object> getDayin(String userId, String detailCode, Integer starPage, Integer size) {
        Map<String,Object> map = new HashMap<>();
        List<Map<String,Object>> WaitLists = new ArrayList<>();
        List<Task> newTasks = new ArrayList<>();
        List<Task> tasks = taskService.createTaskQuery()
                .taskCandidateUser(userId)
                .list();
        for (Task t : tasks) {
            ProcessInstance ins = runtimeService.createProcessInstanceQuery()//根据ProcessInstanceId获取当前运行的流程实例
                    .processInstanceId(t.getProcessInstanceId())
                    .singleResult();
            if (ins == null) {
                continue;
            }
            if (ins.getProcessDefinitionName().equals(detailCode)){
                System.out.println(t.getTaskDefinitionKey());
                if(t.getTaskDefinitionKey().equals("certificate5")){
                    newTasks.add(t);
                }
            }
            //至此已经获得到该用户处在核验的流程实例
        }
        Integer totalCount = newTasks.size();
        for (Task t: newTasks) {
            ProcessInstance ins = runtimeService.createProcessInstanceQuery()//根据ProcessInstanceId获取当前运行的流程实例
                    .processInstanceId(t.getProcessInstanceId())
                    .singleResult();
            System.out.println(ins.getBusinessKey());
            //证书主键
        }
        map.put("totalSize",totalCount);
        map.put("data",WaitLists);//分页部分
        return map;
    }

    @Override
    public Map<String, Object> getFafang(String userId, String detailCode, Integer starPage, Integer size) {
        Map<String,Object> map = new HashMap<>();
        List<Map<String,Object>> WaitLists = new ArrayList<>();
        List<Task> newTasks = new ArrayList<>();
        List<Task> tasks = taskService.createTaskQuery()
                .taskCandidateUser(userId)
                .list();
        for (Task t : tasks) {
            ProcessInstance ins = runtimeService.createProcessInstanceQuery()//根据ProcessInstanceId获取当前运行的流程实例
                    .processInstanceId(t.getProcessInstanceId())
                    .singleResult();
            if (ins == null) {
                continue;
            }
            if (ins.getProcessDefinitionName().equals(detailCode)){
                System.out.println(t.getTaskDefinitionKey());
                if(t.getTaskDefinitionKey().equals("certificate6")){
                    newTasks.add(t);
                }
            }
            //至此已经获得到该用户处在核验的流程实例
        }
        Integer totalCount = newTasks.size();
        for (Task t: newTasks) {
            ProcessInstance ins = runtimeService.createProcessInstanceQuery()//根据ProcessInstanceId获取当前运行的流程实例
                    .processInstanceId(t.getProcessInstanceId())
                    .singleResult();
            System.out.println(ins.getBusinessKey());
            //证书主键
        }
        map.put("totalSize",totalCount);
        map.put("data",WaitLists);//分页部分
        return map;
    }
}


