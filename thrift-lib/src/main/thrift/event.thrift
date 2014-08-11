namespace java ca.thrift

struct Event {
  1: string stream;
  2: string eventType;
  3: string id;
  4: string data;
  5: i64 timestamp;
  6: string version;
}
