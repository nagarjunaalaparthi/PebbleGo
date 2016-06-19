#include "comm.h"

static int s_index, s_num_records;

// static AppTimer *s_timeout_timer;
// static void send_with_timeout(int key, int value) {
//   // Construct and send the message
//   DitionaryIterator *iter;
//   if(app_message_outbox_begin(&iter) == APP_MSG_OK) {
//     dict_write_int(iter, key, &value, sizeof(int), true);
//     app_message_outbox_send();
//   }

//   // Schedule the timeout timer
//   const int interval_ms = 1000;
//   s_timout_timer = app_timer_register(interval_ms, timout_timer_handler, NULL);
// }

static void send_data_item(int index) {
//   int *data = data_get_steps_data();

  DictionaryIterator *out;
  app_message_outbox_begin(&out);
  if(out!=NULL) {
     HealthMetric metric = HealthMetricStepCount;
  HealthServiceAccessibilityMask result = 
    health_service_metric_accessible(metric, time_start_of_today(), time(NULL));
  int steps = 0;
  if(result == HealthServiceAccessibilityMaskAvailable) {
    steps = (int)health_service_sum_today(metric);
  }
  main_window_update_steps(steps);

    dict_write_int(out, AppKeyIndex, &index, sizeof(int), true);
    dict_write_int(out, AppKeyData, &steps, sizeof(int), true);
 

    // Include the total number of data items
    if(s_index == 0) {
      dict_write_int(out, AppKeyNumDataItems, &s_num_records, sizeof(int), true);
    }

    if(app_message_outbox_send() != APP_MSG_OK) {
      APP_LOG(APP_LOG_LEVEL_ERROR, "Error sending message");
    }
  } 
  else {
    APP_LOG(APP_LOG_LEVEL_ERROR, "Error beginning message");
  }
//    s_timout_timer = app_timer_register(interval_ms, timout_timer_handler, NULL);
}

static void send_sleep_data_item(int index) {
//   int *data = data_get_steps_data();

  DictionaryIterator *out;
  app_message_outbox_begin(&out);
  if(out!=NULL) {

  // update sleep
  HealthMetric metricSleep = HealthMetricSleepSeconds;
  HealthServiceAccessibilityMask resultSleep = 
    health_service_metric_accessible(metricSleep, time_start_of_today(), time(NULL));
  int sleep = 0;
  if(resultSleep == HealthServiceAccessibilityMaskAvailable) {
    sleep = (int)health_service_sum_today(metricSleep);
  }
    dict_write_int(out, AppKeyIndex, &index, sizeof(int), true);
    dict_write_int(out, AppKeyData, &sleep, sizeof(int), true);
 

    // Include the total number of data items
    if(s_index == 0) {
      dict_write_int(out, AppKeyNumDataItems, &s_num_records, sizeof(int), true);
    }

    if(app_message_outbox_send() != APP_MSG_OK) {
      APP_LOG(APP_LOG_LEVEL_ERROR, "Error sending message");
    }
  } 
  else {
    APP_LOG(APP_LOG_LEVEL_ERROR, "Error beginning message");
  }
//    s_timout_timer = app_timer_register(interval_ms, timout_timer_handler, NULL);
}

// static void inbox_received_callback(DictionaryIterator *iter, void *context) {
//   // Is the location name inside this message?
//   Tuple *location_tuple = dict_find(iter, 0);
//   if(location_tuple) {
    
//   }
// }

static void outbox_sent_handler(DictionaryIterator *iter, void *context) {
  // Last message was successful
  s_index++;

  if(s_index < s_num_records) {
    // Send next item
    send_data_item(s_index);
  } else {
    APP_LOG(APP_LOG_LEVEL_INFO, "Upload complete!");
  }
}

static void inbox_received_handler(DictionaryIterator *iter, void *context) {
  Tuple *js_ready_t = dict_find(iter, 0);
  if(js_ready_t) {
    // Check that it has been at least INTERVAL_MINUTES since the last upload
    time_t now = time(NULL);
    if(now - data_get_last_upload_time() > INTERVAL_MINUTES * 60) {
      // Send the first data
      int num_items = data_reload_steps();
      comm_begin_upload(num_items);

      // Remember the upload time
      data_record_last_upload_time();
      main_window_set_updated_time(now);
    } else {
      APP_LOG(APP_LOG_LEVEL_DEBUG, "Last update was less than %d minutes ago", (int)INTERVAL_MINUTES);
    }
  }
}

void comm_init(int inbox, int outbox) {
  app_message_register_inbox_received(inbox_received_handler);
  app_message_register_outbox_sent(outbox_sent_handler);
  app_message_open(inbox, outbox);
}

void comm_begin_upload(int num_records) {
  s_index = 0;
  s_num_records = num_records;

  send_data_item(s_index);
//   send_sleep_data_item(s_index);
}
