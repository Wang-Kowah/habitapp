package com.kowah.habitapp.dbmapper;

import com.kowah.habitapp.bean.User;

import java.util.List;

public interface UserMapper {
    int deleteByPrimaryKey(Integer uid);

    int insert(User record);

    int insertAndGetUid(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer uid);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    User selectByMobile(String mobile);

    List<Integer> getUidList();

    int getTotalUserNum(Integer lastWeekEnd);
}