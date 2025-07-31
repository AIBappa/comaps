set(PLATFORM_ANDROID FALSE)

if ("${CMAKE_SYSTEM_NAME}" STREQUAL "Android")
  set(PLATFORM_ANDROID TRUE)
elseif ("${CMAKE_SYSTEM_NAME}" STREQUAL "Linux")
  set(PLATFORM_ANDROID FALSE)
else ()
  message(FATAL_ERROR "Unsupported platform: ${CMAKE_SYSTEM_NAME}. Only Android and Linux are supported.")
endif ()
