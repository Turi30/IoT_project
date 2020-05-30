#include "contiki.h"

#include "../../resource.h"
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
static void res_periodic_handler(void);

#define MAX_AGE 60
#define INTERVAL_MIN 5
#define INTERVAL_MAX (MAX_AGE - 1)
#define CHANGE 1
#define HUM_MAX 100
#define HUM_MIN 0

static int32_t interval_counter = INTERVAL_MIN;

static resource_t humidity = {
    .value = -1,
    .init = init_resource,
    .update = update_resource,
};

PERIODIC_RESOURCE(res_humidity,
                  "title=\"humidity\";methods=\"GET\";rt=\"float\";obs\n",
                  res_get_handler, NULL, NULL, NULL, 1000,
                  res_periodic_handler);

static void res_get_handler(coap_message_t *request, coap_message_t *response,
                            uint8_t *buffer, uint16_t preferred_size,
                            int32_t *offset) {

    unsigned int accept = -1;
    coap_get_header_accept(request, &accept);

    if (accept == APPLICATION_JSON) {
        coap_set_header_content_format(response, APPLICATION_JSON);
        snprintf((char *)buffer, COAP_MAX_CHUNK_SIZE, "{\"temperature\":%d}",
                 humidity.value);

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

/*
 * Additionally, a handler function named [resource name]_handler must be
 * implemented for each PERIODIC_RESOURCE. It will be called by the coap_manager
 * process with the defined period.
 */
static void res_periodic_handler() {
    if (humidity.value == -1)
        humidity.init((void *)&humidity, HUM_MIN, HUM_MAX);

    humidity.update((void *)&humidity);

    ++interval_counter;

    /* Notify the registered observers which will trigger the
     * res_get_handler to create the response. */
    coap_notify_observers(&res_humidity);
}
