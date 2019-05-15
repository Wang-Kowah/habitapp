#coding=utf-8
# 天关键词
import jieba.analyse as analyse
import pymysql
import time

db = pymysql.connect("localhost", "root", '123456', "db_habit")

cursor = db.cursor()

sql = 'select uid  from t_user_list'

# 获取昨天的开始时间戳和结束时间戳
tstr = time.strftime("%Y-%m-%d", time.localtime())
etime = int(time.mktime(time.strptime(tstr, '%Y-%m-%d')))
stime = etime - 86400 + 1

# 得到昨天的日期，格式20190325
struct_time = time.localtime(stime)
dstr = time.strftime("%Y%m%d", struct_time)
print(dstr)
try:
    cursor.execute(sql)
    # results格式是Tuple
    results = cursor.fetchall()
    for uid in results:
        # print(uid,uid[0])
        sql = "select content from t_note_list where uid=" + str(uid[0]) + " and  create_time between " + str(
            stime) + " and " + str(etime) + " and type = 0"

        # print(sql)
        cursor.execute(sql)
        note_resultes = cursor.fetchall()

        # 昨天没有总结就跳过该用户
        # print(len(note_resultes))
        if len(note_resultes) == 0:
            continue
        textlist = []
        for note in note_resultes:
            # print(note)
            textlist.append(note[0])
        wordlist = analyse.extract_tags('.'.join(textlist), 5)
        wordstr = ','.join(wordlist)
        if len(wordstr) > 50:  # 天最大长度50
            wordstr = wordstr[0:50]
        print(wordstr)
        sql = "INSERT INTO t_keyword_day(uid,keywords,date) VALUES(%s,'%s','%s')" % (uid[0], wordstr, dstr)  # 解决单双引号
        # print(sql)
        cursor.execute(sql)
        db.commit()
        print('-------------------------------------------------------------------------------')


except ValueError:
    print('Error')

db.close()
