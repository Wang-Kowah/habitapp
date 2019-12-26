package com.kowah.habitapp.controller;

import com.kowah.habitapp.bean.vo.UserStatisticVo;
import com.kowah.habitapp.dbmapper.NoteMapper;
import com.kowah.habitapp.dbmapper.UserMapper;
import com.kowah.habitapp.utils.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@Controller
@RequestMapping("/")
public class IndexController {
    @Autowired
    private NoteMapper noteMapper;
    @Autowired
    private UserMapper userMapper;

    @GetMapping("/")
    public String Index() {
        return "forward://protocol.html";
    }

    @ResponseBody
    @GetMapping("/statistic")
    public Map<String, Object> statistic() {
        Map<String, Object> result = new HashMap<>();
        int lastWeekEnd = (int) (DateUtil.getMondayBeginTimestamp(System.currentTimeMillis(), 0) / 1000 - 1);
        int lastMonday = (int) (DateUtil.getMondayBeginTimestamp(System.currentTimeMillis(), 1) / 1000);
        //截止上周日总用户数以及每个用户的发帖数、上周发帖人数、上周发图人数、上周发帖带位置人数、连续两周发帖人数
        int totalUser = userMapper.getTotalUserNum(lastWeekEnd);
        List<UserStatisticVo> userDataAllTime = noteMapper.getActiveUserNum(0, lastWeekEnd)
                .stream()
                .filter(vo -> vo.getCount() >= 50)
                .collect(toList());
        List<UserStatisticVo> activeUserLastWeek = noteMapper.getActiveUserNum(lastMonday, lastWeekEnd);
        List<UserStatisticVo> sentPicUserLastWeek = noteMapper.getSentPicUserNum(lastMonday, lastWeekEnd);
        List<UserStatisticVo> sentLocationUserLastWeek = noteMapper.getLocationUserNum(lastMonday, lastWeekEnd);

        List<Integer> uidList = activeUserLastWeek.stream()
                .map(UserStatisticVo::getUid)
                .collect(toList());
//        activeUserTwoWeek.removeIf(vo -> !uidList.contains(vo.getUid()));
        List<UserStatisticVo> activeUserTwoWeek = noteMapper.getActiveUserNum(
                (int) (DateUtil.getMondayBeginTimestamp(System.currentTimeMillis(), 2) / 1000),
                (int) (DateUtil.getMondayBeginTimestamp(System.currentTimeMillis(), 1) / 1000 - 1))
                .stream()
                .filter(vo -> uidList.contains(vo.getUid()))
                .collect(toList());

        result.put("totalUser", totalUser);
        result.put("userDataAllTime", userDataAllTime);
        result.put("activeUserLastWeek", activeUserLastWeek.size());
        result.put("activeUserTwoWeek", activeUserTwoWeek.size());
        result.put("sentPicUserLastWeek", sentPicUserLastWeek.size());
        result.put("sentLocationUserLastWeek", sentLocationUserLastWeek.size());
        return result;
    }

    @GetMapping("/backupDB")
    public void getBackupDB(HttpServletRequest request, HttpServletResponse response) {
        String auth = request.getParameter("authUser");
        if (StringUtils.isEmpty(auth) || StringUtils.isBlank(auth) || !auth.equals("habit")) {
            return;
        }

        BufferedInputStream bis = null;
        try {
            File file = new File("/data/habit/habit.sql");
            if (file.exists()) {
                response.setContentType("application/force-download");// 设置强制下载打开
                response.addHeader("Content-Disposition", "attachment;fileName=" + file.lastModified() / 1000 + ".sql");// 设置文件名
                response.addHeader("Content-Length", String.valueOf(file.length()));
                byte[] buffer = new byte[1024];
                bis = new BufferedInputStream(new FileInputStream(file));

                OutputStream os = response.getOutputStream();
                int i = bis.read(buffer);
                while (i != -1) {
                    os.write(buffer, 0, i);
                    i = bis.read(buffer);
                }
            }
        } catch (Exception ignored) {
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
