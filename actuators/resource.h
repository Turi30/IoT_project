#ifndef RESOURCE_H
#define RESOURCE_H

#define OFFSET_VALUE (1)
#define PROBABILITY_UPDATE (0.2)

#include "random.h"
#include <stdlib.h>
#include <stdio.h>

typedef struct resource {
    float value;
    void (*init)(void *, int, int);
    void (*update)(void *);
} resource_t;

static void init_resource(void *self, int val_min, int val_max)
{
    ((resource_t *)self)->value =
        ((float)rand() / RAND_MAX) * (val_max - val_min) + val_min;
}

static void update_resource(void *self)
{
    if (((float)rand() / RAND_MAX) > PROBABILITY_UPDATE)
        ((resource_t *)self)->value +=
            ((float)rand() / RAND_MAX) * (2 * OFFSET_VALUE) - OFFSET_VALUE;
}

#endif /* RESOURCE_H */
