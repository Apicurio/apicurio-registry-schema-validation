package io.apicurio.schema.validation.json;

public class TestMessageBean {
    
    private String message;
    private long time;

    /**
     * Constructor.
     */
    public TestMessageBean() {
        //
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return the time
     */
    public long getTime() {
        return time;
    }

    /**
     * @param time the time to set
     */
    public void setTime(long time) {
        this.time = time;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "MessageBean [message=" + message + ", time=" + time + "]";
    }

}
