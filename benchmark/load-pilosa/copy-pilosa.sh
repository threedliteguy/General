for f in c*.csv; do docker cp $f pilosa:/ ; done

