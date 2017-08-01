package edu.indiana.d2i.mqtt;

import edu.indiana.d2i.mqtt.client.MqttClientDaily;
import edu.indiana.d2i.mqtt.client.MqttClientWeekly;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttException;

/**
 * Basic launcher for mqtt message subscriber for daily or weekly
 */
public class MqttApp {
	
	static final Logger logger = Logger.getLogger(MqttClientWeekly.class.getName());

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
    	  
    	  File sen_data_file = new File("sensordata");
		  if (!sen_data_file.exists()) {
			  sen_data_file.mkdir();	                
		  }
		  File pub_sen_data_file = new File("publish_sensordata");
		  if (!pub_sen_data_file.exists()) {
			  pub_sen_data_file.mkdir();	                
		  }
			
		  Date date = DateUtils.addDays(new Date(), -1);
		  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	      String yest_date = sdf.format(date);
	      
	      File last_pub_sen_data_file = new File("publish_sensordata/" + yest_date);			
		  File srcDir = new File("sensordata/" + yest_date);
		  File destDir = new File("publish_sensordata");
		  if (!last_pub_sen_data_file.exists()) {
			  try {
				  FileUtils.copyDirectoryToDirectory(srcDir, destDir);
			  } catch (IOException e) {
				  // TODO Auto-generated catch block
				  logger.error("Particular day folder not copied successfully: ", e);
			  }             
		  }		  
    	  
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

