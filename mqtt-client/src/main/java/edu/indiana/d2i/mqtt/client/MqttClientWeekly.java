package edu.indiana.d2i.mqtt.client;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class MqttClientWeekly {

	  	MqttClient mqtt_client;
		
		static final Logger logger = Logger.getLogger(MqttClientWeekly.class.getName());

		static final String broker_url = "tcp://gpssensor.ddns.net";
		static final String client_id = MqttClient.generateClientId();
		
		// the following flag is control whether the subscriber is on or not
		static final Boolean subscriber = true;

		/**
		 * 
		 * MAIN
		 * 
		 */
		public static void main(String[] args) {
			PropertyConfigurator.configure(MqttClientWeekly.class.getClassLoader().getResource("edu/indiana/d2i/mqtt/log4j.properties"));
			//prop.load(SimpleMqttClient.class.getClassLoader().getResourceAsStream("./log4j.properties"));
			//logger.info("== START SUBSCRIBER ==");
			File sen_data_file = new File("sensordata");
	        if (!sen_data_file.exists()) {
	        	sen_data_file.mkdir();	                
	        }
	        MqttClientWeekly mcw = new MqttClientWeekly();
	        mcw.runClient();
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
			String clientID = client_id;
			
			
			// Connect to Broker
			try {
				mqtt_client = new MqttClient(broker_url, clientID, new MemoryPersistence());
				
				mqtt_client.setCallback(new MqttCallback() {
					BufferedWriter bw = null;
					FileWriter fw = null;
					
					Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
					String week_num = String.format("%02d", cal.get(Calendar.WEEK_OF_YEAR));
					int year = cal.get(Calendar.YEAR);
					String month = String.format("%02d", (cal.get(Calendar.MONTH) + 1));
					String day = String.format("%02d", MqttClientWeekly.getLastSunday());
					
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
			            			
			            			File DataFolder = new File(year + "-" + month + "-" + day);
			            			String data_dir = "sensordata/";			            			
			            			File day_sen_data_file = new File(data_dir + DataFolder);
			            	        if (!day_sen_data_file.exists()) {
			            	        	day_sen_data_file.mkdir();	                
			            	        }
			            	        String folder_dir = "sensordata/" + year + "-" + month + "-" + day + "/";
			            	        File dataFile = new File(year + "_W" + week_num + "_" + device_id + ".tmp");
			            			File resource = new File(folder_dir + dataFile);
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
			            			
			            			File directory = new File(data_dir);
			            			File[] listOfFolders = directory.listFiles();

			            		    // get all the files from a directory
			            		    for (int i = 0; i < listOfFolders.length; i++) {
			            		        if (listOfFolders[i].isFile()) {
			            		        	String file_name = listOfFolders[i].getName();
			            			        if(!(file_name.contains(year + "_W" + week_num)) && file_name.contains(".tmp") && file_name.contains(year + "_W") ){
			            			        	String file2_name = file_name.split(".tmp")[0] + ".txt";
			            			        	File file1 = new File(data_dir + file_name);
			            			        	File file2 = new File(data_dir + file2_name); // destination dir of your file
						            			boolean success = file1.renameTo(file2);
			            			        }
			            		        } else if (listOfFolders[i].isDirectory()) {			            		        	
			            			        String folder_name = listOfFolders[i].getName();
			            			        String sub_file_dir = "sensordata/" + folder_name + "/";
			            			        File sub_directory = new File(sub_file_dir);
					            			File[] listOfFiles = sub_directory.listFiles();
					            			
					            			for (int j = 0; j < listOfFiles.length; j++) {	
				            			        String file_name = listOfFiles[j].getName();
				            			        
				            			        if(!(file_name.contains(year + "_W" + week_num)) && file_name.contains(".tmp") && file_name.contains(year + "_W")){
				            			        	String file2_name = file_name.split(".tmp")[0] + ".txt";
				            			        	File file1 = new File(sub_file_dir + file_name);
				            			        	File file2 = new File(sub_file_dir + file2_name); // destination dir of your file
							            			boolean success = file1.renameTo(file2);
				            			        }
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
				
				mqtt_client.connect();
				mqtt_client.subscribe("#", 1);
				
			} catch (MqttException e) {
				logger.error("Disonnected from " + broker_url + e);
				System.exit(-1);
			}
			
			//logger.info("Connected to " + broker_url);

			// setup topic
			String myTopic = client_id;

			// subscribe to topic if subscriber
			if (subscriber) {
				try {
					int subQoS = 0;
					mqtt_client.subscribe(myTopic, subQoS);
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
				mqtt_client.disconnect();
			} catch (Exception e) {
				logger.error("Subscribed messages are notdelivered: ", e);
			}			
			
		}
		
		public static int getLastSunday() {
			Calendar cal=Calendar.getInstance();
			cal.add( Calendar.DAY_OF_WEEK, -(cal.get(Calendar.DAY_OF_WEEK)-1)); 
			return cal.get(Calendar.DATE);
		}
		
	}
