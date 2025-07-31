set(PLATFORM_ANDROID FALSE)

if ("${CMAKE_SYSTEM_NAME}" STREQUAL "Android")
  set(PLATFORM_ANDROID TRUE)
else ()
  message(FATAL_ERROR "Unsupported platform: ${CMAKE_SYSTEM_NAME}. Only Android is supported.")
endif ()
