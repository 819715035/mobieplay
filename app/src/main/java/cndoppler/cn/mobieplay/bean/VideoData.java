package cndoppler.cn.mobieplay.bean;

/**
 * Created by Administrator on 2017/9/13 0013.
 */

public class VideoData {
    private String name;
    private long time;
    private long size;
    private String artist;
    private String url;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "VideoData{" +
                "name='" + name + '\'' +
                ", time=" + time +
                ", size=" + size +
                ", artist='" + artist + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
