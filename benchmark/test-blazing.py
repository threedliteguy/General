import cudf
from random import seed
from random import random
from random import shuffle 
from time import time
import pandas as pd


reload_postgres = True
#reload_postgres = False 


print('Connecting to postgres')
import psycopg2
conn = None
try:
    conn = psycopg2.connect("dbname='postgres' user='user' host='localhost' password='user'")
except:
    print("Unable to connect to the database")
cur = conn.cursor()

print('Connecting to blazingsql')
from blazingsql import BlazingContext
bc = BlazingContext()

print('Create test data')

n = 50000000
print('n',n)

keys = [i for i in range(n)]
seed(1)
f1 = [i for i in range(n)]
shuffle(f1)
f2 = [i for i in range(n)]
shuffle(f2)
f3 = [i for i in range(n)]
shuffle(f3)

if reload_postgres:
    print('Load data to postgres')
    seq = list(zip(keys,f1,f2,f3))
    cur.execute("""drop table if exists test""")
    cur.execute("""create table test(key int, f1 int, f2 int, f3 int)""")
    cur.executemany(""" insert into test (key,f1,f2,f3) values (%s,%s,%s,%s)""",seq)
    conn.commit()
    seq = None

print('Load data into blazingsql')
df = cudf.from_pandas(pd.DataFrame({'key':keys,'f1':f1,'f2':f2,'f3':f3}))
bc.create_table('test', df)


sql = 'SELECT sum(f1) s1, avg(f2) a2, count(f3) c3 FROM test where f3 > 10'

print('Starting test postgres')
for i in range(0,10):
   t0 = time()
   cur.execute(sql)
   a = cur.fetchall()
   print(a[0])
   print('Time=', time()-t0)


print('Starting test blazing sql')
for i in range(0,10):
   t0 = time()
   a = bc.sql(sql)
   print(a)
   print('Time=', time()-t0)


sql = 'SELECT avg(f1) a1, sum(f2) s2, count(f3) c3 FROM test where f3 > 10'

print('Starting test blazing sql - test 2')
for i in range(0,10):
   t0 = time()
   a = bc.sql(sql)
   print(a)
   print('Time=', time()-t0)

sql = 'SELECT max(f1) m1, count(f2) c2, sum(f3) f3 FROM test where f3 > 10'

print('Starting test blazing sql - test 3')
for i in range(0,10):
   t0 = time()
   a = bc.sql(sql)
   print(a)
   print('Time=', time()-t0)


sql = """SELECT max(f1) m1, count(f2) c2, sum(f3) f3 FROM 
(select * from test 
union all select * from test 
union all select * from test) a  
where f3 > 10"""

print('Starting test blazing sql - test 4')
for i in range(0,10):
   t0 = time()
   a = bc.sql(sql)
   print(a)
   print('Time=', time()-t0)






print('Done.')
