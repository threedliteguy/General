for i in {1..50}
do
  docker exec -it pilosa /pilosa import -f c$i  -i test /c$i.csv
done

