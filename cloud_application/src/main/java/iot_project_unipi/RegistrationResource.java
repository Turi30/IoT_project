package iot_project_unipi;

import java.net.InetAddress;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;

public class RegistrationResource extends CoapResource {
    public RegistrationResource(String name) {
        super(name);
    }

    @Override
    public void handleGET(CoapExchange exchange) {
        exchange.accept();

        InetAddress addr = exchange.getSourceAddress();
        String payload = exchange.getRequestText();

        String uri =
                new String("coap://[" + addr.toString().substring(1) + "]:5683/.well-known/core");
        CoapClient req = new CoapClient(uri);

        String response = req.get().getResponseText().replace("</.well-known/core>;", "");

        for (String res : response.split("\n")) {

            Resource new_resource = new Resource(addr.toString().substring(1), res);

            for (int i = 0; i < App.resources_array.size(); i++)
                if (new_resource.getAddr().equals(App.resources_array.get(i).getAddr())
                        && new_resource.getPath().equals(App.resources_array.get(i).getPath()))
                    return;

            if (new_resource.isObservable()) {
                CoapObserveRelation relation = new_resource.observe(new CoapHandler() {

                    @Override
                    public void onLoad(CoapResponse response) {
                        new_resource.insertObservePayload(response.getResponseText());
                    }

                    @Override
                    public void onError() {
                        System.err.println("Failed");
                    }
                });
                new_resource.setRelaton(relation);
            }

            App.resources_array.add(new_resource);
        }
    }
    /*
     * @Override public void handlePOST(CoapExchange exchange) { exchange.accept();
     * 
     * InetAddress addr = exchange.getSourceAddress(); String payload = exchange.getRequestText();
     * 
     * String uri = new String("coap://[" + addr.toString().substring(1) +
     * "]:5683/.well-known/core"); CoapClient req = new CoapClient(uri);
     * 
     * String response = req.get().getResponseText().replace("</.well-known/core>;", "");
     * 
     * for (String res : response.split("\n")) {
     * 
     * Resource new_resource = new Resource(addr.toString().substring(1), res, payload);
     * 
     * for (int i = 0; i < App.resources_array.size(); i++) if
     * (new_resource.getAddr().equals(App.resources_array.get(i).getAddr()) &&
     * new_resource.getPath().equals(App.resources_array.get(i).getPath())) return;
     * 
     * App.resources_array.add(new_resource); }
     */
    /*
     * byte[] request = exchange.getRequestPayload(); String s = new String(request);
     * System.out.println(s);
     * 
     * exchange.respond(ResponseCode.CREATED);
     *//*
        * }
        */
}
