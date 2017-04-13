# Getting started with MQTT and Java

This project is a simple java application to show how to start MQTT application and get the message from air box sensor subscriber and write those raw data into separate files based on the week and device model.

## Prerequisite

* Maven 3.3.x
* Install a MQTT Broker(https://mosquitto.org/)
    
    
## Build and run the application

**1- Build the project with Maven:**

This project is a simple java application that runs a subscriber using the [Eclipse Paho library](https://eclipse.org/paho/).


```
$ mvn clean install
```

For convenience, the project is set up so that the maven package target produces a single executable, 
`/mqtt-client `, that includes all of the programs and dependencies.


**2- Run the Subscriber**

The subscriber will receive and write all the messages(sensor raw data) from sensors into the separate files based on the week and device model.

```
$ nohup ./target/mqtt-sample subscriber > /dev/null 2>&1&
```
