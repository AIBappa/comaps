project(platform)

set(SRC
  ../private.h  # To properly detect its changes with CMake.
  battery_tracker.cpp
  battery_tracker.hpp
  chunks_download_strategy.cpp
  chunks_download_strategy.hpp
  constants.hpp
  country_defines.cpp
  country_defines.hpp
  country_file.cpp
  country_file.hpp
  distance.cpp
  distance.hpp
  duration.cpp
  duration.hpp
  downloader_defines.hpp
  downloader_utils.cpp
  downloader_utils.hpp
  get_text_by_id.cpp
  get_text_by_id.hpp
  gui_thread.hpp
  http_client.cpp
  http_client.hpp
  http_payload.cpp
  http_payload.hpp
  http_request.cpp
  http_request.hpp
  http_thread_callback.hpp
  http_uploader.hpp
  http_uploader_background.hpp
  local_country_file.cpp
  local_country_file.hpp
  local_country_file_utils.cpp
  local_country_file_utils.hpp
  locale.hpp
  localization.cpp
  localization.hpp
  location.hpp
  measurement_utils.cpp
  measurement_utils.hpp
  mwm_traits.cpp
  mwm_traits.hpp
  mwm_version.cpp
  mwm_version.hpp
  platform.cpp
  platform.hpp
  preferred_languages.cpp
  preferred_languages.hpp
  remote_file.cpp
  remote_file.hpp
  secure_storage.hpp
  servers_list.cpp
  servers_list.hpp
  products.cpp
  products.hpp
  settings.cpp
  settings.hpp
  socket.hpp
  trace.hpp
  string_storage_base.cpp
  string_storage_base.hpp
  utm_mgrs_utils.cpp
  utm_mgrs_utils.hpp
)

if (PLATFORM_ANDROID)
  append(SRC
    http_uploader_background_dummy.cpp
    http_uploader_dummy.cpp
    localization_dummy.cpp
    network_policy_dummy.cpp
    platform_android.cpp
    platform_unix_impl.cpp
    platform_unix_impl.hpp
    secure_storage_dummy.cpp
    trace_android.cpp
  )
else()
  # Linux and other Unix-like platforms
  append(SRC
    http_uploader_background_dummy.cpp
    http_uploader_dummy.cpp
    localization_dummy.cpp
    network_policy_dummy.cpp
    platform_unix_impl.cpp
    platform_unix_impl.hpp
    secure_storage_dummy.cpp
    trace_android.cpp  # Use Android trace for now, it should work on Linux too
  )
endif()

omim_add_library(${PROJECT_NAME} ${SRC})

target_link_libraries(${PROJECT_NAME}
  geometry  # mercator::YToLat
  coding
)

omim_add_test_subdirectory(platform_tests_support)
omim_add_test_subdirectory(platform_tests)
# Location service subdirectory removed - not needed for Android-only builds

# Skip unity build for preferred_languages.cpp to avoid potential conflicts.
set_property(SOURCE preferred_languages.cpp PROPERTY SKIP_UNITY_BUILD_INCLUSION ON)
