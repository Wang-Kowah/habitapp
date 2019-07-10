package com.kowah.habitapp.utils;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;
import com.qianxinyao.analysis.jieba.keyword.Keyword;
import com.qianxinyao.analysis.jieba.keyword.TFIDFAnalyzer;

import java.util.List;

public class jiebaUtil {
    public static void main(String[] args) {
        String[] sentences = new String[]{"我来到北京清华大学", "小明硕士毕业于中国科学院计算所，后在日本京都大学深造"};
        for (String sentence : sentences) {
            System.out.println("INDEX:" + segmentByIndex(sentence).toString());
            System.out.println("SEARCH:" + segmentBySearch(sentence).toString());
            System.out.println("DEFAULT:" + segmenterDefault(sentence));
        }
        for (Keyword keyword : getKeyword("孩子上了幼儿园 安全防拐教育要做好", 5)) {
            System.out.println(keyword.getName() + ":" + keyword.getTfidfvalue());
        }
    }

    /**
     * Index模式，用于对索引文档分词
     * 把句子中所有的可以成词的词语都扫描出来,但是不能解决歧义
     *
     * @param sentence
     * @return
     */
    public static List<SegToken> segmentByIndex(String sentence) {
        JiebaSegmenter segmenter = new JiebaSegmenter();
        return segmenter.process(sentence, JiebaSegmenter.SegMode.INDEX);
    }

    /**
     * Search模式，用于对用户查询词分词
     *
     * @param sentence
     * @return
     */
    public static List<SegToken> segmentBySearch(String sentence) {
        JiebaSegmenter segmenter = new JiebaSegmenter();
        return segmenter.process(sentence, JiebaSegmenter.SegMode.SEARCH);
    }

    /**
     * 默认为Search模式
     *
     * @param sentence
     * @return
     */
    public static List<String> segmenterDefault(String sentence) {
        JiebaSegmenter segmenter = new JiebaSegmenter();
        return segmenter.sentenceProcess(sentence);
    }

    /**
     * 1.0.3版本才有提取关键词功能
     * 基于TF-IDF算法的带权关键词抽取
     *
     * @param content 要提取关键词的内容
     * @param topN    要提取关键词的词数
     * @return
     */
    public static List<Keyword> getKeyword(String content, int topN) {
        TFIDFAnalyzer tfidfAnalyzer = new TFIDFAnalyzer();
        return tfidfAnalyzer.analyze(content, topN);
    }

}