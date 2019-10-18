package com.richfit.jobticket.activiti.service.impl;

import com.richfit.jobticket.activiti.service.ContractCirculationService;
import com.richfit.jobticket.base.util.SendEmail;
import com.richfit.jobticket.certificate.mapper.CertificateMapper;
import com.richfit.jobticket.customerOnlinePlatform.mapper.OrdinaryContractChangeApplyMapper;
import com.richfit.jobticket.platformBaseMaintain.mapper.WeekPlanMapper;
import com.richfit.jobticket.workApproval.mapper.ContractMapper;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.identity.User;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ContractCirculationServiceImpl implements ContractCirculationService {
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
    WeekPlanMapper weekPlanMapper;
    @Autowired
    ContractMapper contractMapper;
    @Autowired
    private SendEmail sendEmail;
    @Autowired
    private OrdinaryContractChangeApplyMapper ordinaryContractChangeApplyMapper;
    @Autowired
    private ContractMapper mapper;


    @Override
    public ProcessInstance startOneContractSettlement(String conId, String email, Map<String, Object> variables) throws Exception {
        identityservice.setAuthenticatedUserId(email);//记录启动人或办理人
        ProcessInstance instance=runtimeService.startProcessInstanceByKey("contractSettlement",conId,variables);
        return instance;
    }

    @Override
    public Map<String, Object> overLastOneActivitiTwo(String pid) throws Exception {
//        String email = null;
//        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
//        ProcessInstance ins=runtimeService.createProcessInstanceQuery()//根据ProcessInstanceId获取当前运行的流程实例
//                .processInstanceId(task.getProcessInstanceId())
//                .singleResult();
//        email = ins.getStartUserId();
        Task t = taskService.createTaskQuery().processInstanceId(pid).singleResult();
        Map<String,Object> map = new HashMap<>();
        taskService.complete(t.getId());
//        ProcessInstance rpi = runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
        //流程正常结束，状态：审批通过
//        if (rpi == null) {
//            sendEmail.sendTxtMail("合同审批通知", "一封来自南京计量中心检定平台的邮件，您的合同内部已经确认，请您尽快登陆查看确认！", email);
//        }
        map.put("magree","success");
        return map;
    }

    @Override
    public Map<String, Object> overLastOneActivitiFirst(String pid) throws Exception {
        String email = null;
//        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
//        ProcessInstance ins=runtimeService.createProcessInstanceQuery()//根据ProcessInstanceId获取当前运行的流程实例
//                .processInstanceId(task.getProcessInstanceId())
//                .singleResult();
        Task t = taskService.createTaskQuery().processInstanceId(pid).singleResult();
        ProcessInstance ins = runtimeService.createProcessInstanceQuery().processInstanceId(pid).singleResult();
        String userId = ins.getStartUserId();
        User user = identityservice.createUserQuery().userId(userId).singleResult();
        Map<String,Object> map = new HashMap<>();
        taskService.complete(t.getId());
        ProcessInstance rpi = runtimeService.createProcessInstanceQuery().processInstanceId(pid).singleResult();
        if (user!=null) {
            if (user.getEmail() != null){
                if (rpi == null) {
                    sendEmail.sendTxtMail("合同审批通知", "一封来自南京计量中心检定平台的邮件，您的合同内部已经确认，请您尽快登陆查看确认！", user.getEmail());
                }
            }
        }
        //流程正常结束，状态：审批通过

        map.put("magree","success");
        return map;
    }

    @Override
    public Map<String, Object> CommitOneActiviti(String workId, String route, String opinions) throws Exception {
        String email = null;
        User user =null;
        Task task = taskService.createTaskQuery().taskId(workId).singleResult();
        ProcessInstance ins=runtimeService.createProcessInstanceQuery()//根据ProcessInstanceId获取当前运行的流程实例
                .processInstanceId(task.getProcessInstanceId())
                .singleResult();
        if (task.getTaskDefinitionKey().equals("_3")) {
            String userId = ins.getStartUserId();
            user = identityservice.createUserQuery().userId(userId).singleResult();
            if (user!=null) {
                if (user.getEmail() != null){
                    sendEmail.sendTxtMail("合同审批通知", "一封来自南京计量中心检定平台的邮件，您的合同内部已经确认，请您尽快登陆查看确认！", user.getEmail());
                }
            }
        }
        Map<String,Object> map = new HashMap<>();
        Map<String,Object> variables=new HashMap<String,Object>();
        variables.put("isgo",route);
        taskService.addComment(workId,ins.getProcessInstanceId(),opinions);//储存批注信息
        taskService.complete(workId,variables);
        ProcessInstance rpi = runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
        //流程正常结束，状态：审批通过
        if (rpi == null && route.equals("true")) {
            if (user!=null) {
                if (user.getEmail() != null){
                    sendEmail.sendTxtMail("合同审批通知", "一封来自南京计量中心检定平台的邮件，您的合同已经通过审批了！请您尽快登陆查看确认！", user.getEmail());
                }
            }
        }
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
                m1.put("data",ordinaryContractChangeApplyMapper.selectContractBaseByPrimaryKey(ins.getBusinessKey()));//证书实例
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
                m1.put("data",ordinaryContractChangeApplyMapper.selectContractBaseByPrimaryKey(ins.getBusinessKey()));//证书实例
                m1.put("endTime",t.getEndTime());
                WaitLists.add(m1);
            }
        }
        map.put("totalSize",totalCount);
        map.put("totalPages",pageTotalCount);
        map.put("data",WaitLists);
        return map;
    }
}
