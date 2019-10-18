package com.richfit.jobticket.workApproval.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface ContractMapper {

    @Update("update app_htjc set con_state = #{state} where CON_ID = #{conId}")
    public int updateContract(@Param(value="conId")String conId, @Param(value="state")String state);
        }
