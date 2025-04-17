package com.clase.motorton.ui.eventos;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import com.clase.motorton.R;
import com.clase.motorton.cifrado.CifradoDeDatos;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.osmdroid.views.MapView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.crypto.SecretKey;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CreateEventFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateEventFragment extends Fragment {
    private FirebaseAuth mAuth = null;
    private FirebaseFirestore db = null;

    private EditText editTextNombreEvento = null, editTextDescripcion = null, editTextUbicacion = null, editTextProvincia = null;
    private Spinner spinnerTipoEvento = null;
    private DatePicker datePickerFecha = null;
    private Button buttonCrearEvento = null;
    private double latInicio = 0.0;
    private double latFin = 0.0;
    private double lonInicio = 0.0;
    private double lonFin = 0.0;
    private MapView mapView = null;
    private Button btnIrRuta = null;

    private List<String> tiposEvento = new ArrayList<>();

    private CifradoDeDatos cifrar = null;
    private SecretKey claveSecreta = null;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public CreateEventFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CreateEventFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CreateEventFragment newInstance(String param1, String param2) {
        CreateEventFragment fragment = new CreateEventFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflar la vista
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        return root;
    }

    // Limpiar los campos después de crear el evento
    private void limpiarFormulario() {
        editTextNombreEvento.setText("");
        editTextDescripcion.setText("");
        editTextUbicacion.setText("");
        editTextProvincia.setText("");
        datePickerFecha.updateDate(Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
    }
}