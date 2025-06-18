#include <Wire.h>
#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>
#include <am1008w_k_i2c.h>

// Wi-Fi 설정
const char* ssid = "Your_SSID";         // 🔁 와이파이 이름 입력
const char* password = "Your_PASSWORD"; // 🔁 와이파이 비밀번호 입력

ESP8266WebServer server(80); // HTTP 포트 80 사용

// 릴레이 핀 및 타이머 설정
#define RELAY1 D7
#define RELAY2 D6
#define OPEN_DURATION 300000        // 열린 상태 유지 시간: 5분 (300,000ms)
#define COOLDOWN_DURATION 3600000   // 다시 열리기까지 최소 대기 시간: 1시간 (3,600,000ms)

AM1008W_K_I2C am1008w_k_i2c;

bool isOpen = false;
unsigned long openStartTime = 0;
unsigned long lastActivatedTime = 0;

void setup() {
  Serial.begin(115200);
  Wire.begin(D2, D1);  // I2C 핀 (SDA = D2, SCL = D1, ESP8266 전용 설정)

  pinMode(RELAY1, OUTPUT);
  pinMode(RELAY2, OUTPUT);
  digitalWrite(RELAY1, LOW);  // 릴레이 OFF 상태로 시작
  digitalWrite(RELAY2, LOW);

  am1008w_k_i2c.begin();
  delay(1000);

  // Wi-Fi 연결
  WiFi.begin(ssid, password);
  Serial.print("WiFi 연결 중");
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\n✅ WiFi 연결 완료");
  Serial.print("📡 IP 주소: ");
  Serial.println(WiFi.localIP());

  // HTTP 요청 처리 (/data 엔드포인트)
  server.on("/data", []() {
    int pm25 = am1008w_k_i2c.get_pm2p5();
    int co2 = am1008w_k_i2c.get_co2();
    float temperature = am1008w_k_i2c.get_temperature();
    float humidity = am1008w_k_i2c.get_humidity();

    String json = "{";
    json += "\"pm25\":" + String(pm25) + ",";
    json += "\"co2\":" + String(co2) + ",";
    json += "\"temperature\":" + String(temperature, 1) + ",";
    json += "\"humidity\":" + String(humidity, 1);
    json += "}";

    server.send(200, "application/json", json);
  });

  server.begin();
  Serial.println("🌐 웹서버 시작됨");
}

void loop() {
  server.handleClient();  // HTTP 요청 처리

  uint8_t ret = am1008w_k_i2c.read_data_command();

  if (ret == 0) {
    int pm25 = am1008w_k_i2c.get_pm2p5();
    int co2 = am1008w_k_i2c.get_co2();
    float temperature = am1008w_k_i2c.get_temperature();
    float humidity = am1008w_k_i2c.get_humidity();

    Serial.println("📊 센서 데이터");
    Serial.print("PM2.5: "); Serial.println(pm25);
    Serial.print("CO2   : "); Serial.println(co2);
    Serial.print("온도   : "); Serial.println(temperature);
    Serial.print("습도   : "); Serial.println(humidity);

    unsigned long now = millis();

    // 조건 만족 && 쿨타임 지난 경우 → 엑추에이터 열림
    if (!isOpen && (now - lastActivatedTime >= COOLDOWN_DURATION) &&
        (pm25 > 100 || co2 > 1000)) {
      Serial.println("⚠️ 기준 초과 - 엑추에이터 열림");
      digitalWrite(RELAY1, HIGH);
      digitalWrite(RELAY2, HIGH);
      isOpen = true;
      openStartTime = now;
      lastActivatedTime = now;
    }

    // 열린 상태에서 5분 경과 시 → 엑추에이터 닫힘
    if (isOpen && (now - openStartTime >= OPEN_DURATION)) {
      Serial.println("✅ 자동 닫힘 - 5분 경과");
      digitalWrite(RELAY1, LOW);
      digitalWrite(RELAY2, LOW);
      isOpen = false;
    }

  } else {
    Serial.println("❌ 센서 데이터 읽기 실패");
  }

  Serial.println("------------------------\n");
  delay(5000);  // 5초 간격 측정
}
