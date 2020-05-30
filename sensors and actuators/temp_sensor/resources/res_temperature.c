#include "contiki.h"

//#include "../../resource.h"
#include "coap-engine.h"
#include <limits.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "random.h"

/* Log configuration */
#include "sys/log.h"

#define LOG_MODULE "Sensor"
#define LOG_LEVEL LOG_LEVEL_DBG

static void res_get_handler(coap_message_t *request, coap_message_t *response,
                            uint8_t *buffer, uint16_t preferred_size,
                            int32_t *offset);
static void res_periodic_handler(void);

#define MAX_AGE (60)
#define TEMP_MAX (40)
#define TEMP_MIN (20)

#define OFFSET_VALUE (1)
#define PROBABILITY_UPDATE (0.2)

static float temperature = -1;

extern int conditioner_temperature;
extern bool conditioner_mode;

PERIODIC_RESOURCE(
    res_temperature,
    "title=\"Temperature sensor\";methods=\"GET\";rt=\"float\";obs\n",
    res_get_handler, NULL, NULL, NULL, 5000, res_periodic_handler);

static void res_get_handler(coap_message_t *request, coap_message_t *response,
                            uint8_t *buffer, uint16_t preferred_size,
                            int32_t *offset) {

    unsigned int accept = APPLICATION_JSON;
    coap_get_header_accept(request, &accept);

    if (accept == APPLICATION_JSON) {
        coap_set_header_content_format(response, APPLICATION_JSON);
        snprintf((char *)buffer, COAP_MAX_CHUNK_SIZE, "{\"temperature\":%.1f}",
                 temperature);

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
    if (temperature == -1)
        temperature =
            ((float)rand() / RAND_MAX) * (TEMP_MAX - TEMP_MIN) + TEMP_MIN;

    if (!conditioner_mode)
        temperature +=
            (((float)rand() / RAND_MAX) > PROBABILITY_UPDATE)
                ? ((float)rand() / RAND_MAX) * (2 * OFFSET_VALUE) - OFFSET_VALUE
                : 0;
    else if (temperature > conditioner_temperature)
        temperature -= OFFSET_VALUE * PROBABILITY_UPDATE;
    else
        temperature += OFFSET_VALUE * PROBABILITY_UPDATE;

    coap_notify_observers(&res_temperature);
}