# 周关键词
import jieba.analyse as analyse
import pymysql
import time

db = pymysql.connect("localhost", "root", '123456', "db_habit")

cursor = db.cursor()

sql = 'select uid  from t_user_list'

# 获取上周开始时间戳和结束时间戳
tstr = time.strftime("%Y-%m-%d", time.localtime())
etime = int(time.mktime(time.strptime(tstr, '%Y-%m-%d')))
# etime = int(time.mktime(time.strptime('2019-04-01', '%Y-%m-%d')))   #具体日期
stime = etime - 86400 * 7 + 1

stime_sunday = etime - 86400 + 1
# 得到昨天的日期，格式20190325
struct_time = time.localtime(stime_sunday)
dstr = time.strftime("%Y%m%d", struct_time)
print(dstr)
try:
    cursor.execute(sql)
    # results格式是Tuple
    results = cursor.fetchall()
    for uid in results:
        # print(uid,uid[0])
        sql = "select content from t_note_list where uid=" + str(uid[0]) + " and  create_time between " + str(
            stime) + " and " + str(etime)

        # print(sql)
        cursor.execute(sql)
        note_resultes = cursor.fetchall()

        # 昨天没有总结就跳过该用户
        print(len(note_resultes))
        if len(note_resultes) == 0:
            continue
        textlist = []
        for note in note_resultes:
            print(note)
            textlist.append(note[0])
        wordlist = analyse.extract_tags('.'.join(textlist), 10)
        wordstr = ','.join(wordlist)
        if len(wordstr) > 100:  # 周最大长度100
            wordstr = wordstr[0:100]
        print('-------------------------------------------------------------------------------')
        # print(wordstr)
        # sql ='INSERT INTO t_keyword_day(uid,keywords,date)values('+str(uid[0])+',"'+wordstr+'",'+'20190322)'
        sql = "INSERT INTO t_keyword_period(uid,type,keywords,date) VALUES(%s,%s,'%s','%s')" % (
        uid[0], 1, wordstr, dstr)  # 解决单双引号
        print(sql)
        cursor.execute(sql)
        db.commit()
        print('-------------------------------------------------------------------------------')


except ValueError:
    print('Error')

sql = 'select *  from t_keyword_period where type =1'
cursor.execute(sql)
results = cursor.fetchall()
# print(results)
for r in results:
    print(r)
# print(results)

db.close()
