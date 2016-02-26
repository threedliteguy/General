# General
Grover.scala outputs data in text format.  grover.png was created by pointing Paul Brunt's D3 surface plot at the data.  http://bl.ocks.org/supereggbert/aff58196188816576af0


See threedliteguy/grover project for a complete runnable maven version:

To build and run:

mvn package

java -DIMPL_TYPE=spark -jar target/grover-1.0-SNAPSHOT-allinone.jar

or

java -DIMPL_TYPE=flink -jar target/grover-1.0-SNAPSHOT-allinone.jar

Point browser to:  http://localhost:8080/html/grover.html
Click button and wait a couple seconds.
