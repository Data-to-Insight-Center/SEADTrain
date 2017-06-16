package edu.indiana.d2i.mqtt;

import edu.indiana.d2i.mqtt.client.MqttClientDaily;
import edu.indiana.d2i.mqtt.client.MqttClientWeekly;

import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.paho.client.mqttv3.MqttException;

/**
 * Basic launcher for mqtt message subscriber for daily or weekly
 */
public class MqttApp {

  public static void main(final String[] args) throws MqttException {

    if (args.length < 1) {
      throw new IllegalArgumentException("Must have either 'daily' or 'weekly' as argument");
    }
    switch (args[0]) {
      case "daily":
    	  Timer dt = new Timer();

    	  dt.scheduleAtFixedRate(
    	      new TimerTask()
    	      {
    	          public void run()
    	          {
    	        	  MqttClientDaily.main(args);
    	          }
    	      },
    	      0,      // run first occurrence immediately
    	      1000);
    	  
        break;
        
      case "weekly":
    	  Timer wt = new Timer();

    	  wt.scheduleAtFixedRate(
    	      new TimerTask()
    	      {
    	          public void run()
    	          {
    	        	  MqttClientWeekly.main(args);
    	          }
    	      },
    	      0,      // run first occurrence immediately
    	      1000);
    	  
        break;
      default:
        throw new IllegalArgumentException("Don't know how to do " + args[0]);
    }
  }
}

