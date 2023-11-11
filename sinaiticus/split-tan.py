import sys,os

input='Song'
book='SNG'

with open(input+'.txt') as f:
 lines=f.readlines()

def lpad(s):
  while len(s)<3:
     s = '0' + s
  return s

def wrt(book,ch,out):
    if len(out)>0:
      with open('tan-'+book.upper()+'_'+lpad(str(ch))+'.txt','w') as o:
        o.write(''.join(out))

ch=0
out=[]
for l in lines:
  if 'Chapter' in l:
    wrt(book,ch,out)
    out=[]
    ch=ch+1
  if not 'xxxx' in l:
    out.append(l) 

wrt(book,ch,out)


