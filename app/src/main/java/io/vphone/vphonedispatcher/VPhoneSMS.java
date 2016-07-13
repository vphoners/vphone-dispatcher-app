package io.vphone.vphonedispatcher;

/**
 * VPhone SMS model
 */
public class VPhoneSMS {
    private long id;
    private String smsbody;
    private String smsfrom;
    private String smstimestamp;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSmsbody() {
        return smsbody;
    }

    public void setSmsbody(String smsbody) {
        this.smsbody = smsbody;
    }

    public String getSmsfrom() {
        return smsfrom;
    }

    public void setSmsfrom(String smsfrom) {
        this.smsfrom = smsfrom;
    }

    public String getSmstimestamp() {
        return smstimestamp;
    }

    public void setSmstimestamp(String smstimestamp) {
        this.smstimestamp = smstimestamp;
    }

    @Override
    public String toString() {
        return "VPhoneSMS{" +
                "id=" + id +
                ", smsbody='" + smsbody + '\'' +
                ", smsfrom='" + smsfrom + '\'' +
                ", smstimestamp='" + smstimestamp + '\'' +
                '}';
    }
}
