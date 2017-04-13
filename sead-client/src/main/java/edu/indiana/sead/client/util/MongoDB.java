/*
 *
 * Copyright 2015 University of Michigan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 *
 * @author myersjd@umich.edu
 */


package edu.indiana.sead.client.util;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MongoDB {

    private static MongoClient mongoClientInstance = null;
    private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    //RO Stream status constants
    private static String YEAR = "year";
    private static String WEEK = "week";
    private static String DEVICE_ID = "device_id";
    private static String STATUS_LIST = "status_list";
    private static String STATUS = "status";
    private static String DATE = "date";
    public enum STATUS_ENUM { Processing, Deposited, NotDeposited, Published, NotPublished;}

	public static synchronized MongoClient getMongoClientInstance() {
	    if (mongoClientInstance == null) {
	        try {
	            mongoClientInstance = new MongoClient(Constants.mongoHost, Constants.mongoPort);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	    return mongoClientInstance;
	}

    public static MongoDatabase getClientDB() {
        return getMongoClientInstance().getDatabase(Constants.clientDbName);
	}

    public static MongoCollection<Document> getStreamCollection(String collectionName) {
        MongoCollection<Document> collection = getClientDB().getCollection(collectionName);
        BasicDBObject index = new BasicDBObject();
        index.put(YEAR, 1);
        index.put(WEEK, 1);
        index.put(DEVICE_ID, 1);
        collection.createIndex(index);
        return collection;
    }

    public static void addStreamStatus(String collectionName, String year, String week, String deviceId,
                                       Date date, STATUS_ENUM status) {
        MongoCollection<Document> collection = getStreamCollection(collectionName);
        Bson filter = Filters.and(new Document(YEAR, year), new Document(WEEK, week), new Document(DEVICE_ID, deviceId));
        JSONObject statusObj = new JSONObject();
        statusObj.put(YEAR, year);
        statusObj.put(WEEK, week);
        statusObj.put(DEVICE_ID, deviceId);
        statusObj.put(STATUS_LIST, new JSONObject().put(DATE, df.format(date)).put(STATUS, status));
        collection.updateOne(filter,
                new Document("$push", new Document(STATUS_LIST,
                        Document.parse(new JSONObject().put(DATE, df.format(date)).put(STATUS, status).toString()))),
                (new UpdateOptions()).upsert(true));
    }
}
