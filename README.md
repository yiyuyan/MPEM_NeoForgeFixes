
# <span style="color: #FF5733;">SYMC LICENSE</span>  
**Official Website: https://arr.ccngo.top**  

# MPEM Mod  
### Minecraft Performance Enhancement Mod

## 🌍 Languages  
- [中文](#中文)  
- [English](#english)  
- [한국어](#한국어)  
- [日本語](#日本語)  
- [हिन्दी](#हिन्दी)  
- [Русский](#русский)  

---

## 中文  
### 🚀 概述  
MPEM 是 Minecraft 优化模组：  
- **1.20.1 Forge**: 闭源  
- **1.21.1 NeoForge**: 开源  

对**大型模组包**（尤其是机械类）性能提升显著，适合**多人服务器（20+玩家）**和**中低端硬件**。  

### 🔧 功能  
- 优化 Forge 事件系统  
- **即时切换语言**（无需重载资源包）  
- 修复原版BUG：**船从任意高度坠落均不会损坏或伤害玩家**  
- **掉落物优化**（类似 Stxck 但更高效 + 可自定义）  
- 异步复杂计算，避免主线程阻塞  
- **多线程事件处理**（v2.1.0 强化错误处理；已知与 DH 冲突）  
- 加速区块生成（与其他优化模组兼容）  

### 📊 性能测试 (EventBenchmark)  
| 测试场景           | 原生 Forge | MPEM 优化   | 提升倍数  |  
|--------------------|-----------|-------------|----------|  
| 注册 10,000 个监听器 | 36.229 ms | 7.409 ms    | **4.89 倍** |  
| 注册 1,000 个监听器  | 4.025 ms  | 1.674 ms    | **2.40 倍** |  
| 发布 10,000 个事件  | 79.992 ms | 47.947 ms   | **1.67 倍** |  

**结论**：监听器越多，优化越显著（如 10,000 个 → **4.89 倍提速**）。  

---
## English  
### 🚀 Overview  
MPEM is an optimization mod for Minecraft:  
- **1.20.1 Forge**: Closed-source  
- **1.21.1 NeoForge**: Open-source  

It significantly enhances performance for **large modpacks**, especially those with complex machinery.  
Optimal for **servers (20+ players)** and **low/mid-end hardware**.  

### 🔧 Features  
- Optimized Forge event system  
- **Instant language switching** (no resource pack reload)  
- Fixed vanilla bug: **Boats no longer break or damage players** when falling from any height  
- **Item drop optimization** (similar to Stxck but more efficient + customizable)  
- Async complex calculations to prevent main thread blocking  
- **Multi-threaded event handling** (improved error handling in v2.1.0; known conflict with DH)  
- Faster chunk generation (compatible with other chunk optimizers)  

### 📊 Performance Tests (EventBenchmark)  
| Test Scenario           | Vanilla Forge | MPEM Optimized | Speed Boost |  
|-------------------------|--------------|----------------|-------------|  
| 10,000 listeners        | 36.229 ms    | 7.409 ms       | **4.89x**   |  
| 1,000 listeners         | 4.025 ms     | 1.674 ms       | **2.40x**   |  
| 10,000 events dispatched| 79.992 ms    | 47.947 ms      | **1.67x**   |  

**Key Insight**: More listeners = Better optimization (e.g., 10k listeners → **4.89x faster**).  

---
## 한국어  
### 🚀 개요  
MPEM은 Minecraft 최적화 모드입니다:  
- **1.20.1 Forge**: 클로즈드 소스  
- **1.21.1 NeoForge**: 오픈 소스  

**대형 모드팩** (특히 기계류)에 효과적이며, **서버 (20+ 플레이어)** 및 **중저사양**에 적합합니다.  

### 🔧 기능  
- Forge 이벤트 시스템 최적화  
- **언어 즉시 전환** (리소스팩 재로드 불필요)  
- 버그 수정: **보트가 어느 높이에서 떨어져도 파괴/피해 없음**  
- **아이템 드롭 최적화** (Stxck 대비 효율적 + 커스터마이징 가능)  
- 복잡한 계산을 비동기화하여 메인 스레드 차단 방지  
- **멀티스레드 이벤트 처리** (v2.1.0에서 오류 처리 개선; DH와 충돌 주의)  
- 청크 생성 가속 (다른 최적화 모드와 호환)  

### 📊 성능 테스트 (EventBenchmark)  
| 테스트 시나리오       | 기본 Forge | MPEM 최적화 | 성능 향상 |  
|-----------------------|-----------|-------------|----------|  
| 10,000 리스너 등록    | 36.229 ms | 7.409 ms    | **4.89배** |  
| 1,000 리스너 등록     | 4.025 ms  | 1.674 ms    | **2.40배** |  
| 10,000 이벤트 발송   | 79.992 ms | 47.947 ms   | **1.67배** |  

**핵심**: 리스너越多 → 최적화 효과 越大 (예: 10,000개 → **4.89배 빠름**).  

---

## 日本語  
### 🚀 概要  
MPEMはMinecraftの最適化MODです:  
- **1.20.1 Forge**: クローズドソース  
- **1.21.1 NeoForge**: オープンソース  

**大規模MODパック**（特に機械類）の性能向上に効果的で、**マルチプレイサーバー（20人以上）**や**低/中スペックPC**に最適です。  

### 🔧 機能  
- Forgeイベントシステムの最適化  
- **言語の即時切り替え**（リソースパックの再読み込み不要）  
- バグ修正: **ボートがどの高さから落下しても壊れず、プレイヤーにダメージを与えない**  
- **アイテムドロップ最適化**（Stxck類似機能＋カスタマイズ可能）  
- 複雑な計算を非同期処理化（メインスレッドのブロック防止）  
- **マルチスレッドイベント処理**（v2.1.0でエラー処理改善; DHとの競合あり）  
- チャンク生成速度向上（他最適化MODと互換性あり）  

### 📊 性能テスト (EventBenchmark)  
| テストシナリオ       | 標準Forge | MPEM最適化 | 速度向上率 |  
|---------------------|----------|------------|----------|  
| 10,000リスナー登録  | 36.229 ms| 7.409 ms   | **4.89倍** |  
| 1,000リスナー登録   | 4.025 ms | 1.674 ms   | **2.40倍** |  
| 10,000イベント発行 | 79.992 ms| 47.947 ms  | **1.67倍** |  

**結論**: リスナー数が多いほど最適化効果が顕著（例: 10,000件 → **4.89倍高速化**）。  

---

## हिन्दी  
### 🚀 अवलोकन  
MPEM एक Minecraft ऑप्टिमाइज़ेशन मॉड है:  
- **1.20.1 Forge**: बंद स्रोत  
- **1.21.1 NeoForge**: खुला स्रोत  

**बड़े मॉडपैक** (विशेष रूप से मैकेनिकल प्रकार) के लिए उपयोगी, **सर्वर (20+ खिलाड़ी)** और **कम/मध्यम हार्डवेयर** के लिए आदर्श।  

### 🔧 विशेषताएँ  
- Forge इवेंट सिस्टम ऑप्टिमाइज़ेशन  
- **तुरंत भाषा बदलना** (रिसोर्स पैक पुनः लोड की आवश्यकता नहीं)  
- बग फिक्स: **नाव किसी भी ऊंचाई से गिरने पर न टूटेगी न खिलाड़ी को नुकसान पहुंचाएगी**  
- **आइटम ड्रॉप ऑप्टिमाइज़ेशन** (Stxck से बेहतर + अनुकूलन योग्य)  
- मुख्य थ्रेड ब्लॉकिंग से बचने के लिए एसिंक्रोनस गणना  
- **मल्टीथ्रेडेड इवेंट हैंडलिंग** (v2.1.0 में त्रुटि प्रबंधन सुधारा; DH के साथ संघर्ष)  
- तेज़ चंक जनरेशन (अन्य ऑप्टिमाइज़ेशन मॉड्स के साथ संगत)  

### 📊 प्रदर्शन परीक्षण (EventBenchmark)  
| परीक्षण परिदृश्य      | वैनिला Forge | MPEM अनुकूलित | गति वृद्धि |  
|------------------------|-------------|--------------|-----------|  
| 10,000 श्रोता पंजीकरण | 36.229 ms   | 7.409 ms     | **4.89x** |  
| 1,000 श्रोता पंजीकरण  | 4.025 ms    | 1.674 ms     | **2.40x** |  
| 10,000 इवेंट प्रेषण   | 79.992 ms   | 47.947 ms    | **1.67x** |  

**मुख्य बिंदु**: अधिक श्रोता = बेहतर अनुकूलन (उदा. 10,000 → **4.89x तेज़**)।  

---

## Русский  
### 🚀 Обзор  
MPEM - мод для оптимизации Minecraft:  
- **1.20.1 Forge**: Закрытый исходный код  
- **1.21.1 NeoForge**: Открытый исходный код  

Эффективен для **крупных модпаков** (особенно с механизмами), идеален для **серверов (20+ игроков)** и **слабого/среднего железа**.  

### 🔧 Функции  
- Оптимизация системы событий Forge  
- **Мгновенная смена языка** (без перезагрузки ресурсов)  
- Исправление бага: **Лодки не ломаются и не наносят урон** при падении с любой высоты  
- **Оптимизация дропа** (аналог Stxck, но эффективнее + настройки)  
- Асинхронные сложные вычисления (избегание блокировки основного потока)  
- **Многопоточная обработка событий** (улучшена в v2.1.0; конфликт с DH)  
- Ускоренное создание чанков (совместимо с другими оптимизаторами)  

### 📊 Тесты производительности (EventBenchmark)  
| Сценарий теста       | Ванильный Forge | MPEM Оптимизация | Ускорение |  
|----------------------|----------------|------------------|-----------|  
| 10,000 слушателей    | 36.229 ms      | 7.409 ms         | **4.89x** |  
| 1,000 слушателей     | 4.025 ms       | 1.674 ms         | **2.40x** |  
| 10,000 событий       | 79.992 ms      | 47.947 ms        | **1.67x** |  

**Вывод**: Чем больше слушателей - тем выше оптимизация (напр. 10,000 → **в 4.89 раза быстрее**).  

---

**License**: [SYMC](https://arr.ccngo.top)  
