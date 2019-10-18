package com.richfit.jobticket.activiti.controller;

import com.richfit.jobticket.activiti.service.CertificateActivitiService;
import com.richfit.jobticket.activiti.service.ContractCirculationService;
import com.richfit.jobticket.activiti.service.WeekPlanActivitiService;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("activiti")
public class WeekPlanActivitiController {
    @Autowired
    private WeekPlanActivitiService weekPlanActivitiService;
    @Autowired
    ContractCirculationService contractCirculationService;
    @Autowired
    CertificateActivitiService certificateActivitiService;

    /**
     * 启动一个周计划流程
     * @param weekVerId
     * @return java.util.Map<java.lang.String,java.lang.Object>
     * @author: zxh
     */
    @RequestMapping(value="/startweekplan/{weekVerId}")
    public Map<String,Object> startWeekplan(@PathVariable("weekVerId") String weekVerId,@RequestParam("email")String email)throws Exception{
        Map<String,Object> variables=new HashMap<String, Object>();
        ProcessInstance ins=weekPlanActivitiService.startOneWeekActiviti(weekVerId,email,variables);
        Map<String,Object> map=new HashMap<String, Object>();
        map.put("magree","sucess");
        return map;
    }
    /**
     * @description: 查看我的待办
     * @date 2019/6/12
     * @param userId 用户id
     * @return java.util.Map<java.lang.String,java.lang.Object>
     * @author: zxh
     */
    @RequestMapping(value ="/myweilist/{userId}")
    public Map<String,Object> getMyTodolist(@PathVariable("userId")String userId,
                                            @RequestParam(name = "detailCode", required = false)String detailCode,@RequestParam(name = "starPage", required = false, defaultValue = "1") Integer starPage,
                                            @RequestParam(name = "size", required = false, defaultValue = "10")Integer size)throws Exception{

        if (detailCode==null){
            return weekPlanActivitiService.getMywActivitisByAssignee(userId,starPage,size);
        }
        if (detailCode.equals("ver00000120000006")){//周计划
            return weekPlanActivitiService.getMywActivitisByType(userId,detailCode,starPage,size);
        }
        if(detailCode.equals("ver00000120000001")){//普通申请
            return weekPlanActivitiService.getMywActivitisByTypeByPu(userId,detailCode,starPage,size);
        }
        if(detailCode.equals("ver00000120000002")){//框架申请
            return weekPlanActivitiService.getMywActivitisByTypeByKuang(userId,detailCode,starPage,size);
        }
        if(detailCode.equals("ver00000120000003")){//合同
            return contractCirculationService.getMywActivitisByType(userId,detailCode,starPage,size);
        }
        if (detailCode.equals("ver00000120000005")){//证书
            return certificateActivitiService.getMywActivitisByType(userId,detailCode,starPage,size);
        }
        Map<String,Object> map = new HashMap<>();
        map.put("marrage","获取失败!");
        return map;
    }
    /**
     * @description: 查看我的待办(加条件)
     * @date 2019/6/12
     * @param userId 用户id
     * @return java.util.Map<java.lang.String,java.lang.Object>
     * @author: zxh
     */
    @RequestMapping(value ="/myweilistby/{userId}")
    public Map<String,Object> getMyTodolistByTypeAndTime(@PathVariable("userId")String userId,
                                            @RequestParam(name = "detailCode", required = false)String detailCode,
                                            @RequestParam(name = "starPage", required = false, defaultValue = "1") Integer starPage,
                                            @RequestParam(name = "size", required = false, defaultValue = "10")Integer size)throws Exception{
        if(detailCode==null||detailCode.equals("")){
            return weekPlanActivitiService.getMywActivitisByAssignee(userId,starPage,size);
        }else{
            return weekPlanActivitiService.getMyTodolistByTypeAndTime(userId,detailCode,starPage,size);
        }
    }

    /**
     * @description: 查看我已办(加条件)
     * @date 2019/6/12
     * @param userId 用户id
     * @return java.util.Map<java.lang.String,java.lang.Object>
     * @author: zxh
     */
    @RequestMapping(value ="/myyilistby/{userId}")
    public Map<String,Object> getMyyilistByTypeAndTime(@PathVariable("userId")String userId,
                                                         @RequestParam(name = "detailCode", required = false)String detailCode,
                                                         @RequestParam(name = "starPage", required = false, defaultValue = "1") Integer starPage,
                                                         @RequestParam(name = "size", required = false, defaultValue = "10")Integer size)throws Exception{
        if(detailCode==null||detailCode.equals("")){
            return weekPlanActivitiService.getMyyActivitisByAssignee(userId,starPage,size);
        }else{
            return weekPlanActivitiService.getMyyilistByTypeAndTime(userId,detailCode,starPage,size);
        }
    }


    /**
     * @description: 查看我的已办
     * @date 2019/6/12
     * @param userId
     * @return java.util.Map<java.lang.String,java.lang.Object>
     * @author: zxh
     */
    @RequestMapping(value ="/myyilist/{userId}")
    public Map<String,Object> getMyDonelist(@PathVariable("userId")String userId,
                                            @RequestParam(name = "detailCode", required = false)String detailCode,
                                            @RequestParam(name = "starPage", required = false, defaultValue = "1") Integer starPage,
                                            @RequestParam(name = "size", required = false, defaultValue = "10")Integer size)throws Exception{

        if (detailCode==null||detailCode==""){
            return weekPlanActivitiService.getMyyActivitisByAssignee(userId,starPage,size);
        }
        if (detailCode.equals("ver00000120000006")){
            return weekPlanActivitiService.getMyyActivitisByType(userId,detailCode,starPage,size);
        }
        if(detailCode.equals("ver00000120000001")){//普通申请
            return weekPlanActivitiService.getMyyActivitisByTypeByPu(userId,detailCode,starPage,size);
        }
        if(detailCode.equals("ver00000120000002")){//框架申请
            return weekPlanActivitiService.getMyyActivitisByTypeByKuang(userId,detailCode,starPage,size);
        }
        if(detailCode.equals("ver00000120000003")){//合同
            return contractCirculationService.getMyyActivitisByType(userId,detailCode,starPage,size);
        }
        if (detailCode.equals("ver00000120000005")){//证书
            return certificateActivitiService.getMyyActivitisByType(userId,detailCode,starPage,size);
        }
        Map<String,Object> map = new HashMap<>();
        map.put("marrage","获取失败!");
        return map;
    }

    /**
     * 周计划审批任务
     * @return java.util.Map<java.lang.String,java.lang.Object>
     * @author: zxh
     */
    @RequestMapping(value="/commitactiviti/{workId}")
    public Map<String,Object> commitOneActiviti(@PathVariable("workId")String workId,
                                                @RequestParam("route") String route,
                                                @RequestParam("opinions")String opinions)throws Exception{
        return weekPlanActivitiService.commitOneActiviti(workId,route,opinions);
    }
    /**
     *最后一个审批
     * @return java.util.Map<java.lang.String,java.lang.Object>
     * @author: zxh
     */
    @RequestMapping(value="/overLastOneActiviti/{taskId}")
    public Map<String,Object> jieshouOneActivitiToo(@PathVariable("taskId")String taskId)throws Exception{
        return weekPlanActivitiService.overLastOneActiviti(taskId);
    }


    /**
     *合同最后一个审批
     * @return java.util.Map<java.lang.String,java.lang.Object>
     * @author: zxh
     */
    @RequestMapping(value="/contractLastOneActiviti/{taskId}")
    public Map<String,Object> ContractOneActivitiToo(@PathVariable("taskId")String taskId)throws Exception{
        return new HashMap<>();
    }
    /**
     * 启动一个合同审核流程
     * @param conId
     * @return java.util.Map<java.lang.String,java.lang.Object>
     * @author: zxh
     */
    @RequestMapping(value="/startContract/{conId}")
    public Map<String,Object> startContract(@PathVariable("conId") String conId,@RequestParam("email")String email,@RequestParam("userId")String userId)throws Exception{
        Map<String,Object> variables=new HashMap<String, Object>();
        ProcessInstance ins=weekPlanActivitiService.startOneContractActiviti(conId,email,userId,variables);
        Map<String,Object> map=new HashMap<String, Object>();
        map.put("magree","sucess");
        return map;
    }


    /**
     * 合同审批任务
     * @return java.util.Map<java.lang.String,java.lang.Object>
     * @author: zxh
     */
    @RequestMapping(value="/commitcontractactiviti/{taskId}")
    public Map<String,Object> commitOneActivitiContract(@PathVariable("taskId")String taskId,
                                                @RequestParam("userId") String userId,
                                                @RequestParam("route") String route,
                                                @RequestParam("opinions")String opinions)throws Exception{
        return contractCirculationService.CommitOneActiviti(taskId,route,opinions);
    }
    /**
     * 证书核验，抽查，签批，生成与整改
     * @return java.util.Map<java.lang.String,java.lang.Object>
     * @author: zxh
     */
    @RequestMapping(value="/commitcertificatetiviti/{taskId}")
    public Map<String,Object> commitOneActivitiCertificate(@PathVariable("taskId")String taskId,
                                                        @RequestParam("userId") String userId,
                                                        @RequestParam("route") String route,
                                                        @RequestParam("opinions")String opinions)throws Exception{
        return null;
    }
    /**
     * 证书打印
     * @return java.util.Map<java.lang.String,java.lang.Object>
     * @author: zxh
     */
    @RequestMapping(value="/commitcertificatetivitione/{taskId}")
    public Map<String,Object> commitOneActivitiCertificate1(@PathVariable("taskId")String taskId,
                                                           @RequestParam("userId") String userId)throws Exception{
        return weekPlanActivitiService.overLasttoOneActiviti(taskId,userId);
    }
    /**
     * 证书发放
     * @return java.util.Map<java.lang.String,java.lang.Object>
     * @author: zxh
     */
    @RequestMapping(value="/commitcertificatetivititwo/{taskId}")
    public Map<String,Object> commitOneActivitiCertificate2(@PathVariable("taskId")String taskId)throws Exception{
        return weekPlanActivitiService.overLastOneActiviti(taskId);
    }



    /**
     * 获取审批记录
     * @return java.util.Map<java.lang.String,java.lang.Object>
     * @author: zxh
     */
    @RequestMapping(value="/getApprovalRecord/{processInstanceId}")
    public Map<String,Object> getApprovalRecord(@PathVariable("processInstanceId") String processInstanceId)throws Exception{
        return weekPlanActivitiService.getApprovalRecord(processInstanceId);
    }


    /**
     * 启动一个证书审核流程
     * @param workId
     * @return java.util.Map<java.lang.String,java.lang.Object>
     * @author: zxh
     */
    @RequestMapping(value="/startCertificate/{workId}")
    public Map<String,Object> startCertificate(@PathVariable("workId") String workId,@RequestParam("userId") String userId)throws Exception{
        Map<String,Object> variables=new HashMap<String, Object>();
        variables.put("users",userId);//指定处理该事务的人,多个人用,分隔
        ProcessInstance ins=certificateActivitiService.startOneCertificateActiviti(workId,userId,"","");
        Map<String,Object> map=new HashMap<String, Object>();
        map.put("magree","sucess");
        return map;
    }

    @RequestMapping(value="/getHeyan/{userId}")
    public Map<String,Object> getHeyan(@PathVariable("userId")String userId,
                                       @RequestParam(name = "detailCode", required = false)String detailCode,@RequestParam(name = "starPage", required = false, defaultValue = "1") Integer starPage,
                                       @RequestParam(name = "size", required = false, defaultValue = "10")Integer size){
        return certificateActivitiService.getHeyan(userId,detailCode,starPage,size);
    }

    /**
     * 获取我所有的申请
     * @param starPage
     * @param size
     * @param userId
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/myapply/{userId}")
    public Map<String,Object> getAllExeution(@RequestParam(name = "starPage", required = false, defaultValue = "1") Integer starPage,
                                                       @RequestParam(name = "size", required = false, defaultValue = "10") Integer size,
                                                       @PathVariable("userId")String userId)throws Exception{
        return weekPlanActivitiService.getAllExeution(userId,starPage,size);
    }

    /**
     * 获取我的历史申请
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/record/myapply/{userId}")
    public Map<String,Object> getRecordExeution(@RequestParam(name = "starPage", required = false, defaultValue = "1") Integer starPage,
                                             @RequestParam(name = "size", required = false, defaultValue = "10") Integer size,
                                             @PathVariable("userId")String userId)throws Exception{
        return weekPlanActivitiService.getHistory(userId,starPage,size);
    }

    /**
     * 获取历史申请的审批记录
     * @param processInstanceId
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/myapply/record/{processInstanceId}")
    public Map<String,Object> getHistoryRecord(@PathVariable("processInstanceId")String processInstanceId)throws Exception{
        return weekPlanActivitiService.getHistoryRecord(processInstanceId);
    }
}
