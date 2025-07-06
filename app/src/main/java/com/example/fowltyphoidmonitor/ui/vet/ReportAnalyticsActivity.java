package com.example.fowltyphoidmonitor.ui.vet;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.fowltyphoidmonitor.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportAnalyticsActivity extends AppCompatActivity {

    private static final String TAG = "ReportAnalyticsActivity";
    private static final String PREFS_NAME = "FowlTyphoidMonitorAdminPrefs";

    // UI Components
    private Spinner spinnerReportType, spinnerTimeRange;
    private MaterialButton btnDateRange, btnGenerateReport, btnExportReport;
    private TextView txtDateRange, txtTotalReports, txtActiveReports, txtResolvedReports;
    private TextView txtMostAffectedArea, txtTrendAnalysis;
    private ProgressBar progressBarAnalytics;
    private ImageButton btnBack;

    // Charts
    private PieChart pieChartReportStatus;
    private BarChart barChartAreaWise;
    private LineChart lineChartTrend;

    // Layouts
    private LinearLayout layoutCharts, layoutSummary;
    private CardView cardSummary, cardCharts;

    // Date variables
    private Calendar startDate, endDate;
    private SimpleDateFormat dateFormat;

    // Report data
    private String selectedReportType = "Yote";
    private String selectedTimeRange = "Mwezi mmoja";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_analytics);

        initializeViews();
        setupSpinners();
        setupClickListeners();
        setupCharts();

        // Initialize date variables
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        startDate = Calendar.getInstance();
        endDate = Calendar.getInstance();

        // Set default date range (last 30 days)
        startDate.add(Calendar.DAY_OF_MONTH, -30);
        updateDateRangeDisplay();

        // Load initial analytics
        generateAnalytics();

        Log.d(TAG, "ReportAnalyticsActivity created successfully");
    }

    private void initializeViews() {
        // Back button
        btnBack = findViewById(R.id.btnBack);

        // Spinners
        spinnerReportType = findViewById(R.id.spinnerReportType);
        spinnerTimeRange = findViewById(R.id.spinnerTimeRange);

        // Buttons
        btnDateRange = findViewById(R.id.btnDateRange);
        btnGenerateReport = findViewById(R.id.btnGenerateReport);
        btnExportReport = findViewById(R.id.btnExportReport);

        // Text views
        txtDateRange = findViewById(R.id.txtDateRange);
        txtTotalReports = findViewById(R.id.txtTotalReports);
        txtActiveReports = findViewById(R.id.txtActiveReports);
        txtResolvedReports = findViewById(R.id.txtResolvedReports);
        txtMostAffectedArea = findViewById(R.id.txtMostAffectedArea);
        txtTrendAnalysis = findViewById(R.id.txtTrendAnalysis);

        // Progress bar
        progressBarAnalytics = findViewById(R.id.progressBarAnalytics);

        // Charts
        pieChartReportStatus = findViewById(R.id.pieChartReportStatus);
        barChartAreaWise = findViewById(R.id.barChartAreaWise);
        lineChartTrend = findViewById(R.id.lineChartTrend);

        // Layouts
        layoutCharts = findViewById(R.id.layoutCharts);
        layoutSummary = findViewById(R.id.layoutSummary);
        cardSummary = findViewById(R.id.cardSummary);
        cardCharts = findViewById(R.id.cardCharts);
    }

    private void setupSpinners() {
        // Report type spinner
        String[] reportTypes = {"Yote", "Magonjwa ya Kuku", "Mazingira", "Lishe", "Maongezi"};
        ArrayAdapter<String> reportAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, reportTypes);
        reportAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerReportType.setAdapter(reportAdapter);

        spinnerReportType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedReportType = reportTypes[position];
                generateAnalytics();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Time range spinner
        String[] timeRanges = {"Wiki moja", "Mwezi mmoja", "Miezi mitatu", "Mwaka mmoja", "Chagua tarehe"};
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, timeRanges);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTimeRange.setAdapter(timeAdapter);
        spinnerTimeRange.setSelection(1); // Default to "Mwezi mmoja"

        spinnerTimeRange.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTimeRange = timeRanges[position];
                updateDateRangeBasedOnSelection(position);

                // Show/hide date range button
                btnDateRange.setVisibility(position == 4 ? View.VISIBLE : View.GONE);

                generateAnalytics();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updateDateRangeBasedOnSelection(int position) {
        Calendar today = Calendar.getInstance();
        endDate.setTime(today.getTime());

        switch (position) {
            case 0: // Wiki moja
                startDate.setTime(today.getTime());
                startDate.add(Calendar.DAY_OF_MONTH, -7);
                break;
            case 1: // Mwezi mmoja
                startDate.setTime(today.getTime());
                startDate.add(Calendar.DAY_OF_MONTH, -30);
                break;
            case 2: // Miezi mitatu
                startDate.setTime(today.getTime());
                startDate.add(Calendar.MONTH, -3);
                break;
            case 3: // Mwaka mmoja
                startDate.setTime(today.getTime());
                startDate.add(Calendar.YEAR, -1);
                break;
            // Case 4 (Chagua tarehe) handled by date picker
        }

        updateDateRangeDisplay();
    }

    private void setupClickListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }

        if (btnDateRange != null) {
            btnDateRange.setOnClickListener(v -> showDateRangePicker());
        }

        if (btnGenerateReport != null) {
            btnGenerateReport.setOnClickListener(v -> generateAnalytics());
        }

        if (btnExportReport != null) {
            btnExportReport.setOnClickListener(v -> exportReport());
        }
    }

    private void showDateRangePicker() {
        // Show start date picker
        DatePickerDialog startDatePicker = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    startDate.set(year, month, dayOfMonth);

                    // Show end date picker after start date is selected
                    DatePickerDialog endDatePicker = new DatePickerDialog(this,
                            (endView, endYear, endMonth, endDayOfMonth) -> {
                                endDate.set(endYear, endMonth, endDayOfMonth);

                                // Validate date range
                                if (endDate.before(startDate)) {
                                    Toast.makeText(this, "Tarehe ya mwisho lazima iwe baada ya tarehe ya mwanzo",
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                updateDateRangeDisplay();
                                generateAnalytics();
                            },
                            endDate.get(Calendar.YEAR),
                            endDate.get(Calendar.MONTH),
                            endDate.get(Calendar.DAY_OF_MONTH));

                    endDatePicker.setTitle("Chagua Tarehe ya Mwisho");
                    endDatePicker.show();
                },
                startDate.get(Calendar.YEAR),
                startDate.get(Calendar.MONTH),
                startDate.get(Calendar.DAY_OF_MONTH));

        startDatePicker.setTitle("Chagua Tarehe ya Mwanzo");
        startDatePicker.show();
    }

    private void updateDateRangeDisplay() {
        if (txtDateRange != null) {
            String dateRangeText = dateFormat.format(startDate.getTime()) + " - " +
                    dateFormat.format(endDate.getTime());
            txtDateRange.setText(dateRangeText);
        }
    }

    private void setupCharts() {
        setupPieChart();
        setupBarChart();
        setupLineChart();
    }

    private void setupPieChart() {
        if (pieChartReportStatus == null) return;

        pieChartReportStatus.setUsePercentValues(true);
        pieChartReportStatus.getDescription().setEnabled(false);
        pieChartReportStatus.setExtraOffsets(5, 10, 5, 5);
        pieChartReportStatus.setDragDecelerationFrictionCoef(0.95f);
        pieChartReportStatus.setDrawHoleEnabled(true);
        pieChartReportStatus.setHoleColor(Color.WHITE);
        pieChartReportStatus.setTransparentCircleRadius(61f);
        pieChartReportStatus.setHoleRadius(58f);
        pieChartReportStatus.setRotationAngle(0);
        pieChartReportStatus.setRotationEnabled(true);
        pieChartReportStatus.setHighlightPerTapEnabled(true);
    }

    private void setupBarChart() {
        if (barChartAreaWise == null) return;

        barChartAreaWise.getDescription().setEnabled(false);
        barChartAreaWise.setMaxVisibleValueCount(60);
        barChartAreaWise.setPinchZoom(false);
        barChartAreaWise.setDrawBarShadow(false);
        barChartAreaWise.setDrawGridBackground(false);

        XAxis xAxis = barChartAreaWise.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(7);

        barChartAreaWise.getAxisLeft().setDrawGridLines(false);
        barChartAreaWise.getAxisRight().setEnabled(false);
        barChartAreaWise.getLegend().setEnabled(false);
    }

    private void setupLineChart() {
        if (lineChartTrend == null) return;

        lineChartTrend.getDescription().setEnabled(false);
        lineChartTrend.setTouchEnabled(true);
        lineChartTrend.setDragEnabled(true);
        lineChartTrend.setScaleEnabled(true);
        lineChartTrend.setDrawGridBackground(false);
        lineChartTrend.setPinchZoom(true);

        XAxis xAxis = lineChartTrend.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        lineChartTrend.getAxisLeft().setDrawGridLines(false);
        lineChartTrend.getAxisRight().setEnabled(false);
    }

    private void generateAnalytics() {
        if (progressBarAnalytics != null) {
            progressBarAnalytics.setVisibility(View.VISIBLE);
        }

        // Simulate data loading
        new Thread(() -> {
            try {
                Thread.sleep(1000); // Simulate processing time

                runOnUiThread(() -> {
                    loadAnalyticsData();
                    updateCharts();
                    updateSummaryStats();

                    if (progressBarAnalytics != null) {
                        progressBarAnalytics.setVisibility(View.GONE);
                    }
                });

            } catch (InterruptedException e) {
                Log.e(TAG, "Error generating analytics", e);
            }
        }).start();
    }

    private void loadAnalyticsData() {
        // This method would typically load data from database
        // For now, we'll use sample data
        Log.d(TAG, "Loading analytics data for: " + selectedReportType +
                " from " + dateFormat.format(startDate.getTime()) +
                " to " + dateFormat.format(endDate.getTime()));
    }

    private void updateSummaryStats() {
        // Sample data - replace with actual database queries
        int totalReports = getSampleTotalReports();
        int activeReports = getSampleActiveReports();
        int resolvedReports = totalReports - activeReports;
        String mostAffectedArea = getSampleMostAffectedArea();
        String trendAnalysis = getSampleTrendAnalysis();

        if (txtTotalReports != null) {
            txtTotalReports.setText(String.valueOf(totalReports));
        }
        if (txtActiveReports != null) {
            txtActiveReports.setText(String.valueOf(activeReports));
        }
        if (txtResolvedReports != null) {
            txtResolvedReports.setText(String.valueOf(resolvedReports));
        }
        if (txtMostAffectedArea != null) {
            txtMostAffectedArea.setText(mostAffectedArea);
        }
        if (txtTrendAnalysis != null) {
            txtTrendAnalysis.setText(trendAnalysis);
        }
    }

    private void updateCharts() {
        updatePieChart();
        updateBarChart();
        updateLineChart();
    }

    private void updatePieChart() {
        if (pieChartReportStatus == null) return;

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(45f, "Yamekamilika"));
        entries.add(new PieEntry(35f, "Inaendelea"));
        entries.add(new PieEntry(20f, "Mpya"));

        PieDataSet dataSet = new PieDataSet(entries, "Hali za Ripoti");
        dataSet.setColors(new int[]{Color.GREEN, Color.YELLOW, Color.RED});
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData data = new PieData(dataSet);
        pieChartReportStatus.setData(data);
        pieChartReportStatus.invalidate();
    }

    private void updateBarChart() {
        if (barChartAreaWise == null) return;

        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, 25f));
        entries.add(new BarEntry(1f, 35f));
        entries.add(new BarEntry(2f, 15f));
        entries.add(new BarEntry(3f, 40f));
        entries.add(new BarEntry(4f, 20f));

        BarDataSet dataSet = new BarDataSet(entries, "Ripoti za Eneo");
        dataSet.setColor(Color.BLUE);
        dataSet.setValueTextSize(12f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.9f);

        String[] areas = {"Kinondoni", "Ilala", "Temeke", "Ubungo", "Kigamboni"};
        XAxis xAxis = barChartAreaWise.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(areas));

        barChartAreaWise.setData(data);
        barChartAreaWise.invalidate();
    }

    private void updateLineChart() {
        if (lineChartTrend == null) return;

        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0f, 10f));
        entries.add(new Entry(1f, 15f));
        entries.add(new Entry(2f, 12f));
        entries.add(new Entry(3f, 25f));
        entries.add(new Entry(4f, 18f));
        entries.add(new Entry(5f, 30f));
        entries.add(new Entry(6f, 22f));

        LineDataSet dataSet = new LineDataSet(entries, "Mwenendo wa Ripoti");
        dataSet.setColor(Color.BLUE);
        dataSet.setCircleColor(Color.BLUE);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setValueTextSize(10f);

        LineData data = new LineData(dataSet);
        lineChartTrend.setData(data);
        lineChartTrend.invalidate();
    }

    private void exportReport() {
        if (progressBarAnalytics != null) {
            progressBarAnalytics.setVisibility(View.VISIBLE);
        }

        new Thread(() -> {
            try {
                String fileName = generateReportFileName();
                File exportFile = createExportFile(fileName);

                if (exportFile != null) {
                    writeReportData(exportFile);

                    runOnUiThread(() -> {
                        if (progressBarAnalytics != null) {
                            progressBarAnalytics.setVisibility(View.GONE);
                        }
                        Toast.makeText(this, "Ripoti imehifadhiwa: " + exportFile.getAbsolutePath(),
                                Toast.LENGTH_LONG).show();
                    });
                } else {
                    runOnUiThread(() -> {
                        if (progressBarAnalytics != null) {
                            progressBarAnalytics.setVisibility(View.GONE);
                        }
                        Toast.makeText(this, "Hitilafu katika kutengeneza faili",
                                Toast.LENGTH_SHORT).show();
                    });
                }

            } catch (Exception e) {
                Log.e(TAG, "Error exporting report", e);
                runOnUiThread(() -> {
                    if (progressBarAnalytics != null) {
                        progressBarAnalytics.setVisibility(View.GONE);
                    }
                    Toast.makeText(this, "Hitilafu katika kuhamisha ripoti: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private String generateReportFileName() {
        SimpleDateFormat fileNameFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String timestamp = fileNameFormat.format(new Date());
        return "Ripoti_ya_Uchanganuzi_" + selectedReportType.replace(" ", "_") + "_" + timestamp + ".csv";
    }

    private File createExportFile(String fileName) {
        try {
            File exportDir = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "FowlTyphoidReports");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }

            File exportFile = new File(exportDir, fileName);
            if (exportFile.exists()) {
                exportFile.delete();
            }

            return exportFile;
        } catch (Exception e) {
            Log.e(TAG, "Error creating export file", e);
            return null;
        }
    }

    private void writeReportData(File exportFile) throws IOException {
        FileWriter writer = new FileWriter(exportFile);

        // Write CSV header
        writer.append("Ripoti ya Uchanganuzi wa Mfumo wa Ufuatiliaji wa Kifua Kikuu cha Kuku\n");
        writer.append("Aina ya Ripoti: ").append(selectedReportType).append("\n");
        writer.append("Kipindi: ").append(txtDateRange.getText()).append("\n");
        writer.append("Tarehe ya Kutengeneza: ").append(dateFormat.format(new Date())).append("\n\n");

        // Write summary statistics
        writer.append("MUHTASARI WA TAKWIMU\n");
        writer.append("Jumla ya Ripoti,").append(txtTotalReports.getText()).append("\n");
        writer.append("Ripoti Zinazoendelea,").append(txtActiveReports.getText()).append("\n");
        writer.append("Ripoti Zilizokamilika,").append(txtResolvedReports.getText()).append("\n");
        writer.append("Eneo Lililo na Athari Zaidi,").append(txtMostAffectedArea.getText()).append("\n");
        writer.append("Uchanganuzi wa Mwenendo,").append(txtTrendAnalysis.getText()).append("\n\n");

        // Write detailed data (sample)
        writer.append("TAKWIMU ZA KINA\n");
        writer.append("Eneo,Idadi ya Ripoti,Asilimia\n");
        writer.append("Kinondoni,25,25%\n");
        writer.append("Ilala,35,35%\n");
        writer.append("Temeke,15,15%\n");
        writer.append("Ubungo,40,40%\n");
        writer.append("Kigamboni,20,20%\n");

        writer.flush();
        writer.close();

        Log.d(TAG, "Report exported successfully to: " + exportFile.getAbsolutePath());
    }

    // Sample data methods - replace with actual database queries
    private int getSampleTotalReports() {
        // This would query your database based on selectedReportType and date range
        return (int) (Math.random() * 100) + 50;
    }

    private int getSampleActiveReports() {
        // This would query your database for active reports
        return (int) (Math.random() * 30) + 10;
    }

    private String getSampleMostAffectedArea() {
        String[] areas = {"Kinondoni", "Ilala", "Temeke", "Ubungo", "Kigamboni"};
        return areas[(int) (Math.random() * areas.length)];
    }

    private String getSampleTrendAnalysis() {
        String[] trends = {
                "Ongezeko la asilimia 15 kutoka wiki iliyopita",
                "Kupungua kwa asilimia 8 kutoka mwezi uliopita",
                "Hali imara, hakuna mabadiliko makubwa",
                "Ongezeko la haraka la asilimia 25"
        };
        return trends[(int) (Math.random() * trends.length)];
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "ReportAnalyticsActivity destroyed");
    }
}