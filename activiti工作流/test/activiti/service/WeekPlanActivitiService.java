package com.richfit.jobticket.activiti.service;

import com.richfit.jobticket.platformBaseMaintain.pojo.WeeklyVerificationPlan;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author zxh
 * @version V1.0
 * @Classname WeekPlanActivitiService
 * @Description:
 * @date 2019/6/10
 */
public interface WeekPlanActivitiService {

    /**
     * 开启一个周检定计划流程
     * @author zxh
     * @param weekVerId 主周计划实例id
     * @param variables
     */
    ProcessInstance startOneWeekActiviti(String weekVerId,String email, Map<String, Object> variables)throws Exception;
    /**
     * 开启一个年检定计划流程
     * @author zxh
     * @param annPlanId 主年计划实例id
     * @param variables
     *//*
    ProcessInstance startOneYearActiviti(String annPlanId,String userId, Map<String, Object> variables)throws Exception;*/
   /* *//**
     * 开启一个用户审核计划流程
     * @author zxh
     * @param custId 用户保存信息实例id
     * @param variables
     */
    ProcessInstance startOneUserActiviti(String custId,String email,String userId, Map<String, Object> variables)throws Exception;

    /**
     * 开启一个合同内部流转流程
     * @author zxh
     * @param conId 合同主键
     * @param variables
     */
    ProcessInstance startOneContractActiviti(String conId,String email,String userId, Map<String, Object> variables)throws Exception;

    /**
     * 完成一个任务
     * @author zxh
     */
     Map<String,Object> commitOneActiviti(String workId,  String route, String opinions)throws Exception;
    /**
     * 查询我的待办
     * @author zxh
     * @param role
     */
    Map<String,Object> getMywActivitisByAssignee(String role,Integer starPage, Integer size)throws Exception;
    /**
     * 查询我的已办
     * @author zxh
     * @param role
     */
    Map<String,Object> getMyyActivitisByAssignee(String role,Integer starPage, Integer size)throws Exception;

    /**
     * 完成一个不需要指定代办人，无需判断的任务
     * @author zxh
     */
    Map<String,Object> overLastOneActiviti(String workId)throws Exception;
    /**
     * 完成一个需要指定代办人，无需判断的任务
     * @author zxh
     */
    Map<String,Object> overLasttoOneActiviti(String workId,String userId)throws Exception;

    /**
     * 根据类型查询我的待办
     * @author zxh
     */
    Map<String,Object> getMywActivitisByType(String userId,String typeName,Integer starPage, Integer size)throws Exception;

    /**
     * 根据类型查询我的已办
     * @author zxh
     */
    Map<String,Object> getMyyActivitisByType(String userId,String typeName,Integer starPage, Integer size)throws Exception;

    /**
     * 根据类型查询我的框架申请待办
     * @author zxh
     */
    Map<String,Object> getMywActivitisByTypeByKuang(String userId,String typeName,Integer starPage, Integer size)throws Exception;

    /**
     * 根据类型查询我的已办
     * @author zxh
     */
    Map<String,Object> getMyyActivitisByTypeByKuang(String userId,String typeName,Integer starPage, Integer size)throws Exception;

    /**
     * 根据类型查询我的框架申请待办
     * @author zxh
     */
    Map<String,Object> getMywActivitisByTypeByPu(String userId,String typeName,Integer starPage, Integer size)throws Exception;

    /**
     * 根据类型查询我的已办
     * @author zxh
     */
    Map<String,Object> getMyyActivitisByTypeByPu(String userId,String typeName,Integer starPage, Integer size)throws Exception;

    /**
     * 获取审批记录
     */
    Map<String,Object> getApprovalRecord(String processInstanceId)throws Exception;

    /**
     * 个人待办检索
     */
    Map<String,Object> getMyTodolistByTypeAndTime(String userId,String detailCode,Integer starPage, Integer size)throws Exception ;

    /**
     * 个人已办办检索
     */
    Map<String,Object> getMyyilistByTypeAndTime(String userId,String detailCode,Integer starPage, Integer size)throws Exception ;

    /**
     * 审核过程
     *//*
    public void getActivitiProccessImage(String pProcessInstanceId, HttpServletResponse response);
*/
    /**
     * 个人申请流程跟踪
     * @param email
     * @param pageNo
     * @param pageSize
     * @return
     * @throws Exception
     */
    public Map<String,Object> getAllExeution(String email,Integer pageNo,Integer pageSize)throws  Exception;

    /**
     * 个人申请历史
     * @param email
     * @param pageNo
     * @param pageSize
     * @throws Exception
     */
    public Map<String,Object> getHistory(String email,Integer pageNo,Integer pageSize)throws  Exception;

    /**
     * 个人申请历史--详情
     * @param processInstanceId
     * @return
     * @throws Exception
     */
    public Map<String, Object> getHistoryRecord(String processInstanceId) throws Exception;
}
