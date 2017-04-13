package edu.indiana.d2i.mqtt.client;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Calendar;
import java.util.TimeZone;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.util.Properties;

public class SimpleMqttClient {

	  	MqttClient myClient;
		MqttConnectOptions connOpt;
		
		static final Properties prop = new Properties();
		
		static final Logger logger = Logger.getLogger(SimpleMqttClient.class.getName());

		static final String BROKER_URL = "tcp://gpssensor.ddns.net";
		static final String M2MIO_STUFF = "things";
		static final String M2MIO_THING = MqttClient.generateClientId();
		
		// the following two flags control whether this example is a publisher, a subscriber or both
		static final Boolean subscriber = true;
		static final Boolean publisher = false;

		/**
		 * 
		 * MAIN
		 * 
		 */
		public static void main(String[] args) {
			PropertyConfigurator.configure(SimpleMqttClient.class.getClassLoader().getResource("edu/indiana/d2i/mqtt/log4j.properties"));
			//prop.load(SimpleMqttClient.class.getClassLoader().getResourceAsStream("./log4j.properties"));
			logger.info("== START SUBSCRIBER ==");
			File sen_data_file = new File("sensordata");
	        if (!sen_data_file.exists()) {
	        	sen_data_file.mkdir();	                
	        }
			SimpleMqttClient smc = new SimpleMqttClient();
			smc.runClient();
		}
		
		/**
		 * 
		 * runClient
		 * The main functionality of this simple example.
		 * Create a MQTT client, connect to broker, pub/sub, disconnect.
		 * 
		 */
		public void runClient() {
			// setup MQTT Client
			String clientID = M2MIO_THING;
			
			
			// Connect to Broker
			try {
				myClient = new MqttClient(BROKER_URL, clientID, new MemoryPersistence());
				
				myClient.setCallback(new MqttCallback() {
					BufferedWriter bw = null;
					FileWriter fw = null;
					
					//String current_date = new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime());
					
					Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
					int week_num = cal.get(Calendar.WEEK_OF_YEAR);
					int year = cal.get(Calendar.YEAR);
					
		            @Override
		            public void connectionLost(Throwable cause) { //Called when the client lost the connection to the broker 
		            	logger.error("Connection lost!");
		            }
		 
		            @Override
		            public void messageArrived(String topic, MqttMessage message) throws Exception {
		            	if(topic.contains("LASS/MAPS_COPY")){
		            		String[] value_split = message.toString().split("\\|");
		        			
		            		for (String val : value_split) {
		            			if(val.contains("device_id")){
			            			String device_id = val.split("\\=")[1];
			            			File dataFile = new File(year + ":W" + week_num + " - " + device_id + ".tmp");
			            			String data_dir = "sensordata/";
			            			File resource = new File(data_dir + dataFile);
			            			if(resource.exists()){
			            				
				            			fw = new FileWriter(resource, true);
						    			bw = new BufferedWriter(fw);
						    			bw.write(message.toString() + "\n");
						    			bw.close();
			            			}else{
				            			fw = new FileWriter(resource);
						    			bw = new BufferedWriter(fw);
						    			bw.write(message.toString() + "\n");
						    			bw.close();
			            			}
			            			
			            			File folder = new File(data_dir);
			            			File[] listOfFiles = folder.listFiles();

		            			    for (int i = 0; i < listOfFiles.length; i++) {
		            			      if (listOfFiles[i].isFile()) {
		            			        String file_name = listOfFiles[i].getName();
		            			        int before_weeknum = week_num - 1;
		            			        if(file_name.contains(year + ":W" + before_weeknum) && file_name.contains(".tmp") ){
		            			        	String file2_name = file_name.split(".tmp")[0] + ".txt";
		            			        	File file1 = new File(data_dir + file_name);
		            			        	File file2 = new File(data_dir + file2_name); // destination dir of your file
					            			boolean success = file1.renameTo(file2);
		            			        }
		            			      }
		            			    }
			            		}
		            	    }
		            	}		
		            }
		 
		            @Override
		            public void deliveryComplete(IMqttDeliveryToken token) {//Called when a outgoing publish is complete 
		            }
		        });
				
				myClient.connect();
				myClient.subscribe("#", 1);
				
			} catch (MqttException e) {
				logger.error("Disonnected from " + BROKER_URL + e);
				System.exit(-1);
			}
			
			logger.info("Connected to " + BROKER_URL);

			// setup topic
			// topics on m2m.io are in the form <domain>/<stuff>/<thing>
			//String myTopic = M2MIO_DOMAIN + "/" + M2MIO_STUFF + "/" + M2MIO_THING;
			String myTopic = M2MIO_STUFF + "/" + M2MIO_THING;

			// subscribe to topic if subscriber
			if (subscriber) {
				try {
					int subQoS = 0;
					myClient.subscribe(myTopic, subQoS);
				} catch (Exception e) {
					logger.error("Subscriber error: ", e);
				}
			}
			
			// disconnect
			try {
				// wait to ensure subscribed messages are delivered
				if (subscriber) {
					Thread.sleep(1000);
				}
				//myClient.disconnect();
			} catch (Exception e) {
				//e.printStackTrace();
				logger.error("Subscribed messages are notdelivered: ", e);
			}			
			
		}
		
	}
