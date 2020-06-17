#include "coap-blocking-api.h"
#include "coap-engine.h"
#include "contiki-net.h"
#include "contiki.h"
#include "dev/button-hal.h"
#include "dev/leds.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/* Log configuration */
#include "sys/log.h"

#define LOG_MODULE "Washing machine"
#define LOG_LEVEL LOG_LEVEL_DBG

#define SERVER_EP ("coap://[fd00::1]:5683")
#define SERVER_REGISTRATION ("/registration")

extern coap_resource_t res_washing_machine;
extern bool washing_machine_mode;

static coap_message_type_t result = COAP_TYPE_RST;

PROCESS(washing_machine_node, "Washing Nachine Node");
AUTOSTART_PROCESSES(&washing_machine_node);

static void response_handler(coap_message_t *response) {
    if (response == NULL)
        return;
    LOG_DBG("Response %i\n", response->type);
    result = response->type;
}

PROCESS_THREAD(washing_machine_node, ev, data) {

    static coap_endpoint_t server_ep;
    static coap_message_t request[1];
    static struct etimer timer;

    PROCESS_BEGIN();

    LOG_INFO("Starting washing machine node\n");

    coap_activate_resource(&res_washing_machine,
                           "actuators/ambient/washing-machine");

    coap_endpoint_parse(SERVER_EP, strlen(SERVER_EP), &server_ep);

    do {
        coap_init_message(request, COAP_TYPE_CON, COAP_GET, 0);
        coap_set_header_uri_path(request, (const char *)&SERVER_REGISTRATION);
        COAP_BLOCKING_REQUEST(&server_ep, request, response_handler);
    } while (result == COAP_TYPE_RST);

     etimer_set(&timer, CLOCK_SECOND);

    while (1) {
        PROCESS_YIELD_UNTIL(etimer_expired(&timer) ||
                            ev == button_hal_press_event);
        if (ev == button_hal_press_event) {
            washing_machine_mode = !washing_machine_mode;
        } else if (etimer_expired(&timer)) {
            if (washing_machine_mode)
                leds_set(LEDS_NUM_TO_MASK(LEDS_GREEN));
            else
                leds_set(LEDS_NUM_TO_MASK(LEDS_RED));
            etimer_reset(&timer);
        }
    }

    PROCESS_END();
}
