package com.kowah.habitapp.dbmapper;

import com.kowah.habitapp.bean.DayKeyword;

import java.util.List;

public interface DayKeywordMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(DayKeyword record);

    int insertSelective(DayKeyword record);

    DayKeyword selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(DayKeyword record);

    int updateByPrimaryKey(DayKeyword record);

    List<DayKeyword> selectByUID(int uid);
}