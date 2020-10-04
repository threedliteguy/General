import time
import os,random

rowcount = 40
colcount = 50
block=1000000

# Write header
header = ''
for i in range(0,colcount):
    if i > 0:
      header = header + ','
    header = header + '"c'+str(i)+'"'
header = header + '\n'
try:
  os.remove("header.csv")
except:
  pass    
f = open('header.csv','w')
f.write(header)

# Write data
try:
  os.remove("data.csv")
except:
  pass    
f = open('data.csv','w')
for i in range(rowcount):
  print(i)   
  rows = []
  for j in range(block):
    row = str(i*block+j)
    for k in range(colcount):
      row = row + ',' + str(random.randint(0,100))
    rows.append(row)
  rows = '\n'.join(rows)+'\n'
  f.write(rows)
f.close()
 
