package com.example.fowltyphoidmonitor.ui.common;

import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fowltyphoidmonitor.R;

public class DiseaseInfoActivity extends AppCompatActivity {

    TextView btnCauses, btnSymptoms, btnTreatment, btnPrevention, txtInfo;
    ImageView imgCategoryIcon;
    LinearLayout layoutContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disease_info);

        btnCauses = findViewById(R.id.btnCauses);
        btnSymptoms = findViewById(R.id.btnSymptoms);
        btnTreatment = findViewById(R.id.btnTreatment);
        btnPrevention = findViewById(R.id.btnPrevention);
        txtInfo = findViewById(R.id.txtInfo);
        imgCategoryIcon = findViewById(R.id.imgCategoryIcon);
        layoutContent = findViewById(R.id.layoutContent);

        // Enable scrolling for text content
        txtInfo.setMovementMethod(new ScrollingMovementMethod());

        btnCauses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Reset all button backgrounds
                resetButtonBackgrounds();
                // Highlight selected button
                btnCauses.setBackgroundResource(R.drawable.button_selected);
                // Set category image
                imgCategoryIcon.setImageResource(R.drawable.icon_causes);

                String causes = "<b>SABABU ZA FOWL TYPHOID (HOMA YA MATUMBO YA KUKU)</b><br><br>" +
                        "<b>Chanzo cha Msingi:</b><br>" +
                        "• Husababishwa na bakteria aina ya <i>Salmonella enterica</i> serotype <i>Gallinarum</i><br>" +
                        "• Ni bakteria ambaye hana kuta lakini ana sumu kali inayoathiri mfumo wa kinga ya kuku<br><br>" +

                        "<b>Njia za Maambukizi:</b><br>" +
                        "• <u>Chakula na Maji:</u> Yaliyochafuliwa na kinyesi cha kuku waliombukizwa<br>" +
                        "• <u>Mazingira:</u> Vifaa vichafu, vyombo, mabanda, matandiko na nguo za wafugaji<br>" +
                        "• <u>Moja kwa Moja:</u> Kuambukizwa kutoka kwa kuku mgonjwa hadi kuku mzima kupitia hewa au mgusano<br>" +
                        "• <u>Mayai:</u> Bakteria hupenya kupitia ganda la yai na kuambukiza vifaranga kabla ya kutotolewa<br>" +
                        "• <u>Vekta:</u> Wadudu kama kupe, viroboto, mende na nzi ambao wanabeba bakteria<br>" +
                        "• <u>Wanyama:</u> Ndege pori, panya na wanyama wengine wanaweza kubeba bakteria<br><br>" +

                        "<b>Vichocheo vya Ugonjwa:</b><br>" +
                        "• Msongamano wa kuku kwenye banda moja<br>" +
                        "• Joto jingi na unyevunyevu mkubwa<br>" +
                        "• Misimu ya mvua na kipindi cha mabadiliko ya hali ya hewa<br>" +
                        "• Mfumo dhaifu wa kinga (mfano: wakati wa kutaga, magonjwa mengine)<br>" +
                        "• Lishe duni na upungufu wa vitamini<br><br>" +

                        "<b>Viwango vya Maambukizi:</b><br>" +
                        "• Vifaranga: Asilimia 60-90 ya vifo<br>" +
                        "• Kuku wazima: Asilimia 10-50 ya vifo<br>" +
                        "• Hutofautiana kulingana na aina ya kuku na hali ya afya<br><br>" +

                        "<b>Muhimu Kujua:</b><br>" +
                        "• Bakteria anaweza kuishi mazingira kama udongo kwa miezi 6<br>" +
                        "• Kuku waliopona huwa wabeba wa bakteria kwa maisha yao yote<br>" +
                        "• Ugonjwa huenea haraka zaidi kwenye mifumo ya ufugaji wa kibiashara";

                txtInfo.setText(Html.fromHtml(causes, Html.FROM_HTML_MODE_COMPACT));
            }
        });

        btnSymptoms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Reset all button backgrounds
                resetButtonBackgrounds();
                // Highlight selected button
                btnSymptoms.setBackgroundResource(R.drawable.button_selected);
                // Set category image
                imgCategoryIcon.setImageResource(R.drawable.icon_symptoms);

                String symptoms = "<b>DALILI ZA FOWL TYPHOID (HOMA YA MATUMBO YA KUKU)</b><br><br>" +
                        "<b>Kasi ya Ugonjwa:</b><br>" +
                        "• <u>Kali sana:</u> Vifo vya ghafla bila dalili zinazoonekana<br>" +
                        "• <u>Kali:</u> Dalili zinazoonekana kwa siku 3-5 kabla ya kifo<br>" +
                        "• <u>Sugu:</u> Dalili za polepole kwa wiki kadhaa<br><br>" +

                        "<b>Dalili za Awali:</b><br>" +
                        "• Homa kali (joto la mwili kuongezeka hadi 42°C)<br>" +
                        "• Kukosa hamu ya kula na kunywa<br>" +
                        "• Kutopenda kutembea na kujitenga na kuku wengine<br>" +
                        "• Mabawa yaliyolegea na kushuka chini<br>" +
                        "• Kichwa kikiwa kimezama kwenye mabawa<br><br>" +

                        "<b>Dalili za Baadaye:</b><br>" +
                        "• Kuhara ya manjano, ya kijani au ya kahawia yenye harufu kali<br>" +
                        "• Upungufu wa damu (upanga na ngozi ya miguu kuwa nyeupe)<br>" +
                        "• Kupumua kwa shida na kugugumia<br>" +
                        "• Kukonda haraka na kupoteza uzito<br>" +
                        "• Kuvimba sehemu za uso na shingo<br>" +
                        "• Kupungua kwa uzalishaji wa mayai (hadi 40%)<br>" +
                        "• Mayai kuwa na maganda membamba na yasiyo na rangi nzuri<br><br>" +

                        "<b>Dalili baada ya Kifo:</b><br>" +
                        "• Ini kubwa, laini na lenye madoa ya kijivu au manjano<br>" +
                        "• Wengu kubwa na ulioharibika na rangi ya zambarau<br>" +
                        "• Utumbo mkubwa, uliovimba na wenye damu<br>" +
                        "• Yavuni na figo zilizoharibika na kuvimba<br>" +
                        "• Mishipa ya damu iliyovimba kwenye matumbo<br>" +
                        "• Madoa mekundu kwenye nyama na ngozi ya ndani<br><br>" +

                        "<b>Tofauti za Umri:</b><br>" +
                        "• <u>Vifaranga:</u> Dalili kali na vifo vya haraka<br>" +
                        "• <u>Kuku Wazima:</u> Dalili zinaweza kuwa hafifu, lakini wanaweza kuwa wabeba wa kudumu<br><br>" +

                        "<b>Muda wa Maambukizi:</b><br>" +
                        "• Kipindi cha kukaa kimya: Siku 4-6 baada ya kuambukizwa<br>" +
                        "• Vifo vinaweza kutokea kati ya siku 4 hadi 14 baada ya kuambukizwa<br>" +
                        "• Ugonjwa unaweza kudumu kwa wiki 2-3 kwenye kundi";

                txtInfo.setText(Html.fromHtml(symptoms, Html.FROM_HTML_MODE_COMPACT));
            }
        });

        btnTreatment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Reset all button backgrounds
                resetButtonBackgrounds();
                // Highlight selected button
                btnTreatment.setBackgroundResource(R.drawable.button_selected);
                // Set category image
                imgCategoryIcon.setImageResource(R.drawable.icon_treatment);

                String treatment = "<b>MATIBABU YA FOWL TYPHOID (HOMA YA MATUMBO YA KUKU)</b><br><br>" +
                        "<b>Hatua za Awali za Dharura:</b><br>" +
                        "• Tenga kuku wagonjwa mara moja kutoka kwa kundi zima<br>" +
                        "• Weka katika eneo la karantini lenye joto la kutosha<br>" +
                        "• Peana maji safi yaliyochanganywa na electrolytes na vitamini<br>" +
                        "• Hakikisha lishe bora na rahisi kumeng'enya<br><br>" +

                        "<b>Matibabu ya Dawa (Antibiotics):</b><br>" +
                        "• <u>Sulfonamides:</u><br>" +
                        "  - Sulfadimethoxine: 0.05% kwenye maji kwa siku 5-7<br>" +
                        "  - Sulfaquinoxaline: 0.04% kwenye maji kwa siku 5-7, pumzisha siku 2, rudia kwa siku 5<br><br>" +

                        "• <u>Tetracyclines:</u><br>" +
                        "  - Oxytetracycline: 200-400g/tani ya chakula au 0.025% kwenye maji<br>" +
                        "  - Doxycycline: 10-20mg/kg ya uzito wa mwili<br>" +
                        "  - Chlortetracycline: 400g/tani ya chakula kwa siku 7-14<br><br>" +

                        "• <u>Quinolones:</u><br>" +
                        "  - Enrofloxacin: 10mg/kg ya uzito wa mwili kwa siku 5-7<br>" +
                        "  - Norfloxacin: 10-20mg/kg ya uzito wa mwili<br><br>" +

                        "• <u>Nyinginezo:</u><br>" +
                        "  - Furazolidone: 200-400g/tani ya chakula kwa siku 7-10<br>" +
                        "  - Neomycin: 20mg/kg ya uzito wa mwili<br><br>" +

                        "<b>Tiba Saidizi:</b><br>" +
                        "• Vitamini A, D3, E na K kwa kuimarisha mfumo wa kinga<br>" +
                        "• Probiotics kurudisha bakteria nzuri kwenye utumbo<br>" +
                        "• Zinc na Selenium kwa kupambana na mafadhaiko<br>" +
                        "• Electrolytes kuzuia kupungua kwa maji mwilini<br><br>" +

                        "<b>Tahadhari Muhimu:</b><br>" +
                        "• <span style='color:red;'>MUHIMU: Wasiliana na daktari wa mifugo kabla ya kutumia dawa yoyote</span><br>" +
                        "• Fuata maelekezo ya kipimo na muda wa matibabu kikamilifu<br>" +
                        "• Zingatia muda wa kungoja kabla ya kutumia mayai au nyama<br>" +
                        "• Kuku wanaopona wanaweza kuendelea kuwa wabeba wa bakteria<br>" +
                        "• Katika kesi kali au milipuko mikubwa, inaweza kuwa bora kuondoa kundi zima<br>" +
                        "• Ni marufuku kuchinja na kuuza nyama ya kuku wagonjwa kwa matumizi ya binadamu";

                txtInfo.setText(Html.fromHtml(treatment, Html.FROM_HTML_MODE_COMPACT));
            }
        });

        btnPrevention.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Reset all button backgrounds
                resetButtonBackgrounds();
                // Highlight selected button
                btnPrevention.setBackgroundResource(R.drawable.button_selected);
                // Set category image
                imgCategoryIcon.setImageResource(R.drawable.icon_prevention);

                String prevention = "<b>KUZUIA FOWL TYPHOID (HOMA YA MATUMBO YA KUKU)</b><br><br>" +
                        "<b>Usafi wa Mazingira (Biosecurity):</b><br>" +
                        "• <u>Usafi wa Kila Siku:</u><br>" +
                        "  - Safisha vyombo vya chakula na maji kila siku<br>" +
                        "  - Ondoa kinyesi na matandiko yaliyochafuliwa mara kwa mara<br>" +
                        "  - Hakikisha banda lina hewa safi na kavu wakati wote<br><br>" +

                        "• <u>Usafi wa Kina:</u><br>" +
                        "  - Safisha na kutakasa banda kabisa kati ya makundi<br>" +
                        "  - Tumia dawa za kuua viini kama vile:<br>" +
                        "    * Formalin 10% au Glutaraldehyde<br>" +
                        "    * Phenol compounds (20-25g/L)<br>" +
                        "    * Hypochlorite (Chlorine: 200ppm)<br>" +
                        "    * Iodine compounds (50-100ppm)<br>" +
                        "    * Quaternary ammonium compounds (500ppm)<br><br>" +

                        "<b>Mpango wa Chanjo:</b><br>" +
                        "• <u>Chanjo za 9R (Salmonella Gallinarum 9R):</u><br>" +
                        "  - Kwa vifaranga: Wiki 6 ya umri<br>" +
                        "  - Chanjo rudia: Kila miezi 4-6<br>" +
                        "  - Njia: Sindano chini ya ngozi au kwenye misuli<br><br>" +

                        "• <u>Chanjo za Kuua (Inactivated):</u><br>" +
                        "  - Zinatoa kinga zaidi kuliko chanjo za 9R<br>" +
                        "  - Zinaweza kutolewa wiki 14-16 ya umri<br>" +
                        "  - Fuata maelekezo ya mtengenezaji<br><br>" +

                        "<b>Udhibiti wa Wanyama na Wadudu:</b><br>" +
                        "• Weka wavu kwenye madirisha na matundu ya banda<br>" +
                        "• Funga nyufa na matundu yote kwenye kuta na dari<br>" +
                        "• Dhibiti wadudu kwa kutumia dawa za kuua wadudu<br>" +
                        "• Weka mitego ya panya na weka uzio kuzunguka banda<br><br>" +

                        "<b>Kinga za Kiutaratibu:</b><br>" +
                        "• <u>Ununuzi wa Kuku:</u><br>" +
                        "  - Nunua vifaranga kutoka kwa wazalishaji wenye vyeti vya afya<br>" +
                        "  - Epuka kuchanganya kuku kutoka vyanzo tofauti<br>" +
                        "  - Weka kuku wapya karantini kwa wiki 3-4<br><br>" +

                        "• <u>Udhibiti wa Wageni:</u><br>" +
                        "  - Weka kidimbwi cha dawa ya kuua viini kwenye mlango wa kuingilia<br>" +
                        "  - Tumia viatu na mavazi maalum ya banda<br>" +
                        "  - Zuia wageni wasio wa lazima kuingia banda<br><br>" +

                        "• <u>Kumbukumbu na Ufuatiliaji:</u><br>" +
                        "  - Weka kumbukumbu za chanjo na matibabu<br>" +
                        "  - Fanya uchunguzi wa mara kwa mara wa afya ya kuku<br>" +
                        "  - Chukua sampuli za kinyesi kwa uchunguzi wa maabara<br><br>" +

                        "<b>Utupaji wa Kuku Waliokufa:</b><br>" +
                        "• Ondoa kuku waliokufa mara moja kutoka banda<br>" +
                        "• Zika kwa kina cha futi 3 au choma<br>" +
                        "• Nyunyiza sehemu iliyochafuliwa na dawa za kuua viini<br>" +
                        "• Kamwe usitupe kuku waliokufa kwenye mito au mitaro";

                txtInfo.setText(Html.fromHtml(prevention, Html.FROM_HTML_MODE_COMPACT));
            }
        });

        // Load causes section by default
        btnCauses.performClick();
    }

    // Method to reset all button backgrounds
    private void resetButtonBackgrounds() {
        btnCauses.setBackgroundResource(R.drawable.button_normal);
        btnSymptoms.setBackgroundResource(R.drawable.button_normal);
        btnTreatment.setBackgroundResource(R.drawable.button_normal);
        btnPrevention.setBackgroundResource(R.drawable.button_normal);
    }
}