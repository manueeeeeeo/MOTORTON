package com.clase.motorton.ui.mapas;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;

import com.clase.motorton.R;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
public class MapaEventFragment extends Fragment {
    private MapView map = null;
    private Marker startMarker = null;
    private Marker endMarker = null;
    private Polyline routeLine = null;
    private Button btnConfirmarRuta = null;
    private Button btnBorrarRuta = null;
    private SearchView searchView = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mapa_event, container, false);
        map = view.findViewById(R.id.map);
        btnConfirmarRuta = view.findViewById(R.id.btnConfirmarRuta);
        btnBorrarRuta = view.findViewById(R.id.btnBorrarRuta);
        searchView = view.findViewById(R.id.searchView);

        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());
        map.setMultiTouchControls(true);
        map.getController().setZoom(15.0);
        map.getController().setCenter(new GeoPoint(40.4168, -3.7038));

        return  view;
    }
}