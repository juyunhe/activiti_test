package com.richfit.jobticket.activiti.controller;

import com.richfit.jobticket.activiti.service.ActivitiGroupUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("group")
public class ActivitiGroupUserController {

    @Autowired
    ActivitiGroupUserService activitiGroupUserService;

    @PostMapping("/activiti/allfild")
    public Map<String,Object> getAllActivitiFile(
            @RequestParam(name = "starPage", required = false, defaultValue = "1") Integer starPage,
            @RequestParam(name = "size", required = false, defaultValue = "10")Integer size){
     return activitiGroupUserService.getAllActivitiFile(starPage,size);
    }

    @GetMapping("/activiti/allnode/{processDefinitionId}")
    public Map<String,Object> getAllActivitiFile(@PathVariable("processDefinitionId") String processDefinitionId){
        return activitiGroupUserService.getOneActivitiAllNode(processDefinitionId);
    }

    /**
     * 获取所有工作组
     * @param
     * @return
     */
    @GetMapping("/activiti/allgroup")
    Map<String,Object> getAllGroup(@RequestParam(name = "starPage", required = false, defaultValue = "1") Integer starPage,
                                   @RequestParam(name = "size", required = false, defaultValue = "10")Integer size){
        return  activitiGroupUserService.getAllGroup(starPage,size);
    }

    /**
     * 添加工作组
     * @param groupName
     * @return
     */
    @RequestMapping("/activiti/addgroup")
    Map<String,Object> addGroup(@RequestParam("groupName") String groupName){
        return activitiGroupUserService.addGroup(groupName);
    }

    /**
     * 删除工作组
     * @param groupId
     * @return
     */
    @RequestMapping("/activiti/delgroup")
    Map<String,Object> removeGroup(@RequestParam("groupId") String groupId){
        return activitiGroupUserService.removeGroup(groupId);
    }

    /**
     * 工作组添加用户
     * @param groupId
     * @return
     */
    @PostMapping("/activiti/group/adduser/{groupId}")
    Map<String,Object> groupAddUser(@PathVariable("groupId")String groupId, @RequestBody List<Map<Object,Object>> userData){
        if (userData!=null){
            System.out.println("1212");
        }
        return activitiGroupUserService.groupAddUser(groupId,userData);
    }

    /**
     * 工作组移除用户
     * @param groupId
     * @return
     */
    @PostMapping("/activiti/group/deluser/{groupId}")
    Map<String,Object> groupRemoveUser(@PathVariable("groupId")String groupId,@RequestBody List<Map<Object,Object>> delUsers){
        List<String> list = new ArrayList<>();
        for (Map<Object,Object> m :delUsers) {
            list.add((String)m.get("id"));
        }
        return activitiGroupUserService.groupRemoveUser(groupId,list);
    }
}
