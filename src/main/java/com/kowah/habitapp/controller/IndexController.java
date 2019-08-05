package com.kowah.habitapp.controller;

import com.kowah.habitapp.bean.enums.ErrorCode;
import com.kowah.habitapp.bean.vo.UserStatisticVo;
import com.kowah.habitapp.dbmapper.NoteMapper;
import com.kowah.habitapp.dbmapper.UserMapper;
import com.kowah.habitapp.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/")
public class IndexController {
    @Autowired
    private NoteMapper noteMapper;
    @Autowired
    private UserMapper userMapper;

    @RequestMapping("/")
    public String Index() {
        return "forward://protocol.html";
    }

    @ResponseBody
    @RequestMapping(value = "/statistic", method = RequestMethod.GET)
    public Map<String, Object> statistic() {
        Map<String, Object> result = new HashMap<>();
        ErrorCode errorCode = ErrorCode.SUCCESS;
        //截止上周日总用户数以及每个用户的发帖数、上周发过帖子的人数、上周发过图片的人数
        int lastWeekEnd = (int) (DateUtil.getMondayBeginTimestamp(System.currentTimeMillis(), 0) / 1000 - 1);
        int totalUser = userMapper.getTotalUserNum(lastWeekEnd);
        List<UserStatisticVo> userDataAllTime = noteMapper.getActiveUserNum(0, lastWeekEnd);
        List<UserStatisticVo> activeUserLastWeek = noteMapper.getActiveUserNum((int) (DateUtil.getMondayBeginTimestamp(System.currentTimeMillis(), 1) / 1000), lastWeekEnd);
        List<UserStatisticVo> sentPicUserLastWeek = noteMapper.getSentPicUserNum((int) (DateUtil.getMondayBeginTimestamp(System.currentTimeMillis(), 1) / 1000), lastWeekEnd);

        result.put("totalUser", totalUser);
        result.put("userDataAllTime", userDataAllTime);
        result.put("activeUserLastWeek", activeUserLastWeek.size());
        result.put("sentPicUserLastWeek", sentPicUserLastWeek.size());
        result.put("retcode", errorCode.getCode());
        result.put("msg", errorCode.getMsg());
        return result;
    }
}
