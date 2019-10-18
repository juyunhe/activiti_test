package com.richfit.jobticket.activiti.service;

import org.activiti.engine.runtime.ProcessInstance;

import java.util.Map;

public interface CertificateActivitiService {

    /**
     * 开启一个证书流程
     * @author zxh
     * @param cerBaseId 证书主键
     * @param variables
     */
    ProcessInstance startOneCertificateActiviti(String cerBaseId, String userId,String userId1,String userId2)throws Exception;

    /**
     * 证书审批
     * @author zxh
     */
    Map<String,Object> CommitOneActiviti(String pid, String route,String opinions)throws Exception;

    /**
     * 证书审批(不需要route)//修改状态
     * @author zxh
     */
    Map<String,Object> CommitOneActivitiByUserId(String taskId, String userId, String opinions)throws Exception;
    /**
     * 证书审批(都不需要)
     * @author zxh
     */
    Map<String,Object> CommitOneActivitiByTaskId(String taskId)throws Exception;


    /**
     * 根据类型查询我的证书待办
     * @author zxh
     */
    Map<String,Object> getMywActivitisByType(String userId,String detailCode,Integer starPage, Integer size)throws Exception;

    /**
     * 根据类型查询我的证书已办
     * @author zxh
     */
    Map<String,Object> getMyyActivitisByType(String userId,String detailCode,Integer starPage, Integer size)throws Exception;


    /**
     * 根据待办类型，用户id，查询处在核验节点的证书信息
     * @author zxh
     */
    Map<String,Object> getHeyan(String userId,String detailCode, Integer starPage, Integer size);
    /**
     * 根据待办类型，用户id，查询处在抽查节点的证书信息
     * @author zxh
     */
    Map<String,Object> getChoucha(String userId,String detailCode, Integer starPage, Integer size);

    /**
     * 根据待办类型，用户id，查询处在签批节点的证书信息
     * @author zxh
     */
    Map<String,Object> getQianpi(String userId,String detailCode, Integer starPage, Integer size);
    /**
     * 根据待办类型，用户id，查询处在打印节点的证书信息
     * @author zxh
     */
    Map<String,Object> getDayin(String userId,String detailCode, Integer starPage, Integer size);
    /**
     * 根据待办类型，用户id，查询处在发放节点的证书信息，发放自动归档
     * @author zxh
     */
    Map<String,Object> getFafang(String userId,String detailCode, Integer starPage, Integer size);


}
