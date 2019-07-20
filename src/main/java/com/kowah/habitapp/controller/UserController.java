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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "/user")
public class UserController {
    private final static Logger logger = LoggerFactory.getLogger(UserController.class);

    /**
     * 默认拉取10条便签
     */
    private static final int DEFAULT_NOTE_HISTORY = 10;
    /**
     * 用户头像存储地址
     */
    // NORMAL
    private static final String PROFILE_PIC_LOCATION = "/data/habit/pic";
    private static final String NOTE_PIC_LOCATION = "/data/habit/pic/note";
    // TEST
//    private static final String PROFILE_PIC_LOCATION = "C:" + File.separator + "Data" + File.separator + "pic";
//    private static final String NOTE_PIC_LOCATION = "F:" + File.separator + "Data" + File.separator + "pic" + File.separator + "note";

    @Autowired
    private DayKeywordMapper dayKeywordMapper;
    @Autowired
    private NoteMapper noteMapper;
    @Autowired
    private PageService pageService;
    @Autowired
    private PeriodKeywordMapper periodKeywordMapper;
    @Autowired
    private SendMsgService sendMsgService;
    @Autowired
    private UserMapper userMapper;

    /**
     * 获取个人信息
     */
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getUserInfo(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();
        ErrorCode errorCode = ErrorCode.SUCCESS;
        String uidStr = request.getParameter("uid");

        int uid;
        try {
            uid = Integer.parseInt(uidStr);
        } catch (Exception e) {
            logger.error("", e);
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
    @RequestMapping(value = "/noteList", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getNoteList(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();
        ErrorCode errorCode = ErrorCode.SUCCESS;
        String uidStr = request.getParameter("uid");
        String typeStr = request.getParameter("type");
        String pageNumStr = request.getParameter("pageNum");
        String pageSizeStr = request.getParameter("pageSize");

        int uid, type, pageNum, pageSize;
        try {
            uid = Integer.parseInt(uidStr);
            type = Integer.parseInt(typeStr);
        } catch (Exception e) {
            logger.error("", e);
            errorCode = ErrorCode.PARAM_ERROR;
            result.put("retcode", errorCode.getCode());
            result.put("msg", errorCode.getMsg());
            return result;
        }

        try {
            pageNum = Integer.parseInt(pageNumStr);
            pageSize = Integer.parseInt(pageSizeStr);
        } catch (Exception e) {
            pageSize = DEFAULT_NOTE_HISTORY;
            pageNum = 1;
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
    @RequestMapping(value = "/sendNote", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> sendNote(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();
        ErrorCode errorCode = ErrorCode.SUCCESS;
        String uidStr = request.getParameter("uid");
        String typeStr = request.getParameter("type");
        String msg = request.getParameter("msg");

        int uid, type;
        try {
            uid = Integer.parseInt(uidStr);
            type = Integer.parseInt(typeStr);
            if (msg.trim().equals("")) {
                throw new Exception();
            }
        } catch (Exception e) {
            logger.error("", e);
            errorCode = ErrorCode.USER_IS_NOT_EXIST;
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
    @RequestMapping(value = "/signUp", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> signUp(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();
        ErrorCode errorCode = ErrorCode.SUCCESS;
        String mobileStr = request.getParameter("mobile");
        //用户名后台生成，格式:"用户"+手机号后4位+10位时间戳
//        String name = request.getParameter("name");
        //去除密码，注册码仅用于登录
//        String password = request.getParameter("password");

//        long mobile;
//        try {
//            mobile = Long.parseLong(mobileStr);
//            if (name.equals("") || password.equals("")) {
//                throw new Exception();
//            }
//        } catch (Exception e)
        if (mobileStr.length() != 11) {
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
    @RequestMapping(value = "/logIn", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> logIn(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();
        ErrorCode errorCode = ErrorCode.SUCCESS;
        String mobileStr = request.getParameter("mobile");
//        String password = request.getParameter("password");

//        long mobile;
//        try {
//            mobile = Long.parseLong(mobileStr);
//            if (password.equals("")) {
//                throw new Exception();
//            }
//        } catch (Exception e)
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

//        if (!md5(password).equals(user.getPassword())) {
//            errorCode = ErrorCode.LOGIN_FAIL;
//        }

        if (errorCode.equals(ErrorCode.SUCCESS)) {
            result.put("uid", user.getUid());
        }
        result.put("retcode", errorCode.getCode());
        result.put("msg", errorCode.getMsg());
        return result;
    }

    /**
     * 获取验证码
     */
    @RequestMapping(value = "/getVerifyCode", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> verifyCode(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();
        ErrorCode errorCode;
        String mobile = request.getParameter("mobile");

        if (mobile.length() != 11) {
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
    @RequestMapping(value = "/checkVerifyCode", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> checkVerifyCode(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();
        ErrorCode errorCode = ErrorCode.SUCCESS;
        String mobile = request.getParameter("mobile");
        String msgCode = request.getParameter("code");

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
    @RequestMapping(value = "/uploadProfile", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> profilePic(@RequestParam("pic") MultipartFile pic, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        ErrorCode errorCode = ErrorCode.SUCCESS;
        String uidStr = request.getParameter("uid");

        int uid;
        try {
            uid = Integer.parseInt(uidStr);
        } catch (Exception e) {
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
                String filePath = PROFILE_PIC_LOCATION + File.separator + uidStr + suffix;
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
    @RequestMapping(value = "/profile", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getProfilePic(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();
        ErrorCode errorCode;
        String uidStr = request.getParameter("uid");

        int uid;
        try {
            uid = Integer.parseInt(uidStr);
        } catch (Exception e) {
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
                response.setContentType("application/force-download");// 设置强制下载不打开
                response.addHeader("Content-Disposition", "attachment;fileName=" + uid + suffix);// 设置文件名
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
    @RequestMapping(value = "/dayKeyword", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getDayKeyword(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();
        ErrorCode errorCode = ErrorCode.SUCCESS;
        String uidStr = request.getParameter("uid");
        String pageNumStr = request.getParameter("pageNum");
        String pageSizeStr = request.getParameter("pageSize");

        int uid, pageNum, pageSize;
        try {
            uid = Integer.parseInt(uidStr);
        } catch (Exception e) {
            logger.error("", e);
            errorCode = ErrorCode.PARAM_ERROR;
            result.put("retcode", errorCode.getCode());
            result.put("msg", errorCode.getMsg());
            return result;
        }

        try {
            pageNum = Integer.parseInt(pageNumStr);
            pageSize = Integer.parseInt(pageSizeStr);
        } catch (Exception e) {
            pageSize = DEFAULT_NOTE_HISTORY;
            pageNum = 1;
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
    @RequestMapping(value = "/keyword", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getKeyword(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();
        ErrorCode errorCode = ErrorCode.SUCCESS;
        String uidStr = request.getParameter("uid");
        String typeStr = request.getParameter("type");
        String pageNumStr = request.getParameter("pageNum");
        String pageSizeStr = request.getParameter("pageSize");

        int uid, type, pageNum, pageSize;
        try {
            uid = Integer.parseInt(uidStr);
            type = Integer.parseInt(typeStr);
        } catch (Exception e) {
            logger.error("", e);
            errorCode = ErrorCode.PARAM_ERROR;
            result.put("retcode", errorCode.getCode());
            result.put("msg", errorCode.getMsg());
            return result;
        }

        try {
            pageNum = Integer.parseInt(pageNumStr);
            pageSize = Integer.parseInt(pageSizeStr);
        } catch (Exception e) {
            pageSize = DEFAULT_NOTE_HISTORY;
            pageNum = 1;
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
    @RequestMapping(value = "/search", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> search(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();
        ErrorCode errorCode = ErrorCode.SUCCESS;
        String uidStr = request.getParameter("uid");
        //type 0:每日总结/1:每周总结/2:日关键词/3:周关键词/4:月关键词
        String typeStr = request.getParameter("type");
        String key = request.getParameter("key");
        String pageNumStr = request.getParameter("pageNum");
        String pageSizeStr = request.getParameter("pageSize");

        int uid, type, pageNum, pageSize;
        try {
            uid = Integer.parseInt(uidStr);
            if (StringUtils.isEmpty(key) || StringUtils.isBlank(key)) {
                throw new Exception();
            }
        } catch (Exception e) {
            logger.error("", e);
            errorCode = ErrorCode.PARAM_ERROR;
            result.put("retcode", errorCode.getCode());
            result.put("msg", errorCode.getMsg());
            return result;
        }

        try {
            type = Integer.parseInt(typeStr);
            pageNum = Integer.parseInt(pageNumStr);
            pageSize = Integer.parseInt(pageSizeStr);
        } catch (Exception e) {
            type = 0;
            pageNum = 1;
            pageSize = DEFAULT_NOTE_HISTORY;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("uid", uid);
        params.put("key", '%' + key + '%');
        PageInfo searchResult = pageService.search(params, pageNum, pageSize, type);
        result.put("result", searchResult);
        result.put("retcode", errorCode.getCode());
        result.put("msg", errorCode.getMsg());
        return result;
    }

    /**
     * 发送图片便签
     */
    @RequestMapping(value = "/sendPic", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> sendPic(HttpServletRequest request, @RequestParam("pic") MultipartFile pic) {
        Map<String, Object> result = new HashMap<>();
        ErrorCode errorCode = ErrorCode.SUCCESS;
        String uidStr = request.getParameter("uid");
        String typeStr = request.getParameter("type");

        int uid, type;
        try {
            uid = Integer.parseInt(uidStr);
            type = Integer.parseInt(typeStr);
        } catch (Exception e) {
            logger.error("", e);
            errorCode = ErrorCode.USER_IS_NOT_EXIST;
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
                    throw new Exception();
                }
                long now = System.currentTimeMillis();
                String suffix = fileNameOriginal.substring(pic.getOriginalFilename().lastIndexOf("."));
                String fileDir = NOTE_PIC_LOCATION + File.separator + uidStr;
                File dir = new File(fileDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                String filePath = fileDir + File.separator + now + suffix;
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(filePath)));
                out.write(pic.getBytes());
                out.flush();
                out.close();

                // 更新数据库
                Note note = new Note();
                note.setUid(uid);
                note.setType(type);
                note.setContent("_PIC:" + uidStr + File.separator + now + suffix);
                note.setCreateTime((int) (now / 1000));
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
    @RequestMapping(value = "/pic", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> sendPic(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();
        ErrorCode errorCode = ErrorCode.SUCCESS;
        String picName = request.getParameter("picName");

        if (StringUtils.isEmpty(picName) || StringUtils.isBlank(picName) || !picName.startsWith("_PIC:")) {
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
                String suffix = filePath.substring(filePath.lastIndexOf("."));
                response.setContentType("application/force-download");// 设置强制下载不打开
                response.addHeader("Content-Disposition", "attachment;fileName=" + picName);// 设置文件名
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
}