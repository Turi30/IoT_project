#include "contiki.h"

//#include "../../resource.h"
#include "coap-engine.h"
#include <limits.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/* Log configuration */
#include "sys/log.h"

#define LOG_MODULE "Sensor"
#define LOG_LEVEL LOG_LEVEL_INFO

static void res_get_handler(coap_message_t *request, coap_message_t *response,
                            uint8_t *buffer, uint16_t preferred_size,
                            int32_t *offset);

static void res_post_put_handler(coap_message_t *request,
                                 coap_message_t *response, uint8_t *buffer,
                                 uint16_t preferred_size, int32_t *offset);

#define MAX_AGE 60
#define CHANGE 1
#define TEMP_MAX 35
#define TEMP_MIN 10

static bool mode = false;
static char temperature[2];

RESOURCE(res_conditioner,
         "title=\"conditioner\";methods=\"GET/PUT/POST\" "
         "mode=on|off&temperature = <value>\";rt=\"float\";obs\n",
         res_get_handler, res_post_put_handler, res_post_put_handler, NULL);

static void res_get_handler(coap_message_t *request, coap_message_t *response,
                            uint8_t *buffer, uint16_t preferred_size,
                            int32_t *offset) {

    unsigned int accept = -1;
    coap_get_header_accept(request, &accept);

    if (accept == -1 || accept == TEXT_PLAIN) {
        coap_set_header_content_format(response, TEXT_PLAIN);

        char *res_mode = (mode == false) ? "off" : "on";
        if (!mode)
            snprintf((char *)buffer, COAP_MAX_CHUNK_SIZE, "mode=%s", res_mode);
        else
            snprintf((char *)buffer, COAP_MAX_CHUNK_SIZE,
                     "mode=%s, temperature=%s", res_mode, temperature);

        coap_set_payload(response, (uint8_t *)buffer, strlen((char *)buffer));
    } else if (accept == APPLICATION_JSON) {
        coap_set_header_content_format(response, APPLICATION_JSON);

        char *res_mode = (mode == false) ? "off" : "on";
        if (!mode)
            snprintf((char *)buffer, COAP_MAX_CHUNK_SIZE, "{'mode':%s}",
                     res_mode);
        else
            snprintf((char *)buffer, COAP_MAX_CHUNK_SIZE,
                     "{'mode'=%s, 'temperature'=%s", res_mode, temperature);

        coap_set_payload(response, buffer, strlen((char *)buffer));
    } else {
        coap_set_status_code(response, NOT_ACCEPTABLE_4_06);
        const char *msg =
            "Supporting content-types text/plain and application/json";
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
    const char *temp = NULL;
    const char *mod = NULL;
    int success = 1;

    if ((len = coap_get_post_variable(request, "mode", &mod))) {
        if (strncmp(mod, "on", len) == 0) {
            mode = true;
        } else if (strncmp(mod, "off", len) == 0) {
            mode = false;
        } else {
            success = 0;
        }
    } else {
        success = 0;
    }
    if (success &&
        (len = coap_get_post_variable(request, "temperature", &temp))) {

        strcpy(temperature, temp);
    } else {
        success = 0;
    }
    if (!success) {
        coap_set_status_code(response, BAD_REQUEST_4_00);
    }
}