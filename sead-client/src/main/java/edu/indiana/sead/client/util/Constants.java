/*
 *
 * Copyright 2015 The Trustees of Indiana University
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
 * @author charmadu@umail.iu.edu
 */

package edu.indiana.sead.client.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Constants {

	public static String mongoHost;
	public static int mongoPort;

	public static String clientDbName;
    public static String fedoraUrl;
    public static String curbeeUrl;
    public static String seadClientUrl;
    public final static String INFINITE = "inf";

    public static Map<String, String> OREMetadataMap = new HashMap<String, String>();
    public static Map<String, String> roMetadataMap = new HashMap<String, String>();

    public static final String GRANT_NUMBER = "Grant Number";
    public static final String TITLE = "Title";
    public static final String HAS_PART = "Has Part";
    public static final String LICENSE = "License";
    public static final String SIZE = "Size";
    public static final String CONTACT = "Contact";
    public static final String DESCRIPTION = "Description";
    public static final String RIGHTS = "Rights";
    public static final String LABEL = "Label";
    public static final String CREATION_DATE = "Creation Date";
    public static final String LAST_MODIFIED = "Last Modified";
    public static final String CREATOR = "Creator";
    public static final String ABSTRACT = "Abstract";
    public static final String PUBLICATION_DATE = "Publication Date";
    public static final String IDENTIFIER = "Identifier";
    public static final String DATE = "Date";
    public static final String PUBLISHING_PROJECT = "Publishing Project";
    public static final String PUBLISHING_PROJECT_NAME = "Publishing Project Name";
    public static final String KEYWORDS = "Keyword";
    public static final String MIMETYPE = "Mimetype";
    public static final String UNIT = "Unit";
    public static final String RIGHTS_HOLDER = "Rights Holder";
    public static final String REPOSITORY = "Repository";
    public static final String AFFILIATIONS = "Affiliations";
    public static final String PREFERENCES = "Preferences";
    public static final String SIMILAR_TO = "similarTo";
    public static final String PURPOSE = "Purpose";
    public static final String PUBLICATION_CALLBACK = "Publication Callback";

    public static final String AGGREGATION_STATISTICS = "Aggregation Statistics";
    public static final String NUMBER_OF_DATASETS = "Number of Datasets";
    public static final String MAX_DATA_SIZE = "Max Dataset Size";
    public static final String MAX_COLLECTION_DEPTH = "Max Collection Depth";
    public static final String TOTAL_SIZE = "Total Size";
    public static final String NUMBER_OF_COLLECTIONS = "Number of Collections";
    public static final String DATA_MIMETYPE = "Data Mimetypes";

    //airbox specific metadata
    public static final String DEVICE_ID = "device_id";
    public static final String DEVICE = "device";
    public static final String GPS_LAT = "gps_lat";
    public static final String GPS_LON = "gps_lon";


	static {
		try {
			loadConfigurations();
            populateOREMapContext();
            populateROContext();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    private static void loadConfigurations() throws IOException {
		InputStream inputStream = Constants.class
				.getResourceAsStream("./default.properties");
		Properties props = new Properties();
		props.load(inputStream);
		mongoHost = props.getProperty("mongo.host", "localhost");
		mongoPort = Integer.parseInt(props.getProperty("mongo.port", "27017"));
		clientDbName = props.getProperty("client.db.name", "sead-client");
        fedoraUrl = props.getProperty("fedora.url", "http://localhost:8080/fed/rest");
        curbeeUrl = props.getProperty("curbee.url", "http://localhost:8083/sead-api/api/researchobjects");
        seadClientUrl = props.getProperty("sead.client.url", "http://localhost:8083/sead-client/rest");
	}

    private static void populateOREMapContext() {
        OREMetadataMap.put(GRANT_NUMBER, "http://sead-data.net/terms/GrantNumber");
        OREMetadataMap.put(TITLE, "http://purl.org/dc/elements/1.1/title");
        OREMetadataMap.put(HAS_PART, "http://purl.org/dc/terms/hasPart");
        OREMetadataMap.put(LICENSE, "http://purl.org/dc/terms/license");
        OREMetadataMap.put(SIZE, "tag:tupeloproject.org,2006:/2.0/files/length");
        OREMetadataMap.put(CONTACT, "http://sead-data.net/terms/contact");
        OREMetadataMap.put(DESCRIPTION, "http://purl.org/dc/elements/1.1/description");
        OREMetadataMap.put(RIGHTS, "http://purl.org/dc/terms/rights");
        OREMetadataMap.put(LABEL, "http://www.w3.org/2000/01/rdf-schema#label");
        OREMetadataMap.put(CREATION_DATE, "http://purl.org/dc/terms/created");
        OREMetadataMap.put(LAST_MODIFIED, "http://sead-data.net/terms/lastModified");
        OREMetadataMap.put(CREATOR, "http://purl.org/dc/terms/creator");
        OREMetadataMap.put(ABSTRACT, "http://purl.org/dc/terms/abstract");
        OREMetadataMap.put(PUBLICATION_DATE, "http://purl.org/dc/terms/issued");
        OREMetadataMap.put(IDENTIFIER, "http://purl.org/dc/elements/1.1/identifier");
        OREMetadataMap.put(DATE, "http://purl.org/dc/elements/1.1/date");
        OREMetadataMap.put(PUBLISHING_PROJECT, "http://sead-data.net/terms/publishingProject");
        OREMetadataMap.put(KEYWORDS, "http://www.holygoat.co.uk/owl/redwood/0.1/tags/taggedWithTag");
        OREMetadataMap.put(MIMETYPE, "http://purl.org/dc/elements/1.1/format");
        OREMetadataMap.put(UNIT, "http://ecgs.ncsa.illinois.edu/gsis/sas/unit/udunits2");

        //airbox specific URIs
        OREMetadataMap.put(DEVICE_ID, "http://sead-data.net/terms/deviceId");
        OREMetadataMap.put(DEVICE, "http://sead-data.net/terms/device");
        OREMetadataMap.put(GPS_LAT, "http://sead-data.net/terms/latitude");
        OREMetadataMap.put(GPS_LON, "http://sead-data.net/terms/longitude");
    }

    private static void populateROContext() {
        roMetadataMap.put(TITLE, "http://purl.org/dc/elements/1.1/title");
        roMetadataMap.put(LICENSE, "http://purl.org/dc/terms/license");
        roMetadataMap.put(CONTACT, "http://sead-data.net/terms/contact");
        roMetadataMap.put(DESCRIPTION, "http://purl.org/dc/elements/1.1/description");
        roMetadataMap.put(LABEL, "http://www.w3.org/2000/01/rdf-schema#label");
        roMetadataMap.put(CREATION_DATE, "http://purl.org/dc/terms/created");
        roMetadataMap.put(LAST_MODIFIED, "http://sead-data.net/terms/lastModified");
        roMetadataMap.put(CREATOR, "http://purl.org/dc/terms/creator");
        roMetadataMap.put(ABSTRACT, "http://purl.org/dc/terms/abstract");
        roMetadataMap.put(PUBLICATION_DATE, "http://purl.org/dc/terms/issued");
        roMetadataMap.put(IDENTIFIER, "http://purl.org/dc/elements/1.1/identifier");
        roMetadataMap.put(DATE, "http://purl.org/dc/elements/1.1/date");
        roMetadataMap.put(PUBLISHING_PROJECT, "http://sead-data.net/terms/publishingProject");
        roMetadataMap.put(PUBLISHING_PROJECT_NAME, "http://sead-data.net/terms/publishingProjectName");
        roMetadataMap.put(KEYWORDS, "http://www.holygoat.co.uk/owl/redwood/0.1/tags/taggedWithTag");
        roMetadataMap.put(RIGHTS_HOLDER, "http://purl.org/dc/terms/rightsHolder");
        roMetadataMap.put(REPOSITORY, "http://sead-data.net/terms/requestedrepository");
        roMetadataMap.put(AFFILIATIONS, "http://sead-data.net/terms/affiliations");
        roMetadataMap.put(PREFERENCES, "http://sead-data.net/terms/publicationpreferences");
        roMetadataMap.put(PURPOSE, "http://sead-data.net/vocab/publishing#Purpose");
        roMetadataMap.put(PUBLICATION_CALLBACK, "http://sead-data.net/terms/publicationcallback");

        roMetadataMap.put(AGGREGATION_STATISTICS, "http://sead-data.net/terms/publicationstatistics");
        roMetadataMap.put(TOTAL_SIZE, "tag:tupeloproject.org,2006:/2.0/files/length");
        roMetadataMap.put(DATA_MIMETYPE, "http://purl.org/dc/elements/1.1/format");
        roMetadataMap.put(NUMBER_OF_DATASETS, "http://sead-data.net/terms/datasetcount");
        roMetadataMap.put(MAX_DATA_SIZE, "http://sead-data.net/terms/maxdatasetsize");
        roMetadataMap.put(MAX_COLLECTION_DEPTH, "http://sead-data.net/terms/maxcollectiondepth");
        roMetadataMap.put(NUMBER_OF_COLLECTIONS, "http://sead-data.net/terms/collectioncount");



    }
}
