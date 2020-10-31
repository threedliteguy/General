
print('curl localhost:10101/index/test/query -X POST  -d \'Count(Union(')
for i in range(0,50):
    for j in range(0,100):
	 print('Row(c'+str(i)+'=a'+str(j)+'),')
	 
print('))\'')	

