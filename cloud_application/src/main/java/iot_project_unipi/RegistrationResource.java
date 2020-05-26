package iot_project_unipi;

import java.net.InetAddress;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResource;
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

        String res = req.get().getResponseText();

        App.resources_array.add(new Resource(addr.toString().substring(1), res, payload));
    }

    @Override
    public void handlePOST(CoapExchange exchange) {
        exchange.accept();

        InetAddress addr = exchange.getSourceAddress();
        String payload = exchange.getRequestText();

        String uri =
                new String("coap://[" + addr.toString().substring(1) + "]:5683/.well-known/core");
        CoapClient req = new CoapClient(uri);

        String response = req.get().getResponseText().substring(20);

        for (String res : response.split("\n")) {

            Resource new_res = new Resource(addr.toString().substring(1), res, payload);

            for (int i = 0; i < App.resources_array.size(); i++)
                if (new_res.getAddr().equals(App.resources_array.get(i).getAddr())
                        && new_res.getPath().equals(App.resources_array.get(i).getPath()))
                    return;

            App.resources_array.add(new_res);
        }
        /*
         * byte[] request = exchange.getRequestPayload(); String s = new String(request);
         * System.out.println(s);
         * 
         * exchange.respond(ResponseCode.CREATED);
         */
    }
}
