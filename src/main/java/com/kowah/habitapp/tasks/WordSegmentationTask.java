package com.kowah.habitapp.tasks;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStreamReader;

@Component
@Configurable
@EnableScheduling
public class WordSegmentationTask {

    @Scheduled(cron = "0 0 0 * * ?")
    public void day() {
        try {
            String exe = "python";
            String command = "/data/habit/jieba_xiguanAPP_day.py";
            String[] cmdArr = new String[]{exe, command, "1", "2"};
            Process process = Runtime.getRuntime().exec(cmdArr);
            DataInputStream dis = new DataInputStream(process.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(dis));
            String str = bufferedReader.readLine();
            process.waitFor();
            System.out.println(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Scheduled(cron = "0 0 0 ? * MON")
    public void week() {
        try {
            String exe = "python";
            String command = "/data/habit/jieba_xiguanAPP_week.py";
            String[] cmdArr = new String[]{exe, command, "1", "2"};
            Process process = Runtime.getRuntime().exec(cmdArr);
            DataInputStream dis = new DataInputStream(process.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(dis));
            String str = bufferedReader.readLine();
            process.waitFor();
            System.out.println(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Scheduled(cron = "0 0 0 1 * ?")
    public void month() {
        try {
            String exe = "python";
            String command = "/data/habit/jieba_xiguanAPP_month.py";
            String[] cmdArr = new String[]{exe, command, "1", "2"};
            Process process = Runtime.getRuntime().exec(cmdArr);
            DataInputStream dis = new DataInputStream(process.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(dis));
            String str = bufferedReader.readLine();
            process.waitFor();
            System.out.println(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
