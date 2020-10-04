curl localhost:10101/index/test/query \
     -X POST \
     -d 'Count(Union(
        Intersect(
	Union(Row(c41=1),Row(c1=1)), 
	Union(Row(c42=2),Row(c1=1)),
            Row(c43=3)
	    ),
        Intersect(
	Union(Row(c44=4),Row(c1=1)), 
	Union(Row(c45=5),Row(c1=1)),
	Union(Row(c46=6),Row(c2=1))
	    ),
        Intersect(
	Union(Row(c47=7),Row(c1=1)), 
            Row(c48=8),
            Row(c49=9)
	    ),
        Intersect(
	Union(Row(c40=10),Row(c1=1)), 
            Row(c41=11),
            Row(c42=12)
	    ),
	Union(Row(c33=13),Row(c1=1),Row(c1=2),Row(c49=1),Row(c48=1),Row(c47=1),Row(c46=1),Row(c45=1),Row(c44=1),Row(c43=1),Row(c42=1),
	Row(c2=1),Row(c2=2),Row(c3=3),Row(c4=4)) 
	))'

