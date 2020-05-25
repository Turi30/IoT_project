package iot_project_unipi;

import org.eclipse.californium.core.CoapClient;

public class Resource extends CoapClient {
    private String addr;
    private String name;
    private String path;
    private String methods;
    private boolean isObservable = false;

    public Resource(String addr, String content, String payload) {
        super();

        String[] content_split = content.split(";");

        this.addr = addr;
        this.name = payload;
        this.path = content_split[1].substring(content_split[1].indexOf("<") + 1, content_split[1].indexOf(">"));
        this.methods = content_split[3];
        this.isObservable = content.contains("obs");

        this.setURI("coap://[" + this.addr + "]" + this.path);
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

    public boolean hasMethod(String method) {
        return this.methods.contains(method.toUpperCase());
    }

    public void renameResource(String name) {
        this.name = name;
    }

    public boolean isObservable() {
        return this.isObservable;
    }

}