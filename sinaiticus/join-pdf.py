import datetime, sys
import numpy as np
import matplotlib.pyplot as plt
from matplotlib import rc
from matplotlib import font_manager as fm
import matplotlib
from pathlib import Path
import sys,os
from matplotlib.backends.backend_pgf import PdfPages
from matplotlib.backends.backend_pgf import FigureCanvasPgf
matplotlib.backend_bases.register_backend('pdf', FigureCanvasPgf)
import matplotlib.font_manager as font_manager
from bidi import algorithm as bidialg
import arabic_reshaper

# sudo apt-get install texlive-latex-extra texlive-fonts-recommended dvipng cm-super texlive-lang-greek texlive-xetex texlive-luatex  python3-bidialg python3-arabic-reshaper

# Fonts 
# Hebrew: https://github.com/aharonium/fonts/blob/master/Fonts/Hebrew%20Letters%20with%20Vowels%20and%20Cantillation/Fonts%20for%20scholars%20(OFL)/Cardo/Cardo-regular_104s.ttf


home = '/home/user/Downloads'

tan_path=home+'/tanach'  # https://www.tanach.us/Tanach.xml
can_path=home+'/eng-web' # https://ebible.org/web/
csi_path=home+'/split'   # https://www.codexsinaiticus.org/en/project/transcription_download.aspx
lxx_path=home+'/lxx'     # https://ebible.org/eng-lxx2012/

filt = 'SNG'

tan_list = sorted(list(filter(lambda x:x.startswith('tan') and x.endswith('.txt') and filt in x, os.listdir(tan_path))))
csi_list = sorted(list(filter(lambda x:x.startswith('csi') and x.endswith('.txt') and filt in x, os.listdir(csi_path))))
can_list = sorted(list(filter(lambda x:x.startswith('eng') and x.endswith('.txt') and filt in x, os.listdir(can_path))))
lxx_list = sorted(list(filter(lambda x:x.startswith('eng') and x.endswith('.txt') and filt in x, os.listdir(lxx_path))))

#print(tan_list)
#print(csi_list)
#print(can_list)
#print(lxx_list)

def clean(s):
  s=s.replace('\u202a','')
  s=s.replace('\u202b','')
  s=s.replace('\u202c','')
  s=s.replace('\xa0','')
  return s

a_chapters = {'0':{}}
b_chapters = {'0':{}}
c_chapters = {'0':{}}
d_chapters = {'0':{}}
for i,ch in enumerate(tan_list):
  with open(tan_path+'/'+tan_list[i] ) as f:
    l=f.readlines()
    vs={}
    for v,vt in enumerate(l):
      vs[str(v+1)]=clean(vt).replace('\n','').replace('׃',' ')
    a_chapters[str(i+1)]=vs
  with open(csi_path+'/'+csi_list[i]) as f:
    l=f.readlines()
    l.pop(0)
    vs={}
    for v,vt in enumerate(l):
      vs[str(v+1)]=vt.replace('\n','') 
    b_chapters[str(i+1)]=vs
  with open(can_path+'/'+can_list[i]) as f:
    l=f.readlines()
    l.pop(0)
    l.pop(0)
    vs={}
    for v,vt in enumerate(l):
      vs[str(v+1)]=vt.replace('\n','').replace('\ufeff','') 
    c_chapters[str(i+1)]=vs
  with open(lxx_path+'/'+lxx_list[i]) as f:
    l=f.readlines()
    l.pop(0)
    l.pop(0)
    vs={}
    for v,vt in enumerate(l):
      vs[str(v+1)]=vt.replace('\n','').replace('\ufeff','') 
    d_chapters[str(i+1)]=vs
    
#print(a_chapters['1']['1'])
#print(b_chapters['1'])
#print(c_chapters['1'])
#print(d_chapters['1'])
#sys.exit(0)

new_chaps = []
for ch in sorted(list(a_chapters.keys()),key=int):
   a_vs = a_chapters[str(ch)]
   b_vs = b_chapters[str(ch)]
   c_vs = c_chapters[str(ch)]
   d_vs = d_chapters[str(ch)]
   if len(a_vs.keys()) != len(b_vs.keys()) or len(a_vs.keys()) != len(c_vs.keys()) or len(a_vs.keys()) != len(d_vs.keys()):
     print('Chapter ',ch)
     print(a_vs.keys())
     print(b_vs.keys())
     print(c_vs.keys())
     print(d_vs.keys())
   vs = sorted(list(set(list(a_vs.keys()) + list(b_vs.keys()) + list(c_vs.keys()) + list(d_vs.keys()))),key=int)
   new_chaps.append(vs)

def wraplines(line,width):

     start=r'\textcolor{gray}{'
     start1=r'\textcolor{gray}{['
     end=r'}'
     end1=r']}'

     char_count = 0
     new_words = []
     isin = False
     new_lines = []

     wds = line.split(' ')
     for ind, word in enumerate(wds):
           next = ''
           if ind < len(wds)-1:
              next = wds[ind+1]
           char_count = char_count + len(word) + 1

           if word != ']'  and word != '·' and char_count > width:
              char_count = len(word)
              if isin:
                 new_words.append(end)
              new_lines.append(' '.join(new_words))
              if isin:
                new_words = [start]
              else:
                new_words = []

           if word == '[':
              new_words.append(start1)
              isin = True
           elif word == ']':
              new_words.append(end1)
              isin = False
           else:
              new_words.append(word)

     if len(new_words)>0:
         new_lines.append(' '.join(new_words))
     new_words = []
     char_count = 0
     #new_lines.append('')
     return new_lines
   
def printpdf(pages):

 matplotlib.use('pgf')
 plt.rcParams['pgf.texsystem']='lualatex'

 #flist = font_manager.ttflist
 #for f in flist:
 # print(f.name)


 hpath=Path('/home/user/pdf/sinai.ttf')
 font_manager.fontManager.addfont(hpath)
 f = font_manager.findfont("sinai")

 hpath=Path('/home/user/pdf/Cardo-regular_104s.ttf')
 font_manager.fontManager.addfont(hpath)
 f = font_manager.findfont("Cardo")

 plt.rcParams['font.family'] = ['serif']
 plt.rcParams['font.serif'] = ['sinai']
 plt.rcParams['font.sans-serif'] = ['Cardo']

 plt.rcParams['pgf.preamble'] = r'\usepackage{xcolor}\definecolor{gray}{HTML}{949698}'
 plt.rcParams['pgf.rcfonts'] = True

 with PdfPages('multipage_pdf.pdf') as pdf:
  left = True
  for page in pages: 
    fig = plt.figure()
    fig.set_size_inches([8.5,11])
    plt.axis('off')
    if left:
      plt.text(0  , 1, bidialg.get_display(arabic_reshaper.reshape('\n'.join(page[0]))), fontfamily='sans-serif', size=12, clip_on=False, verticalalignment='top')
      plt.text(0.5, 1, '\n'.join(page[1]), size=12, clip_on=False, verticalalignment='top')
    else:
      plt.text(0  , 1, '\n'.join(page[0]), size=12, clip_on=False, verticalalignment='top')
      plt.text(0.5, 1, '\n'.join(page[1]), size=12, clip_on=False, verticalalignment='top')
    pdf.savefig(fig, orientation='portrait')
    left = not left

 plt.close()
 

pages = []
for chapter, new_chap in enumerate(new_chaps):
   if chapter == 0:
     a_acc = ['']
     b_acc = ['']
     c_acc = ['']
     d_acc = ['']
   else:
     a_acc = ['','']
     b_acc = ['','']
     c_acc = ['Chapter '+str(chapter),'']
     d_acc = ['Chapter '+str(chapter),'']
   for n,v in enumerate(new_chap):
     a = a_chapters[str(chapter)].get(str(v),'-')
     b = b_chapters[str(chapter)].get(str(v),'-')
     c = c_chapters[str(chapter)].get(str(v),'-')
     d = d_chapters[str(chapter)].get(str(v),'-')
     if chapter > 0:
        a = str(n+1)+' '+a 
        b = str(n+1)+' '+b
        c = str(n+1)+' '+c
        d = str(n+1)+' '+d
     a = wraplines(a,40) 
     b = wraplines(b,20)
     c = wraplines(c,35)
     d = wraplines(d,35)
     a.append('')
     b.append('')
     c.append('')
     d.append('')
     if max(len(a)+len(a_acc), max(len(b)+len(b_acc), max(len(c)+len(c_acc), len(d)+len(d_acc)))) > 45:
        pages.append([a_acc,b_acc])
        pages.append([c_acc,d_acc])
        a_acc = []
        b_acc = []
        c_acc = []
        d_acc = []
     a_acc.extend(a)
     b_acc.extend(b)
     c_acc.extend(c)
     d_acc.extend(d)
   if (len(a_acc)>0):
     pages.append([a_acc,b_acc])
     pages.append([c_acc,d_acc])

printpdf(pages)




