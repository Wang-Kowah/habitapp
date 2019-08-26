package com.kowah.habitapp.dbmapper;

import com.kowah.habitapp.bean.Note;
import com.kowah.habitapp.bean.vo.UserStatisticVo;

import java.util.List;
import java.util.Map;

public interface NoteMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Note record);

    int insertSelective(Note record);

    Note selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Note record);

    int updateByPrimaryKey(Note record);

    List<Note> selectByUidAndType(Map<String, Object> params);

    List<String> selectByUidAndTypeAndTime(Map<String, Object> params);

    List<String> selectByUidAndTime(Map<String, Object> params);

    // 指定了collate utf8mb4_unicode_ci，查询不区分key的大小写
    List<Note> searchByUidAndTypeAndKey(Map<String, Object> params);

    List<UserStatisticVo> getActiveUserNum(Integer lastWeekStart, Integer lastWeekEnd);

    List<UserStatisticVo> getSentPicUserNum(Integer lastWeekStart, Integer lastWeekEnd);

    List<UserStatisticVo> getLocationUserNum(Integer lastWeekStart, Integer lastWeekEnd);
}