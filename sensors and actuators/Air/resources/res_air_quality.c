#include "contiki.h"

#include "coap-engine.h"
#include <limits.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "random.h"

/* Log configuration */
#include "sys/log.h"

#define LOG_MODULE "Air quality"
#define LOG_LEVEL LOG_LEVEL_DBG

static void res_get_handler(coap_message_t *request, coap_message_t *response,
                            uint8_t *buffer, uint16_t preferred_size,
                            int32_t *offset);
static void res_periodic_handler(void);

#define MAX_AGE (60)
#define TEMP_MAX (100)
#define TEMP_MIN (0)

#define OFFSET_VALUE (1)
#define PROBABILITY_UPDATE (0.2)

static float carbon_dioxide = -1;

extern bool air_purifier_mode;
extern int air_purifier_value;

PERIODIC_RESOURCE(
    res_air_quality,
    "title=\"Air quality sensor\";methods=\"GET\";rt=\"float\";obs\n",
    res_get_handler, NULL, NULL, NULL, 5000, res_periodic_handler);

static void res_get_handler(coap_message_t *request, coap_message_t *response,
                            uint8_t *buffer, uint16_t preferred_size,
                            int32_t *offset) {

    unsigned int accept = -1;
    if (!coap_get_header_accept(request, &accept))
        accept = APPLICATION_JSON;

    if (accept == APPLICATION_JSON) {
        coap_set_header_content_format(response, APPLICATION_JSON);
        snprintf((char *)buffer, COAP_MAX_CHUNK_SIZE,
                 "{\"carbon dioxide\":%.1f}", carbon_dioxide);

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

static void res_periodic_handler() {
    if (carbon_dioxide == -1)
        carbon_dioxide =
            ((float)rand() / RAND_MAX) * (TEMP_MAX - TEMP_MIN) + TEMP_MIN;

    if (!air_purifier_mode && !air_purifier_value)
        carbon_dioxide +=
            (((float)rand() / RAND_MAX) > PROBABILITY_UPDATE)
                ? ((float)rand() / RAND_MAX) * (2 * OFFSET_VALUE) - OFFSET_VALUE
                : 0;
    else if (carbon_dioxide > air_purifier_value)
        carbon_dioxide -= ((float)rand() / RAND_MAX) * (OFFSET_VALUE);
    else if (carbon_dioxide < air_purifier_value)
        carbon_dioxide += ((float)rand() / RAND_MAX) * (OFFSET_VALUE);

    coap_notify_observers(&res_air_quality);
}