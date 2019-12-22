import cudf 

iters=20

init=[1,0,1,0,-1,0,-1,0,1,0,1,0]

size=100

maxn = size*size*size

# vertices
n=range(maxn)
v=[]
for d in range(0,12):
   v.append([0]*maxn)


center=int(maxn/2)
v[0][center]=init[0]
v[1][center]=init[1]
v[2][center]=init[2]
v[3][center]=init[3]
v[4][center]=init[4]
v[5][center]=init[5]
v[6][center]=init[6]
v[7][center]=init[7]
v[8][center]=init[8]
v[9][center]=init[9]
v[10][center]=init[10]
v[11][center]=init[11]

print("creating verts")
verts = cudf.DataFrame(
{
'id':n,
'v0r':v[0],
'v0i':v[1],
'v1r':v[2],
'v1i':v[3],
'v2r':v[4],
'v2i':v[5],
'v3r':v[6],
'v3i':v[7],
'v4r':v[8],
'v4i':v[9],
'v5r':v[10],
'v5i':v[11]
}
)

print("done creating verts")

#print(verts)

# edges
src=[]
dst=[]
m0=[]
m1=[]
m2=[]
m3=[]
m4=[]
m5=[]

for n in range(maxn):
   n0=n-1
   if n0>=0:
      src.append(n) 
      dst.append(n0)
      m0.append(1)
      m1.append(0)
      m2.append(0)
      m3.append(0)
      m4.append(0)
      m5.append(0)


   n1=n+1
   if n1<maxn: 
      src.append(n) 
      dst.append(n1)
      m0.append(0)
      m1.append(1)
      m2.append(0)
      m3.append(0)
      m4.append(0)
      m5.append(0)
       
   n2=n-size
   if n2>=0: 
      src.append(n) 
      dst.append(n2)
      m0.append(0)
      m1.append(0)
      m2.append(1)
      m3.append(0)
      m4.append(0)
      m5.append(0)

   n3=n+size
   if n3<maxn: 
      src.append(n) 
      dst.append(n3)
      m0.append(0)
      m1.append(0)
      m2.append(0)
      m3.append(1)
      m4.append(0)
      m5.append(0)

   n4=n-size*size
   if n4>=0: 
      src.append(n) 
      dst.append(n4)
      m0.append(0)
      m1.append(0)
      m2.append(0)
      m3.append(0)
      m4.append(1)
      m5.append(0)

   n5=n+size*size
   if n5<maxn: 
      src.append(n) 
      dst.append(n5)
      m0.append(0)
      m1.append(0)
      m2.append(0)
      m3.append(0)
      m4.append(0)
      m5.append(1)




print("creating edges")

edges = cudf.DataFrame(
{
'id':src,
'dst':dst,
'm0':m0,
'm1':m1,
'm2':m2,
'm3':m3,
'm4':m4,
'm5':m5
}
)


print("done creating edges")

#print(edges)


# Start loop

for iter in range(iters):

   m = verts.merge(edges, on=['id'], how='inner')

   #print(len(m))

   m['p0r']=(-2 * m.v0r*m.m0 + m.v1r*m.m1 + m.v2r*m.m2 + m.v3r*m.m3 + m.v4r*m.m4 + m.v5r*m.m5 )/3 
   m['p1r']=(m.v0r*m.m0 + -2 * m.v1r*m.m1 + m.v2r*m.m2 + m.v3r*m.m3 + m.v4r*m.m4 + m.v5r*m.m5 )/3 
   m['p2r']=(m.v0r*m.m0 + m.v1r*m.m1 + -2 * m.v2r*m.m2 + m.v3r*m.m3 + m.v4r*m.m4 + m.v5r*m.m5 )/3 
   m['p3r']=(m.v0r*m.m0 + m.v1r*m.m1 + m.v2r*m.m2 + -2 * m.v3r*m.m3 + m.v4r*m.m4 + m.v5r*m.m5 )/3
   m['p4r']=(m.v0r*m.m0 + m.v1r*m.m1 + m.v2r*m.m2 + m.v3r*m.m3 + -2 * m.v4r*m.m4 + m.v5r*m.m5 )/3 
   m['p5r']=(m.v0r*m.m0 + m.v1r*m.m1 + m.v2r*m.m2 + m.v3r*m.m3 + m.v4r*m.m4 + -2 * m.v5r*m.m5 )/3

   s = m.groupby('dst').sum().reset_index()

   #print(s)

   s['id'] = s.dst
   s['v0r'] = s.p0r 
   s['v1r'] = s.p1r 
   s['v2r'] = s.p2r 
   s['v3r'] = s.p3r 
   s['v4r'] = s.p4r 
   s['v5r'] = s.p5r 
   s['norm'] = s.p0r+s.p1r+s.p2r+s.p3r+s.p4r+s.p5r
   verts = s.loc[:,['id','v0r','v1r','v2r','v3r','v4r','v5r']]

   n = s[s.norm > 0].loc[:,'norm'] 
   print(n)
   print(iter,'-----------------------------------------------')



