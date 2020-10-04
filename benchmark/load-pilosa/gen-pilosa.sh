for i in {1..50}
do
 echo $i	
 awk  -v i=$i -F"," '{n=i+1}{ print $n "," $1 }' data.csv > c$i.csv
done

