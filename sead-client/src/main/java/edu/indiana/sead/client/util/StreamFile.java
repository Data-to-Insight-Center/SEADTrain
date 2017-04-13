package edu.indiana.sead.client.util;

/**
 * Created by charmadu on 4/12/17.
 */
public class StreamFile {

    private String filename;
    private String filePath;
    private String year;
    private String week;
    private String id;

    public StreamFile(String filename, String filePath, String year, String week, String id) {
        this.filename = filename;
        this.filePath = filePath;
        this.year = year;
        this.week = week;
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getWeek() {
        return week;
    }

    public void setWeek(String week) {
        this.week = week;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
