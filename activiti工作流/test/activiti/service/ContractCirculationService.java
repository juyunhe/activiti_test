package com.richfit.jobticket.activiti.service;

import org.activiti.engine.runtime.ProcessInstance;

import java.util.Map;

public interface ContractCirculationService {

    /**
     * 开启一个合同内部流转流程
     * @author zxh
     * @param conId 合同主键
     * @param variables
     */
    ProcessInstance startOneContractSettlement(String conId, String email, Map<String, Object> variables)throws Exception;


    /**
     * 合同审批
     * @author zxh
     */
    Map<String,Object> CommitOneActiviti(String taskId,  String route, String opinions)throws Exception;

    /**
     * 根据类型查询我的合同待办
     * @author zxh
     */
    Map<String,Object> getMywActivitisByType(String userId,String detailCode,Integer starPage, Integer size)throws Exception;

    /**
     * 根据类型查询我的合同已办
     * @author zxh
     */
    Map<String,Object> getMyyActivitisByType(String userId,String detailCode,Integer starPage, Integer size)throws Exception;

    /**
     * 完成一个不需要指定代办人，无需判断的任务
     * @author zxh
     */
    Map<String,Object> overLastOneActivitiFirst(String workId)throws Exception;
    /**
     * 完成一个不需要指定代办人，无需判断的任务
     * @author zxh
     */
    Map<String,Object> overLastOneActivitiTwo(String workId)throws Exception;
}
