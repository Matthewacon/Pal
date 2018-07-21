#pragma once

#ifndef PAL_COMMON_H
#define PAL_COMMON_H

#include <jvmti.h>

static const jvmtiCapabilities capabilities = {
 .can_access_local_variables = 1
};

#endif //PAL_COMMON_H
