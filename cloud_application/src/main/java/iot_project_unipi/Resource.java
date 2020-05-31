package iot_project_unipi;

import java.util.LinkedList;
import java.util.Queue;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;

public class Resource extends CoapClient {
    private String addr;
    private String name;
    private String path;
    private String methods;
    private boolean isObservable = false;
    private boolean isInRoom = false;
    private CoapObserveRelation relation;
    private CoapHandlerObs handlerObs;

    public Resource(String addr, String content) {
        super();

        String[] content_split = content.split(";");

        this.addr = addr;
        this.name = content_split[1].substring(content_split[1].indexOf("=") + 2,
                content_split[1].lastIndexOf("\""));
        this.path = content_split[0].substring(content_split[0].indexOf("<") + 1,
                content_split[0].indexOf(">"));
        this.methods = content_split[2];
        this.isObservable = content.contains("obs");

        this.setURI("coap://[" + this.addr + "]" + this.path);
        if (this.isObservable) {
            this.handlerObs = new CoapHandlerObs();
            this.relation = this.observe(this.handlerObs);
        }
    }

    public String getAddr() {
        return this.addr;
    }

    public String getPath() {
        return this.path;
    }

    public String getName() {
        return this.name;
    }

    public String getPostPutFormat() {
        return this.methods.substring(this.methods.indexOf(",") + 2);
    }

    public boolean hasMethod(String method) {
        return this.methods.contains(method.toUpperCase());
    }

    public void renameResource(String name) {
        this.name = name;
    }

    public boolean isObservable() {
        return this.isObservable;
    }

    public void setInRoom(boolean b) {
        this.isInRoom = b;
    }

    public boolean getInRoom() {
        return this.isInRoom;
    }

    public Queue<String> getQueueObserve() {
        return this.handlerObs.getQueue();

    }

}
