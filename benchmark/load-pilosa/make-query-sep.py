
for i in range(1,51):
    for j in range(0,100):
         print('echo '+str(i)+' '+str(j)+'; curl localhost:10101/index/test/query -X POST  -d \'Count(Row(c'+str(i)+'=a'+str(j)+'))\'')	

