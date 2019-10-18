package com.richfit.jobticket.activiti.service.impl;

import com.richfit.jobticket.activiti.pojo.AppWaitList;
import com.richfit.jobticket.activiti.pojo.EndList;
import com.richfit.jobticket.activiti.pojo.WaitList;
import com.richfit.jobticket.activiti.service.WeekPlanActivitiService;
import com.richfit.jobticket.base.util.DateUtils;
import com.richfit.jobticket.customerOnlinePlatform.mapper.OrdinaryContractChangeApplyMapper;
import com.richfit.jobticket.customerOnlinePlatform.pojo.ContractBaseMsg;
import com.richfit.jobticket.internalOnline.mapper.ContractCreatedMapper;
import com.richfit.jobticket.internalOnline.service.ContractCreatedService;
import com.richfit.jobticket.platformBaseMaintain.mapper.WeekPlanMapper;
import com.richfit.jobticket.platformBaseMaintain.pojo.WeeklyVerificationPlan;
import com.richfit.jobticket.workApproval.mapper.ContractMapper;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.engine.*;
import org.activiti.engine.history.*;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.activiti.image.ProcessDiagramGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.richfit.jobticket.platformBaseMaintain.service.impl.CusQualificationServiceImpl.formatTimeEight;

@Service
public class WeekPlanActivitiServiceImpl implements WeekPlanActivitiService {

    @Autowired
    RuntimeService runtimeService; //流程控制服务
    @Autowired
    TaskService taskService; //任务服务
    @Autowired
    RepositoryService repositoryService; //管理流程仓库，部署，删除，读取
    @Autowired
    HistoryService historyService; //历史服务
    @Autowired
    IdentityService identityservice;//用户服务
    @Autowired
    WeekPlanMapper weekPlanMapper;
    @Autowired
    OrdinaryContractChangeApplyMapper ordinaryContractChangeApplyMapper;
    @Autowired
    ContractCreatedMapper contractCreatedMapper;
    @Autowired
    private ContractMapper mapper;

    @Autowired
    private ContractCreatedService contractCreatedService;//查询普通申请

    @Override
    public ProcessInstance startOneWeekActiviti(String weekVerId, String email, Map<String, Object> variables) throws Exception {
        identityservice.setAuthenticatedUserId(email);
        ProcessInstance instance = runtimeService.startProcessInstanceByKey("weekplan3", weekVerId, variables);//BusinessKey
        weekPlanMapper.updateWeeklyVerificationPlanByWeekVerId(weekVerId, "ver00000140000001");
        return instance;
    }

    @Override
    public ProcessInstance startOneContractActiviti(String conId, String email,String userId ,Map<String, Object> variables) throws Exception {
        User user = identityservice.createUserQuery().userId(userId).singleResult();
        if (user!=null){
            if (user.getEmail()==null){
                user.setEmail(email);
                identityservice.saveUser(user);
            }else if(user.getEmail()!=email){
                user.setEmail(email);
                identityservice.saveUser(user);
            }
        }else{
            User user1 = identityservice.newUser(userId);
            user1.setEmail(email);
            identityservice.saveUser(user1);
        }
        identityservice.setAuthenticatedUserId(userId);
        ProcessInstance instance = runtimeService.startProcessInstanceByKey("newContract2", conId, variables);
        contractCreatedService.updateProcessInstanceId(conId, instance.getId());
        return instance;
    }

    @Override
    public ProcessInstance startOneUserActiviti(String custId, String email,String userId, Map<String, Object> variables) throws Exception {
        User user = identityservice.createUserQuery().userId(userId).singleResult();
        if (user!=null){
            if (user.getEmail()==null){
                user.setEmail(email);
                identityservice.saveUser(user);
            }else if(user.getEmail()!=email){
                user.setEmail(email);
                identityservice.saveUser(user);
            }
        }else{
            User user1 = identityservice.newUser(userId);
            user1.setEmail(email);
            identityservice.saveUser(user1);
        }
        identityservice.setAuthenticatedUserId(userId);
        ProcessInstance instance = runtimeService.startProcessInstanceByKey("newContract3", custId, variables);
        contractCreatedService.updateProcessInstanceId(custId, instance.getId());
        return instance;
    }


   /* @Override
    public ProcessInstance startOneYearActiviti(String annPlanId,String userId, Map<String, Object> variables) throws Exception {
        return null;
    }*/


    @Override
    public Map<String, Object> commitOneActiviti(String workId, String route, String opinions) throws Exception {
        Map<String, Object> map = new HashMap<>();
        Task task = taskService.createTaskQuery().taskId(workId).singleResult();
        ProcessInstance ins = runtimeService.createProcessInstanceQuery()//根据ProcessInstanceId获取当前运行的流程实例
                .processInstanceId(task.getProcessInstanceId())
                .singleResult();
        if (route.equals("false")) {
            weekPlanMapper.updateWeeklyVerificationPlanByWeekVerId(ins.getBusinessKey(), "ver00000140000006");//修改状态
            if (opinions.equals("已终止")){
                weekPlanMapper.updateWeeklyVerificationPlanByWeekVerId(ins.getBusinessKey(), "ver00000140000005");//修改状态
            }
        }
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("isgo", route);
        taskService.setVariable(workId, "opinions", opinions);
        /*SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date madeTimeDate = df1.parse(reqEndTime);
        String madeTime = formatTimeEight(df1.format(madeTimeDate));//mytime 是原来的时间，newtime是新时间
        taskService.setVariable(workId,"reqEndTime",madeTime);//已经转化时间格式*/
        taskService.addComment(workId, task.getProcessInstanceId(), opinions);
        taskService.complete(workId, variables);
        if (route.equals("true") && task.getTaskDefinitionKey().equals("productionsectionaudit")) {
            int i = weekPlanMapper.updateWeeklyVerificationPlanByWeekVerId(ins.getBusinessKey(), "ver00000140000002");//修改状态
        }
        if (route.equals("true") && task.getTaskDefinitionKey().equals("coreleaderaudit")) {
            int i = weekPlanMapper.updateWeeklyVerificationPlanByWeekVerId(ins.getBusinessKey(), "ver00000140000004");//修改状态
        }
        map.put("magree", "success");
        return map;
    }


    @Override
    public Map<String, Object> getMywActivitisByAssignee(String role, Integer starPage, Integer size) throws Exception {//查看我的代办
        Map<String, Object> map = new HashMap<>();
        List<WaitList> WaitLists = new ArrayList<>();

        List<Task> tasks = taskService.createTaskQuery().orderByTaskCreateTime().desc()
                .taskCandidateUser(role)
                .listPage((starPage - 1) * size, size);
        List<Task> taskss = taskService.createTaskQuery()
                .taskCandidateUser(role)
                .list();
        Integer totalCount = taskss.size();
        Integer pageTotalCount = totalCount % size == 0 ? totalCount / size : totalCount / size + 1;//总页数
        if (tasks.size() < 1) {
            map.put("data", "null");
            return map;
        }
        for (Task t : tasks) {
            ProcessInstance ins = runtimeService.createProcessInstanceQuery()//根据ProcessInstanceId获取当前运行的流程实例
                    .processInstanceId(t.getProcessInstanceId())
                    .singleResult();
            if (ins == null) {
                continue;
            }
            WaitList waitList = new WaitList();
            waitList.setWaitId(ins.getBusinessKey());
            waitList.setCloseCondition("流程完结或撤销");
            if (ins.getProcessDefinitionName().equals("ver00000120000001")) {
                waitList.setWaitType("普通检定");//使用流程图的名字作为待办的类型名称
            }
            if (ins.getProcessDefinitionName().equals("ver00000120000002")) {
                waitList.setWaitType("框架检定");//使用流程图的名字作为待办的类型名称
            }
            if (ins.getProcessDefinitionName().equals("ver00000120000003")) {
                waitList.setWaitType("合同");//使用流程图的名字作为待办的类型名称
            }
            if (ins.getProcessDefinitionName().equals("ver00000120000005")) {
                waitList.setWaitType("证书");//使用流程图的名字作为待办的类型名称
            }
            if (ins.getProcessDefinitionName().equals("ver00000120000006")) {
                waitList.setWaitType("周计划");//使用流程图的名字作为待办的类型名称
            }
            if (ins.getProcessDefinitionName().equals("ver00000120000007")) {
                waitList.setWaitType("临时计划");//使用流程图的名字作为待办的类型名称
            }
            waitList.setMessage((String) taskService.getVariable(t.getId(), "opinions"));
            /*waitList.setReqEndTime((String)taskService.getVariable(t.getId(),"reqEndTime"));*/
            waitList.setProcessInstanceId(ins.getProcessInstanceId());
            waitList.setTaskId(t.getId());
            waitList.setDetailsUrl(t.getFormKey());
            waitList.setWaitName(t.getName());
            DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//需要转化成的时间格式
            df1.setTimeZone(TimeZone.getTimeZone("GMT"));
            String startTime = null;
            if (ins.getStartTime() != null) {
                startTime = formatTimeEight(df1.format(t.getCreateTime()));
            }
            waitList.setStartTime(startTime);
            WaitLists.add(waitList);
        }
        map.put("totalSize", totalCount);
        map.put("totalPages", pageTotalCount);
        map.put("data", WaitLists);
        return map;

    }

    @Override
    public Map<String, Object> getMywActivitisByType(String userId, String typeName, Integer starPage, Integer size) throws Exception {
        Map<String, Object> map = new HashMap<>();
        List<AppWaitList> WaitLists = new ArrayList<>();
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
            if (ins.getProcessDefinitionName().equals(typeName)) {
                newTasks.add(t);
            }
        }
        Integer pageTotalCount = null;
        Integer totalCount = null;
        if (newTasks != null) {
            totalCount = newTasks.size();//总记录数
            pageTotalCount = totalCount % size == 0 ? totalCount / size : totalCount / size + 1;//总页数
            if (starPage < 1) {
                starPage = 1;
            }
            if (starPage > pageTotalCount) {
                starPage = pageTotalCount;
            }
            Integer pageSize = totalCount - size * starPage;
            if (pageSize > size) {
                pageSize = size;
            }
            if (pageSize < 0) {
                pageSize = totalCount;
            }
            int a = (starPage - 1) * size;
            int b = (starPage - 1) * size + pageSize;
            for (int i = a; i < b; i++) {
                Task t = newTasks.get(i);
                ProcessInstance ins = runtimeService.createProcessInstanceQuery()//根据ProcessInstanceId获取当前运行的流程实例
                        .processInstanceId(t.getProcessInstanceId())
                        .singleResult();
                if (ins == null) {
                    continue;
                }
                AppWaitList waitList = new AppWaitList();
                waitList.setWorkId(ins.getBusinessKey());
                waitList.setProcessInstanceId(ins.getProcessInstanceId());
                waitList.setTaskId(t.getId());
                WeeklyVerificationPlan w = weekPlanMapper.getWeeklyVerificationPlanByWeekVerId(ins.getBusinessKey());
                waitList.setEndTime(w.getEndTime());
                waitList.setMadeTime(w.getMadeTime());
                waitList.setStartTime(w.getStartTime());
                waitList.setMadePrearf(w.getMadePrepare());
                waitList.setState(w.getState());
                WaitLists.add(waitList);
            }
        }
        map.put("totalSize", totalCount);
        map.put("totalPages", pageTotalCount);
        map.put("data", WaitLists);
        return map;
    }

    @Override
    public Map<String, Object> getMyyActivitisByType(String userId, String typeName, Integer starPage, Integer size) throws Exception {
        Map<String, Object> map = new HashMap<>();
        List<AppWaitList> WaitLists = new ArrayList<>();
        List<HistoricTaskInstance> newTasks = new ArrayList<>();
        List<HistoricTaskInstance> historicTaskInstances = historyService//与历史数据（历史表）相关的Service
                .createHistoricTaskInstanceQuery()//创建历史任务实例查询
                .taskCandidateUser(userId)//指定历史任务的办理人
                .finished()
                .list();

        for (HistoricTaskInstance t : historicTaskInstances) {
            HistoricProcessInstance ins = historyService // 历史任务Service，根据ProcessInstanceId获取历史流程实例
                    .createHistoricProcessInstanceQuery() // 创建历史流程实例查询
                    .processInstanceId(t.getProcessInstanceId()) // 指定流程实例ID
                    .singleResult();
            if (ins == null) {
                continue;
            }
            if (ins.getProcessDefinitionName().equals(typeName)) {
                newTasks.add(t);
            }
        }
        Integer pageTotalCount = null;
        Integer totalCount = null;
        if (newTasks != null) {
            totalCount = newTasks.size();//总记录数
            pageTotalCount = totalCount % size == 0 ? totalCount / size : totalCount / size + 1;//总页数
            if (starPage < 1) {
                starPage = 1;
            }
            if (starPage > pageTotalCount) {
                starPage = pageTotalCount;
            }
            Integer pageSize = totalCount - size * starPage;
            if (pageSize > size) {
                pageSize = size;
            }
            if (pageSize < 0) {
                pageSize = totalCount;
            }
            int a = (starPage - 1) * size;
            int b = (starPage - 1) * size + pageSize;
            for (int i = a; i < b; i++) {
                HistoricTaskInstance t = newTasks.get(i);
                ProcessInstance ins = runtimeService.createProcessInstanceQuery()//根据ProcessInstanceId获取当前运行的流程实例
                        .processInstanceId(t.getProcessInstanceId())
                        .singleResult();
                if (ins == null) {
                    continue;
                }
                AppWaitList waitList = new AppWaitList();
                waitList.setWorkId(ins.getBusinessKey());
                waitList.setProcessInstanceId(ins.getProcessInstanceId());
                waitList.setTaskId(t.getId());
                WeeklyVerificationPlan w = weekPlanMapper.getWeeklyVerificationPlanByWeekVerId(ins.getBusinessKey());
                waitList.setEndTime(w.getEndTime());
                waitList.setMadeTime(w.getMadeTime());
                waitList.setStartTime(w.getStartTime());
                waitList.setMadePrearf(w.getMadePrepare());
                waitList.setState(w.getState());
                DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//需要转化成的时间格式
                df1.setTimeZone(TimeZone.getTimeZone("GMT"));
                String endTime = null;
                if (t.getEndTime() != null) {
                    endTime = formatTimeEight(df1.format(t.getEndTime()));
                }
                waitList.setHandle(endTime);
                WaitLists.add(waitList);
            }
        }
        map.put("totalSize", totalCount);
        map.put("totalPages", pageTotalCount);
        map.put("data", WaitLists);
        return map;
    }

    @Override
    public Map<String, Object> getMyyActivitisByAssignee(String role, Integer starPage, Integer size) throws Exception {

        Map<String, Object> map = new HashMap<>();
        List<EndList> EndLists = new ArrayList<>();
        Integer totalCount = historyService//与历史数据（历史表）相关的Service
                .createHistoricTaskInstanceQuery()//创建历史任务实例查询
                .taskCandidateUser(role)//指定历史任务的办理人
                .finished().list().size();//已完成的任务
        List<HistoricTaskInstance> historicTaskInstances = historyService//与历史数据（历史表）相关的Service
                .createHistoricTaskInstanceQuery().orderByHistoricTaskInstanceEndTime().desc()//创建历史任务实例查询
                .taskCandidateUser(role)//指定历史任务的办理人
                .finished()//已完成的任务
                .listPage((starPage - 1) * size, size);
        if (historicTaskInstances.size() < 1) {
            map.put("data", historicTaskInstances.size());
            return map;
        }
        for (HistoricTaskInstance h : historicTaskInstances) {
            HistoricProcessInstance hins = historyService // 历史任务Service，根据ProcessInstanceId获取历史流程实例
                    .createHistoricProcessInstanceQuery() // 创建历史流程实例查询
                    .processInstanceId(h.getProcessInstanceId()) // 指定流程实例ID
                    .singleResult();
            if (hins == null) {
                continue;
            }
            EndList endList = new EndList();
            endList.setEndId(hins.getBusinessKey());
            if (hins.getProcessDefinitionName().equals("ver00000120000001")) {
                endList.setWaitType("普通检定");//使用流程图的名字作为待办的类型名称
            }
            if (hins.getProcessDefinitionName().equals("ver00000120000002")) {
                endList.setWaitType("框架检定");//使用流程图的名字作为待办的类型名称
            }
            if (hins.getProcessDefinitionName().equals("ver00000120000003")) {
                endList.setWaitType("合同");//使用流程图的名字作为待办的类型名称
            }
            if (hins.getProcessDefinitionName().equals("ver00000120000005")) {
                endList.setWaitType("证书");//使用流程图的名字作为待办的类型名称
            }
            if (hins.getProcessDefinitionName().equals("ver00000120000006")) {
                endList.setWaitType("周计划");//使用流程图的名字作为待办的类型名称
            }
            if (hins.getProcessDefinitionName().equals("ver00000120000007")) {
                endList.setWaitType("临时计划");//使用流程图的名字作为待办的类型名称
            }
            List<Comment> comments = taskService.getProcessInstanceComments(h.getProcessInstanceId());
            if (comments != null) {
                for (Comment c : comments) {
                    if (c.getTaskId().equals(h.getId())) {
                        endList.setMessage(c.getFullMessage());
                    }
                }
            }
            List<HistoricVariableInstance> lists = historyService.createHistoricVariableInstanceQuery().processInstanceId(h.getProcessInstanceId()).list();
            /*if (lists!=null){
                for (HistoricVariableInstance h1:lists) {
                    if(h1.getVariableName().equals("reqEndTime")){
                        if (h1.getValue()!=null) {
                            endList.setReqEndTime(h1.getValue().toString());
                        }
                    }
                }
            }*/
            endList.setProcessInstanceId(hins.getSuperProcessInstanceId());
            endList.setTaskId(h.getId());
            endList.setEndName(h.getName());
            DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//需要转化成的时间格式
            df1.setTimeZone(TimeZone.getTimeZone("GMT"));
            String endTime = null;
            String startTime = null;
            if (hins.getStartTime() != null) {
                startTime = formatTimeEight(df1.format(hins.getStartTime()));
            }
            if (h.getEndTime() != null) {
                endTime = formatTimeEight(df1.format(h.getEndTime()));
            }
            endList.setStartTime(startTime);
            endList.setEndTime(endTime);
            endList.setDetailsUrl(h.getFormKey());
            EndLists.add(endList);
        }
        map.put("totalSize", totalCount);
        map.put("totalPages", starPage);
        map.put("data", EndLists);
        return map;
    }


    @Override
    public Map<String, Object> overLastOneActiviti(String workId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        Task task = taskService.createTaskQuery().taskId(workId).singleResult();
        ProcessInstance ins = runtimeService.createProcessInstanceQuery()//根据ProcessInstanceId获取当前运行的流程实例
                .processInstanceId(task.getProcessInstanceId())
                .singleResult();
        weekPlanMapper.updateWeeklyVerificationPlanByWeekVerId(ins.getBusinessKey(), "ver00000140000004");
        taskService.complete(workId);
        map.put("magree", "success");
        return map;
    }

    @Override
    public Map<String, Object> overLasttoOneActiviti(String workId, String userId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> variables = new HashMap<>();
        variables.put("users", userId);
        taskService.complete(workId, variables);
        map.put("magree", "success");
        return map;
    }

    @Override
    public Map<String, Object> getMywActivitisByTypeByKuang(String userId, String typeName, Integer starPage, Integer size) throws Exception {
        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> WaitLists = new ArrayList<>();
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
            if (ins.getProcessDefinitionName().equals(typeName)) {
                newTasks.add(t);
            }
        }
        Integer pageTotalCount = null;
        Integer totalCount = null;
        if (newTasks != null) {
            totalCount = newTasks.size();//总记录数
            pageTotalCount = totalCount % size == 0 ? totalCount / size : totalCount / size + 1;//总页数
            if (starPage < 1) {
                starPage = 1;
            }
            if (starPage > pageTotalCount) {
                starPage = pageTotalCount;
            }
            Integer pageSize = totalCount - size * starPage;
            if (pageSize > size) {
                pageSize = size;
            }
            if (pageSize < 0) {
                pageSize = totalCount;
            }
            int a = (starPage - 1) * size;
            int b = (starPage - 1) * size + pageSize;
            for (int i = a; i < b; i++) {
                Task t = newTasks.get(i);
                ProcessInstance ins = runtimeService.createProcessInstanceQuery()//根据ProcessInstanceId获取当前运行的流程实例
                        .processInstanceId(t.getProcessInstanceId())
                        .singleResult();
                if (ins == null) {
                    continue;
                }
                //返回的数据接受getFrameContractApplyById
                //返回的数据接受
                Map<String, Object> m1 = new HashMap<>();
                m1.put("taskId", t.getId());
                m1.put("ProcessInstanceId", t.getProcessInstanceId());
                m1.put("data", contractCreatedService.getFrameContractApplyById(ins.getBusinessKey()).getData());
                WaitLists.add(m1);
            }
        }
        map.put("totalSize", totalCount);
        map.put("totalPages", pageTotalCount);
        map.put("data", WaitLists);
        return map;
    }

    @Override
    public Map<String, Object> getMyyActivitisByTypeByKuang(String userId, String typeName, Integer starPage, Integer size) throws Exception {
        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> WaitLists = new ArrayList<>();
        List<HistoricTaskInstance> newTasks = new ArrayList<>();
        List<HistoricTaskInstance> historicTaskInstances = historyService//与历史数据（历史表）相关的Service
                .createHistoricTaskInstanceQuery()//创建历史任务实例查询
                .taskCandidateUser(userId)//指定历史任务的办理人
                .finished()
                .list();

        for (HistoricTaskInstance t : historicTaskInstances) {
            HistoricProcessInstance ins = historyService // 历史任务Service，根据ProcessInstanceId获取历史流程实例
                    .createHistoricProcessInstanceQuery() // 创建历史流程实例查询
                    .processInstanceId(t.getProcessInstanceId()) // 指定流程实例ID
                    .singleResult();
            if (ins == null) {
                continue;
            }
            if (ins.getProcessDefinitionName().equals(typeName)) {
                newTasks.add(t);
            }
        }
        Integer pageTotalCount = null;
        Integer totalCount = null;
        if (newTasks != null) {
            totalCount = newTasks.size();//总记录数
            pageTotalCount = totalCount % size == 0 ? totalCount / size : totalCount / size + 1;//总页数
            if (starPage < 1) {
                starPage = 1;
            }
            if (starPage > pageTotalCount) {
                starPage = pageTotalCount;
            }
            Integer pageSize = totalCount - size * starPage;
            if (pageSize > size) {
                pageSize = size;
            }
            if (pageSize < 0) {
                pageSize = totalCount;
            }
            int a = (starPage - 1) * size;
            int b = (starPage - 1) * size + pageSize;
            for (int i = a; i < b; i++) {
                HistoricTaskInstance t = newTasks.get(i);
                HistoricProcessInstance ins = historyService // 历史任务Service，根据ProcessInstanceId获取历史流程实例
                        .createHistoricProcessInstanceQuery() // 创建历史流程实例查询
                        .processInstanceId(t.getProcessInstanceId()) // 指定流程实例ID
                        .singleResult();
                if (ins == null) {
                    continue;
                }
                //返回的数据接受
                Map<String, Object> m1 = new HashMap<>();
                m1.put("taskId", t.getId());
                m1.put("ProcessInstanceId", t.getProcessInstanceId());
                m1.put("data", contractCreatedService.getFrameContractApplyById(ins.getBusinessKey()).getData());
                m1.put("endTime", t.getEndTime());
                WaitLists.add(m1);
            }
        }
        map.put("totalSize", totalCount);
        map.put("totalPages", pageTotalCount);
        map.put("data", WaitLists);
        return map;
    }

    @Override
    public Map<String, Object> getMywActivitisByTypeByPu(String userId, String typeName, Integer starPage, Integer size) throws Exception {
        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> WaitLists = new ArrayList<>();
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
            if (ins.getProcessDefinitionName().equals(typeName)) {
                newTasks.add(t);
            }
        }
        Integer pageTotalCount = null;
        Integer totalCount = null;
        if (newTasks != null) {
            totalCount = newTasks.size();//总记录数
            pageTotalCount = totalCount % size == 0 ? totalCount / size : totalCount / size + 1;//总页数
            if (starPage < 1) {
                starPage = 1;
            }
            if (starPage > pageTotalCount) {
                starPage = pageTotalCount;
            }
            Integer pageSize = totalCount - size * starPage;
            if (pageSize > size) {
                pageSize = size;
            }
            if (pageSize < 0) {
                pageSize = totalCount;
            }
            int a = (starPage - 1) * size;
            int b = (starPage - 1) * size + pageSize;
            for (int i = a; i < b; i++) {
                Task t = newTasks.get(i);
                ProcessInstance ins = runtimeService.createProcessInstanceQuery()//根据ProcessInstanceId获取当前运行的流程实例
                        .processInstanceId(t.getProcessInstanceId())
                        .singleResult();
                if (ins == null) {
                    continue;
                }
                //返回的数据接受
                Map<String, Object> m1 = new HashMap<>();
                m1.put("taskId", t.getId());
                m1.put("ProcessInstanceId", t.getProcessInstanceId());
                m1.put("data", contractCreatedService.getOrdinaryContractApplyById(ins.getBusinessKey()).getData());
                WaitLists.add(m1);
            }
        }
        map.put("totalSize", totalCount);
        map.put("totalPages", pageTotalCount);
        map.put("data", WaitLists);
        return map;
    }

    @Override
    public Map<String, Object> getMyyActivitisByTypeByPu(String userId, String typeName, Integer starPage, Integer size) throws Exception {
        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> WaitLists = new ArrayList<>();
        List<HistoricTaskInstance> newTasks = new ArrayList<>();
        List<HistoricTaskInstance> historicTaskInstances = historyService//与历史数据（历史表）相关的Service
                .createHistoricTaskInstanceQuery()//创建历史任务实例查询
                .taskCandidateUser(userId)//指定历史任务的办理人
                .finished()
                .list();

        for (HistoricTaskInstance t : historicTaskInstances) {
            HistoricProcessInstance ins = historyService // 历史任务Service，根据ProcessInstanceId获取历史流程实例
                    .createHistoricProcessInstanceQuery() // 创建历史流程实例查询
                    .processInstanceId(t.getProcessInstanceId()) // 指定流程实例ID
                    .singleResult();
            if (ins == null) {
                continue;
            }
            if (ins.getProcessDefinitionName().equals(typeName)) {
                newTasks.add(t);
            }
        }
        Integer pageTotalCount = null;
        Integer totalCount = null;
        if (newTasks != null) {
            totalCount = newTasks.size();//总记录数
            pageTotalCount = totalCount % size == 0 ? totalCount / size : totalCount / size + 1;//总页数
            if (starPage < 1) {
                starPage = 1;
            }
            if (starPage > pageTotalCount) {
                starPage = pageTotalCount;
            }
            Integer pageSize = totalCount - size * starPage;
            if (pageSize > size) {
                pageSize = size;
            }
            if (pageSize < 0) {
                pageSize = totalCount;
            }
            int a = (starPage - 1) * size;
            int b = (starPage - 1) * size + pageSize;
            for (int i = a; i < b; i++) {
                HistoricTaskInstance t = newTasks.get(i);
                HistoricProcessInstance ins = historyService // 历史任务Service，根据ProcessInstanceId获取历史流程实例
                        .createHistoricProcessInstanceQuery() // 创建历史流程实例查询
                        .processInstanceId(t.getProcessInstanceId()) // 指定流程实例ID
                        .singleResult();
                if (ins == null) {
                    continue;
                }
                //返回的数据接受
                Map<String, Object> m1 = new HashMap<>();
                m1.put("taskId", t.getId());
                m1.put("ProcessInstanceId", t.getProcessInstanceId());
                m1.put("data", contractCreatedService.getOrdinaryContractApplyById(ins.getBusinessKey()).getData());
                m1.put("endTime", t.getEndTime());
                WaitLists.add(m1);
            }
        }
        map.put("totalSize", totalCount);
        map.put("totalPages", pageTotalCount);
        map.put("data", WaitLists);
        return map;
    }

    @Override
    public Map<String, Object> getApprovalRecord(String processInstanceId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();
        List<HistoricTaskInstance> newTasks = new ArrayList<>();
        List<HistoricTaskInstance> historicTaskInstances = historyService//与历史数据（历史表）相关的Service
                .createHistoricTaskInstanceQuery()//创建历史任务实例查
                .processInstanceId(processInstanceId)
                .list();
        DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//需要转化成的时间格式
        df1.setTimeZone(TimeZone.getTimeZone("GMT"));
        for (HistoricTaskInstance h : historicTaskInstances) {
            Map<String, Object> m1 = new HashMap<>();
            m1.put("taskName", h.getName());
            if (h.getEndTime() != null) {
                String endTime = formatTimeEight(df1.format(h.getEndTime()));
                m1.put("endTime", endTime);
            } else {
                m1.put("endTime", "待执行");
            }
            String userId = null;
            List<HistoricIdentityLink> ls = historyService.getHistoricIdentityLinksForTask(h.getId());
            for (HistoricIdentityLink i : ls) {
                userId = i.getUserId();
            }
            m1.put("userId", userId);
            list.add(m1);
        }
        map.put("data", list);
        return map;
    }


    @Override
    public Map<String, Object> getMyTodolistByTypeAndTime(String userId, String detailCode, Integer starPage, Integer size) throws Exception {
        Map<String, Object> map = new HashMap<>();
        List<WaitList> WaitLists = new ArrayList<>();
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
            if (detailCode != null) {
                if (ins.getProcessDefinitionName().equals(detailCode)) {
                    newTasks.add(t);
                }
            } else {
                newTasks.add(t);
            }
        }
        Integer pageTotalCount = null;
        Integer totalCount = null;
        if (newTasks != null) {
            totalCount = newTasks.size();//总记录数
            pageTotalCount = totalCount % size == 0 ? totalCount / size : totalCount / size + 1;//总页数
            if (starPage < 1) {
                starPage = 1;
            }
            if (starPage > pageTotalCount) {
                starPage = pageTotalCount;
            }
            Integer pageSize = totalCount - size * starPage;
            if (pageSize > size) {
                pageSize = size;
            }
            if (pageSize < 0) {
                pageSize = totalCount;
            }
            int a = (starPage - 1) * size;
            int b = (starPage - 1) * size + pageSize;
            for (int i = a; i < b; i++) {
                Task t = newTasks.get(i);
                ProcessInstance ins = runtimeService.createProcessInstanceQuery()//根据ProcessInstanceId获取当前运行的流程实例
                        .processInstanceId(t.getProcessInstanceId())
                        .singleResult();
                if (ins == null) {
                    continue;
                }
                WaitList w = new WaitList();
                w.setWaitId(ins.getBusinessKey());
                w.setCloseCondition("流程完结或撤销");
                if (ins.getProcessDefinitionName().equals("ver00000120000001")) {
                    w.setWaitType("普通检定");//使用流程图的名字作为待办的类型名称
                }
                if (ins.getProcessDefinitionName().equals("ver00000120000002")) {
                    w.setWaitType("框架检定");//使用流程图的名字作为待办的类型名称
                }
                if (ins.getProcessDefinitionName().equals("ver00000120000003")) {
                    w.setWaitType("合同");//使用流程图的名字作为待办的类型名称
                }
                if (ins.getProcessDefinitionName().equals("ver00000120000005")) {
                    w.setWaitType("证书");//使用流程图的名字作为待办的类型名称
                }
                if (ins.getProcessDefinitionName().equals("ver00000120000006")) {
                    w.setWaitType("周计划");//使用流程图的名字作为待办的类型名称
                }
                if (ins.getProcessDefinitionName().equals("ver00000120000007")) {
                    w.setWaitType("临时计划");//使用流程图的名字作为待办的类型名称
                }
                w.setMessage((String) taskService.getVariable(t.getId(), "opinions"));
                /*w.setReqEndTime((String)taskService.getVariable(t.getId(),"reqEndTime"));*/
                w.setDetailsUrl(t.getFormKey());
                w.setWaitName(t.getName());
                w.setTaskId(t.getId());
                w.setProcessInstanceId(t.getProcessInstanceId());
                DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//需要转化成的时间格式
                df1.setTimeZone(TimeZone.getTimeZone("GMT"));
                String startTime = null;
                if (ins.getStartTime() != null) {
                    startTime = formatTimeEight(df1.format(ins.getStartTime()));
                }
                w.setStartTime(startTime);
                WaitLists.add(w);
            }
        }
        map.put("totalSize", totalCount);
        map.put("totalPages", pageTotalCount);
        map.put("data", WaitLists);
        return map;
    }

    @Override
    public Map<String, Object> getMyyilistByTypeAndTime(String userId, String detailCode, Integer starPage, Integer size) throws Exception {
        Map<String, Object> map = new HashMap<>();
        List<EndList> WaitLists = new ArrayList<>();
        List<HistoricTaskInstance> newTasks = new ArrayList<>();
        List<HistoricTaskInstance> historicTaskInstances = historyService//与历史数据（历史表）相关的Service
                .createHistoricTaskInstanceQuery()//创建历史任务实例查询
                .taskCandidateUser(userId)//指定历史任务的办理人
                .finished()
                .list();

        for (HistoricTaskInstance t : historicTaskInstances) {
            HistoricProcessInstance ins = historyService // 历史任务Service，根据ProcessInstanceId获取历史流程实例
                    .createHistoricProcessInstanceQuery() // 创建历史流程实例查询
                    .processInstanceId(t.getProcessInstanceId()) // 指定流程实例ID
                    .singleResult();
            if (ins == null) {
                continue;
            }
            if (ins.getProcessDefinitionName().equals(detailCode)) {
                newTasks.add(t);
            }
        }
        Integer pageTotalCount = null;
        Integer totalCount = null;
        if (newTasks != null) {
            totalCount = newTasks.size();//总记录数
            pageTotalCount = totalCount % size == 0 ? totalCount / size : totalCount / size + 1;//总页数
            if (starPage < 1) {
                starPage = 1;
            }
            if (starPage > pageTotalCount) {
                starPage = pageTotalCount;
            }
            Integer pageSize = totalCount - size * starPage;
            if (pageSize > size) {
                pageSize = size;
            }
            if (pageSize < 0) {
                pageSize = totalCount;
            }
            int a = (starPage - 1) * size;
            int b = (starPage - 1) * size + pageSize;
            for (int i = a; i < b; i++) {
                HistoricTaskInstance t = newTasks.get(i);
                HistoricProcessInstance ins = historyService // 历史任务Service，根据ProcessInstanceId获取历史流程实例
                        .createHistoricProcessInstanceQuery() // 创建历史流程实例查询
                        .processInstanceId(t.getProcessInstanceId()) // 指定流程实例ID
                        .singleResult();
                if (ins == null) {
                    continue;
                }
                EndList endList = new EndList();
                endList.setEndId(ins.getBusinessKey());
                if (ins.getProcessDefinitionName().equals("ver00000120000001")) {
                    endList.setWaitType("普通检定");//使用流程图的名字作为待办的类型名称
                }
                if (ins.getProcessDefinitionName().equals("ver00000120000002")) {
                    endList.setWaitType("框架检定");//使用流程图的名字作为待办的类型名称
                }
                if (ins.getProcessDefinitionName().equals("ver00000120000003")) {
                    endList.setWaitType("合同");//使用流程图的名字作为待办的类型名称
                }
                if (ins.getProcessDefinitionName().equals("ver00000120000005")) {
                    endList.setWaitType("证书");//使用流程图的名字作为待办的类型名称
                }
                if (ins.getProcessDefinitionName().equals("ver00000120000006")) {
                    endList.setWaitType("周计划");//使用流程图的名字作为待办的类型名称
                }
                if (ins.getProcessDefinitionName().equals("ver00000120000007")) {
                    endList.setWaitType("临时计划");//使用流程图的名字作为待办的类型名称
                }
                List<Comment> comments = taskService.getProcessInstanceComments(t.getProcessInstanceId());
                if (comments != null) {
                    for (Comment c : comments) {
                        if (c.getTaskId().equals(t.getId())) {
                            endList.setMessage(c.getFullMessage());
                        }
                    }
                }
                List<HistoricVariableInstance> lists = historyService.createHistoricVariableInstanceQuery().processInstanceId(t.getProcessInstanceId()).list();
                /*if (lists!=null){
                    for (HistoricVariableInstance h1:lists) {
                        if(h1.getVariableName().equals("reqEndTime")){
                            if (h1.getValue()!=null) {
                                endList.setReqEndTime(h1.getValue().toString());
                            }
                        }
                    }
                }*/
                endList.setProcessInstanceId(t.getProcessInstanceId());
                endList.setTaskId(t.getId());
                endList.setEndName(t.getName());
                DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//需要转化成的时间格式
                df1.setTimeZone(TimeZone.getTimeZone("GMT"));
                String endTime = null;
                String startTime = null;
                if (ins.getStartTime() != null) {
                    startTime = formatTimeEight(df1.format(ins.getStartTime()));
                }
                if (t.getEndTime() != null) {
                    endTime = formatTimeEight(df1.format(t.getEndTime()));
                }
                if (ins.getStartTime() != null) {
                    startTime = formatTimeEight(df1.format(ins.getStartTime()));
                }
                endList.setStartTime(startTime);
                endList.setEndTime(endTime);
                endList.setDetailsUrl(t.getFormKey());
                WaitLists.add(endList);
            }
        }
        map.put("totalSize", totalCount);
        map.put("totalPages", pageTotalCount);
        map.put("data", WaitLists);
        return map;
    }


    /*@Override
    public void getActivitiProccessImage(String pProcessInstanceId, HttpServletResponse response)  {
        // 设置页面不缓存
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        try {
            //  获取历史流程实例
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(pProcessInstanceId).singleResult();

            if (historicProcessInstance == null) {
                throw new Exception();
            } else {

                // 获取流程历史中已执行节点，并按照节点在流程中执行先后顺序排序
                List<HistoricActivityInstance> historicActivityInstanceList = historyService.createHistoricActivityInstanceQuery()
                        .processInstanceId(pProcessInstanceId).orderByHistoricActivityInstanceId().asc().list();

                // 已执行的节点ID集合
                List<String> executedActivityIdList = new ArrayList<String>();
                @SuppressWarnings("unused")
                int index = 1;
                for (HistoricActivityInstance activityInstance : historicActivityInstanceList) {
                    executedActivityIdList.add(activityInstance.getActivityId());
                    index++;
                }
                ProcessEngine defaultProcessEngine = ProcessEngines.getDefaultProcessEngine();

                ProcessEngineConfigurationImpl processEngineConfiguration = (ProcessEngineConfigurationImpl) defaultProcessEngine.getProcessEngineConfiguration();

                //获取默认图片生成器
                ProcessDiagramGenerator processDiagramGenerator = processEngineConfiguration.getProcessDiagramGenerator();
                //获取要生成的流程图的BpmnModel
                BpmnModel bpmnModel = defaultProcessEngine.getRepositoryService().getBpmnModel(historicProcessInstance.getProcessDefinitionId());

                InputStream inputStream = processDiagramGenerator.generateDiagram(bpmnModel, "PNG", executedActivityIdList);
                // 获取流程图图像字符流
                response.setContentType("image/png");
                OutputStream os = response.getOutputStream();
                int bytesRead = 0;
                byte[] buffer = new byte[8192];
                while ((bytesRead = inputStream.read(buffer, 0, 8192)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.close();
                inputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    @Override
    public Map<String, Object> getAllExeution(String email, Integer pageNo, Integer pageSize) throws Exception {
        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();
        int firstrow = (pageNo - 1) * pageSize;
        DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//需要转化成的时间格式
        df1.setTimeZone(TimeZone.getTimeZone("GMT"));
        List<ProcessInstance> processInstanceList = runtimeService.createProcessInstanceQuery().orderByProcessDefinitionId().desc()
                .involvedUser(email)
                .listPage(firstrow, pageSize);
        int total = (int) runtimeService.createProcessInstanceQuery().involvedUser(email).count();
        for (ProcessInstance p : processInstanceList) {
            Task take = taskService.createTaskQuery().processInstanceId(p.getProcessInstanceId()).singleResult();
            Map<String, Object> map1 = new HashMap<>();
            if (p.getProcessDefinitionName().equals("ver00000120000001")) {
                map1.put("name", "普通检定");
            }
            if (p.getProcessDefinitionName().equals("ver00000120000002")) {
                map1.put("name", "框架检定");
            }
            if (p.getProcessDefinitionName().equals("ver00000120000003")) {
                map1.put("name", "合同");
            }
            if (p.getProcessDefinitionName().equals("ver00000120000005")) {
                map1.put("name", "证书");
            }
            if (p.getProcessDefinitionName().equals("ver00000120000006")) {
                map1.put("name", "周计划");
            }
            if (p.getProcessDefinitionName().equals("ver00000120000007")) {
                map1.put("name", "临时计划");
            }
            String startTime = null;
            if (p.getStartTime() != null) {
                startTime = formatTimeEight(df1.format(p.getStartTime()));
            }
            map1.put("businesskey", p.getBusinessKey());
            map1.put("processInstanceId", p.getProcessInstanceId());
            map1.put("startTime", startTime);
            map1.put("now", take.getName());
            map1.put("state", "未完成");
            list.add(map1);
        }
        map.put("data", list);
        map.put("total", total);
        return map;
    }

    @Override
    public Map<String, Object> getHistory(String email, Integer pageNo, Integer pageSize) throws Exception {
        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();
        DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df1.setTimeZone(TimeZone.getTimeZone("GMT"));
        HistoricProcessInstanceQuery process = historyService.createHistoricProcessInstanceQuery()
                .startedBy(email).finished();
        int total = (int) process.count();
        int firstrow = (pageNo - 1) * pageSize;
        List<HistoricProcessInstance> info = process.listPage(firstrow, pageSize);
        for (HistoricProcessInstance p : info) {
            Map<String, Object> map1 = new HashMap<>();
            //类型
            if (p.getProcessDefinitionName().equals("ver00000120000001")) {
                map1.put("name", "普通检定");
            }
            if (p.getProcessDefinitionName().equals("ver00000120000002")) {
                map1.put("name", "框架检定");
            }
            if (p.getProcessDefinitionName().equals("ver00000120000003")) {
                map1.put("name", "合同");
            }
            if (p.getProcessDefinitionName().equals("ver00000120000005")) {
                map1.put("name", "证书");
            }
            if (p.getProcessDefinitionName().equals("ver00000120000006")) {
                map1.put("name", "周计划");
            }
            if (p.getProcessDefinitionName().equals("ver00000120000007")) {
                map1.put("name", "临时计划");
            }
            String startTime = null;
            if (p.getStartTime() != null) {
                startTime = formatTimeEight(df1.format(p.getStartTime()));
            }
            String endTime = null;
            if (p.getStartTime() != null) {
                endTime = formatTimeEight(df1.format(p.getEndTime()));
            }
            map1.put("businesskey", p.getBusinessKey());
            map1.put("processInstanceId", p.getId());
            map1.put("processDefinitionId", p.getProcessDefinitionId());
            map1.put("startTime", startTime);
            map1.put("endTime", endTime);
            map1.put("state", "已结束");
            list.add(map1);
        }
        map.put("data", list);
        map.put("total", total);
        return map;
    }

    /**
     * 历史申请流程记录
     */
    @Override
    public Map<String, Object> getHistoryRecord(String processInstanceId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();
        List<HistoricTaskInstance> newTasks = new ArrayList<>();
        List<HistoricTaskInstance> historicTaskInstances = historyService//与历史数据（历史表）相关的Service
                .createHistoricTaskInstanceQuery()//创建历史任务实例查
                .processInstanceId(processInstanceId)
                .list();
        DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//需要转化成的时间格式
        df1.setTimeZone(TimeZone.getTimeZone("GMT"));
        for (int i = 0; i < historicTaskInstances.size(); i++) {
            Map<String, Object> m1 = new HashMap<>();
            m1.put("taskName", historicTaskInstances.get(i).getName());
            if (historicTaskInstances.get(i).getEndTime() != null) {
                String endTime = formatTimeEight(df1.format(historicTaskInstances.get(i).getEndTime()));
                m1.put("endTime", endTime);
            }
            if (historicTaskInstances.get(i).getStartTime() != null) {
                String startTime = formatTimeEight(df1.format(historicTaskInstances.get(i).getStartTime()));
                m1.put("startTime", startTime);
            }
            String userId = null;
            //List<HistoricIdentityLink> ls = historyService.getHistoricIdentityLinksForProcessInstance(processInstanceId);
            List<HistoricIdentityLink> lss = historyService.getHistoricIdentityLinksForTask(historicTaskInstances.get(i).getId());
            for (HistoricIdentityLink h:lss) {
                if (h.getUserId()!=null){
                    userId = h.getUserId();
                }
            }
            if (userId != null) {
                User user = identityservice.createUserQuery().userId(userId).singleResult();
                if (user != null) {
                    m1.put("userName", user.getFirstName());
                }
            }
            m1.put("userId", userId);
            List<Comment> comments = taskService.getProcessInstanceComments(processInstanceId);
            if (comments != null) {
                for (Comment c : comments) {
                    if (c.getTaskId().equals(historicTaskInstances.get(i).getId())) {
                        m1.put("message", c.getFullMessage());
                    }
                }
            }
            List<HistoricVariableInstance> opinionsList = historyService.createHistoricVariableInstanceQuery() // 创建一个历史的流程变量查询对象
                          .variableName("opinions").list();
                     if (null != opinionsList && opinionsList.size() > 0) {
                             for (HistoricVariableInstance hvi : opinionsList) {
                                    if (processInstanceId.equals(hvi.getProcessInstanceId())){
                                        m1.put("opinions", hvi.getValue());
                                    }

                                }
                         }
            list.add(m1);
        }
        map.put("data", list);
        return map;
    }
}
