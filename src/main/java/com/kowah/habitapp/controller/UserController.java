package com.kowah.habitapp.controller;

import com.kowah.habitapp.bean.Note;
import com.kowah.habitapp.bean.User;
import com.kowah.habitapp.bean.enums.ErrorCode;
import com.kowah.habitapp.dbmapper.NoteMapper;
import com.kowah.habitapp.dbmapper.UserMapper;
import com.kowah.habitapp.service.SendMsgService;
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

import static com.alibaba.druid.util.Utils.md5;

@Controller
@RequestMapping(value = "/user")
public class UserController {
    private final static Logger logger = LoggerFactory.getLogger(UserController.class);

    /**
     * 默认拉取50条便签
     */
    private static final int DEFUALT_NOTE_HISTORY = 50;
    /**
     * 用户头像存储地址
     */
    private static final String PROFILE_PIC_LOCATION = "C:" + File.separator + "Data" + File.separator + "pic";

    @Autowired
    private NoteMapper noteMapper;
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

        int uid, type;
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

        Map<String, Object> params = new HashMap<>();
        params.put("uid", uid);
        params.put("type", type);
        List<Note> notes = noteMapper.selectByUidAndType(params);
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
        String name = request.getParameter("name");
//        String password = request.getParameter("password");
        //去除密码，注册码仅用于登录

        int mobile;
        try {
            mobile = Integer.parseInt(mobileStr);
            if (name.equals("") /*|| password.equals("")*/) {
                throw new Exception();
            }
        } catch (Exception e) {
            errorCode = ErrorCode.PARAM_ERROR;
            result.put("retcode", errorCode.getCode());
            result.put("msg", errorCode.getMsg());
            return result;
        }

        if (userMapper.selectByMobile(mobile) != null) {
            errorCode = ErrorCode.MOBILE_EXIST_ERROR;
            result.put("retcode", errorCode.getCode());
            result.put("msg", errorCode.getMsg());
            return result;
        }

        try {
            User user = new User();
            user.setMobile(mobile);
            user.setName(name);
//            user.setPassword(md5(password));
            user.setCreateTime((int) (System.currentTimeMillis() / 1000));
            user.setProfile(PROFILE_PIC_LOCATION + File.separator + "default.jpg");
            int uid = userMapper.insertAndGetUid(user);
            result.put("uid", uid);
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

        int mobile;
        try {
            mobile = Integer.parseInt(mobileStr);
//            if (password.equals("")) {
//                throw new Exception();
//            }
        } catch (Exception e) {
            errorCode = ErrorCode.PARAM_ERROR;
            result.put("retcode", errorCode.getCode());
            result.put("msg", errorCode.getMsg());
            return result;
        }

        User user = userMapper.selectByMobile(mobile);
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

        if (userMapper.selectByMobile((int) Long.parseLong(mobile)) != null) {
            errorCode = ErrorCode.MOBILE_EXIST_ERROR;
            result.put("retcode", errorCode.getCode());
            result.put("msg", errorCode.getMsg());
            return result;
        }

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
            try {// Springboot自带Tomcat上传文件大小限制为1m，无需再做限制

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
                errorCode = ErrorCode.UPLOAD_PROFILE_PIC_ERROR;
                logger.error("Upload error", e);
            }
        } else {
            errorCode = ErrorCode.UPLOAD_PROFILE_PIC_ERROR;
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

    //TODO 高频词总结接口
}