#include "contiki.h"

#include "../../common.h"
#include "coap-engine.h"
#include <limits.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/* Log configuration */
#include "sys/log.h"

#define LOG_MODULE "Air purifier"
#define LOG_LEVEL LOG_LEVEL_DBG

static void res_get_handler(coap_message_t *request, coap_message_t *response,
                            uint8_t *buffer, uint16_t preferred_size,
                            int32_t *offset);

static void res_post_put_handler(coap_message_t *request,
                                 coap_message_t *response, uint8_t *buffer,
                                 uint16_t preferred_size, int32_t *offset);

static void res_periodic_handler(void);

#define MAX_AGE 60

bool air_purifier_mode = false;
bool force_off = false;
bool post_on = false;
int air_purifier_value;

extern float carbon_dioxide;

PERIODIC_RESOURCE(
    res_air_purifier,
    "title=\"Air purifier actuator\";methods=\"GET/PUT/POST\", "
    "mode=on|off&recommended carbon dioxide[1e-3]=<value>\";rt=\"float\"\n",
    res_get_handler, res_post_put_handler, res_post_put_handler, NULL, 5000,
    res_periodic_handler);

static void res_get_handler(coap_message_t *request, coap_message_t *response,
                            uint8_t *buffer, uint16_t preferred_size,
                            int32_t *offset) {

    unsigned int accept = -1;
    if (!coap_get_header_accept(request, &accept))
        accept = APPLICATION_JSON;

    if (accept == APPLICATION_JSON) {
        coap_set_header_content_format(response, APPLICATION_JSON);

        char *res_mode = (air_purifier_mode == false) ? "off" : "on";

        if (!air_purifier_mode)
            snprintf((char *)buffer, COAP_MAX_CHUNK_SIZE, "{\"mode\":\"%s\"}",
                     res_mode);
        else
            snprintf(
                (char *)buffer, COAP_MAX_CHUNK_SIZE,
                "{\"mode\":\"%s\", \"recommended carbon dioxide[1e-3]\":%d}",
                res_mode, air_purifier_value);

        coap_set_payload(response, buffer, strlen((char *)buffer));
    } else {
        coap_set_status_code(response, NOT_ACCEPTABLE_4_06);
        const char *msg = "Supporting content-type application/json";
        coap_set_payload(response, msg, strlen(msg));
    }

    coap_set_header_max_age(response, MAX_AGE);
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
            post_on = true;
            force_off = false;
            air_purifier_mode = true;
        } else if (strncmp(value, "\"off\"", len) == 0) {
            if (!post_on)
                force_off = true;
            air_purifier_mode = false;
        } else
            success = 0;
    } else
        success = 0;
    if (air_purifier_mode == true) {
        if (success &&
            (len = coap_get_variable_json(
                 (const char *)request->payload, request->payload_len,
                 "\"recommended carbon dioxide[1e-3]\"", &value))) {
            air_purifier_value = atoi(value);
        } else
            success = 0;
    }
    if (!success) {
        coap_set_status_code(response, BAD_REQUEST_4_00);
    }
}

static void res_periodic_handler() {
    if (carbon_dioxide > 0.04 && !air_purifier_mode && !force_off) {
        air_purifier_mode = true;
        air_purifier_value = 40;
    } else if (air_purifier_mode &&
               carbon_dioxide < ((float)air_purifier_value / 1000))
        air_purifier_value = false;
}
