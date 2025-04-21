package com.clase.motorton.ui.perfil;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.clase.motorton.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EstadisticasVehiculos extends AppCompatActivity {
    private Button btnGenerarPdf = null;
    private static final int REQUEST_CODE_PERMISOS = 101;
    private FirebaseFirestore db = null;
    private Toast mensajeToast = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_estadisticas_vehiculos);

        db = FirebaseFirestore.getInstance();
        btnGenerarPdf = (Button) findViewById(R.id.btnGenerarPDF);

        db.collection("vehiculos")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int coches = 0;
                        int motos = 0;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String tipoVehiculo = document.getString("tipoVehiculo");
                            if (tipoVehiculo.equalsIgnoreCase("Coches")) coches++;
                            else if (tipoVehiculo.equalsIgnoreCase("Motos")) motos++;
                        }

                        mostrarGraficaPastel(coches, motos);
                    } else {
                        showToast("Error al cargar datos");
                    }
                });

        obtenerMarcas("Coches");
        obtenerMarcas("Motos");

        btnGenerarPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(EstadisticasVehiculos.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(EstadisticasVehiculos.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_CODE_PERMISOS);
                } else {
                    ejecutarGeneracionPDF();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISOS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ejecutarGeneracionPDF();
            } else {
                showToast("Permiso denegado para escribir almacenamiento.");
            }
        }
    }

    private void ejecutarGeneracionPDF() {
        PieChart pie = findViewById(R.id.pieChart);
        BarChart barCoches = findViewById(R.id.barChartCoches);
        BarChart barMotos = findViewById(R.id.barChartMotos);
        generarPDF(pie, barCoches, barMotos);
    }

    public void generarPDF(PieChart pieChart, BarChart barCoches, BarChart barMotos) {
        try {
            File pdfDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!pdfDir.exists()) pdfDir.mkdirs();
            File file = new File(pdfDir, "EstadisticasVehiculos.pdf");

            PdfWriter writer = new PdfWriter(new FileOutputStream(file));
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            Paragraph titulo = new Paragraph("Estadísticas de Vehículos").setFontSize(20).setBold();
            document.add(titulo);

            document.add(new Paragraph("Distribución Coches vs Motos").setBold());
            document.add(convertirChartEnImagen(pieChart));

            document.add(new Paragraph("Top Marcas de Coches").setBold().setMarginTop(10));
            document.add(convertirChartEnImagen(barCoches));

            document.add(new Paragraph("Top Marcas de Motos").setBold().setMarginTop(10));
            document.add(convertirChartEnImagen(barMotos));

            document.close();

            showToast("PDF creado en: " + file.getAbsolutePath());

            compartirPDF(file);

        } catch (Exception e) {
            e.printStackTrace();
            showToast("Error al crear PDF: " + e.getMessage());
        }
    }

    private void compartirPDF(File file) {
        Uri uri = FileProvider.getUriForFile(
                this,
                getPackageName() + ".provider",
                file
        );

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivity(Intent.createChooser(intent, "Compartir PDF con..."));
    }

    private Image convertirChartEnImagen(View chartView) {
        chartView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(chartView.getWidth(), chartView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        chartView.draw(canvas);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        return new Image(ImageDataFactory.create(byteArray)).setAutoScale(true);
    }

    private void mostrarGraficaPastel(int coches, int motos) {
        PieChart pieChart = findViewById(R.id.pieChart);

        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(coches, "Coches"));
        entries.add(new PieEntry(motos, "Motos"));

        PieDataSet dataSet = new PieDataSet(entries, "Tipos de Vehículos");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.invalidate();
    }

    private void obtenerMarcas(String tipoVehiculo) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Integer> conteoMarcas = new HashMap<>();

        db.collection("vehiculos")
                .whereEqualTo("tipoVehiculo", tipoVehiculo)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String marca = document.getString("marca");
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                conteoMarcas.put(marca, conteoMarcas.getOrDefault(marca, 0) + 1);
                            }
                        }

                        mostrarGraficaBarras(conteoMarcas, tipoVehiculo);
                    }
                });
    }

    private void mostrarGraficaBarras(Map<String, Integer> conteoMarcas, String tipoVehiculo) {
        BarChart barChart;

        if (tipoVehiculo.equals("Coches")) {
            barChart = findViewById(R.id.barChartCoches);
        } else {
            barChart = findViewById(R.id.barChartMotos);
        }

        List<Map.Entry<String, Integer>> listaOrdenada = new ArrayList<>(conteoMarcas.entrySet());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            listaOrdenada.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));
        }

        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        int maxItems = Math.min(5, listaOrdenada.size());
        for (int i = 0; i < maxItems; i++) {
            entries.add(new BarEntry(i, listaOrdenada.get(i).getValue()));
            labels.add(listaOrdenada.get(i).getKey());
        }

        BarDataSet dataSet = new BarDataSet(entries, "Marcas populares de " + tipoVehiculo);
        BarData data = new BarData(dataSet);

        barChart.setData(data);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.invalidate();
    }

    /**
     * @param mensaje
     * Método para ir matando los Toast y mostrar todos en el mismo para evitar
     * colas de Toasts y que se ralentice el dispositivo
     */
    public void showToast(String mensaje){
        if (this != null){
            // Comprobamos si existe algun toast cargado en el toast de la variable global
            if (mensajeToast != null) { // En caso de que si que exista
                mensajeToast.cancel(); // Le cancelamos, es decir le "matamos"
            }

            // Creamos un nuevo Toast con el mensaje que nos dan de argumento en el método
            mensajeToast = Toast.makeText(this, mensaje, Toast.LENGTH_SHORT);
            // Mostramos dicho Toast
            mensajeToast.show();
        }
    }
}