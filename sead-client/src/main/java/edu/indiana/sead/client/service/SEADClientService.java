package edu.indiana.sead.client.service;

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
 * @author charmadu@umail.iu.edu
 */


import edu.indiana.sead.client.util.Constants;
import edu.indiana.sead.client.util.MongoDB;
import edu.indiana.sead.client.util.StreamFile;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Path("/")
public class SEADClientService {

    private WebTarget fedoraWebService;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
    private SimpleDateFormat folderDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat roDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private static Logger logger = Logger.getLogger(SEADClientService.class);

    //Stream RO constants
    public static final String FOLDER = "folder";
    public static final String PROJECT = "project";
    public static final String ABSTRACT = "abstract";
    public static final String CREATOR = "creator";
    public static final String REPOSITORY = "repository";
    public static final String PURPOSE = "purpose";
    public static final String RIGHTS_HOLDER = "rightsHolder";
    public static final String TITLE = "title";
    public static final String FILES = "files";
    public static final String CHANNEL = "channel";

    public static final String streamFileFormat = "([\\w-]+)_([\\w-]+)_([\\w-]+)"; // year-week-deviceID
    public static Map<String, List<String>> projectMetadata = new HashMap<>();

    public SEADClientService() {
        fedoraWebService = ClientBuilder.newClient().target(Constants.fedoraUrl + "/dibbs");
        projectMetadata.put("airbox", Arrays.asList(Constants.DEVICE_ID, Constants.DEVICE,
                Constants.GPS_LAT, Constants.GPS_LON));
        //projectMetadata.put("twitter", new ArrayList<String>());
    }

    @GET
    @Path("/{id}/status")
    public Response getStatus(@PathParam("id") String id) {
        Client curbeeClient = ClientBuilder.newClient();
        WebTarget curbeeTarget = curbeeClient.target(Constants.curbeeUrl);
        Response curbeeResponse =
                curbeeTarget.path(id + "/status").request(MediaType.APPLICATION_JSON)
                        .get();

        int status = curbeeResponse.getStatus();
        String curbeeResponseString = curbeeResponse.readEntity(String.class);

        return Response.status(status).entity(curbeeResponseString).build();
    }

    @POST
    @Path("/rorequest")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllPublishedROs(String roData) throws IOException {

        JSONObject metadataObject = new JSONObject(roData);
        String bytes = metadataObject.getString("DATA_BYTES").trim();
        String[] array = bytes.split(" ");
        byte[] byteArray = new byte[array.length];
        for(int i = 0 ; i < array.length ; i++){
            byteArray[i] = (byte)Integer.parseInt(array[i].substring(2),16);
        }

        String id = "dibbs-" + UUID.randomUUID().toString();
        Response fedoraResponse = fedoraWebService.request().header("Slug", id).post(null);
        String filepath = "/Users/charmadu/Downloads/" + metadataObject.getString("FILE_NAME");
        FileUtils.writeByteArrayToFile(new File(filepath), byteArray);

        Client client = ClientBuilder.newBuilder()
                .register(MultiPartFeature.class).build();

        WebTarget webTarget = client.target(Constants.fedoraUrl + "/dibbs/" + id);
        MultiPart multiPart = new MultiPart();

        File newFile = new File(filepath);

        FileDataBodyPart fileDataBodyPart = new FileDataBodyPart("file",
                newFile, MediaType.APPLICATION_OCTET_STREAM_TYPE);
        multiPart.bodyPart(fileDataBodyPart);

        byte[] byateArray = Files.readAllBytes(new File(filepath).toPath());

        Response file_response = webTarget.request().header("Slug",metadataObject.getString("FILE_NAME")).post(
                Entity.entity(byateArray, metadataObject.getString("FILE_TYPE")));

        JSONObject oreMap = createOREMap(metadataObject, id);
        Response oreMap_response = webTarget.request().header("Slug", "oreMap").post(
                Entity.entity(oreMap.toString().getBytes(), "application/json"));

        JSONObject roObject = createRO(metadataObject, id);

        Client curbeeClient = ClientBuilder.newClient();
        WebTarget curbeeTarget = curbeeClient.target(Constants.curbeeUrl);
        Response curbeeResponse =
                curbeeTarget.request(MediaType.APPLICATION_JSON)
                        .post(Entity.entity(roObject.toString(), MediaType.APPLICATION_JSON));

        int status = curbeeResponse.getStatus();
        String curbeeResponseString = curbeeResponse.readEntity(String.class);

        return Response.status(status).entity(curbeeResponseString).build();
    }

    private JSONObject createRO(JSONObject metadataObject, String identifier) {
        JSONObject roObject = new JSONObject();

        JSONObject context = new JSONObject();
        for(String key : Constants.roMetadataMap.keySet()) {
            context.put(key, Constants.roMetadataMap.get(key));
        }

        roObject.put("@context", new JSONArray().put("https://w3id.org/ore/context").put(context));
        roObject.put(Constants.RIGHTS_HOLDER, metadataObject.getString("CREATOR"));
        roObject.put(Constants.REPOSITORY, "sda");
        roObject.put(Constants.PREFERENCES, new JSONObject());

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat fileDateFormat = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss 'GMT'Z (z)");
        String creation_date = simpleDateFormat.format(new Date());

        JSONObject aggregation = new JSONObject();
        aggregation.put("@type", "Aggregation");
        aggregation.put("@id", Constants.fedoraUrl + "/dibbs/" + identifier + "/oreMap");
        aggregation.put(Constants.IDENTIFIER, identifier);
        aggregation.put(Constants.CREATION_DATE, creation_date);
        //aggregation.put(Constants.PUBLICATION_DATE, creation_date);
        aggregation.put(Constants.TITLE, metadataObject.getString("TITLE"));
        //aggregation.put(Constants.LABEL, metadataObject.getString("TITLE"));
        aggregation.put(Constants.CREATOR, metadataObject.getString("CREATOR"));
        aggregation.put(Constants.ABSTRACT, metadataObject.getString("ABSTRACT"));
        aggregation.put(Constants.PUBLISHING_PROJECT, "DIBBS");
        //aggregation.put(Constants.PUBLISHING_PROJECT_NAME, "DIBBS");
        roObject.put("Aggregation", aggregation);

        JSONObject aggregationStatistics = new JSONObject();
        aggregationStatistics.put(Constants.NUMBER_OF_DATASETS, "1");
        aggregationStatistics.put(Constants.MAX_DATA_SIZE, metadataObject.getString("FILE_SIZE"));
        aggregationStatistics.put(Constants.MAX_COLLECTION_DEPTH, "0");
        aggregationStatistics.put(Constants.TOTAL_SIZE, metadataObject.getString("FILE_SIZE"));
        aggregationStatistics.put(Constants.NUMBER_OF_COLLECTIONS, "0");
        aggregationStatistics.put(Constants.DATA_MIMETYPE, new JSONArray().put(metadataObject.getString("FILE_TYPE")));
        roObject.put(Constants.AGGREGATION_STATISTICS, aggregationStatistics);

        return roObject;
    }

    private JSONObject createOREMap(JSONObject metadataObject, String identifier) {
        JSONObject oreMap = new JSONObject();

        JSONObject context = new JSONObject();
        for(String key : Constants.OREMetadataMap.keySet()) {
            context.put(key, Constants.OREMetadataMap.get(key));
        }

        oreMap.put("@context", new JSONArray().put("https://w3id.org/ore/context").put(context));
        oreMap.put("@type", "ResourceMap");
        oreMap.put("@id", Constants.fedoraUrl + "/dibbs/" + identifier + "/oreMap");

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat fileDateFormat = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss 'GMT'Z (z)");
        String creation_date = simpleDateFormat.format(new Date());

        oreMap.put(Constants.CREATION_DATE, creation_date);

        JSONObject describes = new JSONObject();
        describes.put("@type", new JSONArray().put("Aggregation").put("http://cet.ncsa.uiuc.edu/2015/Dataset"));
        describes.put(Constants.IDENTIFIER, identifier);
        describes.put(Constants.CREATION_DATE, creation_date);
        describes.put(Constants.PUBLICATION_DATE, creation_date);
        describes.put(Constants.TITLE, metadataObject.getString("TITLE"));
        describes.put(Constants.LABEL, metadataObject.getString("TITLE"));
        describes.put(Constants.CREATOR, metadataObject.getString("CREATOR"));
        describes.put(Constants.ABSTRACT, metadataObject.getString("ABSTRACT"));

        String fileIdentifier = "file-" + UUID.randomUUID().toString();
        describes.put(Constants.HAS_PART, new JSONArray().put(fileIdentifier));

        JSONArray aggregates = new JSONArray();
        JSONObject fileObject = new JSONObject();
        fileObject.put("@type", new JSONArray().put("AggregatedResource").put("http://cet.ncsa.uiuc.edu/2015/File"));
        fileObject.put("@id", fileIdentifier);
        fileObject.put(Constants.IDENTIFIER, fileIdentifier);
        fileObject.put(Constants.TITLE, metadataObject.getString("FILE_NAME"));
        fileObject.put(Constants.LABEL, metadataObject.getString("FILE_NAME"));
        fileObject.put(Constants.SIZE, metadataObject.getString("FILE_SIZE"));
        fileObject.put(Constants.MIMETYPE, metadataObject.getString("FILE_TYPE"));
        try {
            fileObject.put(Constants.CREATION_DATE, simpleDateFormat.format(fileDateFormat.parse(metadataObject.getString("LAST_MODIFIED_DATE"))));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        fileObject.put(Constants.PUBLICATION_DATE, creation_date);
        fileObject.put("similarTo", Constants.fedoraUrl + "/dibbs/" + identifier + "/" + metadataObject.getString("FILE_NAME"));
        aggregates.put(fileObject);

        describes.put("aggregates", aggregates);
        oreMap.put("describes", describes);

        return oreMap;
    }

    @POST
    @Path("/streamro")
    @Produces(MediaType.APPLICATION_JSON)
    public Response publishStreamRo(String roData) throws IOException, ParseException {

        JSONObject metadataObject = new JSONObject(roData);
        String folder = metadataObject.getString(FOLDER);
        String project = metadataObject.getString(PROJECT);
        String id = project + "-" + UUID.randomUUID().toString();
        Client client = ClientBuilder.newBuilder().register(MultiPartFeature.class).build();

        String[] paths = folder.split("/");
        String folderName = paths[paths.length -1];
        String fromDateTime = folderName + "T00:00:00Z";
        Calendar c = Calendar.getInstance();
        c.setTime(folderDateFormat.parse(folderName));
        c.add(Calendar.DATE, 1);
        String toDateTime = roDateFormat.format(c.getTime());

        // Create folder in Fedora with id
        WebTarget fedoraWebService = ClientBuilder.newClient().target(Constants.fedoraUrl + "/" + project);
        Response fedoraResponse = null;
        try {
            fedoraResponse = fedoraWebService.request().header("Slug", id).post(null);
        } catch (Exception e) {
            logger.error("Couldn't create RO folder in Fedora repository", e);
            return Response.serverError()
                    .entity(new JSONObject().put("error", "Error occurred while connecting to Fedora repository").toString())
                    .build();
        }

        if (fedoraResponse.getStatus() != Response.Status.CREATED.getStatusCode()
                && fedoraResponse.getStatus() != Response.Status.OK.getStatusCode()) {
            logger.error("Couldn't create RO folder in Fedora repository");
            return Response.serverError()
                    .entity(new JSONObject().put("error", "Couldn't create RO folder in Fedora repository").toString())
                    .build();
        }

        WebTarget webTarget = client.target(Constants.fedoraUrl + "/" + project + "/" + id); // folder of RO in fedora
        Iterator it = FileUtils.iterateFiles(new File(folder), null, false);
        ArrayList<StreamFile> fileList = new ArrayList<StreamFile>();

        while (it.hasNext()) { // iterate through files
            File file = (File) it.next();
            String fileName = file.getName();
            String mimeType = file.toURL().openConnection().getContentType();
            // check format and .txt extension
            if (!fileName.split("\\/")[fileName.split("\\/").length - 1].endsWith(".txt") ||
                    !Pattern.matches(streamFileFormat, fileName.split("\\.")[0])) {
                continue;
            }
            Pattern p = Pattern.compile(streamFileFormat);
            Matcher m = p.matcher(fileName.split("\\.")[0]);
            m.find();
            StreamFile streamFile = new StreamFile(fileName, file.getAbsolutePath(), m.group(1), m.group(2),
                    m.group(3), mimeType);
            //add files to fileList
            fileList.add(streamFile);
        }

        for (StreamFile streamFile : fileList) {
            File file = new File(streamFile.getFilePath());
            File file_rename = new File(streamFile.getFilePath() + ".processing");
            //rename file with .processing appended at the end
            file.renameTo(file_rename);
            streamFile.setFilePath(streamFile.getFilePath() + ".processing");
            // add "Processing" status to DB
            MongoDB.addStreamStatus(project, streamFile.getYear(), streamFile.getWeek(), streamFile.getId(),
                    new Date(), id, MongoDB.STATUS_ENUM.Processing);
        }

        long totalSize = 0;
        long maxSize = 0;
        int noOfFiles = 0;
        JSONObject roMetadataObj = new JSONObject();
        ArrayList<String> mimeTypes = new ArrayList<String>();
        JSONArray filesArray = new JSONArray();

        for (StreamFile streamFile : fileList) {

            File file = new File(streamFile.getFilePath());
            String fileName = streamFile.getFilename();
            long size = file.length();
            String mimeType = streamFile.getMimeType();
            Date lastModified = new Date(file.toURL().openConnection().getLastModified());

            totalSize += size;
            if (maxSize < size)
                maxSize = size;
            noOfFiles++;
            if (!mimeTypes.contains(mimeType))
                mimeTypes.add(mimeType);

            // deposit file in fedora
            byte[] byateArray = Files.readAllBytes(file.toPath());
            Response file_response = null;
            try {
                file_response = webTarget.request().header("Slug", fileName).post(
                        Entity.entity(byateArray, mimeType));
            } catch (Exception e) {
                //remove .processing from file
                File file_name = new File(streamFile.getFilePath());
                File file_rename = new File(streamFile.getFilePath().replaceAll(".processing", ""));
                file_name.renameTo(file_rename);
                //add "NotDeposited" status to DB
                MongoDB.addStreamStatus(project, streamFile.getYear(), streamFile.getWeek(), streamFile.getId(),
                        new Date(), id, MongoDB.STATUS_ENUM.NotDeposited);
                logger.error("File not deposited in fedora : " + fileName);
                continue;
            }

            if (file_response.getStatus() != Response.Status.CREATED.getStatusCode() &&
                    file_response.getStatus() != Response.Status.OK.getStatusCode()) {
                //remove .processing from file
                File file_name = new File(streamFile.getFilePath());
                File file_rename = new File(streamFile.getFilePath().replaceAll(".processing", ""));
                file_name.renameTo(file_rename);
                //add "NotDeposited" status to DB
                MongoDB.addStreamStatus(project, streamFile.getYear(), streamFile.getWeek(), streamFile.getId(),
                        new Date(), id, MongoDB.STATUS_ENUM.NotDeposited);
                logger.error("File not deposited in fedora : " + fileName);
                continue;
            }
            //add "Deposited" status to DB
            MongoDB.addStreamStatus(project, streamFile.getYear(), streamFile.getWeek(), streamFile.getId(),
                    new Date(), id, MongoDB.STATUS_ENUM.Deposited);

            String similarTo = Constants.fedoraUrl + "/" + project + "/" + id + "/" + fileName;
            JSONObject fileObject = new JSONObject();
            fileObject.put(Constants.TITLE, fileName);
            fileObject.put(Constants.SIZE, size);
            fileObject.put(Constants.MIMETYPE, mimeType);
            fileObject.put(Constants.CREATION_DATE, fromDateTime);
            fileObject.put(Constants.LAST_MODIFIED, toDateTime);
            fileObject.put(Constants.SIMILAR_TO, similarTo);
            addProjectSpecificMetadata(project, fileObject, streamFile);
            filesArray.put(fileObject);
        }

        if(filesArray.length() == 0) {
            return Response.status(Response.Status.NO_CONTENT)
                    .entity(new JSONObject().put("message", "There were no files to publish").toString()).build();
        }

        //populate metadata
        roMetadataObj.put(Constants.CREATOR, metadataObject.getString(CREATOR));
        roMetadataObj.put(Constants.TITLE, metadataObject.getString(TITLE));
        roMetadataObj.put(Constants.ABSTRACT, metadataObject.getString(ABSTRACT));
        roMetadataObj.put(Constants.CREATION_DATE, fromDateTime);
        roMetadataObj.put(Constants.LAST_MODIFIED, toDateTime);
        roMetadataObj.put(Constants.PUBLISHING_PROJECT, project);
        roMetadataObj.put(Constants.REPOSITORY, metadataObject.getString(REPOSITORY));
        roMetadataObj.put(Constants.PURPOSE, metadataObject.getString(PURPOSE));
        roMetadataObj.put(Constants.CHANNEL, metadataObject.getString(CHANNEL));
        roMetadataObj.put(Constants.NUMBER_OF_DATASETS, noOfFiles + "");
        roMetadataObj.put(Constants.MAX_DATA_SIZE, maxSize + "");
        roMetadataObj.put(Constants.MAX_COLLECTION_DEPTH, 0 +"");
        roMetadataObj.put(Constants.TOTAL_SIZE, totalSize + "");
        roMetadataObj.put(Constants.NUMBER_OF_COLLECTIONS, 0 + "");
        roMetadataObj.put(Constants.DATA_MIMETYPE, new JSONArray(mimeTypes));
        roMetadataObj.put(FILES, filesArray);

        //create and deposito OREMap in fedora
        JSONObject oreMap = createStreamOREMap(roMetadataObj, id);
        Response oreMap_response = webTarget.request().header("Slug", "oreMap").post(
                Entity.entity(oreMap.toString().getBytes(), "application/json"));
        //TODO handle error if failed

        //create RO object
        JSONObject roObject = createStreamRO(roMetadataObj, id);

        //send RO request to SEAD services
        Client curbeeClient = ClientBuilder.newClient();
        WebTarget curbeeTarget = curbeeClient.target(Constants.curbeeUrl);
        Response curbeeResponse = null;
        int status = 0;
        String curbeeResponseString = new JSONObject().put("error", "Error while sending RO to Curbee services").toString();
        try {
            curbeeResponse = curbeeTarget.request(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(roObject.toString(), MediaType.APPLICATION_JSON));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(curbeeResponse != null) {
            status = curbeeResponse.getStatus();
            curbeeResponseString = curbeeResponse.readEntity(String.class);
        }

        //check status and update status of file and objects in the database accordingly
        for (StreamFile streamFile : fileList) {
            MongoDB.STATUS_ENUM published = MongoDB.STATUS_ENUM.NotPublished;

            if (MongoDB.streamROStatusHas(project, streamFile.getYear(), streamFile.getWeek(), streamFile.getId(), id,
                    MongoDB.STATUS_ENUM.Deposited)) {
                if (status == Response.Status.OK.getStatusCode()) {
                    published = MongoDB.STATUS_ENUM.Published;
                    //delete file if published
                    File file_name = new File(streamFile.getFilePath());
                    file_name.delete();
                } else{
                    //remove .processing from file if not published
                    File file_name = new File(streamFile.getFilePath());
                    File file_rename = new File(streamFile.getFilePath().replaceAll(".processing", ""));
                    file_name.renameTo(file_rename);
                }
            }
            //add published status to DB
            MongoDB.addStreamStatus(project, streamFile.getYear(), streamFile.getWeek(), streamFile.getId(),
                    new Date(), id, published);
        }

        if(status != Response.Status.OK.getStatusCode()) {
            logger.info("RO failed to publish : " + id);
        }
        return Response.status(status).entity(curbeeResponseString).build();
    }

    private void addProjectSpecificMetadata(String project, JSONObject fileObject, StreamFile streamFile) {
        if(project.equals("airbox")) {
            try {
                FileReader fileReader = new FileReader(new File(streamFile.getFilePath()));
                BufferedReader br = new BufferedReader(fileReader);
                String line = null;
                while ((line = br.readLine()) != null) {
                    String[] array = line.split("\\|");
                    for(int i = 0 ; i < array.length ; i++) {
                        for(String key : projectMetadata.get("airbox")) {
                            if(array[i].split("=")[0].equals(key)) {
                                fileObject.put(key, array[i].split("=")[1]);
                            }
                        }
                    }
                    break;
                }
            } catch (IOException e) {
                logger.error("Error while adding project specific metadata for the project Airbox : " + e.getMessage());
            }
        }
    }

    private JSONObject createStreamRO(JSONObject metadataObject, String identifier) {
        JSONObject roObject = new JSONObject();

        JSONObject context = new JSONObject();
        for(String key : Constants.roMetadataMap.keySet()) {
            context.put(key, Constants.roMetadataMap.get(key));
        }

        roObject.put("@context", new JSONArray().put("https://w3id.org/ore/context").put(context));
        roObject.put(Constants.RIGHTS_HOLDER, metadataObject.getString(Constants.CREATOR));
        roObject.put(Constants.REPOSITORY, metadataObject.getString(Constants.REPOSITORY));
        roObject.put(Constants.PREFERENCES, new JSONObject());

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat fileDateFormat = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss 'GMT'Z (z)");
        String creation_date = simpleDateFormat.format(new Date());

        JSONObject aggregation = new JSONObject();
        aggregation.put("@type", "Aggregation");
        aggregation.put("@id", Constants.fedoraUrl + "/" +
                metadataObject.getString(Constants.PUBLISHING_PROJECT) + "/" + identifier + "/oreMap");
        aggregation.put(Constants.IDENTIFIER, identifier);
        aggregation.put(Constants.CREATION_DATE, metadataObject.getString(Constants.CREATION_DATE));
        aggregation.put(Constants.LAST_MODIFIED, metadataObject.getString(Constants.LAST_MODIFIED));
        //aggregation.put(Constants.PUBLICATION_DATE, creation_date);
        aggregation.put(Constants.TITLE, metadataObject.getString(Constants.TITLE));
        //aggregation.put(Constants.LABEL, metadataObject.getString("TITLE"));
        aggregation.put(Constants.CREATOR, metadataObject.getString(Constants.CREATOR));
        aggregation.put(Constants.ABSTRACT, metadataObject.getString(Constants.ABSTRACT));
        aggregation.put(Constants.PUBLISHING_PROJECT, metadataObject.getString(Constants.PUBLISHING_PROJECT));
        aggregation.put(Constants.PUBLISHING_PROJECT_NAME, metadataObject.getString(Constants.PUBLISHING_PROJECT));
        aggregation.put(Constants.CHANNEL, metadataObject.getString(Constants.CHANNEL));
        //aggregation.put(Constants.PUBLISHING_PROJECT_NAME, "DIBBS");
        roObject.put("Aggregation", aggregation);

        JSONObject preferences = new JSONObject();
        preferences.put(Constants.PURPOSE, metadataObject.getString(Constants.PURPOSE));
        preferences.put(Constants.LICENSE, "All Rights Reserved");
        roObject.put(Constants.PREFERENCES, preferences);
        roObject.put(Constants.PUBLICATION_CALLBACK, Constants.seadClientUrl + "/" + identifier + "/status");

        JSONObject aggregationStatistics = new JSONObject();
        aggregationStatistics.put(Constants.NUMBER_OF_DATASETS, metadataObject.getString(Constants.NUMBER_OF_DATASETS));
        aggregationStatistics.put(Constants.MAX_DATA_SIZE, metadataObject.getString(Constants.MAX_DATA_SIZE));
        aggregationStatistics.put(Constants.MAX_COLLECTION_DEPTH, metadataObject.getString(Constants.MAX_COLLECTION_DEPTH));
        aggregationStatistics.put(Constants.TOTAL_SIZE, metadataObject.getString(Constants.TOTAL_SIZE));
        aggregationStatistics.put(Constants.NUMBER_OF_COLLECTIONS, metadataObject.getString(Constants.NUMBER_OF_COLLECTIONS));
        aggregationStatistics.put(Constants.DATA_MIMETYPE, metadataObject.get(Constants.DATA_MIMETYPE));
        roObject.put(Constants.AGGREGATION_STATISTICS, aggregationStatistics);

        return roObject;
    }

    @POST
    @Path("/{id}/status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response streamROAddStatus(@PathParam("id") String id) throws IOException {
        return Response.ok().build();
    }

    @GET
    @Path("/streamro/{project}/status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response streamROGetStatus(@PathParam("project") String project) throws IOException {
        JSONArray results = MongoDB.streamROGetStatus(project);
        return Response.ok().entity(results.toString()).build();
    }

    private JSONObject createStreamOREMap(JSONObject metadataObject, String identifier) {
        JSONObject oreMap = new JSONObject();

        JSONObject context = new JSONObject();
        for(String key : Constants.OREMetadataMap.keySet()) {
            context.put(key, Constants.OREMetadataMap.get(key));
        }

        oreMap.put("@context", new JSONArray().put("https://w3id.org/ore/context").put(context));
        oreMap.put("@type", "ResourceMap");
        oreMap.put("@id", Constants.fedoraUrl + "/" +
                metadataObject.getString(Constants.PUBLISHING_PROJECT) + "/" + identifier + "/oreMap");

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat fileDateFormat = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss 'GMT'Z (z)");
        String creation_date = simpleDateFormat.format(new Date());

        oreMap.put(Constants.CREATION_DATE, metadataObject.getString(Constants.CREATION_DATE));

        JSONObject describes = new JSONObject();
        describes.put("@type", new JSONArray().put("Aggregation").put("http://cet.ncsa.uiuc.edu/2015/Dataset"));
        describes.put(Constants.IDENTIFIER, identifier);
        describes.put(Constants.CREATION_DATE, metadataObject.getString(Constants.CREATION_DATE));
        describes.put(Constants.LAST_MODIFIED, metadataObject.getString(Constants.LAST_MODIFIED));
        describes.put(Constants.PUBLICATION_DATE, creation_date);
        describes.put(Constants.TITLE, metadataObject.getString(Constants.TITLE));
        describes.put(Constants.LABEL, metadataObject.getString(Constants.TITLE));
        describes.put(Constants.CREATOR, metadataObject.getString(Constants.CREATOR));
        describes.put(Constants.ABSTRACT, metadataObject.getString(Constants.ABSTRACT));

        JSONArray aggregates = new JSONArray();
        JSONArray fileObjects = metadataObject.getJSONArray(FILES);
        JSONArray fileIds = new JSONArray();

        for(int i = 0 ; i < fileObjects.length() ; i ++ ) {
            JSONObject fileMetaObj = fileObjects.getJSONObject(i);
            JSONObject fileObject = new JSONObject();

            String fileIdentifier = "file-" + UUID.randomUUID().toString();
            fileIds.put(fileIdentifier);
            fileObject.put("@type", new JSONArray().put("AggregatedResource").put("http://cet.ncsa.uiuc.edu/2015/File"));
            fileObject.put("@id", fileIdentifier);
            fileObject.put(Constants.IDENTIFIER, fileIdentifier);
            fileObject.put(Constants.TITLE, fileMetaObj.getString(Constants.TITLE));
            fileObject.put(Constants.LABEL, fileMetaObj.getString(Constants.TITLE));
            fileObject.put(Constants.SIZE, fileMetaObj.getLong(Constants.SIZE));
            fileObject.put(Constants.MIMETYPE, fileMetaObj.getString(Constants.MIMETYPE));
            fileObject.put(Constants.CREATION_DATE, fileMetaObj.getString(Constants.CREATION_DATE));
            fileObject.put(Constants.LAST_MODIFIED, fileMetaObj.getString(Constants.LAST_MODIFIED));
            fileObject.put(Constants.PUBLICATION_DATE, creation_date);
            fileObject.put(Constants.SIMILAR_TO, fileMetaObj.getString(Constants.SIMILAR_TO));
            for(String projectMeta : projectMetadata.get(metadataObject.getString(Constants.PUBLISHING_PROJECT))) {
                fileObject.put(projectMeta, fileMetaObj.getString(projectMeta));
            }
            aggregates.put(fileObject);
        }
        describes.put(Constants.HAS_PART, fileIds);
        describes.put("aggregates", aggregates);
        oreMap.put("describes", describes);

        return oreMap;
    }

    //TODO add cleanup mechanism to fedora (check C3PR status)
}
