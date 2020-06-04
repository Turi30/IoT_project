package iot_project_unipi;

import java.net.InetAddress;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;

/*
 * Class to receive the registration request of the resources
 */
public class ResourcesRegistration extends CoapResource {
    public ResourcesRegistration(String name) {
        super(name);
    }

    @Override
    public void handleGET(CoapExchange exchange) {
        exchange.accept();

        // Get the address of the request
        InetAddress addr = exchange.getSourceAddress();

        // Create a coap client to perform well-known/core and get all the resources at the address
        String uri =
                new String("coap://[" + addr.toString().substring(1) + "]:5683/.well-known/core");
        CoapClient req = new CoapClient(uri);

        String response = req.get().getResponseText().replace("</.well-known/core>;", "");

        // Create a new class resource for each resource
        for (String res : response.split("\n")) {

            Resource new_resource = new Resource(addr.toString().substring(1), res);

            for (int i = 0; i < App.resources_array.size(); i++)
                if (new_resource.getAddr().equals(App.resources_array.get(i).getAddr())
                        && new_resource.getPath().equals(App.resources_array.get(i).getPath()))
                    return;

            App.resources_array.add(new_resource);
            System.out.println("The resource " + new_resource.getName() + " has been registered");
        }
    }
}
