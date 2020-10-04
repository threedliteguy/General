import cudf, time, random
from blazingsql import BlazingContext

# cat header.csv data.csv >hdata.csv

bc = BlazingContext()
print('creating table')
bc.create_table('test','hdata.csv')

for it in range(0,3):
 print('query')
 t = time.time()
 r = bc.sql("""select c0 from test where 
        c1="""+str(random.randint(0,100))+""" and c2 in (3,21,44) and c3<6 and c4=5 
        and c5=8 or c6=4 or c7=3 or c8=2 or c9=1 or c10<5 
        and c13=1 and c14=1
        """)
 print(r)
 print('time =',time.time()-t)
