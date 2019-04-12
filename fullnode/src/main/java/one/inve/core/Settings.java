package one.inve.core;

import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

class Settings {
  //使用RSA
  static boolean useRSA = true;
  static boolean logStack = true;
  static final Marker LOGM_EXCEPTION = MarkerManager.getMarker("EXCEPTION");
  static final Marker LOGM_SOCKET_EXCEPTIONS =
      MarkerManager.getMarker("SOCKET_EXCEPTIONS");
  static final Marker LOGM_SYNC_START = MarkerManager.getMarker("SYNC_START");
  static final Marker LOGM_SYNC_DONE = MarkerManager.getMarker("SYNC_DONE");
  static final Marker LOGM_SYNC_ERROR = MarkerManager.getMarker("SYNC_ERROR");
  static final Marker LOGM_SYNC = MarkerManager.getMarker("SYNC");
  static final Marker LOGM_CREATE_EVENT =
      MarkerManager.getMarker("CREATE_EVENT");
  static final Marker LOGM_WATCH_EVENTS_SEND_REC =
      MarkerManager.getMarker("WATCH_EVENTS_SEND_REC");
  static final Marker LOGM_QUEUES = MarkerManager.getMarker("QUEUES");
  static final Marker LOGM_HEARTBEAT = MarkerManager.getMarker("HEARTBEAT");
  static final Marker LOGM_SYNC_STEPS = MarkerManager.getMarker("SYNC_STEPS");
  static final Marker LOGM_EVENT_SIG = MarkerManager.getMarker("EVENT_SIG");
  static final Marker LOGM_LOCKS = MarkerManager.getMarker("LOCKS");
  static final Marker LOGM_TIME_MEASURE =
      MarkerManager.getMarker("TIME_MEASURE");
  static final Marker LOGM_STARTUP = MarkerManager.getMarker("STARTUP");
}
