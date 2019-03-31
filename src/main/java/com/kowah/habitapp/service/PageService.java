package com.kowah.habitapp.service;

import com.github.pagehelper.PageInfo;
import com.kowah.habitapp.bean.DayKeyword;
import com.kowah.habitapp.bean.Note;
import com.kowah.habitapp.bean.PeriodKeyword;
import org.springframework.stereotype.Service;

import java.util.Map;

public interface PageService {

    PageInfo<Note> getNoteList(Map<String, Object> params, int pageNum, int pageSize);
    PageInfo<DayKeyword> getDayKeywordList(int uid, int pageNum, int pageSize);
    PageInfo<PeriodKeyword> getKeywordList(Map<String, Object> params, int pageNum, int pageSize);

}
