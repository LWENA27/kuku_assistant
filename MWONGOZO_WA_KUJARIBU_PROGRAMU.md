# Mwongozo wa Kujaribu Programu ya Kuku Assistant (App Testing Guide)

Mwongozo huu utakusaidia kujaribu vipengele muhimu vya programu ya Kuku Assistant baada ya kusakinishwa, hasa ukizingatia vipengele vyote muhimu vya programu kama mazungumzo/ushauri, wasifu, na vitendo vya haraka.

## Yaliyomo
1. [Maelekezo ya Usakinishaji](#maelekezo-ya-usakinishaji-installation-instructions)
2. [Kupima Vipengele Muhimu](#kupima-vipengele-muhimu-testing-key-features)
3. [Kuripoti Masuala](#kuripoti-masuala-reporting-issues)
4. [Majaribio ya Juu](#majaribio-ya-juu-kwa-waendelezaji)

## Maelekezo ya Usakinishaji (Installation Instructions)

### 1. Jenga na Sakinisha Programu (Build and Install)

Kwanza, jenge na sakinisha programu kwenye kifaa chako au emuletа:

```bash
# Hakikisha gradlew inaweza kutekelezwa
chmod +x gradlew

# Jenga APK ya majaribio
./gradlew assembleDebug

# Sakinisha kwenye kifaa kilichounganishwa
./gradlew installDebug
```

Au, unaweza kutumia kazi za VS Code:
1. Endesha kazi "Android: Make gradlew executable"
2. Endesha kazi "Android: Build Debug"
3. Endesha kazi "Android: Install Debug"

### 2. Unganisha na Seva ya Supabase (Connect to Backend)

Programu inatumia Supabase kwa huduma za seva. Hakikisha kifaa chako kina muunganisho wa intaneti ili kuunganisha na mfumo wa Supabase.

## Kupima Vipengele Muhimu (Testing Key Features)

### A. Uthibitishaji na Usimamizi wa Akaunti (Authentication & User Management)

#### 1. Usajili (Registration)
- Fungua programu na nenda kwenye skrini ya usajili (skrini ya "Sajili")
- Jaribu kuunda akaunti mpya kwa kutumia maelezo sahihi
- Thibitisha uthibitishaji wa:
  - Sehemu tupu ("Tafadhali jaza sehemu hii")
  - Muundo batili wa barua pepe (kutumia @ na .)
  - Mahitaji ya ugumu wa nenosiri (idadi ya herufi, namba, n.k.)
  - Akaunti zilizopo tayari ("Barua pepe tayari imetumika")

#### 2. Kuingia (Login)
- Jaribu kuingia kwa kutumia kitambulisho sahihi ("Ingia" au "Jisajili")
- Jaribu kuingia kwa kutumia kitambulisho kisichofaa
- Jaribu kazi ya "Nikumbuke" ("Kumbuka akaunti yangu")
- Jaribu mtiririko wa "Nimesahau Nenosiri" ("Umesahau nenosiri?")
- Thibitisha ujumbe sahihi wa makosa

#### 3. Wasifu wa Mtumiaji (User Profile)
- Angalia na hariri maelezo ya wasifu ("Wasifu wangu" au "Wasifu")
- Pakia/badilisha picha ya wasifu
- Sasisha maelezo ya mawasiliano
- Thibitisha mabadiliko yanabaki baada ya kutoka/kuingia

### B. Usogezaji wa Dashibodi (Dashboard Navigation)

#### 1. Vitufe vya Vitendo vya Haraka (Quick Action Buttons)
- Thibitisha kitufe "Ripoti Magonjwa" kinaenda kwenye ReportSymptomsActivity
- Thibitisha kitufe "Fuatilia Dalili" kinaenda kwenye SymptomTrackerActivity
- Thibitisha kitufe "Shauri na Daktari" kinaenda kwenye FarmerConsultationsActivity
- Angalia ujumbe wa makosa yanatolewa iwapo kuna matatizo ya usogezaji

#### 2. Menyu ya Usogezaji wa Chini (Bottom Navigation)
- Thibitisha vitufe vyote vya usogezaji wa chini vinafanya kazi sahihi:
  - Dashibodi (Nyumbani)
  - Ripoti (Kwenda ReportSymptomsActivity)
  - Wasifu (Kwenda ProfileActivity)
  - Mipangilio (Kwenda SettingsActivity)

#### 3. Vipengele vya UI
- Thibitisha maelezo ya wasifu yanaonyeshwa kwenye dashibodi
- Thibitisha arifa zinaonyeshwa vizuri
- Thibitisha viashiria vya kupakia vinafanya kazi vizuri

### C. Vipengele Maalum vya Mfugaji (Farmer Features)

#### 1. Usimamizi wa Shamba (Farm Management)
- Ongeza/hariri maelezo ya shamba ("Shamba langu" au "Ongeza shamba")
- Angalia muhtasari wa shamba
- Rekodi maelezo ya kundi la ndege ("Kuku wangu" au "Ongeza kundi")
- Jaribu uthibitishaji wa data

#### 2. Ufuatiliaji wa Afya (Health Monitoring)
- Rekodi uchunguzi wa afya ("Rekodi dalili" au "Ripoti afya")
- Angalia historia ya afya ("Historia ya afya")
- Jaribu ufuatiliaji wa dalili

#### 3. Rekodi za Uzalishaji (Production Records)
- Ongeza data ya uzalishaji ("Ongeza uzalishaji" au "Rekodi mayai")
- Angalia historia ya uzalishaji
- Jaribu uwasilishaji wa data

### D. Vipengele vya Mazungumzo/Ushauri (Chat/Consultation Features)

#### 1. Upande wa Mfugaji (Farmer Side)

**Kuanza Ushauri Mpya (Starting a New Consultation):**
- Kutoka kwa dashibodi ya mfugaji, bofya kitufe "Shauri na Daktari"
- Unda ushauri mpya ("Ushauri mpya" au "Pata ushauri")
- Thibitisha kitambulisho cha ushauri kimeundwa
- Thibitisha vipengele vya UI vinaonyeshwa vizuri (ingizo la ujumbe, kitufe cha kutuma)

**Kutuma Ujumbe (Sending Messages):**
- Andika na tuma ujumbe
- Thibitisha ujumbe unaonekana kwenye mazungumzo
- Thibitisha kiambishi sahihi cha mtumiaji ("Mfugaji: ")
- Thibitisha ujumbe unabaki ukitoka na kurudi

**Kuangalia Ushauri Uliopo (Viewing Existing Consultations):**
- Rudi kwa orodha ya ushauri
- Thibitisha ushauri wako mpya unaonekana
- Fungua ushauri uliopo
- Thibitisha historia ya ujumbe inapakiwa vizuri
- Jaribu kupanga na kuchuja (ikiwa inapatikana)

**Kupima bila Mtandao (Offline Testing):**
- Zima muunganisho wa intaneti
- Jaribu kutuma ujumbe
- Thibitisha kushughulikia makosa ipasavyo
- Thibitisha ujumbe unahifadhiwa kwa muda
- Rejesha muunganisho wa intaneti
- Thibitisha ujumbe unasawazishwa kwenye Supabase

#### 2. Upande wa Mshauri (Advisor Side)

**Kuangalia Ushauri Uliopatikana (Viewing Available Consultations):**
- Ingia kama mshauri
- Nenda kwa orodha ya ushauri ("Ushauri" au "Maombi ya ushauri")
- Thibitisha ushauri wote uliopangiwa unaonekana

**Kujibu kwa Ushauri (Responding to Consultations):**
- Fungua ushauri uliopo
- Soma ujumbe wa mfugaji
- Jibu kwa ushauri
- Thibitisha ujumbe wako unaonekana kwa kiambishi "Mshauri: "
- Thibitisha ujumbe unahifadhiwa kwenye Supabase

**Usimamizi wa Ushauri (Consultation Management):**
- Weka ushauri kuwa umetatuliwa (ikiwa inapatikana)
- Chuja ushauri kwa hali
- Tafuta ushauri maalum

#### 3. Kushughulikia Makosa (Error Handling)

- Jaribu kwa muunganisho mbaya wa mtandao
- Funga na kufungua upya wakati wa mazungumzo hai
- Jaribu kwa ujumbe mrefu sana
- Jaribu kwa herufi maalum
- Jaribu urudi nyuma wa usogezaji

#### 4. Kupima Utendaji (Performance Testing)

- Jaribu kwa historia kubwa ya ujumbe
- Fuatilia ujibu wa programu
- Angalia matumizi ya kumbukumbu
- Thibitisha uchunguzi haumalizi betri

### E. Vipengele Maalum vya Mshauri (Advisor Features)

- Jaribu zana za utambuzi wa magonjwa
- Jaribu vipengele vya mapendekezo ya matibabu
- Jaribu dashibodi ya uchambuzi

### F. Mipangilio na Mapendeleo (Settings & Preferences)

- Jaribu ubadilishaji wa lugha ("Mipangilio" > "Lugha")
- Jaribu mapendeleo ya arifa
- Jaribu mipangilio ya matumizi ya data

## Kuripoti Masuala (Reporting Issues)

Ukikumbana na masuala yoyote wakati wa majaribio, tafadhali andika:

1. **Kipengele kinachojaribiwa:** (k.m., "Mazungumzo ya Ushauri wa Mfugaji")
2. **Hatua za kurudia:** (k.m., "1. Ingia kama mfugaji, 2. Fungua ushauri uliopo...")
3. **Tabia inayotarajiwa:** (k.m., "Ujumbe unapaswa kuonekana kwenye mazungumzo")
4. **Tabia halisi:** (k.m., "Programu imeanguka na NullPointerException")
5. **Maelezo ya kifaa:** (Mfano, toleo la Android, n.k.)
6. **Picha za skrini:** (Ikiwa zinahitajika)

Ripoti masuala kupitia mfumo wa ufuatiliaji wa masuala wa mradi.

## Majaribio ya Juu (kwa Waendelezaji)

### Majaribio ya Ushirikishwaji wa API

- Tumia hati `diagnose_supabase.sh` kupima uunganisho wa Supabase
- Thibitisha kushughulikia makosa ipasavyo wakati seva haipatikani
- Jaribu kushughulikia vikomo vya kasi

### Usalama wa Uthibitishaji

- Jaribu kushughulikia muda wa mwisho wa ishara
- Jaribu kudumu kwa kipindi
- Jaribu kutoka ipasavyo

### Usawazishaji wa Data

- Jaribu tabia ya usawazishaji baada ya kukatika kwa mtandao
- Thibitisha utatuzi wa migogoro

### Matengenezo ya Matatizo ya Kawaida

#### 1. Tatizo la Usogezaji (Navigation Issues)
- Hakikisha shughuli zote zimetangazwa kwenye AndroidManifest.xml
- Hakikisha njia za usogezaji zinakamilika kwenye MainActivity.java
- Angalia maelezo ya makosa kwenye logcat

#### 2. Matatizo ya Wasifu (Profile Issues)
- Tumia skrini ya kuboresha wasifu (fix_profile_issues.sh)
- Hakikisha ProfileActivity na FarmerProfileEditActivity zinafanya kazi
- Angalia utunzaji wa data kwenye Supabase na SharedPreferences

#### 3. Matatizo ya Mazungumzo/Ushauri (Consultation Issues)
- Hakikisha FarmerConsultationsActivity na FarmerConsultationActivity zinafanya kazi
- Angalia usimamizi wa ujumbe na uhifadhi
- Thibitisha usawazishaji wa Supabase

---

Majaribio yenye furaha! Ukihitaji msaada, tafadhali wasiliana na timu ya maendelezi.
