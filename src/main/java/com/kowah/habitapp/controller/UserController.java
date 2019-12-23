package com.kowah.habitapp.controller;

import com.github.pagehelper.PageInfo;
import com.kowah.habitapp.bean.DayKeyword;
import com.kowah.habitapp.bean.Note;
import com.kowah.habitapp.bean.PeriodKeyword;
import com.kowah.habitapp.bean.User;
import com.kowah.habitapp.bean.enums.ErrorCode;
import com.kowah.habitapp.dbmapper.DayKeywordMapper;
import com.kowah.habitapp.dbmapper.NoteMapper;
import com.kowah.habitapp.dbmapper.PeriodKeywordMapper;
import com.kowah.habitapp.dbmapper.UserMapper;
import com.kowah.habitapp.service.PageService;
import com.kowah.habitapp.service.SendMsgService;
import com.kowah.habitapp.utils.JiebaUtil;
import com.kowah.habitapp.utils.LatAndLongitudeUtil;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Api
@RestController
@RequestMapping("/user")
public class UserController {
    private final static Logger logger = LoggerFactory.getLogger(UserController.class);

    // 默认拉取10条便签
    private static final int DEFAULT_NOTE_HISTORY = 10;
    // 此时此地默认取60分钟
    private static final int DEFAULT_TIME_RANGE = 60;
    // 此时此地默认取1000m
    private static final int DEFAULT_DISTANCE_RANGE = 1000;
    // 此时此地默认取四周
    private static final int DEFAULT_DATE_RANGE = 4 * 7;
    // 用户头像、图片存储地址
    private static final String PROFILE_PIC_LOCATION = "/data/habit/pic";
    private static final String NOTE_PIC_LOCATION = "/data/habit/pic/note";

    @Autowired
    private NoteMapper noteMapper;
    @Autowired
    private PageService pageService;
    @Autowired
    private SendMsgService sendMsgService;
    @Autowired
    private UserMapper userMapper;

    /**
     * 获取个人信息
     */
    @GetMapping("/info")
    public Map<String, Object> getUserInfo(@RequestParam("uid") Integer uid) {
        Map<String, Object> result = new HashMap<>();
        ErrorCode errorCode = ErrorCode.SUCCESS;

        if (uid == null) {
            errorCode = ErrorCode.USER_IS_NOT_EXIST;
            result.put("retcode", errorCode.getCode());
            result.put("msg", errorCode.getMsg());
            return result;
        }

        User user = userMapper.selectByPrimaryKey(uid);
        user.setProfile(null);
        result.put("info", user);
        result.put("retcode", errorCode.getCode());
        result.put("msg", errorCode.getMsg());
        return result;
    }

    /**
     * 获取便签历史记录
     */
    @GetMapping("/noteList")
    public Map<String, Object> getNoteList(@RequestParam("uid") Integer uid,
                                           @RequestParam("type") Integer type,
                                           @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                                           @RequestParam(value = "pageSize", required = false, defaultValue = DEFAULT_NOTE_HISTORY + "") Integer pageSize) {
        Map<String, Object> result = new HashMap<>();
        ErrorCode errorCode = ErrorCode.SUCCESS;

        if (uid == null || type == null) {
            errorCode = ErrorCode.PARAM_ERROR;
            result.put("retcode", errorCode.getCode());
            result.put("msg", errorCode.getMsg());
            return result;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("uid", uid);
        params.put("type", type);
        // 分页
        PageInfo<Note> notes = pageService.getNoteList(params, pageNum, pageSize);

        result.put("noteList", notes);
        result.put("retcode", errorCode.getCode());
        result.put("msg", errorCode.getMsg());
        return result;
    }

    /**
     * 发送便签
     */
    @PostMapping("/sendNote")
    public Map<String, Object> sendNote(@RequestParam("uid") Integer uid,
                                        @RequestParam("type") Integer type,
                                        @RequestParam("lat") String latStr,
                                        @RequestParam("lng") String lngStr,
                                        @RequestParam("msg") String msg) {
        Map<String, Object> result = new HashMap<>();
        ErrorCode errorCode = ErrorCode.SUCCESS;

        if (uid == null || type == null || msg == null || msg.trim().isEmpty()) {
            errorCode = ErrorCode.PARAM_ERROR;
            result.put("retcode", errorCode.getCode());
            result.put("msg", errorCode.getMsg());
            return result;
        }

        try {
            Note note = new Note();
            note.setUid(uid);
            note.setType(type);
            note.setContent(msg);
            note.setCreateTime((int) (System.currentTimeMillis() / 1000));
            try {
                BigDecimal lat = new BigDecimal(latStr);
                BigDecimal lng = new BigDecimal(lngStr);
                note.setLat(lat);
                note.setLng(lng);
            } catch (Exception ignored) {
            }
            noteMapper.insertSelective(note);
        } catch (Exception e) {
            logger.error("", e);
            errorCode = ErrorCode.SYSTEM_ERROR;
            result.put("retcode", errorCode.getCode());
            result.put("msg", errorCode.getMsg());
            return result;
        }
        result.put("retcode", errorCode.getCode());
        result.put("msg", errorCode.getMsg());
        return result;
    }

    /**
     * 用户注册
     */
    @PostMapping("/signUp")
    public Map<String, Object> signUp(@RequestParam("mobile") String mobileStr) {
        Map<String, Object> result = new HashMap<>();
        ErrorCode errorCode = ErrorCode.SUCCESS;

        if (mobileStr == null || mobileStr.length() != 11) {
            errorCode = ErrorCode.PARAM_ERROR;
            result.put("retcode", errorCode.getCode());
            result.put("msg", errorCode.getMsg());
            return result;
        }

        User user = userMapper.selectByMobile(mobileStr);
        if (user != null) {
            errorCode = ErrorCode.MOBILE_EXIST_ERROR;
            result.put("uid", user.getUid());
            result.put("retcode", errorCode.getCode());
            result.put("msg", errorCode.getMsg());
            return result;
        }

        try {
            String name = "用户" + mobileStr.substring(7) + System.currentTimeMillis() / 1000;

            user = new User();
            user.setMobile(mobileStr);
            user.setName(name);
//            user.setPassword(md5(password));
            user.setCreateTime((int) (System.currentTimeMillis() / 1000));
            user.setProfile(PROFILE_PIC_LOCATION + File.separator + "default.png");
            userMapper.insertAndGetUid(user);
            //xml设置返回自增uid无效，手动获取uid
            result.put("uid", user.getUid());
        } catch (Exception e) {
            logger.error("", e);
            errorCode = ErrorCode.SYSTEM_ERROR;
            result.put("retcode", errorCode.getCode());
            result.put("msg", errorCode.getMsg());
            return result;
        }
        result.put("retcode", errorCode.getCode());
        result.put("msg", errorCode.getMsg());
        return result;
    }

    /**
     * 用户登录
     */
    @Deprecated
    @PostMapping("/logIn")
    public Map<String, Object> logIn(@RequestParam("mobile") String mobileStr) {
        Map<String, Object> result = new HashMap<>();
        ErrorCode errorCode = ErrorCode.SUCCESS;
        if (mobileStr.length() != 11) {
            errorCode = ErrorCode.PARAM_ERROR;
            result.put("retcode", errorCode.getCode());
            result.put("msg", errorCode.getMsg());
            return result;
        }

        User user = userMapper.selectByMobile(mobileStr);
        if (user == null) {
            errorCode = ErrorCode.USER_IS_NOT_EXIST;
            result.put("retcode", errorCode.getCode());
            result.put("msg", errorCode.getMsg());
            return result;
        }

        result.put("uid", user.getUid());
        result.put("retcode", errorCode.getCode());
        result.put("msg", errorCode.getMsg());
        return result;
    }

    /**
     * 获取验证码
     */
    @PostMapping("/getVerifyCode")
    public Map<String, Object> verifyCode(@RequestParam("mobile") String mobile) {
        Map<String, Object> result = new HashMap<>();
        ErrorCode errorCode;

        if (mobile == null || mobile.length() != 11) {
            errorCode = ErrorCode.PARAM_ERROR;
            result.put("retcode", errorCode.getCode());
            result.put("msg", errorCode.getMsg());
            return result;
        }

//        if (userMapper.selectByMobile(mobile) != null) {
//            errorCode = ErrorCode.MOBILE_EXIST_ERROR;
//            result.put("retcode", errorCode.getCode());
//            result.put("msg", errorCode.getMsg());
//            return result;
//        }

        errorCode = sendMsgService.sendVerifyCode(mobile);
        result.put("retcode", errorCode.getCode());
        result.put("msg", errorCode.getMsg());
        return result;
    }

    /**
     * 验证码校验模块
     */
    @PostMapping("/checkVerifyCode")
    public Map<String, Object> checkVerifyCode(@RequestParam("mobile") String mobile,
                                               @RequestParam("code") String msgCode) {
        Map<String, Object> result = new HashMap<>();
        ErrorCode errorCode = ErrorCode.SUCCESS;

        // iOS上架审核要求白名单
        if (mobile.equals("15302714670") && msgCode.equals("1234")) {
            result.put("retcode", errorCode.getCode());
            result.put("msg", errorCode.getMsg());
            return result;
        }

        if (!sendMsgService.checkCode(mobile, msgCode)) {
            errorCode = ErrorCode.MSM_CODE_ERROR;
        }

        result.put("retcode", errorCode.getCode());
        result.put("msg", errorCode.getMsg());
        return result;
    }

    /**
     * 上传头像
     */
    @PostMapping("/uploadProfile")
    public Map<String, Object> profilePic(@RequestParam("uid") Integer uid,
                                          @RequestParam("pic") MultipartFile pic,
                                          HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        ErrorCode errorCode = ErrorCode.SUCCESS;

        if (uid == null) {
            errorCode = ErrorCode.PARAM_ERROR;
            result.put("retcode", errorCode.getCode());
            result.put("msg", errorCode.getMsg());
            return result;
        }

        if (!pic.isEmpty()) {
            try {// 上传文件大小限制设置为10M

                // 先移除旧头像以免用户上传重复头像导致的错误
                String oldPath = userMapper.selectByPrimaryKey(uid).getProfile();
                File oldProfile = new File(oldPath);
                if (!oldPath.contains("default") && oldProfile.exists()) {
                    oldProfile.delete();
                }

                // 获得文件后缀名判断其类型
                String fileNameOriginal = pic.getOriginalFilename();
                String mimeType = request.getServletContext().getMimeType(fileNameOriginal);
                if (!mimeType.startsWith("image/")) {
                    throw new Exception();
                }
                String suffix = fileNameOriginal.substring(pic.getOriginalFilename().lastIndexOf("."));
                String filePath = PROFILE_PIC_LOCATION + File.separator + uid + suffix;
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(filePath)));
                out.write(pic.getBytes());
                out.flush();
                out.close();

                // 更新数据库
                User newUser = new User();
                newUser.setUid(uid);
                newUser.setProfile(filePath);
                userMapper.updateByPrimaryKeySelective(newUser);
            } catch (Exception e) {
                errorCode = ErrorCode.UPLOAD_PIC_ERROR;
                logger.error("Upload error", e);
            }
        } else {
            errorCode = ErrorCode.UPLOAD_PIC_ERROR;
        }

        result.put("retcode", errorCode.getCode());
        result.put("msg", errorCode.getMsg());
        return result;
    }

    /**
     * 获取头像
     */
    @GetMapping("/profile")
    public Map<String, Object> getProfilePic(@RequestParam("uid") Integer uid, HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();
        ErrorCode errorCode;

        if (uid == null) {
            errorCode = ErrorCode.PARAM_ERROR;
            result.put("retcode", errorCode.getCode());
            result.put("msg", errorCode.getMsg());
            return result;
        }

        BufferedInputStream bis = null;
        try {
            User user = userMapper.selectByPrimaryKey(uid);
            String filePath = user.getProfile();

            File file = new File(filePath);
            if (file.exists()) {
                String suffix = filePath.substring(filePath.lastIndexOf("."));
                response.setContentType("application/force-download");// 设置强制下载打开
                response.addHeader("Content-Disposition", "attachment;fileName=" + file.lastModified() / 1000 + suffix);// 设置文件名
                response.addHeader("Content-Length", String.valueOf(file.length()));
                byte[] buffer = new byte[1024];
                bis = new BufferedInputStream(new FileInputStream(file));

                OutputStream os = response.getOutputStream();
                int i = bis.read(buffer);
                while (i != -1) {
                    os.write(buffer, 0, i);
                    i = bis.read(buffer);
                }
                return null;
            } else {
                errorCode = ErrorCode.SYSTEM_ERROR;
            }
        } catch (Exception e) {
            logger.error("", e);
            errorCode = ErrorCode.USER_IS_NOT_EXIST;
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        result.put("retcode", errorCode.getCode());
        result.put("msg", errorCode.getMsg());
        return result;
    }

    /**
     * 获取每日关键词记录
     */
    @GetMapping("/dayKeyword")
    public Map<String, Object> getDayKeyword(@RequestParam("uid") Integer uid,
                                             @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                                             @RequestParam(value = "pageSize", required = false, defaultValue = DEFAULT_NOTE_HISTORY + "") Integer pageSize) {
        Map<String, Object> result = new HashMap<>();
        ErrorCode errorCode = ErrorCode.SUCCESS;

        if (uid == null) {
            errorCode = ErrorCode.PARAM_ERROR;
            result.put("retcode", errorCode.getCode());
            result.put("msg", errorCode.getMsg());
            return result;
        }

        PageInfo<DayKeyword> dayKeywords = pageService.getDayKeywordList(uid, pageNum, pageSize);
        result.put("dayKeywordList", dayKeywords);
        result.put("retcode", errorCode.getCode());
        result.put("msg", errorCode.getMsg());
        return result;
    }

    /**
     * 获取每周/月关键词记录
     */
    @GetMapping("/keyword")
    public Map<String, Object> getKeyword(@RequestParam("uid") Integer uid,
                                          @RequestParam("type") Integer type,
                                          @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                                          @RequestParam(value = "pageSize", required = false, defaultValue = DEFAULT_NOTE_HISTORY + "") Integer pageSize) {
        Map<String, Object> result = new HashMap<>();
        ErrorCode errorCode = ErrorCode.SUCCESS;

        if (uid == null || type == null) {
            errorCode = ErrorCode.PARAM_ERROR;
            result.put("retcode", errorCode.getCode());
            result.put("msg", errorCode.getMsg());
            return result;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("uid", uid);
        params.put("type", type);
        PageInfo<PeriodKeyword> periodKeywords = pageService.getKeywordList(params, pageNum, pageSize);
        result.put("keywordList", periodKeywords);
        result.put("retcode", errorCode.getCode());
        result.put("msg", errorCode.getMsg());
        return result;
    }

    /**
     * 搜索每日总结
     */
    @PostMapping("/search")
    public Map<String, Object> search(@RequestParam("uid") Integer uid,
                                      @RequestParam("key") String key,
                                      @RequestParam(value = "pic", required = false, defaultValue = "false") boolean pic,
                                      @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                                      @RequestParam(value = "pageSize", required = false, defaultValue = DEFAULT_NOTE_HISTORY + "") Integer pageSize) {
        Map<String, Object> result = new HashMap<>();
        ErrorCode errorCode = ErrorCode.SUCCESS;

        if (uid == null || StringUtils.isEmpty(key) || StringUtils.isBlank(key)) {
            errorCode = ErrorCode.PARAM_ERROR;
            result.put("retcode", errorCode.getCode());
            result.put("msg", errorCode.getMsg());
            return result;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("uid", uid);
        params.put("key", '%' + key + '%');
        params.put("pic", pic);
        PageInfo searchResult = pageService.search(params, pageNum, pageSize, 0);
        result.put("result", searchResult);
        result.put("retcode", errorCode.getCode());
        result.put("msg", errorCode.getMsg());
        return result;
    }

    /**
     * 发送图片便签
     */
    @PostMapping("/sendPic")
    public Map<String, Object> sendPic(@RequestParam("uid") Integer uid,
                                       @RequestParam("type") Integer type,
                                       @RequestParam("lat") String latStr,
                                       @RequestParam("lng") String lngStr,
                                       @RequestParam("pic") MultipartFile pic,
                                       HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        ErrorCode errorCode = ErrorCode.SUCCESS;


        if (uid == null || type == null) {
            errorCode = ErrorCode.PARAM_ERROR;
            result.put("retcode", errorCode.getCode());
            result.put("msg", errorCode.getMsg());
            return result;
        }

        if (!pic.isEmpty()) {
            try {// 上传文件大小限制设置为10M

                // 获得文件后缀名判断其类型
                String fileNameOriginal = pic.getOriginalFilename();
                String mimeType = request.getServletContext().getMimeType(fileNameOriginal);
                if (!mimeType.startsWith("image/")) {
                    //TODO webp会出错
                    throw new Exception();
                }
                long now = System.currentTimeMillis();
                String suffix = fileNameOriginal.substring(pic.getOriginalFilename().lastIndexOf("."));
                String fileDir = NOTE_PIC_LOCATION + File.separator + uid;
                File dir = new File(fileDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                String filePath = fileDir + File.separator + now + suffix;
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(filePath)));
                out.write(pic.getBytes());
                out.flush();
                out.close();

                String picName = "_PIC:" + uid + File.separator + now + suffix;
                result.put("picName", picName);

                // 更新数据库
                Note note = new Note();
                note.setUid(uid);
                note.setType(type);
                note.setContent(picName);
                note.setCreateTime((int) (now / 1000));
                try {
                    BigDecimal lat = new BigDecimal(latStr);
                    BigDecimal lng = new BigDecimal(lngStr);
                    note.setLat(lat);
                    note.setLng(lng);
                } catch (Exception ignored) {
                }
                noteMapper.insertSelective(note);
            } catch (Exception e) {
                errorCode = ErrorCode.UPLOAD_PIC_ERROR;
                logger.error("Upload error", e);
            }
        } else {
            errorCode = ErrorCode.UPLOAD_PIC_ERROR;
        }

        result.put("retcode", errorCode.getCode());
        result.put("msg", errorCode.getMsg());
        return result;
    }

    /**
     * 获取图片便签
     */
    @GetMapping("/pic")
    public Map<String, Object> getPic(@RequestParam("picName") String picName, HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();
        ErrorCode errorCode;

        if (picName == null || !picName.startsWith("_PIC:")) {
            errorCode = ErrorCode.PARAM_ERROR;
            result.put("retcode", errorCode.getCode());
            result.put("msg", errorCode.getMsg());
            return result;
        }

        BufferedInputStream bis = null;
        try {
            String filePath = NOTE_PIC_LOCATION + File.separator + picName.substring(5);

            File file = new File(filePath);
            if (file.exists()) {
                response.setContentType("application/force-download");// 设置强制下载打开
                response.addHeader("Content-Disposition", "attachment;fileName=" + picName);// 设置文件名
                response.addHeader("Content-Length", String.valueOf(file.length()));
                byte[] buffer = new byte[1024];
                bis = new BufferedInputStream(new FileInputStream(file));

                OutputStream os = response.getOutputStream();
                int i = bis.read(buffer);
                while (i != -1) {
                    os.write(buffer, 0, i);
                    i = bis.read(buffer);
                }
                return null;
            } else {
                errorCode = ErrorCode.SYSTEM_ERROR;
            }
        } catch (Exception e) {
            logger.error("", e);
            errorCode = ErrorCode.USER_IS_NOT_EXIST;
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        result.put("retcode", errorCode.getCode());
        result.put("msg", errorCode.getMsg());
        return result;
    }

    /**
     * 此时此地模块
     */
    @PostMapping("/hereAndNow")
    public Map<String, Object> hereAndNow(@RequestParam("uid") Integer uid,
                                          @RequestParam("lat") String latStr,
                                          @RequestParam("lng") String lngStr,
                                          @RequestParam(value = "pic", required = false, defaultValue = "false") boolean pic,
                                          @RequestParam(value = "time", required = false, defaultValue = DEFAULT_TIME_RANGE + "") Integer time,
                                          @RequestParam(value = "distance", required = false, defaultValue = DEFAULT_DISTANCE_RANGE + "") Integer distance) {
        Map<String, Object> result = new HashMap<>();
        ErrorCode errorCode = ErrorCode.SUCCESS;

        if (uid == null || latStr == null || lngStr == null) {
            errorCode = ErrorCode.PARAM_ERROR;
            result.put("retcode", errorCode.getCode());
            result.put("msg", errorCode.getMsg());
            return result;
        }

        BigDecimal lat, lng;
        try {
            time *= 60;
            lat = new BigDecimal(latStr);
            lng = new BigDecimal(lngStr);

            int now = (int) (System.currentTimeMillis() / 1000);
            double lat1, lat2, lng1, lng2;
            LatAndLongitudeUtil.Location[] locations = LatAndLongitudeUtil.getRectangle4Point(lat.doubleValue(), lng.doubleValue(), (double) distance);
            lat1 = locations[2].latitude;
            lat2 = locations[1].latitude;
            lng1 = locations[2].longitude;
            lng2 = locations[1].longitude;

            Map<String, Object> params = new HashMap<>();
            params.put("uid", uid);
            params.put("latStart", lat1);
            params.put("latEnd", lat2);
            params.put("lngStart", lng1);
            params.put("lngEnd", lng2);
            params.put("pic", pic);
            List<Note> noteList = new ArrayList<>();

            for (int i = 0; i < DEFAULT_DATE_RANGE; i++) {
                now -= 24 * 60 * 60;
                params.put("timeStart", now - time);
                params.put("timeEnd", now + time);
                noteList.addAll(noteMapper.searchByUidAndTimeAndLocation(params));

                if (noteList.size() >= 30) {
                    noteList = noteList.subList(0, 30);
                    break;
                }
            }

            result.put("noteList", noteList);
            result.put("size", noteList.size());
            result.put("timeRange(min)", time / 60);
            result.put("distanceRange(m)", distance);
        } catch (NumberFormatException e) {
            errorCode = ErrorCode.PARAM_ERROR;
        } catch (Exception e) {
            errorCode = ErrorCode.INVALID_LOCATION;
            logger.debug("here and now error", e);
        }

        result.put("retcode", errorCode.getCode());
        result.put("msg", errorCode.getMsg());
        return result;
    }

    /**
     * 提取关键词
     */
    @Deprecated
    @PostMapping("/extractKeyword")
    public Map<String, Object> extractKeyword(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();
        ErrorCode errorCode = ErrorCode.SUCCESS;
        String voiceStr = request.getParameter("text");
        String topNStr = request.getParameter("topN");

        int topN;
        try {
            topN = Integer.parseInt(topNStr);
        } catch (Exception e) {
            topN = 2;
        }

        try {
            List<String> keywords = JiebaUtil.getKeyword(voiceStr, topN);
            result.put("keywords", keywords);
        } catch (Exception e) {
            errorCode = ErrorCode.EXTRACT_KEYWORD_ERROR;
        }

        result.put("retcode", errorCode.getCode());
        result.put("msg", errorCode.getMsg());
        return result;
    }

    /**
     * 交流助理模块
     */
    @PostMapping("/extractVoiceText")
    public Map<String, Object> extractVoiceText(@RequestParam("uid") Integer uid,
                                                @RequestParam("text") String voiceStr,
                                                @RequestParam(value = "pic", required = false, defaultValue = "false") boolean pic) {
        Map<String, Object> result = new HashMap<>();
        ErrorCode errorCode = ErrorCode.SUCCESS;

        int topN = 2, type = 0;

        // 空白字符串直接返回
        if (StringUtils.isEmpty(voiceStr) || StringUtils.isBlank(voiceStr)) {
            result.put("size", 0);
            result.put("keywords", new ArrayList<>());
            result.put("retcode", errorCode.getCode());
            result.put("msg", errorCode.getMsg());
            return result;
        }

        if (uid == null) {
            errorCode = ErrorCode.PARAM_ERROR;
            result.put("retcode", errorCode.getCode());
            result.put("msg", errorCode.getMsg());
            return result;
        }

        try {
            List<String> keywords = JiebaUtil.getKeyword(voiceStr, topN);

            List<Note> notes = new ArrayList<>();
            for (String keyword : keywords) {
                Map<String, Object> params = new HashMap<>();
                params.put("uid", uid);
                params.put("type", type);
                params.put("pic", pic);
                params.put("key", '%' + keyword + '%');
                notes.addAll(noteMapper.searchByUidAndTypeAndKey(params));
            }

            if (notes.size() != 0) {
                // 去重排序截取
                notes = notes.stream()
                        .distinct()     // 需重写equals跟hashCode
                        .sorted((o1, o2) -> o2.getCreateTime() - o1.getCreateTime())
                        .limit(30)
                        .collect(Collectors.toList());

                result.put("result", notes);
            }

            result.put("keywords", keywords);
            result.put("size", notes.size());
        } catch (Exception e) {
            errorCode = ErrorCode.EXTRACT_KEYWORD_ERROR;
        }

        result.put("retcode", errorCode.getCode());
        result.put("msg", errorCode.getMsg());
        return result;
    }
}
