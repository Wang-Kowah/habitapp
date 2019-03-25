package com.kowah.habitapp.dbmapper;

import com.kowah.habitapp.bean.PeriodKeyword;

import java.util.List;
import java.util.Map;

public interface PeriodKeywordMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(PeriodKeyword record);

    int insertSelective(PeriodKeyword record);

    PeriodKeyword selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(PeriodKeyword record);

    int updateByPrimaryKey(PeriodKeyword record);

    List<PeriodKeyword> selectByUidAndType(Map<String, Object> params);
}