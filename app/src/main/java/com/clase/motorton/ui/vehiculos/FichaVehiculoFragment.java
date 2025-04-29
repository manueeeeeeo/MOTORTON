package com.clase.motorton.ui.vehiculos;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.clase.motorton.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class FichaVehiculoFragment extends Fragment {
    // Variable para manejar la base de datos de firestore
    private FirebaseFirestore db = null;

    // Variable para manejar la autentificación del usuario
    private FirebaseAuth auth = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_ficha_vehiculo, container, false);
    }
}