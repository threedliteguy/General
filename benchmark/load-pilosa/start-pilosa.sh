docker run --env PILOSA_METRIC_DIAGNOSTICS=false -d --rm --name pilosa -p 10101:10101 pilosa/pilosa:latest server --bind 0.0.0.0:10101

