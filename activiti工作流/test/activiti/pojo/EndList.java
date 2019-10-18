package com.richfit.jobticket.activiti.pojo;

public class EndList {

    //已办id
    private String endId;
    //已办名称
    private String endName;
    //待办类型
    private String waitType;
    //申请时间
    private String startTime;
    //要求处理时间
    private String reqEndTime;
    //完成时间
    private String endTime;
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


    public String getReqEndTime() {
        return reqEndTime;
    }

    public void setReqEndTime(String reqEndTime) {
        this.reqEndTime = reqEndTime;
    }

    public String getWaitType() {
        return waitType;
    }

    public void setWaitType(String waitType) {
        this.waitType = waitType;
    }

    public String getEndId() {
        return endId;
    }

    public void setEndId(String endId) {
        this.endId = endId;
    }

    public String getEndName() {
        return endName;
    }

    public void setEndName(String endName) {
        this.endName = endName;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
