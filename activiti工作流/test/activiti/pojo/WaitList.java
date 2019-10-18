package com.richfit.jobticket.activiti.pojo;

public class WaitList {
    //待办id
    private String waitId;
    //待办名称
    private String waitName;
    //待办类型
    private String waitType;
    //待办申请时间
    private String startTime;
    //要求处理时间
    private String reqEndTime;
    //关闭条件
    private String closeCondition;
    //任务id
    private String taskId;
    //功能连接
    private String detailsUrl;
    //批注信息
    private String message;
    //流程实例id
    private String ProcessInstanceId;


    public String getProcessInstanceId() {
        return ProcessInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        ProcessInstanceId = processInstanceId;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public String getDetailsUrl() {
        return detailsUrl;
    }

    public void setDetailsUrl(String detailsUrl) {
        this.detailsUrl = detailsUrl;
    }

    public String getWaitType() {
        return waitType;
    }

    public void setWaitType(String waitType) {
        this.waitType = waitType;
    }

    public String getReqEndTime() {
        return reqEndTime;
    }

    public void setReqEndTime(String reqEndTime) {
        this.reqEndTime = reqEndTime;
    }

    public String getCloseCondition() {
        return closeCondition;
    }

    public void setCloseCondition(String closeCondition) {
        this.closeCondition = closeCondition;
    }



    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getWaitId() {
        return waitId;
    }

    public void setWaitId(String waitId) {
        this.waitId = waitId;
    }

    public String getWaitName() {
        return waitName;
    }

    public void setWaitName(String waitName) {
        this.waitName = waitName;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

}
