package com.kowah.habitapp.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.kowah.habitapp.bean.DayKeyword;
import com.kowah.habitapp.bean.Note;
import com.kowah.habitapp.bean.PeriodKeyword;
import com.kowah.habitapp.dbmapper.DayKeywordMapper;
import com.kowah.habitapp.dbmapper.NoteMapper;
import com.kowah.habitapp.dbmapper.PeriodKeywordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class PageServiceImpl implements PageService {
    @Autowired
    private DayKeywordMapper dayKeywordMapper;
    @Autowired
    private NoteMapper noteMapper;
    @Autowired
    private PeriodKeywordMapper periodKeywordMapper;

    @Override
    public PageInfo<Note> getNoteList(Map<String, Object> params, int pageNum, int pageSize) {

        PageHelper.startPage(pageNum, pageSize);
        List<Note> notes = noteMapper.selectByUidAndType(params);
        //用PageInfo对结果进行包装
        return new PageInfo<>(notes);
    }

    @Override
    public PageInfo<DayKeyword> getDayKeywordList(int uid, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<DayKeyword> dayKeywords = dayKeywordMapper.selectByUID(uid);
        return new PageInfo<>(dayKeywords);
    }

    @Override
    public PageInfo<PeriodKeyword> getKeywordList(Map<String, Object> params, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<PeriodKeyword> periodKeywords = periodKeywordMapper.selectByUidAndType(params);
        return new PageInfo<>(periodKeywords);
    }

}
