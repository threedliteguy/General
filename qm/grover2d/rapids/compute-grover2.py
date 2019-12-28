import cudf 
from flask import Flask, request, json
from numpy import array
from flask_cors import CORS



# Example request:  {"size":100,"iterations":20,"initialVector":[1,0,1,0,-1,0,-1,0]}
def calc(r):

	print("Received: ",r)
 
	size = r['size']
	iterations = r['iterations']
	init = r['initialVector']   
       
 
	maxn = size*size

	# vertices
	n=range(maxn)
	v=[]
	for d in range(0,8):
	   v.append([0]*maxn)


	cp=int(size/2)
	center=cp+cp*size
	v[0][center]=init[0]
	v[1][center]=init[1]
	v[2][center]=init[2]
	v[3][center]=init[3]
	v[4][center]=init[4]
	v[5][center]=init[5]
	v[6][center]=init[6]
	v[7][center]=init[7]

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

	for n in range(maxn):

	   x=n%size
	   y=int(n/size)%size
 
	   if x>0:
	      n0=n-1
	      src.append(n) 
	      dst.append(n0)
	      m0.append(1)
	      m1.append(0)
	      m2.append(0)
	      m3.append(0)


	   if x<size-1: 
	      n1=n+1
	      src.append(n) 
	      dst.append(n1)
	      m0.append(0)
	      m1.append(1)
	      m2.append(0)
	      m3.append(0)
	       
	   if y>0: 
	      n2=n-size
	      src.append(n) 
	      dst.append(n2)
	      m0.append(0)
	      m1.append(0)
	      m2.append(1)
	      m3.append(0)

	   if y<size-1: 
	      n3=n+size
	      src.append(n) 
	      dst.append(n3)
	      m0.append(0)
	      m1.append(0)
	      m2.append(0)
	      m3.append(1)



	print("len(edges)=",len(src))
	print("creating edges")

	edges = cudf.DataFrame(
	{
	'id':src,
	'dst':dst,
	'm0':m0,
	'm1':m1,
	'm2':m2,
	'm3':m3
	}
	)


	print("done creating edges")

	#print(edges)


	# Start loop

	for iter in range(iterations):

	   m = verts.merge(edges, on=['id'], how='inner')

	   #print(len(m))

	   m['p0r']=(-1 * m.v0r*m.m0 + m.v1r*m.m1 + m.v2r*m.m2 + m.v3r*m.m3 )/2 
	   m['p1r']=(m.v0r*m.m0 + -1 * m.v1r*m.m1 + m.v2r*m.m2 + m.v3r*m.m3 )/2 
	   m['p2r']=(m.v0r*m.m0 + m.v1r*m.m1 + -1 * m.v2r*m.m2 + m.v3r*m.m3 )/2 
	   m['p3r']=(m.v0r*m.m0 + m.v1r*m.m1 + m.v2r*m.m2 + -1 * m.v3r*m.m3 )/2
	   m['p0i']=(-1 * m.v0i*m.m0 + m.v1i*m.m1 + m.v2i*m.m2 + m.v3i*m.m3 )/2 
	   m['p1i']=(m.v0i*m.m0 + -1 * m.v1i*m.m1 + m.v2i*m.m2 + m.v3i*m.m3 )/2 
	   m['p2i']=(m.v0i*m.m0 + m.v1i*m.m1 + -1 * m.v2i*m.m2 + m.v3i*m.m3 )/2 
	   m['p3i']=(m.v0i*m.m0 + m.v1i*m.m1 + m.v2i*m.m2 + -1 * m.v3i*m.m3 )/2

	   s = m.groupby('dst').sum().reset_index()

	   #print(s)

	   s['id'] = s.dst
	   s['v0r'] = s.p0r 
	   s['v1r'] = s.p1r 
	   s['v2r'] = s.p2r 
	   s['v3r'] = s.p3r 
	   s['v0i'] = s.p0i 
	   s['v1i'] = s.p1i 
	   s['v2i'] = s.p2i 
	   s['v3i'] = s.p3i 
	   verts = s.loc[:,['id','v0r','v1r','v2r','v3r','v0i','v1i','v2i','v3i']]

	   #print(iter,'-----------------------------------------------')

	# c.conjugate * c = a**2 + b**2  
	verts['norm'] = ( verts.v0r**2 + verts.v1r**2 + verts.v2r**2 + verts.v3r**2 + verts.v0i**2 + verts.v1i**2 + verts.v2i**2 + verts.v3i**2 )  * -3000
	n = verts.loc[:,'norm'].to_pandas().values
	a=array(n).reshape(size,size).tolist()
	
	#print(a)
	print("calc done")
	return a
 




app = Flask(__name__)
CORS(app)

@app.route('/')
def hello():
    return "CuDF Compute Server"

@app.route('/test/<s>', methods=['GET'])
def convert(s):
    result = calc(json.loads(s))
    return json.dumps(result)

@app.route('/compute', methods=['POST'])
def compute():
    r = request.json
    print(r)
    c = r['initialVector'].split(',')
    c = list(map(lambda x: complex(x.replace(' ','').replace('i','j')),c))
    r['initialVector'] = [c[0].real,c[0].imag,c[1].real,c[1].imag,c[2].real,c[2].imag,c[3].real,c[3].imag] 
    result = calc(r)
    s = '{"result":'+str(result)+'}'
    #print(s)
    return s


if __name__ == '__main__':
    app.run(host="localhost", port=8080)

