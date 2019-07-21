package com.kowah.habitapp.service;

import com.github.pagehelper.PageInfo;
import com.kowah.habitapp.bean.DayKeyword;
import com.kowah.habitapp.bean.Note;
import com.kowah.habitapp.bean.PeriodKeyword;

import java.util.Map;

public interface PageService {

    PageInfo<Note> getNoteList(Map<String, Object> params, int pageNum, int pageSize);

    PageInfo<DayKeyword> getDayKeywordList(int uid, int pageNum, int pageSize);

    PageInfo<PeriodKeyword> getKeywordList(Map<String, Object> params, int pageNum, int pageSize);

    /**
     * 搜索历史便签/关键词
     * @param type 0:每日总结/1:每周总结/2:日关键词/3:周关键词/4:月关键词
     */
    PageInfo search(Map<String, Object> params, int pageNum, int pageSize, int type);
}
