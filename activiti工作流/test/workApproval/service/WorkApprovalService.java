package com.richfit.jobticket.workApproval.service;

import com.richfit.jobticket.base.util.EntityResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

public interface WorkApprovalService {

    public int reportWorkInfoOne(String workId,String email,String userId);

    public int reportWorkInfoTwo(String workId,String email,String userId);

    public int approvalWorkById(String taskId, String workId,String route,String opinions)throws Exception;

    public Map<String,Object> getVerificationInformation(String appBaseId)throws Exception;
}
