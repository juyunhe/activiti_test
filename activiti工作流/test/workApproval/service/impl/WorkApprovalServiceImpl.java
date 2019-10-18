package com.richfit.jobticket.workApproval.service.impl;

import com.richfit.jobticket.activiti.pojo.EndList;
import com.richfit.jobticket.base.config.DicConfig;
import com.richfit.jobticket.base.util.EntityResult;
import com.richfit.jobticket.base.util.SendEmail;
import com.richfit.jobticket.customerOnlinePlatform.mapper.OrdinaryContractApplyMapper;
import com.richfit.jobticket.customerOnlinePlatform.pojo.FrameBaseApply;
import com.richfit.jobticket.customerOnlinePlatform.service.OrdinaryContractApplyService;
import com.richfit.jobticket.internalOnline.service.ContractCreatedService;
import com.richfit.jobticket.workApproval.mapper.ContractMapper;
import com.richfit.jobticket.workApproval.service.WorkApprovalService;
import org.activiti.engine.*;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.identity.User;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.richfit.jobticket.platformBaseMaintain.service.impl.CusQualificationServiceImpl.formatTimeEight;

@Service("workApprovalServiceImpl")
public class WorkApprovalServiceImpl implements WorkApprovalService {
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private FormService formService;
    @Autowired
    IdentityService identityservice;//用户服务
    @Autowired
    private SendEmail sendEmail;
    @Autowired
    HistoryService historyService; //历史服务
    @Autowired
    OrdinaryContractApplyMapper mapper;
    @Autowired
    private ContractCreatedService contractCreatedService;

    @Autowired
    private OrdinaryContractApplyService ordinaryContractApplyService;

    @Override
    public int reportWorkInfoTwo(String workId, String email,String userId) {
        Map<String, Object> param = new HashMap<String, Object>();
        User user = identityservice.createUserQuery().userId(userId).singleResult();
        if (user!=null){
            if (email!=null){
                user.setEmail(email);
                identityservice.saveUser(user);
            }
        }else{
            User user1 = identityservice.newUser(userId);
            user1.setEmail(email);
            identityservice.saveUser(user1);
        }
        identityservice.setAuthenticatedUserId(userId);
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("newWork2", workId, param);
        /*return mapper.updateContract(pi.getProcessInstanceId(), workId, "ver0000080000005");*/
        return mapper.updateApplyStatus(workId, "ver0000020000002");
    }

    @Override
    public int reportWorkInfoOne(String workId, String email,String userId) {
        Map<String, Object> param = new HashMap<String, Object>();
        User user = identityservice.createUserQuery().userId(userId).singleResult();
        if (user!=null){
            if (email!=null){
                user.setEmail(email);
                identityservice.saveUser(user);
            }
        }else{
            User user1 = identityservice.newUser(userId);
            user1.setEmail(email);
            identityservice.saveUser(user1);
        }
        identityservice.setAuthenticatedUserId(userId);
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("newWork1", workId, param);
        /*return mapper.updateContract(pi.getProcessInstanceId(), workId, "ver0000080000005");*/
        int i = 0;
        //调用普通申请提交方法
        EntityResult result = ordinaryContractApplyService.updateApplyStatus(workId, "ver0000020000002");
        if (result.getStatus().equals("200")) {
            i= Integer.parseInt(result.getData().toString());
        }
        return i;
        //return mapper.updateApplyStatus(workId,"ver0000020000002");
    }

    @Override
    public int approvalWorkById(String taskId, String workId, String route, String opinions) throws Exception {
        String userId = "";
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        String procInstId = task.getProcessInstanceId();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("route", route);
        taskService.setVariable(taskId, "opinions", opinions);
        SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        /*Date madeTimeDate = df1.parse(reqEndTime);
        String madeTime = formatTimeEight(df1.format(madeTimeDate));//mytime 是原来的时间，newtime是新时间
        taskService.setVariable(taskId, "reqEndTime", madeTime);//已经转化时间格式*/
        taskService.addComment(taskId, procInstId, opinions);
        ProcessInstance nstance = runtimeService.createProcessInstanceQuery().processInstanceId(procInstId).singleResult();
        userId = nstance.getStartUserId();
        if (task.getTaskDefinitionKey().equals("_7")) {
            User user = identityservice.createUserQuery().userId(userId).singleResult();
            if (user!=null) {
                if (user.getEmail() != null){
                    sendEmail.sendTxtMail("申请函通知", "一封来自南京计量中心检定平台的邮件，您的检定申请函已更新，请您尽快登陆查看", user.getEmail());
                }
            }
        }
        if (task.getTaskDefinitionKey().equals("_3")) {
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(procInstId).singleResult();
            String appBaseId = processInstance.getBusinessKey();
            String type = processInstance.getProcessDefinitionName();
            Map<String,Object> map =new HashMap<>();
            if(type.equals("ver00000120000002")){
                map = (Map<String,Object>)contractCreatedService.getFrameContractApplyById(appBaseId).getData();
            }
            if(type.equals("ver00000120000001")){
                map = (Map<String,Object>)contractCreatedService.getOrdinaryContractApplyById(appBaseId).getData();
            }
            if (appBaseId != null) {
                //调用根据id获取获取申请基础判断南京还是广州
                if (map.get("verInstName").equals("南京分站")){
                    params.put("userId","8a960ce96ab3fc95016ac410092c0250");//南京郭哲
                }
                if (map.get("verInstName").equals("广州分站")){
                    params.put("userId","8a960ce96ab3fc95016ac4054493020e");//广州孙楠
                }
            }
        }
        /*double d = Math.random();
        Integer i = (int)(d*100);
        taskService.claim(taskId,i.toString());*/
        taskService.complete(taskId, params);
        if (route.equals("false")) {
            /*return mapper.updateContract(procInstId, workId, "ver0000080000008");*/
            return mapper.updateApplyStatus(workId, "ver0000020000005");
        }
        ProcessInstance rpi = runtimeService.createProcessInstanceQuery().processInstanceId(procInstId).singleResult();
        if (task.getTaskDefinitionKey().equals("_4")) {
            return mapper.updateApplyStatus(workId, "ver0000020000003");
        }
        //流程正常结束，状态：审批通过
        if (rpi == null) {
            User user = identityservice.createUserQuery().userId(userId).singleResult();
            if (user!=null) {
                if (user.getEmail() != null){
                    sendEmail.sendTxtMail("检定申请通知", "一封来自南京计量中心检定平台的邮件，您的检定申请已通过，可以编制合同了", user.getEmail());
                }
            }
            return mapper.updateApplyStatus(workId, "ver0000020000004");
        }
        return 1;
    }

    @Override
    public Map<String, Object> getVerificationInformation(String appBaseId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        List<EndList> EndLists = new ArrayList<>();//已完成的任务
        List<HistoricTaskInstance> historicTaskInstances = historyService//与历史数据（历史表）相关的Service
                .createHistoricTaskInstanceQuery().orderByHistoricTaskInstanceEndTime().desc()//创建历史任务实例查询
                .taskCandidateUser("8a960ce96a33753c016a35b9bd3b0366")//指定历史任务的办理人
                .finished()//已完成的任务
                .list();
        if (historicTaskInstances.size() < 1) {
            map.put("data", historicTaskInstances.size());
            return map;
        }
        String endTime = null;
        for (HistoricTaskInstance h : historicTaskInstances) {
            HistoricProcessInstance hins = historyService // 历史任务Service，根据ProcessInstanceId获取历史流程实例
                    .createHistoricProcessInstanceQuery() // 创建历史流程实例查询
                    .processInstanceId(h.getProcessInstanceId()) // 指定流程实例ID
                    .singleResult();
            SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (hins.getBusinessKey().equals(appBaseId)) {
                if (h.getEndTime() != null) {
                    endTime = formatTimeEight(df1.format(h.getEndTime()));
                }
            }
        }
        map.put("people1", "李雪健");
        map.put("people2", "陈行川");
        map.put("people3", "周雷");
        map.put("endTime", endTime);
        return map;
    }
}