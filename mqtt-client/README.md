# Getting started with MQTT and Java

This project is a simple java application to show how to start MQTT application, get the message from air box sensor subscriber and write the raw data into separate files depending on the week and device model.

## Prerequisite

* Maven 3.3.x
* Install a MQTT Broker(https://mosquitto.org/)
    
    
## Build and run the application

**1- Build the project with Maven:**

This project is a simple java application that runs a subscriber using the [Eclipse Paho library](https://eclipse.org/paho/).


```
$ mvn clean install
```

For convenience, the project is set up that the maven package target produces a single executable, 
`/mqtt-client `, that includes all of the programs and dependencies.


**2- Run the subscriber**

The subscriber will receive and write all the messages(sensor raw data) from air box sensors into separate files depending on the day or week by device model. 

To get the raw data of "day by device" output file run the below command
```
$ nohup ./target/mqtt-client day > /dev/null 2>&1&
```

OR

To get the raw data of "week by device" output file run the below command
```
$ nohup ./target/mqtt-client weekly > /dev/null 2>&1&
```

After ran one of the above command you can find the output files in the "sensordata" folder.
```
$ cd ./sensordata/
```
