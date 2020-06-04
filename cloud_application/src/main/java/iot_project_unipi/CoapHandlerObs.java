package iot_project_unipi;

import java.util.LinkedList;
import java.util.Queue;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

/*
 * Class to implement the handler for the observable resources
 */
public class CoapHandlerObs implements CoapHandler {
    private final int max_index_array = 20;
    private Queue<String> observeQueue;

    public CoapHandlerObs() {
        super();
        this.observeQueue = new LinkedList<String>();
    }

    @Override
    public void onLoad(CoapResponse response) {
        // Check if the response is in JSON format
        if (!response.getOptions().isContentFormat(MediaTypeRegistry.APPLICATION_JSON))
            return;

        if (this.observeQueue.size() == this.max_index_array) {
            this.observeQueue.poll();
        }

        this.observeQueue.add(response.getResponseText());
    }

    @Override
    public void onError() {
        System.err.println("Failed");
    }

    public Queue<String> getQueue() {
        return this.observeQueue;
    }


}
