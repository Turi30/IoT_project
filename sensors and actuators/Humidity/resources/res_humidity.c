#include "contiki.h"

#include "coap-engine.h"
#include <limits.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/* Log configuration */
#include "sys/log.h"

#define LOG_MODULE "Humidity sensor"
#define LOG_LEVEL LOG_LEVEL_INFO

static void res_get_handler(coap_message_t *request, coap_message_t *response,
                            uint8_t *buffer, uint16_t preferred_size,
                            int32_t *offset);
static void res_periodic_handler(void);

#define MAX_AGE (60)
#define HUM_MAX (100)
#define HUM_MIN (0)

#define OFFSET_VALUE (1)
#define PROBABILITY_UPDATE (0.2)

static float humidity = -1;
float old_humidity = -1;

extern int humidifier_mode;
extern bool humidifier_value;

PERIODIC_RESOURCE(
    res_humidity,
    "title=\"Humidity sensor\";methods=\"GET\";rt=\"float\";obs\n",
    res_get_handler, NULL, NULL, NULL, 5000, res_periodic_handler);

static void res_get_handler(coap_message_t *request, coap_message_t *response,
                            uint8_t *buffer, uint16_t preferred_size,
                            int32_t *offset) {

    unsigned int accept = -1;
    if (!coap_get_header_accept(request, &accept))
        accept = APPLICATION_JSON;

    if (accept == APPLICATION_JSON) {
        coap_set_header_content_format(response, APPLICATION_JSON);
        snprintf((char *)buffer, COAP_MAX_CHUNK_SIZE, "{\"humidity\":%.1f}",
                 humidity);

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
    if (humidity == -1)
        humidity = ((float)rand() / RAND_MAX) * (HUM_MAX - HUM_MIN) + HUM_MIN;

    if (!humidifier_mode)
        humidity +=
            (((float)rand() / RAND_MAX) > PROBABILITY_UPDATE)
                ? ((float)rand() / RAND_MAX) * (2 * OFFSET_VALUE) - OFFSET_VALUE
                : 0;
    else if (humidity < humidifier_value)
        humidity += ((float)rand() / RAND_MAX) * (OFFSET_VALUE);
    else
        humidity -= ((float)rand() / RAND_MAX) * (OFFSET_VALUE);

    if (old_humidity != humidity) {
        old_humidity = humidity;
        coap_notify_observers(&res_humidity);
    }
}
