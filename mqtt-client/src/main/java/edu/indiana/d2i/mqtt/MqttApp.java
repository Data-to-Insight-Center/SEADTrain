package edu.indiana.d2i.mqtt;

import edu.indiana.d2i.mqtt.client.SimpleMqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

/**
 * Basic launcher for Publisher and Subscriber
 */
public class MqttApp {

  public static void main(String[] args) throws MqttException {

    if (args.length < 1) {
      throw new IllegalArgumentException("Must have either 'publisher' or 'subscriber' as argument");
    }
    switch (args[0]) {
      case "subscriber":
    	  SimpleMqttClient.main(args);
        break;
      default:
        throw new IllegalArgumentException("Don't know how to do " + args[0]);
    }
  }
}

