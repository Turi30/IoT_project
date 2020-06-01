#include "contiki.h"

#include "coap-engine.h"
#include "../../common.h"
#include <limits.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/* Log configuration */
#include "sys/log.h"

#define LOG_MODULE "Radiator actuator"
#define LOG_LEVEL LOG_LEVEL_DBG

static void res_get_handler(coap_message_t *request, coap_message_t *response,
                            uint8_t *buffer, uint16_t preferred_size,
                            int32_t *offset);

static void res_post_put_handler(coap_message_t *request,
                                 coap_message_t *response, uint8_t *buffer,
                                 uint16_t preferred_size, int32_t *offset);

#define MAX_AGE 60

bool radiator_mode = false;
int radiator_temperature = 0;

RESOURCE(res_radiator,
         "title=\"Radiator actuator\";methods=\"GET/PUT/POST\", "
         "mode=on|off&temperature=<value>\";rt=\"float\"\n",
         res_get_handler, res_post_put_handler, res_post_put_handler, NULL);

static void res_get_handler(coap_message_t *request, coap_message_t *response,
                            uint8_t *buffer, uint16_t preferred_size,
                            int32_t *offset) {

    unsigned int accept = -1;
    if (!coap_get_header_accept(request, &accept))
        accept = APPLICATION_JSON;

    if (accept == APPLICATION_JSON) {
        coap_set_header_content_format(response, APPLICATION_JSON);

        char *res_mode = (radiator_mode == false) ? "off" : "on";
        if (!radiator_mode)
            snprintf((char *)buffer, COAP_MAX_CHUNK_SIZE, "{\"mode\":\"%s\"}",
                     res_mode);
        else
            snprintf((char *)buffer, COAP_MAX_CHUNK_SIZE,
                     "{\"mode\":\"%s\", \"temperature\":%d}", res_mode,
                     radiator_temperature);

        coap_set_payload(response, buffer, strlen((char *)buffer));
    } else {
        coap_set_status_code(response, NOT_ACCEPTABLE_4_06);
        const char *msg = "Supporting content-type application/json";
        coap_set_payload(response, msg, strlen(msg));
    }

    coap_set_header_max_age(response, MAX_AGE);

    /* The coap_subscription_handler() will be called for observable resources
     * by the coap_framework. */
}

static void res_post_put_handler(coap_message_t *request,
                                 coap_message_t *response, uint8_t *buffer,
                                 uint16_t preferred_size, int32_t *offset) {
    size_t len = 0;
    const char *value = NULL;
    int success = 1;

    if ((len = coap_get_variable_json((const char *)request->payload,
                                      request->payload_len, "\"mode\"",
                                      &value))) {
        if (strncmp(value, "\"on\"", len) == 0) {
            radiator_mode = true;
        } else if (strncmp(value, "\"off\"", len) == 0) {
            radiator_mode = false;
        } else
            success = 0;
    } else
        success = 0;
    if (radiator_mode == true) {
        if (success && (len = coap_get_variable_json(
                            (const char *)request->payload,
                            request->payload_len, "\"temperature\"", &value))) {
            radiator_temperature = atoi(value);
        } else
            success = 0;
    }
    if (!success) {
        coap_set_status_code(response, BAD_REQUEST_4_00);
    }
}
