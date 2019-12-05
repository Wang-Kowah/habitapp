package com.kowah.habitapp.tasks;

import com.kowah.habitapp.bean.Note;
import com.kowah.habitapp.dbmapper.NoteMapper;
import com.kowah.habitapp.utils.OCRUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Configurable
@EnableScheduling
public class OCRTask {
    @Autowired
    private NoteMapper noteMapper;

    @Scheduled(cron = "0 * * * * ?") // 每分钟识别一条，避免带宽占用过高
    public void OCR() {
        OCRUtil ocrUtil = new OCRUtil();
//        String token = ocrUtil.updateToken();
        List<Note> notes = noteMapper.selectUntaggedPicByTime(0);
        if (!notes.isEmpty()) {
            Note node = notes.get(0);
            String result = ocrUtil.processOCR(node.getContent(), "24.cafe131ad76121d3915abfa296ef0518.2592000.1577467964.282335-17874287");
            // catch Exception的情况 -> ∅
            // 不存在文字的情况为空字符串 -> ""
            // OCR错误的也暂存 if result.startsWith("error_msg:")
            node.setPicText(result == null ? "∅" : result.length() > 512 ? result.substring(0, 512) : result);
            noteMapper.updateByPrimaryKeySelective(node);
        }
    }
}
