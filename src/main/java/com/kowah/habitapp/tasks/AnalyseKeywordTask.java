package com.kowah.habitapp.tasks;

import com.kowah.habitapp.bean.DayKeyword;
import com.kowah.habitapp.bean.PeriodKeyword;
import com.kowah.habitapp.dbmapper.DayKeywordMapper;
import com.kowah.habitapp.dbmapper.NoteMapper;
import com.kowah.habitapp.dbmapper.PeriodKeywordMapper;
import com.kowah.habitapp.dbmapper.UserMapper;
import com.kowah.habitapp.utils.DateUtil;
import com.kowah.habitapp.utils.JiebaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

@Component
@Configurable
@EnableScheduling
public class AnalyseKeywordTask {

    @Autowired
    private DayKeywordMapper dayKeywordMapper;
    @Autowired
    private NoteMapper noteMapper;
    @Autowired
    private PeriodKeywordMapper periodKeywordMapper;
    @Autowired
    private UserMapper userMapper;


    @Scheduled(cron = "0 0 0 * * ?") // 每天00:00:00调度
    public void analyseKeyword() {
        try {
            long now = System.currentTimeMillis();
            boolean isMonday = DateUtil.isMonday(now);
            boolean isFirstDayOfMonth = DateUtil.isFirstDayOfMonth(now);
            List<Integer> uidList = userMapper.getUidList();

            for (int uid : uidList) {
                // 天关键词
                HashMap<String, Object> params = new HashMap<>();
                params.put("uid", uid);
                params.put("type", 0);
                params.put("start", DateUtil.getDayBeginTimestamp(now, 1) / 1000);
                params.put("end", DateUtil.getDayBeginTimestamp(now, 0) / 1000 - 1);
                List<String> noteList = noteMapper.selectByUidAndTypeAndTime(params);
                // 去除图片
                noteList.removeIf(s -> s.startsWith("_PIC:"));
                if (!noteList.isEmpty()) {
                    List<String> keywordList = JiebaUtil.getKeyword(String.join(".", noteList), 5);
                    DayKeyword dayKeyword = new DayKeyword();
                    dayKeyword.setUid(uid);
                    dayKeyword.setKeywords(String.join(",", keywordList));
                    dayKeyword.setDate(Integer.parseInt(DateUtil.formatDate(DateUtil.getDayBeginTimestamp(now, 1), "yyyyMMdd")));
                    dayKeywordMapper.insertSelective(dayKeyword);
                }

                // 周关键词
                if (isMonday) {
                    params.put("start", DateUtil.getMondayBeginTimestamp(now, 1) / 1000);
                    params.put("end", DateUtil.getMondayBeginTimestamp(now, 0) / 1000 - 1);
                    noteList = noteMapper.selectByUidAndTime(params);
                    // 去除图片
                    noteList.removeIf(s -> s.startsWith("_PIC:"));
                    if (!noteList.isEmpty()) {
                        List<String> keywordList = JiebaUtil.getKeyword(String.join(".", noteList), 10);
                        PeriodKeyword periodKeyword = new PeriodKeyword();
                        periodKeyword.setUid(uid);
                        periodKeyword.setType(1);
                        periodKeyword.setKeywords(String.join(",", keywordList));
                        periodKeyword.setDate(Integer.parseInt(DateUtil.formatDate(DateUtil.getMondayBeginTimestamp(now, 0) - 1, "yyyyMMdd")));
                        periodKeywordMapper.insertSelective(periodKeyword);
                    }
                }

                // 月关键词
                if (isFirstDayOfMonth) {
                    params.put("start", DateUtil.getMonthBeginTimestamp(now, 1) / 1000);
                    params.put("end", DateUtil.getMonthEndTimestamp(now, -1));
                    noteList = noteMapper.selectByUidAndTime(params);
                    // 去除图片
                    noteList.removeIf(s -> s.startsWith("_PIC:"));
                    if (!noteList.isEmpty()) {
                        List<String> keywordList = JiebaUtil.getKeyword(String.join(".", noteList), 15);
                        PeriodKeyword periodKeyword = new PeriodKeyword();
                        periodKeyword.setUid(uid);
                        periodKeyword.setType(2);
                        periodKeyword.setKeywords(String.join(",", keywordList));
                        periodKeyword.setDate(Integer.parseInt(DateUtil.formatDate(DateUtil.getMonthEndTimestamp(now, -1), "yyyyMMdd")));
                        periodKeywordMapper.insertSelective(periodKeyword);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
