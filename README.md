# vertx-reproducer

It`s a two simple applications for help to reproduce bug. <br>
<b>simple-spring-boot-node</b> - genarate random data and published, also hase handler for check is node still live<br>
<b>monitoring</b> - contains periodic timer which sends every 2 sec event to check is node still live. 
and simple html page for help monitoring status at port 8082<br><br>

Steps.
 1. Build all mvn clean install.
 2. Run two nodes of  simple-spring-boot-node : <br>
  <b>java -jar java -jar target/vertx-spring-boot-service-1.0-SNAPSHOT.jar channel_1</b> <br>
  <b>java -jar java -jar target/vertx-spring-boot-service-1.0-SNAPSHOT.jar channel_2</b>
 3. Run one monitoring <br>
  <b>java -jar target/monitoring-1.0-SNAPSHOT.jar channel_2 channel_1</b>

You can check simple html page at localhost: 8082. Where you can see some stuff received from simple-spring-boot-node
and see simple table with last node status. 
At this stage at html all green and success. Any timeout errors at logs.

4. Next. Look for PID of started java apps. <br>
<b>$ ps ax | grep vertx-spring-boot-service </b> <br>

4. Terminate kill this nodes, so should left only monitoring node <br>
<b> kill -9 PID1 PID2 </b>

5. Start this nodes again. (step 2)


6. Wait minutes and check logs in console - time to time you can see <b>(TIMEOUT,-1) Timed out waiting for reply</b>
And this never end. 
At monitoring this issues will marked as unstable


