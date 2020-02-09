# pip install pyspark

from pyspark.ml.clustering import LDA
from pyspark.sql import SparkSession, SQLContext
from pyspark.conf import SparkConf
from pyspark.context import SparkContext
from pyspark.ml.feature import CountVectorizer
from pyspark.ml.feature import HashingTF, IDF, RegexTokenizer
import re
from itertools import izip, count

filelocation = '/home/user/data'
topiccount=100
topicwordcount=10
maxiter=10
vocab_size=10000

#nltk stopwords
stopwords = ['www', 'https', 'b', 'one', 'ourselves', 'hers', 'between', 'yourself', 'but', 'again', 'there', 'about', 'once', 'during', 'out', 'very', 'having', 'with', 'they', 'own', 'an', 'be', 'some', 'for', 'do', 'its', 'yours', 'such', 'into', 'of', 'most', 'itself', 'other', 'off', 'is', 's', 'am', 'or', 'who', 'as', 'from', 'him', 'each', 'the', 'themselves', 'until', 'below', 'are', 'we', 'these', 'your', 'his', 'through', 'don', 'nor', 'me', 'were', 'her', 'more', 'himself', 'this', 'down', 'should', 'our', 'their', 'while', 'above', 'both', 'up', 'to', 'ours', 'had', 'she', 'all', 'no', 'when', 'at', 'any', 'before', 'them', 'same', 'and', 'been', 'have', 'in', 'will', 'on', 'does', 'yourselves', 'then', 'that', 'because', 'what', 'over', 'why', 'so', 'can', 'did', 'not', 'now', 'under', 'he', 'you', 'herself', 'has', 'just', 'where', 'too', 'only', 'myself', 'which', 'those', 'i', 'after', 'few', 'whom', 't', 'being', 'if', 'theirs', 'my', 'against', 'a', 'by', 'doing', 'it', 'how', 'further', 'was', 'here', 'than']


MAX_MEMORY = "20g"

spark = SparkSession \
    .builder \
    .appName("topics") \
    .config("spark.executor.memory", MAX_MEMORY) \
    .config("spark.driver.memory", MAX_MEMORY) \
    .getOrCreate()
sc = spark.sparkContext
sql = SQLContext(sc)


documents = sc.wholeTextFiles(filelocation).cache()
documents = documents.toDF(["id","text"])
def f0(r):
    res = []
    rid = r['id']
    aid = rid[rid.rfind('/')+1:]
    docs = r['text'].split('Daf ')
    for doc in docs:
        i = doc.find('\n')
        pid = aid + "-" + doc[:i]
        txt = doc[i+1:]
        res.append([pid,txt])
    return res 
documents = documents.rdd.flatMap(f0).toDF(['id','text'])
#documents.select(['id']).show(10, False)


tokenizer = RegexTokenizer(inputCol="text", outputCol="words", pattern="""\\W""")
words = tokenizer.transform(documents)
def f1(r):
    words = list(filter(lambda x:x not in stopwords, r['words']))
    return [r['id'],words]
words = words.rdd.map(f1).toDF(['id','words'])    

cv = CountVectorizer(inputCol="words", outputCol="rawFeatures", vocabSize=vocab_size, minDF=0.001)
model = cv.fit(words)
data = model.transform(words)
vocab=model.vocabulary


idf = IDF().setInputCol("rawFeatures").setOutputCol("features")
idfModel = idf.fit(data)
data = idfModel.transform(data)


lda = LDA(k=topiccount, maxIter=maxiter)
model = lda.fit(data)
#print("Likelihood: " + str(model.logLikelihood(data)))
#print("Perplexity: " + str(model.logPerplexity(data)))

topics = model.describeTopics(topicwordcount)
def f1(r):
    topicno=str(r['topic'])
    return [topicno, ", ".join([vocab[i] for i in r['termIndices']])]
topicwords = topics.rdd.map(f1)
topicwords = topicwords.toDF(['topic','topic_words'])
topicwords.show(1000,False)

transformed = model.transform(data)
#transformed.show(10,False)


def f2(r):
    topics=[]
    a=list(r['topicDistribution'])
    topic=a.index(max(a))
    if int(a[topic]*100) == 0:
        topic = -1
    a = sorted(izip(a, count()), reverse = True)[:3]
    for w,i in a:
        topics.append(str(i)+":"+str(int(w*1000)/10.0)+"%")
    topicstr=", ".join(topics)
    return [r['id'],topic,topicstr]

final=transformed.rdd.map(f2)
final=final.toDF(["id","topic","topics"])
finalwords=final.join(topicwords, on=['topic'], how='left')
finalwords.select(["id","topic","topics","topic_words"]).collect().sort()
finalwords.show(100000,False)


