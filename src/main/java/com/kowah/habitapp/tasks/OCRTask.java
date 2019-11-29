package com.kowah.habitapp.tasks;

import com.kowah.habitapp.bean.Note;
import com.kowah.habitapp.dbmapper.NoteMapper;
import com.kowah.habitapp.utils.OCRUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Component
@Configurable
@EnableScheduling
public class OCRTask {
    @Autowired
    private NoteMapper noteMapper;

    @Scheduled(cron = "0 10 * * * ?") // 每小时10分调度
    public void OCR() {
        OCRUtil ocrUtil = new OCRUtil();
        String token = ocrUtil.updateToken();
        List<Note> notes = noteMapper.selectPicByTime((int) (System.currentTimeMillis() / 1000));
        for (Note node : notes.toArray(new Note[0])) {
            String picPath = "http://www.shunlushunlu.cn/habit/user/pic?picName=" + node.getContent();
            String result = ocrUtil.processOCR(picPath, "24.cafe131ad76121d3915abfa296ef0518.2592000.1577467964.282335-17874287");
            if (result != null) {
                node.setPicText(result.length() > 512 ? result.substring(0, 512) : result);
                noteMapper.updateByPrimaryKeySelective(node);
            }
        }

    }
}
