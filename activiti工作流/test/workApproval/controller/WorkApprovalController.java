package com.richfit.jobticket.workApproval.controller;

import com.richfit.jobticket.base.util.EntityResult;
import com.richfit.jobticket.workApproval.service.WorkApprovalService;
import org.omg.CORBA.OBJ_ADAPTER;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 工作审批
 */
@RestController
public class WorkApprovalController {

    @Resource(name = "workApprovalServiceImpl")
    private WorkApprovalService workApprovalService;

    /**
     * 普通检定申请
     * @param workId
     * @return//普通
     */
    @RequestMapping(value="/work/reportOne/{workId}", method= RequestMethod.POST)
    public EntityResult reportWorkInfoOne(@PathVariable("workId") String workId,@RequestParam("email") String email,
                                          @RequestParam("userId") String userId){
        try{
            return EntityResult.ok(workApprovalService.reportWorkInfoOne(workId,email,userId));
        }catch (Exception e){
            return EntityResult.build(202, e.getMessage());
        }
    }

    /**
     * 框架检定申请
     * @param workId
     * @return//
     */
    @RequestMapping(value="/work/reportTwo/{workId}", method= RequestMethod.POST)
    public EntityResult reportWorkInfoTwo(@PathVariable("workId") String workId,@RequestParam("email") String email,@RequestParam("userId") String userId){
        try{
            return EntityResult.ok(workApprovalService.reportWorkInfoTwo(workId,email,userId));
        }catch (Exception e){
            return EntityResult.build(202, e.getMessage());
        }
    }

    /**
     * 客户审批确认
     * @param taskId
     * @param workId
     * @param route
     * @return
     */
    @RequestMapping(value="/work/approval/{taskId}", method= RequestMethod.PUT)
    public EntityResult approvalWorkById(@PathVariable("taskId") String taskId,
                                         @RequestParam("workId") String workId,
                                         @RequestParam("route") String route,
                                         @RequestParam("opinions") String opinions)throws Exception{
        try{
            System.out.println(taskId+"##############");
            return EntityResult.ok(workApprovalService.approvalWorkById(taskId, workId,route,opinions));
        }catch (Exception e){
            return EntityResult.build(202, e.getMessage());
        }
    }

    @RequestMapping(value="/work/getVerificationInformation/{applyNumber}")
    public Map<String, Object> getVerificationInformation(@PathVariable("applyNumber")String applyNumber)throws Exception{
        return workApprovalService.getVerificationInformation(applyNumber);
    }
}
