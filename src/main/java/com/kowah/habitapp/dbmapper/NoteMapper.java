package com.kowah.habitapp.dbmapper;

import com.kowah.habitapp.bean.Note;

import java.util.List;

public interface NoteMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Note record);

    int insertSelective(Note record);

    Note selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Note record);

    int updateByPrimaryKey(Note record);

    List<Note> selectByUid(Integer uid);
}