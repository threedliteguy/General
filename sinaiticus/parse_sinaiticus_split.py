# Code placed in public domain by author Dan Meany 

import xml.etree.ElementTree as ET
from xml.etree.ElementTree import Element

dict = {}
with open('books-map.txt') as f:
  for line in f:
    a = line.split(',')
    if len(a)>1:
        dict[a[0]] = a[1].strip()
print(dict)

def parse(f):

    tree = ET.parse(f)
  
    root = tree.getroot()

    # find first reading in apparatus with content, and use that only, under app       
    for p in root.findall('.//'):
      for app in p.findall('app'):
         flg=0
         for rdg in app.findall('rdg'):
              flg = flg + 1
              corr = rdg.attrib.get('n')
              sep = Element("w")
              sep.text = ''
              if flg > 1:
                 sep.text = '|'
              if corr != None:
                 sep.text = sep.text + corr + ': '
              app.append(sep)
              ws = rdg.findall('w')
              if len(ws)>0 and len(parseword(ws[0]))>0:
                for w in ws:
                   app.append(w)
              app.remove(rdg)
    
    for p in root.findall('.//'):
      for i,c in enumerate(p):
         if c.tag == 'app':
            e = Element("w")
            e.text = "]"
            p.insert(i,e)
            for n in c.findall('w')[::-1]:
               p.insert(i,n)
            e = Element("w")
            e.text = "["
            p.insert(i,e)
            p.remove(c)


    for wit in root.findall('.//div'):
      if wit.attrib['type'] == 'wit':
        books = []
        for book in wit.findall('div'):
          if book.attrib['type'] == 'book':
            chapters = []
            chapters.append([[str(book.attrib['title'])]])
            for chapter in book.findall('div'):
              if chapter.attrib['type'] == 'chapter':
                verses = []
                cno = str(chapter.attrib['n'])
                while len(cno)<3:
                   cno = '0'+cno
                verses.append([cno])
                for verse in chapter.findall('ab'):
                   if verse.attrib['id'][0:2] == 'V-':
                     words = []
                     words.append(str(verse.attrib['n']))
                     for word in verse.findall('w'): 
                        w = parseword(word)
                        if len(w)>0:
                           words.append(w)
                     verses.append(words)
              chapters.append(verses)
            books.append(chapters)
        return books


def parseword(word):
                parts = []      
                if word.text != None:
                  parts.append(word.text)  
                for c in word:
                  if c.attrib.get('rend') == 'ol2':
                    parts.append('_')
                  if c.text != None:
                    parts.append(c.text)
                  if c.attrib.get('rend') == 'ol2':
                    parts.append('_')
                  if c.tail != None:
                    parts.append(c.tail)
                return '-'.join(parts).replace('\n','').replace('-','').upper()


f = 'FINAL_TRANSCRIPTION_version104.xml'
#f = 'test.xml'
p = parse(f)
for b in p[1:]:
  for c in b[2:]:
    out = '\n'.join([' '.join(v) for v in c])
    with open('csi_'+dict.get(b[0][0][0],'~')+'_'+c[0][0]+'.txt','w') as f:
       f.write(out)
    


